package in.sunilpaulmathew.batteryplus.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

import in.sunilpaulmathew.batteryplus.R;
import in.sunilpaulmathew.batteryplus.serializables.DataEntry;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Sept. 07, 2026
 */
public class XYPlot extends View {

    private Paint linePaint, gridPaint, textPaint;
    private List<DataEntry> dataPoints;
    private String unit = "";

    public XYPlot(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        int themeColor = Color.parseColor(Settings.isDarkTheme(getContext()) ? "#82B1FF" : "#2962FF");

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(themeColor);
        linePaint.setStrokeWidth(4f);
        linePaint.setStyle(Paint.Style.STROKE);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#44AAAAAA"));
        gridPaint.setStrokeWidth(2f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(themeColor);
        textPaint.setTextSize(48f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setData(List<DataEntry> points) {
        boolean wasEmpty = (this.dataPoints == null || this.dataPoints.size() < 2);
        this.dataPoints = points;
        boolean isEmptyNow = (this.dataPoints == null || this.dataPoints.size() < 2);

        if (wasEmpty != isEmptyNow) {
            requestLayout();
        } else {
            invalidate();
        }
    }

    public void setUnit(String unit) {
        this.unit = unit != null ? unit : "";
        invalidate();
    }

    private String formatValue(float value) {
        int decimals;
        String upperUnit = unit.toUpperCase(Locale.getDefault());

        if (upperUnit.contains("V")) {
            decimals = 2;
        } else if (upperUnit.contains("W") || upperUnit.contains("°")) {
            decimals = 1;
        } else {
            decimals = 0;
        }

        String formatString = "%." + decimals + "f %s";
        return String.format(Locale.getDefault(), formatString, value, unit).trim();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (dataPoints == null || dataPoints.size() < 2) {
            int compactHeightPx = (int) (60 * getResources().getDisplayMetrics().density);
            setMeasuredDimension(
                    MeasureSpec.getSize(widthMeasureSpec),
                    MeasureSpec.makeMeasureSpec(compactHeightPx, MeasureSpec.EXACTLY)
            );
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (dataPoints == null || dataPoints.size() < 2) {
            textPaint.setTextSize(48f);
            textPaint.setColor(linePaint.getColor());
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint.setTextAlign(Paint.Align.CENTER);

            float centerX = getWidth() / 2f;
            float centerY = (getHeight() / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);

            canvas.drawText(getContext().getString(R.string.status_analyzing), centerX, centerY, textPaint);
            return;
        }

        textPaint.setTextSize(30f);
        textPaint.setColor(linePaint.getColor());
        textPaint.setTypeface(Typeface.DEFAULT);

        float paddingLeft = 200f;
        float paddingRight = 100f;
        float paddingTop = 40f;
        float paddingBottom = 40f;

        float width = getWidth() - (paddingLeft + paddingRight);
        float height = getHeight() - (paddingTop + paddingBottom);

        float minValue = dataPoints.get(0).getValue();
        float maxValue = minValue;
        for (DataEntry pt : dataPoints) {
            if (pt.getValue() < minValue) minValue = pt.getValue();
            if (pt.getValue() > maxValue) maxValue = pt.getValue();
        }

        if (maxValue == minValue) {
            maxValue += 1f;
            minValue -= 1f;
        }

        float range = maxValue - minValue;
        minValue -= range * 0.05f;
        maxValue += range * 0.05f;

        textPaint.setTextAlign(Paint.Align.RIGHT);

        for (int i = 0; i <= 4; i++) {
            float y = paddingTop + (height * i / 4f);
            canvas.drawLine(paddingLeft, y, getWidth() - paddingRight, y, gridPaint);

            float valueAtGrid = maxValue - ((maxValue - minValue) * (i / 4f));

            String label = formatValue(valueAtGrid);

            canvas.drawText(label, paddingLeft - 15f, y + 10f, textPaint);
        }

        long minTime = dataPoints.get(0).getTimestamp();
        long maxTime = dataPoints.get(dataPoints.size() - 1).getTimestamp();
        long timeSpan = maxTime - minTime;
        if (timeSpan <= 0) timeSpan = 1;

        int xDivisions = 4;
        for (int i = 0; i <= xDivisions; i++) {
            float x = paddingLeft + (width * i / (float) xDivisions);
            canvas.drawLine(x, paddingTop, x, paddingTop + height, gridPaint);

            if (i == 0) {
                textPaint.setTextAlign(Paint.Align.LEFT);
            } else if (i == xDivisions) {
                textPaint.setTextAlign(Paint.Align.RIGHT);
            } else {
                textPaint.setTextAlign(Paint.Align.CENTER);
            }

            long timeAtPoint = minTime + (long) (timeSpan * ((float) i / xDivisions));

            canvas.drawText(getString(timeAtPoint, minTime, timeSpan), x, paddingTop + height + 40f, textPaint);
        }

        @SuppressLint("DrawAllocation")
        Path path = new Path();
        boolean first = true;

        for (int i = 0; i < dataPoints.size(); i++) {
            DataEntry pt = dataPoints.get(i);

            float xRatio = (float) (pt.getTimestamp() - minTime) / timeSpan;
            float x = paddingLeft + (xRatio * width);

            float yRatio = (pt.getValue() - minValue) / (maxValue - minValue);
            float y = paddingTop + height - (yRatio * height);

            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }

            canvas.drawCircle(x, y, 5f, linePaint);
        }

        canvas.drawPath(path, linePaint);
    }

    @NonNull
    private String getString(long timeAtPoint, long minTime, long timeSpan) {
        long elapsedMinutes = (timeAtPoint - minTime) / (1000 * 60);

        long totalMinutesSpan = timeSpan / (1000 * 60);
        String label;

        if (elapsedMinutes >= 60) {
            long hours = elapsedMinutes / 60;
            long mins = elapsedMinutes % 60;

            if (totalMinutesSpan > 180) {
                label = hours + "h";
            } else {
                label = mins > 0 ? hours + "h " + mins + "m" : hours + "h";
            }
        } else {
            label = elapsedMinutes + "m";
        }
        return label;
    }

}