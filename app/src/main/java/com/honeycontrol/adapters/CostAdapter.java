package com.honeycontrol.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.honeycontrol.R;
import com.honeycontrol.models.Cost;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CostAdapter extends RecyclerView.Adapter<CostAdapter.CostViewHolder> {
    
    private List<Cost> costs;
    private Context context;
    private OnCostClickListener onCostClickListener;
    
    public interface OnCostClickListener {
        void onCostClick(Cost cost);
        void onCostEdit(Cost cost);
        void onCostDelete(Cost cost);
    }
    
    public CostAdapter(Context context, List<Cost> costs) {
        this.context = context;
        this.costs = costs;
    }
    
    public void setOnCostClickListener(OnCostClickListener listener) {
        this.onCostClickListener = listener;
    }
    
    @NonNull
    @Override
    public CostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cost, parent, false);
        return new CostViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CostViewHolder holder, int position) {
        Cost cost = costs.get(position);
        
        holder.nameTextView.setText(cost.getName());
        holder.categoryTextView.setText(cost.getCategory());
        holder.descriptionTextView.setText(cost.getDescription() != null ? cost.getDescription() : "");
        
        // Format amount as currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        holder.amountTextView.setText(currencyFormat.format(cost.getAmount()));
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (onCostClickListener != null) {
                onCostClickListener.onCostClick(cost);
            }
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (onCostClickListener != null) {
                onCostClickListener.onCostEdit(cost);
            }
            return true;
        });
    }
    
    @Override
    public int getItemCount() {
        return costs != null ? costs.size() : 0;
    }
    
    public void updateCosts(List<Cost> newCosts) {
        this.costs = newCosts;
        notifyDataSetChanged();
    }
    
    public void removeCost(int position) {
        costs.remove(position);
        notifyItemRemoved(position);
    }
    
    static class CostViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView categoryTextView;
        TextView descriptionTextView;
        TextView amountTextView;
        
        public CostViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.costNameTextView);
            categoryTextView = itemView.findViewById(R.id.costCategoryTextView);
            descriptionTextView = itemView.findViewById(R.id.costDescriptionTextView);
            amountTextView = itemView.findViewById(R.id.costAmountTextView);
        }
    }
}
