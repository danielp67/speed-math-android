package com.example.speedMath.ui.league;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.speedMath.R;
import com.example.speedMath.core.LevelGenerator;
import com.example.speedMath.core.PlayerManager;

import java.util.List;

public class LeagueFragment extends Fragment {

    private RecyclerView recyclerView;
    private LevelAdapter adapter;
    private List<LevelItem> levels;
    private PlayerManager playerManager;
    private TextView textScoreRight, textLevelNumber;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_league, container, false);

        recyclerView = root.findViewById(R.id.recyclerLevels);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        textLevelNumber = root.findViewById(R.id.textLevelNumber);
        textScoreRight = root.findViewById(R.id.textScoreRight);

        playerManager = PlayerManager.getInstance(requireContext());

        int currentLevel = playerManager.getCurrentLevel();
        int score = playerManager.getTotalScore();
        textLevelNumber.setText(currentLevel + " ⭐");
        textScoreRight.setText(score + " pts");

        adapter = new LevelAdapter(LevelGenerator.generateLevels(playerManager.getCurrentLevel()), v -> {
            LevelItem item = (LevelItem) v.getTag();
            Bundle args = new Bundle();
            args.putInt("LEVEL", item.levelNumber);
            args.putString("MODE", item.mode);
            args.putLong("TARGET_SCORE", item.targetScore);
            args.putInt("DIFFICULTY", item.difficulty);
            args.putInt("STATUS", 0);
            Navigation.findNavController(v)
                    .navigate(R.id.action_navigation_dashboard_to_levelFragment, args);
        });

        recyclerView.setAdapter(adapter);

        return root;
    }

/*    public static void setStarIcon(TextView tv, int count, Context context) {
        SpannableStringBuilder sb = new SpannableStringBuilder(count + " ⭐⭐⭐");



            Drawable d = ContextCompat.getDrawable(context, R.drawable.star_solid_full);// vector drawable

            // Taille = 120% du texte
            int size = (int) (tv.getTextSize() * 1f);
            d.setBounds(0, 0, size, size);

            // Tint : couleur or
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, ContextCompat.getColor(context, R.color.gold_accent));
 //           int color = MaterialColors.getColor(context, R.attr.colorBackground, Color.BLACK);
 //           DrawableCompat.setTint(drawable, color);

            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_CENTER);
          //  sb.setSpan(span, i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(span, 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        tv.setText(sb);
    }*/

}
