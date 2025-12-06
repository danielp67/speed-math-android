package com.example.speedMath.ui.arcade;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.speedMath.R;
import com.example.speedMath.core.FeedbackManager;
import com.example.speedMath.core.PlayerManager;

import java.util.ArrayList;
import java.util.List;

public class ArcadeFragment extends Fragment {

    private FeedbackManager feedbackManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_arcade, container, false);
        feedbackManager = new FeedbackManager(requireContext());
        feedbackManager.loadSounds(R.raw.correct, R.raw.wrong, R.raw.levelup);


        RecyclerView recycler = root.findViewById(R.id.recyclerArcade);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<ArcadeItem> items = new ArrayList<>();
        items.add(new ArcadeItem("👤", 16,"Solo", "🔢 👤", "QCM"));
        items.add(new ArcadeItem("👥", 16,"Battle", "🔢 👥", "DUAL"));
        items.add(new ArcadeItem("🧠", 16,"Memory", "Find pairs", "MEMORY"));
        items.add(new ArcadeItem("+ - × ÷", 10,"All Suite", "+ - × ÷", "ALL"));
        items.add(new ArcadeItem("+", 16,"Addition Suite", "a + b", "ADD"));
        items.add(new ArcadeItem("-", 16,"Subtraction Suite", "a - b", "SUB"));
        items.add(new ArcadeItem("×", 16,"Multiplication Suite", "a × b", "MUL"));
        items.add(new ArcadeItem("÷", 16,"Division Suite", "a ÷ b", "DIV"));

        ArcadeAdapter adapter = new ArcadeAdapter(items, v -> openGame(v));
        recycler.setAdapter(adapter);

        return root;
    }

    private void openGame(View v) {
        String mode = (String) v.getTag();

        feedbackManager.correctFeedback(v);

        Bundle args = new Bundle();
        args.putString("MODE", mode);

        switch (mode) {
            case "QCM":
                Navigation.findNavController(v)
                        .navigate(R.id.action_navigation_home_to_qcmFragment, args);
                break;

            case "DUAL":
                Navigation.findNavController(v)
                        .navigate(R.id.action_navigation_home_to_dualFragment, args);
                break;

            case "MEMORY":
                Navigation.findNavController(v)
                        .navigate(R.id.action_navigation_home_to_memoryFragment, args);
                break;

            default:
                Navigation.findNavController(v)
                        .navigate(R.id.action_navigation_home_to_gameFragment, args);
        }
    }
}
