package fr.accentweb.speedMath.ui.daily;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class MagicWheelView extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rectF = new RectF();
    private String[] labels = {"🏎️ Kart", "🛸 Space", "🧱 Tetris", "🏎️ Kart", "🛸 Space", "🧱 Tetris"};
    private int[] colors = {0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF3F51B5, 0xFF2196F3, 0xFF00BCD4};

    public MagicWheelView(Context context) {
        super(context);
    }

    public MagicWheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2 - 20;

        rectF.set(width / 2f - radius, height / 2f - radius, width / 2f + radius, height / 2f + radius);

        float startAngle = 0;
        float sweepAngle = 360f / 6;

        for (int i = 0; i < 6; i++) {
            paint.setColor(colors[i]);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

            // Draw label
            paint.setColor(Color.WHITE);
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);
            
            float angle = startAngle + sweepAngle / 2;
            double rad = Math.toRadians(angle);
            float x = (float) (width / 2f + (radius / 1.5f) * Math.cos(rad));
            float y = (float) (height / 2f + (radius / 1.5f) * Math.sin(rad));

            canvas.save();
            canvas.rotate(angle, x, y);
            canvas.drawText(labels[i], x, y, paint);
            canvas.restore();

            startAngle += sweepAngle;
        }

        // Draw center circle
        paint.setColor(Color.WHITE);
        canvas.drawCircle(width / 2f, height / 2f, 20, paint);
    }
}
