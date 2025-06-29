package com.honeycontrol;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import com.honeycontrol.models.*;

import java.util.Collections;
import com.google.gson.reflect.TypeToken;
import com.honeycontrol.requests.CompanyCreateRequest;
import com.honeycontrol.requests.UserCreateRequest;
import com.honeycontrol.requests.CustomerCreateRequest;
import com.honeycontrol.requests.CostCreateRequest;
import com.honeycontrol.requests.ProductCreateRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SupabaseClient {
    private static final String TAG = "SupabaseClient";
    private static final String BASE_URL = "https://dcwdfnuziqlhfhrtcgjw.supabase.co/rest/v1/";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRjd2RmbnV6aXFsaGZocnRjZ2p3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA2MjQ5MDEsImV4cCI6MjA2NjIwMDkwMX0.mAoIg8VBng_Ua1-yCTxU4ESofa9NY1zv_3ZkcDCFrjE";
    private static final Gson gson;
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    static {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .setLenient()
                .create();
    }

    public static class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),  // 6 dígitos de microssegundos
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSS"),   // 5 dígitos
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSS"),    // 4 dígitos
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),     // 3 dígitos (milissegundos)
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS"),      // 2 dígitos
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.S"),       // 1 dígito
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),         // Sem fração de segundo
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),       // Formato alternativo com espaço
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        };

        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) 
                throws JsonParseException {
            String dateString = json.getAsString();
            
            for (DateTimeFormatter formatter : FORMATTERS) {
                try {
                    return LocalDateTime.parse(dateString, formatter);
                } catch (DateTimeParseException e) {
                    // Continue tentando com o próximo formato
                }
            }
            
            throw new JsonParseException("Não foi possível fazer parse da data: " + dateString);
        }
    }

    public static <T> T createService(Class<T> serviceClass) {
        try {
            if (serviceClass == SupabaseApi.class) {
                return serviceClass.cast(new SupabaseApiImplementation());
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar serviço: " + e.getMessage());
            return null;
        }
    }

    public static class SupabaseApiImplementation implements SupabaseApi {
        private <T> void executeRequest(String endpoint, String method, Object body, Type responseType, ApiCallback<T> callback) {
            executor.execute(() -> {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(BASE_URL + endpoint);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod(method);
                    connection.setRequestProperty("apikey", API_KEY);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    
                    if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) {
                        connection.setRequestProperty("Prefer", "return=representation");
                        connection.setDoOutput(true);
                        
                        try (OutputStream os = connection.getOutputStream()) {
                            String json = gson.toJson(body);
                            Log.d(TAG, "Sending JSON: " + json);
                            byte[] input = json.getBytes(StandardCharsets.UTF_8);
                            os.write(input, 0, input.length);
                        }
                    }

                    int responseCode = connection.getResponseCode();
                    
                    if (responseCode >= 200 && responseCode < 300) {
                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }
                            
                            String jsonResponse = response.toString();
                            
                            // Verificamos se a resposta é uma lista ou objeto
                            T result;
                            if (jsonResponse.isEmpty()) {
                                result = null;
                            } else {
                                try {
                                    JsonParser parser = new JsonParser();
                                    JsonElement jsonElement = parser.parse(jsonResponse);
                                    if (jsonElement.isJsonArray() && 
                                            !List.class.isAssignableFrom(TypeToken.get(responseType).getRawType())) {
                                        // Se esperamos um objeto mas recebemos um array
                                        JsonArray array = jsonElement.getAsJsonArray();
                                        if (!array.isEmpty()) {
                                            result = gson.fromJson(array.get(0), responseType);
                                        } else {
                                            result = null;
                                        }
                                    } else if (jsonElement.isJsonObject() && 
                                            List.class.isAssignableFrom(TypeToken.get(responseType).getRawType())) {
                                        // Se esperamos um array mas recebemos um objeto
                                        Object obj = gson.fromJson(jsonElement, Object.class);
                                        result = (T) Collections.singletonList(obj);
                                    } else {
                                        // Caso normal
                                        result = gson.fromJson(jsonResponse, responseType);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Erro ao converter resposta: " + e.getMessage());
                                    mainHandler.post(() -> callback.onFailure(new Exception("Erro ao processar resposta")));
                                    return;
                                }
                            }
                            
                            final T finalResult = result;
                            mainHandler.post(() -> callback.onSuccess(finalResult, responseCode));
                        }
                    } else {
                        // Erro na resposta
                        String errorResponse = readErrorStream(connection.getErrorStream());
                        Log.e(TAG, "API error: " + errorResponse);
                        mainHandler.post(() -> callback.onFailure(new HttpException("HTTP error: " + responseCode, responseCode, errorResponse)));
                    }
                    
                } catch (IOException e) {
                    Log.e(TAG, "Network error: " + e.getMessage());
                    final Exception exception = e;
                    mainHandler.post(() -> callback.onFailure(exception));
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            });
        }
        
        private String readErrorStream(InputStream errorStream) {
            if (errorStream == null) {
                return "Unknown error";
            }
            
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            } catch (IOException e) {
                return "Error reading error stream: " + e.getMessage();
            }
        }

        @Override
        public ApiCall<Companies> createCompanies(CompanyCreateRequest companyRequest) {
            return callback -> executeRequest("companies", "POST", companyRequest,
                    Companies.class, callback);
        }

        @Override
        public ApiCall<User> createUser(UserCreateRequest userRequest) {
            return callback -> executeRequest("users", "POST", userRequest,
                    User.class, callback);
        }

        @Override
        public ApiCall<User> getUserByEmail(String email) {
            return callback -> {
                String endpoint = "users?email=eq." + email + "&select=*";
                executeRequest(endpoint, "GET", null, User.class, callback);
            };
        }

        @Override
        public ApiCall<List<Customer>> getCustomersByCompany(String companyId) {
            return callback -> {
                String endpoint = "customers?company_id=eq." + companyId + "&select=*";
                Type listType = new TypeToken<List<Customer>>(){}.getType();
                executeRequest(endpoint, "GET", null, listType, callback);
            };
        }

        @Override
        public ApiCall<Customer> getCustomerById(String customerId) {
            return callback -> {
                String endpoint = "customers?id=eq." + customerId + "&select=*";
                executeRequest(endpoint, "GET", null, Customer.class, callback);
            };
        }

        @Override
        public ApiCall<Customer> createCustomer(CustomerCreateRequest customerRequest) {
            return callback -> executeRequest("customers", "POST", customerRequest,
                    Customer.class, callback);
        }

        @Override
        public ApiCall<Customer> updateCustomer(String customerId, CustomerCreateRequest customerRequest) {
            return callback -> {
                String endpoint = "customers?id=eq." + customerId;
                executeRequest(endpoint, "PATCH", customerRequest, Customer.class, callback);
            };
        }

        @Override
        public ApiCall<Void> deleteCustomer(String customerId) {
            return callback -> {
                String endpoint = "customers?id=eq." + customerId;
                executeRequest(endpoint, "DELETE", null, Void.class, callback);
            };
        }

        @Override
        public ApiCall<List<Cost>> getCostsByCompany(String companyId) {
            return callback -> {
                String endpoint = "costs?company_id=eq." + companyId + "&select=*";
                Type listType = new TypeToken<List<Cost>>(){}.getType();
                executeRequest(endpoint, "GET", null, listType, callback);
            };
        }

        @Override
        public ApiCall<Cost> getCostById(String costId) {
            return callback -> {
                String endpoint = "costs?id=eq." + costId + "&select=*";
                executeRequest(endpoint, "GET", null, Cost.class, callback);
            };
        }

        @Override
        public ApiCall<Cost> createCost(CostCreateRequest costRequest) {
            return callback -> executeRequest("costs", "POST", costRequest,
                    Cost.class, callback);
        }

        @Override
        public ApiCall<Cost> updateCost(String costId, CostCreateRequest costRequest) {
            return callback -> {
                String endpoint = "costs?id=eq." + costId;
                executeRequest(endpoint, "PATCH", costRequest, Cost.class, callback);
            };
        }

        @Override
        public ApiCall<Void> deleteCost(String costId) {
            return callback -> {
                String endpoint = "costs?id=eq." + costId;
                executeRequest(endpoint, "DELETE", null, Void.class, callback);
            };
        }

        @Override
        public ApiCall<List<Product>> getProductsByCompany(String companyId) {
            return callback -> {
                String endpoint = "products?company_id=eq." + companyId + "&select=*";
                Type listType = new TypeToken<List<Product>>(){}.getType();
                executeRequest(endpoint, "GET", null, listType, callback);
            };
        }

        @Override
        public ApiCall<Product> getProductById(String productId) {
            return callback -> {
                String endpoint = "products?id=eq." + productId + "&select=*";
                executeRequest(endpoint, "GET", null, Product.class, callback);
            };
        }

        @Override
        public ApiCall<Product> createProduct(ProductCreateRequest productRequest) {
            return callback -> executeRequest("products", "POST", productRequest,
                    Product.class, callback);
        }

        @Override
        public ApiCall<Product> updateProduct(String productId, ProductCreateRequest productRequest) {
            return callback -> {
                String endpoint = "products?id=eq." + productId;
                executeRequest(endpoint, "PATCH", productRequest, Product.class, callback);
            };
        }

        @Override
        public ApiCall<Void> deleteProduct(String productId) {
            return callback -> {
                String endpoint = "products?id=eq." + productId;
                executeRequest(endpoint, "DELETE", null, Void.class, callback);
            };
        }
    }

    public static class HttpException extends Exception {
        private final int code;
        private final String errorBody;
        
        public HttpException(String message, int code, String errorBody) {
            super(message);
            this.code = code;
            this.errorBody = errorBody;
        }
        
        public int code() {
            return code;
        }
        
        public String errorBody() {
            return errorBody;
        }
    }
}

