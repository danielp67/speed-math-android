package fr.accentweb.speedMath.ui.arcade;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.PlayerManager;

public class MemoryDifficultyDialog extends DialogFragment {

    private PlayerManager playerManager;
    private MemoryDifficulty selectedDifficulty;
    private String mode; // "MEMORY" ou "MEMORY_DUO"
    private Runnable onDismissCallback;

    public MemoryDifficultyDialog(PlayerManager playerManager, MemoryDifficulty current, String mode, Runnable onDismissCallback) {
        this.playerManager = playerManager;
        this.selectedDifficulty = current;
        this.mode = mode;
        this.onDismissCallback = onDismissCallback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_difficulty, null);

        Button btnEasy = view.findViewById(R.id.btnEasy);
        Button btnMedium = view.findViewById(R.id.btnMedium);
        Button btnHard = view.findViewById(R.id.btnHard);

        updateButtonSelection(btnEasy, btnMedium, btnHard, selectedDifficulty);

        btnEasy.setOnClickListener(v -> {
            selectedDifficulty = MemoryDifficulty.EASY;
            updateButtonSelection(btnEasy, btnMedium, btnHard, selectedDifficulty);
        });

        btnMedium.setOnClickListener(v -> {
            selectedDifficulty = MemoryDifficulty.MEDIUM;
            updateButtonSelection(btnEasy, btnMedium, btnHard, selectedDifficulty);
        });

        btnHard.setOnClickListener(v -> {
            selectedDifficulty = MemoryDifficulty.HARD;
            updateButtonSelection(btnEasy, btnMedium, btnHard, selectedDifficulty);
        });

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Sauvegarder dans la bonne clé selon le mode
                    if (mode.equals("MEMORY")) {
                        playerManager.setMemoryDifficulty(selectedDifficulty.ordinal());
                    } else if (mode.equals("MEMORY_DUO")) {
                        playerManager.setMemoryDuoDifficulty(selectedDifficulty.ordinal());
                    }
                    if (onDismissCallback != null) {
                        onDismissCallback.run();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .create();
    }

    private void updateButtonSelection(Button btnEasy, Button btnMedium, Button btnHard, MemoryDifficulty difficulty) {
        btnEasy.setSelected(difficulty == MemoryDifficulty.EASY);
        btnMedium.setSelected(difficulty == MemoryDifficulty.MEDIUM);
        btnHard.setSelected(difficulty == MemoryDifficulty.HARD);

        btnEasy.setTextColor(difficulty == MemoryDifficulty.EASY ? Color.WHITE : Color.BLACK);
        btnMedium.setTextColor(difficulty == MemoryDifficulty.MEDIUM ? Color.WHITE : Color.BLACK);
        btnHard.setTextColor(difficulty == MemoryDifficulty.HARD ? Color.WHITE : Color.BLACK);
    }
}
