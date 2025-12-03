package com.example.speedMath.ui.settings;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.speedMath.R;
import com.example.speedMath.core.PlayerManager;
import com.example.speedMath.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private PlayerManager playerManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        playerManager = PlayerManager.getInstance(requireContext());

        // Switch
        SwitchCompat switchDark = root.findViewById(R.id.switchDark);
        SwitchCompat switchSound = root.findViewById(R.id.switchSound);
        SwitchCompat switchMusic = root.findViewById(R.id.switchMusic);
        SwitchCompat switchVibration = root.findViewById(R.id.switchVibration);
        SwitchCompat switchAnimation = root.findViewById(R.id.switchAnimations);
        SwitchCompat switchHaptic = root.findViewById(R.id.switchHaptic);

        // Spinners
        Spinner spinnerDifficulty = root.findViewById(R.id.spinnerDifficulty);
        Spinner spinnerNbQuestions = root.findViewById(R.id.spinnerNbQuestions);

        // Bouton reset score
        Button btnResetScore = root.findViewById(R.id.btnResetScore);

        // --- Initialiser Switch avec l'état actuel ---
        switchDark.setChecked(playerManager.isDarkModeEnabled());
        switchSound.setChecked(playerManager.isSoundEnabled());
        switchMusic.setChecked(playerManager.isMusicEnabled());
        switchVibration.setChecked(playerManager.isVibrationEnabled());
        switchAnimation.setChecked(playerManager.isAnimationEnabled());
        switchHaptic.setChecked(playerManager.isHapticEnabled());

        applyTheme(playerManager.isDarkModeEnabled());

        // --- Initialiser Spinners ---
        ArrayAdapter<String> adapterDifficulty = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Adaptive", "Easy", "Medium", "Hard", "Extreme"}
        );
        adapterDifficulty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapterDifficulty);
        spinnerDifficulty.setSelection(playerManager.getArcadeDifficulty());

        ArrayAdapter<Integer> adapterNbQuestions = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new Integer[]{5, 10, 20, 25}
        );
        adapterNbQuestions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNbQuestions.setAdapter(adapterNbQuestions);
        spinnerNbQuestions.setSelection(playerManager.getNbQuestions());

        // --- Listeners Switch ---
        switchDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(playerManager.isHapticEnabled()){
                switchDark.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            playerManager.setDarkMode(isChecked);
            applyTheme(isChecked);
        });

        switchSound.setOnCheckedChangeListener((b, on) -> {
            if(playerManager.isHapticEnabled()){
                switchSound.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            playerManager.setSoundEnabled(on);
        });

        switchMusic.setOnCheckedChangeListener((b, on) -> {
            if(playerManager.isHapticEnabled()){
                switchMusic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            playerManager.setMusicEnabled(on);
        });

        switchVibration.setOnCheckedChangeListener((b, on) -> {
            if(playerManager.isHapticEnabled()){
                switchVibration.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            playerManager.setVibrationEnabled(on);
        });

        switchAnimation.setOnCheckedChangeListener((b, on) -> {
            if(playerManager.isHapticEnabled()){
                switchAnimation.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            playerManager.setAnimationEnabled(on);
        });

        switchHaptic.setOnCheckedChangeListener((b, on) -> {
            if(playerManager.isHapticEnabled()){
                switchHaptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            playerManager.setHapticEnabled(on);
        });

        // --- Listeners Spinners ---
        spinnerDifficulty.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                playerManager.setArcadeDifficulty(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        spinnerNbQuestions.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                playerManager.setNbQuestions(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // --- Reset score ---
        btnResetScore.setOnClickListener(v -> {

            if (playerManager.isHapticEnabled()) {
                btnResetScore.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete all settings and scores ?")
                    .setMessage("Are you sure ?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        resetAllSettings();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


        return root;
    }

    private void resetAllSettings() {
        // Réinitialiser PlayerManager
        playerManager.resetUserStats();
        playerManager.setDarkMode(false);
        playerManager.setSoundEnabled(true);
        playerManager.setMusicEnabled(true);
        playerManager.setVibrationEnabled(true);
        playerManager.setAnimationEnabled(true);
        playerManager.setHapticEnabled(true);
        playerManager.setArcadeDifficulty(0);
        playerManager.setNbQuestions(0);

        // Réinitialiser l'UI (Switch + Spinners)
        SwitchCompat switchDark = getView().findViewById(R.id.switchDark);
        SwitchCompat switchSound = getView().findViewById(R.id.switchSound);
        SwitchCompat switchMusic = getView().findViewById(R.id.switchMusic);
        SwitchCompat switchVibration = getView().findViewById(R.id.switchVibration);
        SwitchCompat switchAnimation = getView().findViewById(R.id.switchAnimations);
        SwitchCompat switchHaptic = getView().findViewById(R.id.switchHaptic);

        Spinner spinnerDifficulty = getView().findViewById(R.id.spinnerDifficulty);
        Spinner spinnerNbQuestions = getView().findViewById(R.id.spinnerNbQuestions);

        // Switch
        switchDark.setChecked(playerManager.isDarkModeEnabled());
        switchSound.setChecked(playerManager.isSoundEnabled());
        switchMusic.setChecked(playerManager.isMusicEnabled());
        switchVibration.setChecked(playerManager.isVibrationEnabled());
        switchAnimation.setChecked(playerManager.isAnimationEnabled());
        switchHaptic.setChecked(playerManager.isHapticEnabled());

        // Spinners
        spinnerDifficulty.setSelection(playerManager.getArcadeDifficulty());
        spinnerNbQuestions.setSelection(playerManager.getNbQuestions());

        // Appliquer thème
        applyTheme(playerManager.isDarkModeEnabled());
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
