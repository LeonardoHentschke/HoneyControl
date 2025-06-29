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
import com.honeycontrol.models.StockLog;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StockLogAdapter extends RecyclerView.Adapter<StockLogAdapter.StockLogViewHolder> {
    
    private List<StockLog> stockLogs;
    private final Context context;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public StockLogAdapter(Context context, List<StockLog> stockLogs) {
        this.context = context;
        this.stockLogs = stockLogs;
    }
    
    @NonNull
    @Override
    public StockLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stock_log, parent, false);
        return new StockLogViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull StockLogViewHolder holder, int position) {
        StockLog stockLog = stockLogs.get(position);
        
        // Configurar tipo da movimentação
        String type = stockLog.getType();
        holder.typeTextView.setText(type);
        
        // Configurar cores baseado no tipo
        int color;
        if ("ENTRADA".equalsIgnoreCase(type) || "IN".equalsIgnoreCase(type)) {
            color = ContextCompat.getColor(context, R.color.green_400);
        } else if ("SAIDA".equalsIgnoreCase(type) || "OUT".equalsIgnoreCase(type)) {
            color = ContextCompat.getColor(context, R.color.red_400);
        } else {
            color = ContextCompat.getColor(context, R.color.amber_400);
        }
        
        holder.typeTextView.setTextColor(color);
        holder.typeIndicator.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(color)
        );
        
        // Configurar quantidade com sinal
        Integer quantity = stockLog.getQuantity();
        String quantityText;
        int quantityColor;
        
        if ("ENTRADA".equalsIgnoreCase(type) || "IN".equalsIgnoreCase(type)) {
            quantityText = "+" + quantity;
            quantityColor = ContextCompat.getColor(context, R.color.green_400);
        } else {
            quantityText = "-" + Math.abs(quantity);
            quantityColor = ContextCompat.getColor(context, R.color.red_400);
        }
        
        holder.quantityTextView.setText(quantityText);
        holder.quantityTextView.setTextColor(quantityColor);
        
        // Configurar data
        if (stockLog.getCreatedAt() != null) {
            holder.dateTextView.setText(stockLog.getCreatedAt().format(DATE_FORMATTER));
        } else {
            holder.dateTextView.setText("Data não disponível");
        }
        
        // Configurar motivo
        String reason = stockLog.getReason();
        if (reason != null && !reason.trim().isEmpty()) {
            holder.reasonTextView.setText("Motivo: " + reason);
            holder.reasonTextView.setVisibility(View.VISIBLE);
        } else {
            holder.reasonTextView.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return stockLogs != null ? stockLogs.size() : 0;
    }
    
    public void updateStockLogs(List<StockLog> newStockLogs) {
        this.stockLogs = newStockLogs;
        notifyDataSetChanged();
    }
    
    static class StockLogViewHolder extends RecyclerView.ViewHolder {
        View typeIndicator;
        TextView typeTextView;
        TextView dateTextView;
        TextView quantityTextView;
        TextView reasonTextView;
        
        public StockLogViewHolder(@NonNull View itemView) {
            super(itemView);
            typeIndicator = itemView.findViewById(R.id.typeIndicator);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            reasonTextView = itemView.findViewById(R.id.reasonTextView);
        }
    }
}