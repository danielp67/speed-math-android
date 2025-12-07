package com.example.speedMath.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.speedMath.R;
import com.example.speedMath.core.PlayerManager;
import com.example.speedMath.databinding.FragmentSettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private PlayerManager playerManager;

    private EditText editPseudo;
    private ImageView pseudoStatus;
    private Button btnSavePseudo;
    private SharedPreferences prefs;
    private FirebaseAuth mAuth;
    private DatabaseReference playersRef;
    private String currentUid;


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


        editPseudo = root.findViewById(R.id.editPseudo);
        pseudoStatus = root.findViewById(R.id.pseudoStatus);
        btnSavePseudo = root.findViewById(R.id.btnSavePseudo);

        prefs = requireContext().getSharedPreferences("SpeedMathPrefs", Context.MODE_PRIVATE);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        playersRef = FirebaseDatabase.getInstance().getReference("players");

        signInAnonymously();

        // Vérification instantanée du pseudo
        editPseudo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkPseudoAvailable(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnSavePseudo.setOnClickListener(v -> savePseudo());

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
        spinnerDifficulty.setSelection(playerManager.getArcadeDifficulty(), false);
        spinnerNbQuestions.setSelection(playerManager.getNbQuestions(), false);

        // Appliquer thème
        applyTheme(playerManager.isDarkModeEnabled());
    }


    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    currentUid = user.getUid();
                    // Si déjà pseudo sauvegardé, on l'affiche
                    String savedPseudo = prefs.getString("pseudo", "");
                    if (!savedPseudo.isEmpty()) {
                        editPseudo.setText(savedPseudo);
                        pseudoStatus.setImageResource(R.drawable.circle_check_full);
                    }
                }
            } else {
                Toast.makeText(getContext(), "Firebase Auth failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkPseudoAvailable(String pseudo) {
        if (pseudo.isEmpty()) {
            pseudoStatus.setImageResource(R.drawable.circle_xmark_full);
            return;
        }

        playersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                boolean exists = false;
                for (DataSnapshot snap : task.getResult().getChildren()) {
                    String existingPseudo = snap.child("pseudo").getValue(String.class);
                    if (pseudo.equalsIgnoreCase(existingPseudo) && !snap.getKey().equals(currentUid)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    pseudoStatus.setImageResource(R.drawable.circle_xmark_full);
                } else {
                    pseudoStatus.setImageResource(R.drawable.circle_check_full);
                }
            }
        });
    }

    private void savePseudo() {
        String pseudo = editPseudo.getText().toString().trim();
        if (pseudo.isEmpty()) {
            Toast.makeText(getContext(), "You must enter a pseudo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérification finale
        playersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                boolean exists = false;
                for (DataSnapshot snap : task.getResult().getChildren()) {
                    String existingPseudo = snap.child("pseudo").getValue(String.class);
                    if (pseudo.equalsIgnoreCase(existingPseudo) && !snap.getKey().equals(currentUid)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    Toast.makeText(getContext(), "Pseudo déjà utilisé", Toast.LENGTH_SHORT).show();
                    pseudoStatus.setImageResource(R.drawable.circle_xmark_full);
                } else {
                    // Sauvegarde Firebase
                    Map<String, Object> data = new HashMap<>();
                    data.put("pseudo", pseudo);
                    data.put("uid", currentUid);
                    playersRef.child(currentUid).updateChildren(data);

                    // Sauvegarde SharedPreferences
                    prefs.edit().putString("pseudo", pseudo).apply();
                    prefs.edit().putString("uid", currentUid).apply();

                    Toast.makeText(getContext(), "Pseudo enregistré ✅", Toast.LENGTH_SHORT).show();
                    pseudoStatus.setImageResource(R.drawable.circle_check_full);
                }
            }
        });
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
