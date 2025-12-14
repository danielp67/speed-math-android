package fr.accentweb.speedMath.utils;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.content.Context;
import android.view.animation.TranslateAnimation;
import android.widget.Button;

public class AnimUtils {

    // Animation "pop" : grossit puis revient normal
    public static void pop(View v) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.2f, 1f);
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        scaleX.start();
        scaleY.start();
    }

    // Secouer (shake) : utile pour erreurs
    public static void shake(View v) {
        v.animate()
                .translationXBy(20)
                .setDuration(50)
                .withEndAction(() ->
                        v.animate()
                                .translationXBy(-20)
                                .setDuration(50)
                );
    }

    // Fade-in
    public static void fadeIn(View v) {
        v.setAlpha(0f);
        v.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    // Fade-out
    public static void fadeOut(View v) {
        v.animate()
                .alpha(0f)
                .setDuration(300)
                .start();
    }

    // Charger une anim XML
    public static void applyXml(Context ctx, View v, int animRes) {
        Animation a = AnimationUtils.loadAnimation(ctx, animRes);
        v.startAnimation(a);
    }

    public static void slideLeftRight(View v) {
        v.animate()
                .translationX(-200)
                .alpha(0)
                .setDuration(150)
                .withEndAction(() -> {
                    v.setTranslationX(200);
                    v.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    public static void comboPop(View v) {
        v.setScaleX(0.5f);
        v.setScaleY(0.5f);
        v.setAlpha(1f);

        v.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(150)
                .withEndAction(() ->
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(0f)
                                .setDuration(800)
                                .start()
                )
                .start();
    }


    // Flip vers l'avant (affiche le contenu)
    public static void flipToFront(final Button btn, final String frontText) {
        if (btn == null) return;

        // Animation de rétrécissement horizontal
        btn.animate()
                .scaleX(0f)
                .setDuration(120)
                .withEndAction(() -> {
                    // Changer le texte
                    btn.setText(frontText);
                    // Agrandir à nouveau pour simuler le flip
                    btn.setScaleX(0f);
                    btn.animate()
                            .scaleX(1f)
                            .setDuration(120)
                            .start();
                })
                .start();
    }

    // Flip vers l'arrière (cache le contenu)
    public static void flipToBack(final Button btn) {
        if (btn == null) return;

        btn.animate()
                .scaleX(0f)
                .setDuration(120)
                .withEndAction(() -> {
                    // Cacher le texte
                    btn.setText("");
                    btn.setScaleX(0f);
                    btn.animate()
                            .scaleX(1f)
                            .setDuration(120)
                            .start();
                })
                .start();
    }
    public static void slideTextTurn(View view, boolean isPlayer1) {
        float distance = 200f; // pixels de déplacement (tu peux adapter)

        float fromX = isPlayer1 ? distance : -distance;
        float toX = 0f;

        TranslateAnimation anim = new TranslateAnimation(
                fromX,  // startX
                toX,    // endX
                0f,     // startY
                0f      // endY
        );

        anim.setDuration(300);
        anim.setFillAfter(false); // important : la position revient automatiquement

        view.startAnimation(anim);
    }

    // Dans ta classe AnimUtils (ou directement dans GameFragment)
    public static void scaleAnimation(View view) {
        view.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(300)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(300)
                            .start();
                })
                .start();
    }

}
