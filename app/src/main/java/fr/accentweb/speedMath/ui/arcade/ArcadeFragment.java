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
        items.add(new ArcadeItem("👤", 16, "Solo", "Difficulty: Medium", "QCM"));
        items.add(new ArcadeItem("👥", 16, "Battle", "Difficulty: Hard", "DUAL"));
        items.add(new ArcadeItem("\uD83C\uDF10", 16, "Online", "Difficulty: Expert", "ONLINE"));
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
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_qcmFragment, args);
                break;
            case "DUAL":
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_dualFragment, args);
                break;
            case "ONLINE":
                onlineMode(v, args);
                break;
            case "MEMORY":
                int savedMemoryDifficulty = playerManager.getMemoryDifficulty();
                MemoryDifficulty memoryDifficulty = MemoryDifficulty.values()[savedMemoryDifficulty];
                args.putSerializable("DIFFICULTY", memoryDifficulty);
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_memoryFragment, args);
                break;
            case "MEMORY_DUO":
                int savedMemoryDuoDifficulty = playerManager.getMemoryDuoDifficulty();
                MemoryDifficulty memoryDuoDifficulty = MemoryDifficulty.values()[savedMemoryDuoDifficulty];
                args.putSerializable("DIFFICULTY", memoryDuoDifficulty);
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_memoryDualFragment, args);
                break;
            default:
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_gameFragment, args);
        }
    }

    @Override
    public void onSettingsClick(View v, String mode) {
        if (mode.equals("MEMORY")) {
            int savedDifficulty = playerManager.getMemoryDifficulty();
            MemoryDifficulty currentDifficulty = MemoryDifficulty.values()[savedDifficulty];

            MemoryDifficultyDialog dialog = new MemoryDifficultyDialog(
                    playerManager,
                    currentDifficulty,
                    "MEMORY",
                    () -> adapter.notifyDataSetChanged()
            );
            dialog.show(getParentFragmentManager(), "difficulty_dialog");

        } else if (mode.equals("MEMORY_DUO")) {
            int savedDifficulty = playerManager.getMemoryDuoDifficulty();
            MemoryDifficulty currentDifficulty = MemoryDifficulty.values()[savedDifficulty];

            MemoryDifficultyDialog dialog = new MemoryDifficultyDialog(
                    playerManager,
                    currentDifficulty,
                    "MEMORY_DUO",
                    () -> adapter.notifyDataSetChanged()
            );
            dialog.show(getParentFragmentManager(), "difficulty_dialog");
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
