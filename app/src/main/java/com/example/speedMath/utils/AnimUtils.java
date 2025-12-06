package com.example.speedMath.utils;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.content.Context;

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

    // Flip 3D animation pour une carte (recto → verso ou inverse)
// On peut passer un callback (Runnable) pour exécuter du code au milieu du flip
    public static void flipCard(View card, Runnable midAction) {

        // Première partie : rotation 0 → 90°
        card.animate()
                .rotationY(90f)
                .setDuration(150)
                .withEndAction(() -> {

                    // Au milieu de la rotation : on change le contenu
                    if (midAction != null) midAction.run();

                    // Remet la rotation à 270° pour éviter un effet miroir
                    card.setRotationY(-90f);

                    // Deuxième partie : rotation 270° → 360°
                    card.animate()
                            .rotationY(0f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

}
