package com.honeycontrol.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.honeycontrol.R;
import com.honeycontrol.models.Companies;
import java.util.List;

public class CompaniesAdapter extends RecyclerView.Adapter<CompaniesAdapter.CompanyViewHolder> {

    private List<Companies> companiesList;

    public CompaniesAdapter(List<Companies> companiesList) {
        this.companiesList = companiesList;
    }

    @NonNull
    @Override
    public CompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_company, parent, false);
        return new CompanyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyViewHolder holder, int position) {
        Companies company = companiesList.get(position);
        Log.d("CompaniesAdapter", "Binding company at position " + position + ": " + company.getName());
        holder.textViewCompanyName.setText(company.getName());
        // Defina outros dados aqui, se houver
    }

    @Override
    public int getItemCount() {
        return companiesList == null ? 0 : companiesList.size();
    }

    public void updateData(List<Companies> newCompaniesList) {
        Log.d("CompaniesAdapter", "Updating data with " + (newCompaniesList != null ? newCompaniesList.size() : 0) + " companies");
        this.companiesList = newCompaniesList;
        notifyDataSetChanged(); // Notifica o RecyclerView que os dados mudaram
    }

    static class CompanyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCompanyName;
        // Declare outros Views do item_company.xml aqui

        public CompanyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCompanyName = itemView.findViewById(R.id.textViewCompanyName);
            // Inicialize outros Views aqui
        }
    }
}