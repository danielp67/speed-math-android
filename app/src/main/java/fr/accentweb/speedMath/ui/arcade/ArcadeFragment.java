package fr.accentweb.speedMath.ui.arcade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.FeedbackManager;
import fr.accentweb.speedMath.core.GameDifficulty;
import fr.accentweb.speedMath.core.MemoryDifficulty;
import fr.accentweb.speedMath.core.PlayerManager;
import fr.accentweb.speedMath.utils.NetworkUtils;
import java.util.ArrayList;
import java.util.List;

public class ArcadeFragment extends Fragment implements ArcadeAdapter.OnItemClickListener {

    private PlayerManager playerManager;
    private FeedbackManager feedbackManager;
    private ArcadeAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_arcade, container, false);
        feedbackManager = new FeedbackManager(requireContext());
        feedbackManager.loadSounds(R.raw.correct, R.raw.wrong, R.raw.levelup);
        playerManager = PlayerManager.getInstance(requireContext());

        RecyclerView recycler = root.findViewById(R.id.recyclerArcade);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<ArcadeItem> items = new ArrayList<>();
        items.add(new ArcadeItem("👤", 16, "Solo", "QCM Mode", "QCM"));
        items.add(new ArcadeItem("👥", 16, "Battle", "Dual Mode", "DUAL"));
        items.add(new ArcadeItem("\uD83C\uDF10", 16, "Online", "Online Mode", "ONLINE"));
        items.add(new ArcadeItem("🧠", 16, "Memory", "Find pairs", "MEMORY"));
        items.add(new ArcadeItem("🧠🧠", 10, "Memory Duo", "🧠 vs 🧠", "MEMORY_DUO"));
        items.add(new ArcadeItem("+ - × ÷", 10, "All Suite", "Mixed operations", "ALL"));
        items.add(new ArcadeItem("+", 16, "Addition Suite", "a + b", "ADD"));
        items.add(new ArcadeItem("-", 16, "Subtraction Suite", "a - b", "SUB"));
        items.add(new ArcadeItem("×", 16, "Multiplication Suite", "a × b", "MUL"));
        items.add(new ArcadeItem("÷", 16, "Division Suite", "a ÷ b", "DIV"));

        adapter = new ArcadeAdapter(items, this, playerManager);
        recycler.setAdapter(adapter);

        return root;
    }

    @Override
    public void onPlayClick(View v, String mode) {
        feedbackManager.correctFeedback(v);
        Bundle args = new Bundle();
        args.putString("MODE", mode);

        switch (mode) {
            case "QCM":
                args.putInt("DIFFICULTY", playerManager.getSoloDifficulty());
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_qcmFragment, args);
                break;
            case "DUAL":
                args.putInt("DIFFICULTY", playerManager.getBattleDifficulty());
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_dualFragment, args);
                break;
            case "ONLINE":
                onlineMode(v, args);
                break;
            case "MEMORY":
                args.putInt("DIFFICULTY", playerManager.getMemoryDifficulty());
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_memoryFragment, args);
                break;
            case "MEMORY_DUO":
                args.putInt("DIFFICULTY", playerManager.getMemoryDuoDifficulty());
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_memoryDualFragment, args);
                break;
            default:
                args.putInt("DIFFICULTY", getCurrentGameDifficultyValue(mode));
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_gameFragment, args);
        }
    }

    @Override
    public void onSettingsClick(View v, String mode) {
        Object currentDifficulty;

        if (mode.equals("MEMORY")) {
            int savedDifficulty = playerManager.getMemoryDifficulty();
            currentDifficulty = MemoryDifficulty.values()[savedDifficulty];
        }
        else if (mode.equals("MEMORY_DUO")) {
            int savedDifficulty = playerManager.getMemoryDuoDifficulty();
            currentDifficulty = MemoryDifficulty.values()[savedDifficulty];
        }
        else {
            currentDifficulty = getCurrentGameDifficulty(mode);
        }

        DifficultyDialog dialog = new DifficultyDialog(
                playerManager,
                currentDifficulty,
                mode,
                () -> adapter.notifyDataSetChanged()
        );
        dialog.show(getParentFragmentManager(), "difficulty_dialog");
    }

    private GameDifficulty getCurrentGameDifficulty(String mode) {
        int difficultyValue = 3; // PROGRESSIVE par défaut

        switch (mode) {
            case "QCM": difficultyValue = playerManager.getSoloDifficulty(); break;
            case "DUAL": difficultyValue = playerManager.getBattleDifficulty(); break;
            case "ALL": difficultyValue = playerManager.getAllSuiteDifficulty(); break;
            case "ADD": difficultyValue = playerManager.getAddSuiteDifficulty(); break;
            case "SUB": difficultyValue = playerManager.getSubSuiteDifficulty(); break;
            case "MUL": difficultyValue = playerManager.getMulSuiteDifficulty(); break;
            case "DIV": difficultyValue = playerManager.getDivSuiteDifficulty(); break;
        }

        return GameDifficulty.fromValue(difficultyValue);
    }

    private int getCurrentGameDifficultyValue(String mode) {
        switch (mode) {
            case "QCM": return playerManager.getSoloDifficulty();
            case "DUAL": return playerManager.getBattleDifficulty();
            case "ALL": return playerManager.getAllSuiteDifficulty();
            case "ADD": return playerManager.getAddSuiteDifficulty();
            case "SUB": return playerManager.getSubSuiteDifficulty();
            case "MUL": return playerManager.getMulSuiteDifficulty();
            case "DIV": return playerManager.getDivSuiteDifficulty();
            default: return 3; // PROGRESSIVE par défaut
        }
    }

    private void onlineMode(View v, Bundle args) {
        if (!NetworkUtils.isInternetAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection. Online mode unavailable.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (playerManager.getTodayDate().equals(playerManager.getLastConnection()) &&
                playerManager.getDailyMatchPlayed() >= playerManager.getDailyMatchLimit()) {
            Toast.makeText(requireContext(), "Daily Limit Reached: " + playerManager.getDailyMatchLimit() + " matches played.", Toast.LENGTH_SHORT).show();
        } else if (playerManager.getOnlinePseudo().isEmpty()) {
            Toast.makeText(requireContext(), "Please set a pseudo before playing online.", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_settingsFragment);
        } else {
            Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_waitingRoomFragment, args);
        }
    }
}
