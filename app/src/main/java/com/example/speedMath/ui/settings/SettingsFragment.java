package com.example.speedMath.ui.settings;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
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
        SwitchCompat switchMusic = root.findViewById(R.id.switchMusic);
        SwitchCompat switchVibration = root.findViewById(R.id.switchVibration);
        SwitchCompat switchAnimation = root.findViewById(R.id.switchAnimations);
        SwitchCompat switchHaptic = root.findViewById(R.id.switchHaptic);
        Spinner spinnerDifficulty = root.findViewById(R.id.spinnerDifficulty);
        Button btnResetScore = root.findViewById(R.id.btnResetScore);

        playerManager = PlayerManager.getInstance(requireContext());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Facile", "Normal", "Difficile"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);


        switchDark.setOnCheckedChangeListener((b, on) -> {
            if(playerManager.isHapticEnabled()){
                switchDark.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            playerManager.setDarkMode(on);
        });

        switchSound.setOnCheckedChangeListener((b, on) -> {
            if(playerManager.isHapticEnabled()){
                switchSound.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            playerManager.setSoundEnabled(on);
        });

        switchMusic.setOnCheckedChangeListener((b, on) -> {
            if (playerManager.isHapticEnabled()) {
                switchMusic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            playerManager.setMusicEnabled(on);

        });
        switchVibration.setOnCheckedChangeListener((b, on) -> {
            if (playerManager.isHapticEnabled()) {
                switchVibration.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            playerManager.setVibrationEnabled(on);
        });

        switchAnimation.setOnCheckedChangeListener((b, on) -> {
            if (playerManager.isHapticEnabled()) {
                switchAnimation.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            playerManager.setAnimationEnabled(on);
        });

        switchHaptic.setOnCheckedChangeListener((b, on) -> {
            if (playerManager.isHapticEnabled()) {
                switchHaptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            playerManager.setHapticEnabled(on);
        });


        btnResetScore.setOnClickListener(v -> {
            if (playerManager.isHapticEnabled()) {
                btnResetScore.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
            playerManager.resetUserStats();
        });



        // Charger l'état actuel
        boolean isDark = playerManager.isDarkModeEnabled();
        switchDark.setChecked(isDark);

        applyTheme(isDark);

        switchDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            playerManager.setDarkMode(isChecked);
            applyTheme(isChecked);
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

    private void applyTheme(boolean isDark) {
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}