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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.GameDifficulty;
import fr.accentweb.speedMath.core.MemoryDifficulty;
import fr.accentweb.speedMath.core.PlayerManager;

public class DifficultyDialog extends DialogFragment {

    private PlayerManager playerManager;
    private Object currentDifficulty;
    private String mode;
    private Runnable onDismissCallback;
    private boolean isMemoryMode;

    public DifficultyDialog(PlayerManager playerManager, Object currentDifficulty,
                            String mode, Runnable onDismissCallback) {
        this.playerManager = playerManager;
        this.currentDifficulty = currentDifficulty;
        this.mode = mode;
        this.onDismissCallback = onDismissCallback;
        this.isMemoryMode = mode.equals("MEMORY") || mode.equals("MEMORY_DUO");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_difficulty, null);

        Button btnProgressive = view.findViewById(R.id.btnProgressive);
        Button btnEasy = view.findViewById(R.id.btnEasy);
        Button btnMedium = view.findViewById(R.id.btnMedium);
        Button btnHard = view.findViewById(R.id.btnHard);
        Button btnExtreme = view.findViewById(R.id.btnExtreme);

        // Masquer les boutons non pertinents pour le mode Memory
        if (isMemoryMode) {
            btnProgressive.setVisibility(View.GONE);
            btnExtreme.setVisibility(View.GONE);
        }

        // Sélectionner le bouton correspondant à la difficulté actuelle
        updateButtonSelection(btnProgressive, btnEasy, btnMedium, btnHard, btnExtreme);

        // Pour les modes non-Memory
        if (!isMemoryMode) {
            btnProgressive.setOnClickListener(v -> {
                currentDifficulty = GameDifficulty.PROGRESSIVE;
                updateButtonSelection(btnProgressive, btnEasy, btnMedium, btnHard, btnExtreme);
            });
        }

        btnEasy.setOnClickListener(v -> {
            currentDifficulty = isMemoryMode ?
                    MemoryDifficulty.EASY : GameDifficulty.EASY;
            updateButtonSelection(btnProgressive, btnEasy, btnMedium, btnHard, btnExtreme);
        });

        btnMedium.setOnClickListener(v -> {
            currentDifficulty = isMemoryMode ?
                    MemoryDifficulty.MEDIUM : GameDifficulty.MEDIUM;
            updateButtonSelection(btnProgressive, btnEasy, btnMedium, btnHard, btnExtreme);
        });

        btnHard.setOnClickListener(v -> {
            currentDifficulty = isMemoryMode ?
                    MemoryDifficulty.HARD : GameDifficulty.HARD;
            updateButtonSelection(btnProgressive, btnEasy, btnMedium, btnHard, btnExtreme);
        });

        if (!isMemoryMode) {
            btnExtreme.setOnClickListener(v -> {
                currentDifficulty = GameDifficulty.EXTREME;
                updateButtonSelection(btnProgressive, btnEasy, btnMedium, btnHard, btnExtreme);
            });
        }

        return new AlertDialog.Builder(requireContext(), R.style.SpeedMath_Dialog)
                .setView(view)
                .setTitle(getDialogTitle())
                .setPositiveButton("OK", (dialog, which) -> {
                    saveDifficulty();
                    if (onDismissCallback != null) {
                        onDismissCallback.run();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .create();
    }

    private String getDialogTitle() {
        if (mode.equals("MEMORY")) return "Memory Difficulty";
        if (mode.equals("MEMORY_DUO")) return "Memory Duo Difficulty";
        return getModeTitle(mode) + " Difficulty";
    }

    private String getModeTitle(String mode) {
        switch (mode) {
            case "QCM": return "Solo";
            case "DUAL": return "Battle";
            case "ALL": return "All Operations";
            case "ADD": return "Addition";
            case "SUB": return "Subtraction";
            case "MUL": return "Multiplication";
            case "DIV": return "Division";
            default: return mode;
        }
    }

    private void updateButtonSelection(Button btnProgressive, Button btnEasy,
                                       Button btnMedium, Button btnHard, Button btnExtreme) {
        // Réinitialiser tous les boutons
        btnProgressive.setSelected(false);
        btnEasy.setSelected(false);
        btnMedium.setSelected(false);
        btnHard.setSelected(false);
        btnExtreme.setSelected(false);

        btnProgressive.setTextColor(Color.BLACK);
        btnEasy.setTextColor(Color.BLACK);
        btnMedium.setTextColor(Color.BLACK);
        btnHard.setTextColor(Color.BLACK);
        btnExtreme.setTextColor(Color.BLACK);

        if (isMemoryMode) {
            MemoryDifficulty memoryDifficulty = (MemoryDifficulty) currentDifficulty;
            if (memoryDifficulty == MemoryDifficulty.EASY) {
                btnEasy.setSelected(true);
                btnEasy.setTextColor(Color.WHITE);
            } else if (memoryDifficulty == MemoryDifficulty.MEDIUM) {
                btnMedium.setSelected(true);
                btnMedium.setTextColor(Color.WHITE);
            } else if (memoryDifficulty == MemoryDifficulty.HARD) {
                btnHard.setSelected(true);
                btnHard.setTextColor(Color.WHITE);
            }
        } else {
            GameDifficulty gameDifficulty = (GameDifficulty) currentDifficulty;
            if (gameDifficulty == GameDifficulty.PROGRESSIVE) {
                btnProgressive.setSelected(true);
                btnProgressive.setTextColor(Color.WHITE);
            } else if (gameDifficulty == GameDifficulty.EASY) {
                btnEasy.setSelected(true);
                btnEasy.setTextColor(Color.WHITE);
            } else if (gameDifficulty == GameDifficulty.MEDIUM) {
                btnMedium.setSelected(true);
                btnMedium.setTextColor(Color.WHITE);
            } else if (gameDifficulty == GameDifficulty.HARD) {
                btnHard.setSelected(true);
                btnHard.setTextColor(Color.WHITE);
            } else if (gameDifficulty == GameDifficulty.EXTREME) {
                btnExtreme.setSelected(true);
                btnExtreme.setTextColor(Color.WHITE);
            }
        }
    }

    private void saveDifficulty() {
        if (isMemoryMode) {
            MemoryDifficulty selected = (MemoryDifficulty) currentDifficulty;
            if (mode.equals("MEMORY")) {
                playerManager.setMemoryDifficulty(selected.ordinal());
            } else if (mode.equals("MEMORY_DUO")) {
                playerManager.setMemoryDuoDifficulty(selected.ordinal());
            }
        } else {
            GameDifficulty selected = (GameDifficulty) currentDifficulty;
            switch (mode) {
                case "QCM": playerManager.setSoloDifficulty(selected.getValue()); break;
                case "DUAL": playerManager.setBattleDifficulty(selected.getValue()); break;
                case "ALL": playerManager.setAllSuiteDifficulty(selected.getValue()); break;
                case "ADD": playerManager.setAddSuiteDifficulty(selected.getValue()); break;
                case "SUB": playerManager.setSubSuiteDifficulty(selected.getValue()); break;
                case "MUL": playerManager.setMulSuiteDifficulty(selected.getValue()); break;
                case "DIV": playerManager.setDivSuiteDifficulty(selected.getValue()); break;
            }
        }
    }
}
