package fr.accentweb.speedMath.ui.settings;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.GameDifficulty;
import fr.accentweb.speedMath.core.MemoryDifficulty;
import fr.accentweb.speedMath.core.PlayerManager;
import fr.accentweb.speedMath.databinding.FragmentSettingsBinding;
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
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
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

        // --- Reset score ---
        btnResetScore.setOnClickListener(v -> {

            if (playerManager.isHapticEnabled()) {
                btnResetScore.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete all datas and account ?")
                    .setMessage("Are you sure ?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        resetAllSettings();

                        Toast.makeText(getContext(), "Settings reset ✅", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


        editPseudo = root.findViewById(R.id.editPseudo);
        pseudoStatus = root.findViewById(R.id.pseudoStatus);
        btnSavePseudo = root.findViewById(R.id.btnSavePseudo);

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
        playerManager.setSoloDifficulty(GameDifficulty.PROGRESSIVE.ordinal());
        playerManager.setBattleDifficulty(GameDifficulty.PROGRESSIVE.ordinal());
        playerManager.setAllSuiteDifficulty(GameDifficulty.PROGRESSIVE.ordinal());
        playerManager.setAddSuiteDifficulty(GameDifficulty.PROGRESSIVE.ordinal());
        playerManager.setSubSuiteDifficulty(GameDifficulty.PROGRESSIVE.ordinal());
        playerManager.setMulSuiteDifficulty(GameDifficulty.PROGRESSIVE.ordinal());
        playerManager.setDivSuiteDifficulty(GameDifficulty.PROGRESSIVE.ordinal());
        playerManager.setMemoryDifficulty(MemoryDifficulty.EASY.ordinal());
        playerManager.setMemoryDuoDifficulty(MemoryDifficulty.EASY.ordinal());
        playerManager.setOnlinePseudo("");
        playerManager.setOnlineUid("");
        playerManager.setOnlineScore(0);
        playerManager.setOnlinePlayedMatches(0);
        playerManager.setOnlineWins(0);
        playerManager.setOnlineLosses(0);
        playerManager.setOnlineDraws(0);
        playerManager.setCurrentLevel(0);
        playerManager.setLastPlayedLevel(0);
        editPseudo.setText("");


        // Réinitialiser l'UI (Switch + Spinners)
        SwitchCompat switchDark = requireView().findViewById(R.id.switchDark);
        SwitchCompat switchSound = requireView().findViewById(R.id.switchSound);
        SwitchCompat switchMusic = requireView().findViewById(R.id.switchMusic);
        SwitchCompat switchVibration = requireView().findViewById(R.id.switchVibration);
        SwitchCompat switchAnimation = requireView().findViewById(R.id.switchAnimations);
        SwitchCompat switchHaptic = requireView().findViewById(R.id.switchHaptic);

        // Switch
        switchDark.setChecked(playerManager.isDarkModeEnabled());
        switchSound.setChecked(playerManager.isSoundEnabled());
        switchMusic.setChecked(playerManager.isMusicEnabled());
        switchVibration.setChecked(playerManager.isVibrationEnabled());
        switchAnimation.setChecked(playerManager.isAnimationEnabled());
        switchHaptic.setChecked(playerManager.isHapticEnabled());

        // Appliquer thème
        applyTheme(playerManager.isDarkModeEnabled());

        // Firebase delete from players list
        playersRef = FirebaseDatabase.getInstance().getReference("players");
        playersRef.child(currentUid).removeValue();

    }


    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                 currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    currentUid = currentUser.getUid();
                    String savedPseudo =  playerManager.getOnlinePseudo();
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
            Toast.makeText(getContext(), "You must enter an available pseudo", Toast.LENGTH_SHORT).show();
            return;
        }

        // A user is new if they don't have a locally saved pseudo.
        final boolean isNewPlayer = playerManager.getOnlineUid().isEmpty();
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
                    Toast.makeText(getContext(), "Account already exists", Toast.LENGTH_SHORT).show();
                    pseudoStatus.setImageResource(R.drawable.circle_xmark_full);
                } else {
                    // Sauvegarde Firebase
                    Map<String, Object> data = new HashMap<>();
                    data.put("pseudo", pseudo);
                    data.put("uid", currentUid);

                    if (isNewPlayer) {
                        data.put("points", 0);
                        data.put("matches_played", 0);
                        data.put("matches_won", 0);
                        data.put("matches_drawn", 0);
                        data.put("matches_lost", 0);
                        data.put("daily_match_played",0);
                        data.put("daily_match_limit",5);
                        data.put("last_connection", "");
                        data.put("rank", 999999);

                        playerManager.setDailyMatchLimit(5);
                        playerManager.setDailyMatchPlayed(0);
                        playerManager.setLastConnection(playerManager.getTodayDate());
                        playerManager.setRank(999999);
                        playerManager.setOnlineScore(0);
                        playerManager.setOnlinePlayedMatches(0);
                        playerManager.setOnlineWins(0);
                        playerManager.setOnlineLosses(0);
                        playerManager.setOnlineDraws(0);

                    }

                    playersRef.child(currentUid).updateChildren(data);

                    // Sauvegarde SharedPreferences
                    playerManager.setOnlinePseudo(pseudo);
                    playerManager.setOnlineUid(currentUid);

                    Toast.makeText(getContext(), "Account saved ✅", Toast.LENGTH_SHORT).show();
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
