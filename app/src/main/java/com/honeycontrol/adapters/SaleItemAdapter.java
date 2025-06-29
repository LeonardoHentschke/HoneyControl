package com.honeycontrol.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.honeycontrol.R;
import com.honeycontrol.models.TempSaleItem;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SaleItemAdapter extends RecyclerView.Adapter<SaleItemAdapter.SaleItemViewHolder> {
    
    private List<TempSaleItem> saleItems;
    private Context context;
    private OnSaleItemClickListener onSaleItemClickListener;
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    
    public interface OnSaleItemClickListener {
        void onRemoveItem(int position);
    }
    
    public SaleItemAdapter(Context context, List<TempSaleItem> saleItems) {
        this.context = context;
        this.saleItems = saleItems;
    }
    
    public void setOnSaleItemClickListener(OnSaleItemClickListener listener) {
        this.onSaleItemClickListener = listener;
    }
    
    @NonNull
    @Override
    public SaleItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sale_item, parent, false);
        return new SaleItemViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SaleItemViewHolder holder, int position) {
        TempSaleItem saleItem = saleItems.get(position);
        
        // Nome do produto
        holder.productNameTextView.setText(saleItem.getProduct().getName());
        
        // Quantidade
        holder.quantityTextView.setText(String.valueOf(saleItem.getQuantity()));
        
        // Preço unitário
        holder.unitPriceTextView.setText(CURRENCY_FORMAT.format(saleItem.getUnitPrice()));
        
        // Desconto
        if (saleItem.hasDiscount()) {
            holder.discountLayout.setVisibility(View.VISIBLE);
            holder.discountTextView.setText(CURRENCY_FORMAT.format(saleItem.getDiscount()));
        } else {
            holder.discountLayout.setVisibility(View.GONE);
        }
        
        // Subtotal
        holder.subtotalTextView.setText(CURRENCY_FORMAT.format(saleItem.getSubtotal()));
        
        // Listener do botão remover
        holder.removeButton.setOnClickListener(v -> {
            if (onSaleItemClickListener != null) {
                onSaleItemClickListener.onRemoveItem(position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return saleItems != null ? saleItems.size() : 0;
    }
    
    public void updateSaleItems(List<TempSaleItem> newSaleItems) {
        this.saleItems = newSaleItems;
        notifyDataSetChanged();
    }
    
    public void removeItem(int position) {
        if (position >= 0 && position < saleItems.size()) {
            saleItems.remove(position);
            notifyItemRemoved(position);
            // Notificar mudanças nas posições subsequentes
            notifyItemRangeChanged(position, saleItems.size());
        }
    }
    
    public void addItem(TempSaleItem saleItem) {
        saleItems.add(saleItem);
        notifyItemInserted(saleItems.size() - 1);
    }
    
    static class SaleItemViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView;
        TextView quantityTextView;
        TextView unitPriceTextView;
        LinearLayout discountLayout;
        TextView discountTextView;
        TextView subtotalTextView;
        MaterialButton removeButton;
        
        public SaleItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            unitPriceTextView = itemView.findViewById(R.id.unitPriceTextView);
            discountLayout = itemView.findViewById(R.id.discountLayout);
            discountTextView = itemView.findViewById(R.id.discountTextView);
            subtotalTextView = itemView.findViewById(R.id.subtotalTextView);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}
