package com.honeycontrol.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.honeycontrol.fragments.CompanyStepFragment;
import com.honeycontrol.fragments.UserStepFragment;

public class SignupWizardAdapter extends FragmentStateAdapter {
    
    private static final int TOTAL_STEPS = 2;
    
    public SignupWizardAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CompanyStepFragment();
            case 1:
                return new UserStepFragment();
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }
    
    @Override
    public int getItemCount() {
        return TOTAL_STEPS;
    }
}
