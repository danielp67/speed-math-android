package com.example.speedMath.core;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.example.speedMath.MainActivity;

public abstract class BaseGameFragment extends Fragment {

    private GameTimer gameTimer;

    private FeedbackManager feedbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setNavigationEnabled(false);
        ((MainActivity) requireActivity()).animateNavigation(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) requireActivity()).setNavigationEnabled(true);
        ((MainActivity) requireActivity()).animateNavigation(true);
        if (gameTimer != null) gameTimer.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (feedbackManager != null) feedbackManager.release();
    }
}

