package com.honeycontrol.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.honeycontrol.R;
import com.honeycontrol.models.Sale;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class SaleAdapter extends RecyclerView.Adapter<SaleAdapter.SaleViewHolder> {
    
    private List<Sale> sales;
    private Context context;
    private OnSaleClickListener onSaleClickListener;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public interface OnSaleClickListener {
        void onSaleClick(Sale sale);
        void onSaleEdit(Sale sale);
        void onSaleDelete(Sale sale);
    }
    
    public SaleAdapter(Context context, List<Sale> sales) {
        this.context = context;
        this.sales = sales;
    }
    
    public void setOnSaleClickListener(OnSaleClickListener listener) {
        this.onSaleClickListener = listener;
    }
    
    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sale, parent, false);
        return new SaleViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SaleViewHolder holder, int position) {
        Sale sale = sales.get(position);
        
        // Exibir nome do cliente ao invés do ID
        String customerName = "Cliente não informado";
        if (sale.getCustomer() != null && sale.getCustomer().getName() != null) {
            customerName = sale.getCustomer().getName();
        } else if (sale.getCustomerId() != null) {
            // Fallback caso não tenha carregado o relacionamento
            customerName = "Cliente " + sale.getCustomerId().substring(0, Math.min(8, sale.getCustomerId().length()));
        }
        holder.customerTextView.setText(customerName);
        
        // Format total as currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        holder.totalTextView.setText(currencyFormat.format(sale.getTotal()));
        
        // Format date
        if (sale.getCreatedAt() != null) {
            holder.dateTextView.setText(sale.getCreatedAt().format(DATE_FORMATTER));
        } else {
            holder.dateTextView.setText("Data não disponível");
        }
        
        // Items count
        int itemsCount = sale.getSaleItems() != null ? sale.getSaleItems().size() : 0;
        String itemsText = itemsCount == 1 ? "1 item vendido" : itemsCount + " itens vendidos";
        holder.itemsCountTextView.setText(itemsText);
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (onSaleClickListener != null) {
                onSaleClickListener.onSaleClick(sale);
            }
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (onSaleClickListener != null) {
                onSaleClickListener.onSaleEdit(sale);
            }
            return true;
        });
    }
    
    @Override
    public int getItemCount() {
        return sales != null ? sales.size() : 0;
    }
    
    public void updateSales(List<Sale> newSales) {
        this.sales = newSales;
        notifyDataSetChanged();
    }
    
    public void removeSale(int position) {
        sales.remove(position);
        notifyItemRemoved(position);
    }
    
    static class SaleViewHolder extends RecyclerView.ViewHolder {
        TextView customerTextView;
        TextView totalTextView;
        TextView dateTextView;
        TextView itemsCountTextView;
        
        public SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            customerTextView = itemView.findViewById(R.id.saleCustomerTextView);
            totalTextView = itemView.findViewById(R.id.saleTotalTextView);
            dateTextView = itemView.findViewById(R.id.saleDateTextView);
            itemsCountTextView = itemView.findViewById(R.id.saleItemsCountTextView);
        }
    }
}
