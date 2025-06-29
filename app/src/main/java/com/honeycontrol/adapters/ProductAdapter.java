package com.honeycontrol.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.honeycontrol.R;
import com.honeycontrol.models.Product;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    
    private List<Product> products;
    private final Context context;
    private OnProductClickListener onProductClickListener;
    
    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onProductEdit(Product product);
        void onProductDelete(Product product);
    }
    
    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
    }
    
    public void setOnProductClickListener(OnProductClickListener listener) {
        this.onProductClickListener = listener;
    }
    
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        
        holder.nameTextView.setText(product.getName());
        holder.descriptionTextView.setText(product.getDescription() != null ? product.getDescription() : "Sem descrição");
        holder.unitTextView.setText(product.getUnit() != null ? product.getUnit() : "un");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        holder.priceTextView.setText(currencyFormat.format(product.getUnitPrice()));

        Integer stockQuantity = product.getStockQuantity();
        holder.stockQuantityTextView.setText(String.valueOf(stockQuantity));

        if (stockQuantity == 0) {
            holder.stockQuantityTextView.setTextColor(ContextCompat.getColor(context, R.color.red_400));
            holder.stockLabelTextView.setTextColor(ContextCompat.getColor(context, R.color.red_400));
        } else if (stockQuantity <= 5) {
            holder.stockQuantityTextView.setTextColor(ContextCompat.getColor(context, R.color.amber_400));
            holder.stockLabelTextView.setTextColor(ContextCompat.getColor(context, R.color.amber_400));
        } else {
            holder.stockQuantityTextView.setTextColor(ContextCompat.getColor(context, R.color.green_400));
            holder.stockLabelTextView.setTextColor(ContextCompat.getColor(context, R.color.green_400));
        }

        holder.itemView.setOnClickListener(v -> {
            if (onProductClickListener != null) {
                onProductClickListener.onProductClick(product);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onProductClickListener != null) {
                onProductClickListener.onProductEdit(product);
            }
            return true;
        });
    }
    
    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }
    
    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }
    
    public void removeProduct(int position) {
        products.remove(position);
        notifyItemRemoved(position);
    }
    
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        TextView priceTextView;
        TextView unitTextView;
        TextView stockQuantityTextView;
        TextView stockLabelTextView;
        
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.productNameTextView);
            descriptionTextView = itemView.findViewById(R.id.productDescriptionTextView);
            priceTextView = itemView.findViewById(R.id.productPriceTextView);
            unitTextView = itemView.findViewById(R.id.productUnitTextView);
            stockQuantityTextView = itemView.findViewById(R.id.productStockQuantityTextView);
            stockLabelTextView = itemView.findViewById(R.id.productStockLabelTextView);
        }
    }
}
