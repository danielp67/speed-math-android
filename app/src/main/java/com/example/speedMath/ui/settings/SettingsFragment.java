package com.example.speedMath.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.speedMath.R;
import com.example.speedMath.core.PlayerManager;
import com.example.speedMath.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    private PlayerManager playerManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SwitchCompat switchDark = root.findViewById(R.id.switchDark);
        SwitchCompat switchSound = root.findViewById(R.id.switchSound);
        SwitchCompat switchVibration = root.findViewById(R.id.switchVibration);
        Spinner spinnerDifficulty = root.findViewById(R.id.spinnerDifficulty);
        Button btnResetScore = root.findViewById(R.id.btnResetScore);

// Exemple : remplir le spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Facile", "Normal", "Difficile"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);

// Exemple d’actions
        switchSound.setOnCheckedChangeListener((b, on) -> {
            // save to preferences
        });

        playerManager = PlayerManager.getInstance(requireContext());

        btnResetScore.setOnClickListener(v -> {
            playerManager.resetUserStats();
        });

        //        final TextView textView = binding.textNotifications;
        //settingsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}