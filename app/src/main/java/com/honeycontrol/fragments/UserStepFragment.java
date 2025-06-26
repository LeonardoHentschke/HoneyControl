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

public class UserStepFragment extends Fragment {
    
    private TextInputLayout userNameLayout, userEmailLayout, userPasswordLayout;
    private TextInputEditText userNameInput, userEmailInput, userPasswordInput;
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
        return inflater.inflate(R.layout.fragment_user_step, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupObservers();
        setupTextWatchers();
        
        // Restaurar dados se houver
        restoreSavedData();
    }
    
    private void initViews(View view) {
        userNameLayout = view.findViewById(R.id.userNameLayout);
        userEmailLayout = view.findViewById(R.id.userEmailLayout);
        userPasswordLayout = view.findViewById(R.id.userPasswordLayout);
        
        userNameInput = view.findViewById(R.id.userNameInput);
        userEmailInput = view.findViewById(R.id.userEmailInput);
        userPasswordInput = view.findViewById(R.id.userPasswordInput);
    }
    
    private void setupObservers() {
        viewModel.getUserName().observe(getViewLifecycleOwner(), this::validateUserName);
        
        viewModel.getUserEmail().observe(getViewLifecycleOwner(), this::validateUserEmail);
        
        viewModel.getUserPassword().observe(getViewLifecycleOwner(), this::validateUserPassword);
    }
    
    private void setupTextWatchers() {
        userNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setUserName(s.toString().trim());
            }
        });
        
        userEmailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setUserEmail(s.toString().trim());
            }
        });
        
        userPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setUserPassword(s.toString());
            }
        });
    }
    
    private void restoreSavedData() {
        String savedUserName = viewModel.getUserName().getValue();
        if (savedUserName != null) {
            userNameInput.setText(savedUserName);
        }
        
        String savedUserEmail = viewModel.getUserEmail().getValue();
        if (savedUserEmail != null) {
            userEmailInput.setText(savedUserEmail);
        }
        
        String savedUserPassword = viewModel.getUserPassword().getValue();
        if (savedUserPassword != null) {
            userPasswordInput.setText(savedUserPassword);
        }
    }
    
    private void validateUserName(String name) {
        if (name == null || name.trim().isEmpty()) {
            userNameLayout.setError("Nome completo é obrigatório");
        } else {
            userNameLayout.setError(null);
        }
    }
    
    private void validateUserEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            userEmailLayout.setError("Email é obrigatório");
        } else if (!isValidEmail(email)) {
            userEmailLayout.setError("Email inválido");
        } else {
            userEmailLayout.setError(null);
        }
    }
    
    private void validateUserPassword(String password) {
        if (password == null || password.isEmpty()) {
            userPasswordLayout.setError("Senha é obrigatória");
        } else if (password.length() < 6) {
            userPasswordLayout.setError("Senha deve ter pelo menos 6 caracteres");
        } else {
            userPasswordLayout.setError(null);
        }
    }
    
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}
