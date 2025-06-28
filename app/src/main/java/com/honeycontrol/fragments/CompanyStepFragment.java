package com.honeycontrol.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.honeycontrol.R;
import com.honeycontrol.viewmodels.SignupWizardViewModel;

public class CompanyStepFragment extends Fragment {
    
    private TextInputLayout companyNameLayout;
    private TextInputEditText companyNameInput;
    private SignupWizardViewModel viewModel;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SignupWizardViewModel.class);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_company_step, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupObservers();
        setupTextWatcher();

        // Restaurar dados se houver
        String savedCompanyName = viewModel.getCompanyName().getValue();
        if (savedCompanyName != null) {
            companyNameInput.setText(savedCompanyName);
        }
    }
    
    private void initViews(View view) {
        companyNameLayout = view.findViewById(R.id.companyNameLayout);
        companyNameInput = view.findViewById(R.id.companyNameInput);
    }
    
    private void setupObservers() {
        viewModel.getCompanyName().observe(getViewLifecycleOwner(), this::validateCompanyName);
    }
    
    private void setupTextWatcher() {
        companyNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String companyName = s.toString().trim();
                viewModel.setCompanyName(companyName);
            }
        });
    }
    
    private void validateCompanyName(String name) {
        if (name == null || name.trim().isEmpty()) {
            companyNameLayout.setError("Nome da empresa é obrigatório");
        } else {
            companyNameLayout.setError(null);
        }
    }
}
