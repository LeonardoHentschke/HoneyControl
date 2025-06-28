package com.honeycontrol.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.honeycontrol.R;
import com.honeycontrol.models.Customer;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    
    private List<Customer> customers;
    private Context context;
    private OnCustomerClickListener onCustomerClickListener;
    
    public interface OnCustomerClickListener {
        void onCustomerClick(Customer customer);
        void onCustomerEdit(Customer customer);
        void onCustomerDelete(Customer customer);
    }
    
    public CustomerAdapter(Context context, List<Customer> customers) {
        this.context = context;
        this.customers = customers;
    }
    
    public void setOnCustomerClickListener(OnCustomerClickListener listener) {
        this.onCustomerClickListener = listener;
    }
    
    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customers.get(position);
        
        holder.nameTextView.setText(customer.getName());
        holder.emailTextView.setText(customer.getEmail());
        holder.phoneTextView.setText(customer.getPhone());
        
        // Formatação do endereço
        String address = customer.getAddress();
        if (customer.getCity() != null && !customer.getCity().isEmpty()) {
            address += ", " + customer.getCity();
        }

        holder.addressTextView.setText(address);
        
        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (onCustomerClickListener != null) {
                onCustomerClickListener.onCustomerClick(customer);
            }
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (onCustomerClickListener != null) {
                onCustomerClickListener.onCustomerEdit(customer);
            }
            return true;
        });
    }
    
    @Override
    public int getItemCount() {
        return customers.size();
    }
    
    public void updateCustomers(List<Customer> newCustomers) {
        this.customers = newCustomers;
        notifyDataSetChanged();
    }
    
    public void removeCustomer(int position) {
        customers.remove(position);
        notifyItemRemoved(position);
    }
    
    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
        TextView phoneTextView;
        TextView addressTextView;
        
        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.customerNameTextView);
            emailTextView = itemView.findViewById(R.id.customerEmailTextView);
            phoneTextView = itemView.findViewById(R.id.customerPhoneTextView);
            addressTextView = itemView.findViewById(R.id.customerAddressTextView);
        }
    }
}
