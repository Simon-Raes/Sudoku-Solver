package be.simonraes.sudokusolver.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import be.simonraes.sudokusolver.R;

/**
 * Custom View to display a 9*9 Sudoku grid.
 * Created by Simon Raes on 25/07/2014.
 */
public class SudokuView extends View {

    private static final String TAG = "Sudoku";

    private float width;          // width of one tile
    private float height;         // height of one tile
    private int selectionX;       // X index of selection
    private int selectionY;       // Y index of selection

    private final Rect selRect = new Rect();

    private int[][] values;
    private int[][] errors = new int[9][9];
    private int[][] enteredValues;


    public SudokuView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public SudokuView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SudokuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public Parcelable onSaveInstanceState() {

        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putInt("selectionX", selectionX);
        bundle.putInt("selectionY", selectionY);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            selectionX = bundle.getInt("selectionX");
            selectionY = bundle.getInt("selectionY");
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w / 9f;
        height = h / 9f;
        getRect(selectionX, selectionY, selRect);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Makes the view square (to whichever dimension is smallest).
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int smallestDimension = getMeasuredWidth() > getMeasuredHeight() ? getMeasuredHeight() : getMeasuredWidth();
        setMeasuredDimension(smallestDimension, smallestDimension);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        drawBoard(canvas);
    }

    private void drawBoard(Canvas canvas) {

        // Draw the background...
        Paint background = new Paint();
        background.setColor(getResources().getColor(R.color.puzzle_background));
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);

        // Draw the board...

        // Define colors for the grid lines
        Paint dark = new Paint();
        dark.setColor(getResources().getColor(R.color.puzzle_dark));

        Paint hilite = new Paint();
        hilite.setColor(getResources().getColor(R.color.puzzle_hilite));

        Paint light = new Paint();
        light.setColor(getResources().getColor(R.color.puzzle_light));

        // Draw the minor grid lines
        for (int i = 0; i < 9; i++) {
            canvas.drawLine(0, i * height, getWidth(), i * height, light);
            canvas.drawLine(0, i * height + 1, getWidth(), i * height + 1, hilite);
            canvas.drawLine(i * width, 0, i * width, getHeight(), light);
            canvas.drawLine(i * width + 1, 0, i * width + 1, getHeight(), hilite);
        }

        // Draw the major grid lines
        for (int i = 0; i < 9; i++) {
            if (i % 3 != 0)
                continue;
            canvas.drawLine(0, i * height, getWidth(), i * height, dark);
            canvas.drawLine(0, i * height + 1, getWidth(), i * height + 1, hilite);
            canvas.drawLine(i * width, 0, i * width, getHeight(), dark);
            canvas.drawLine(i * width + 1, 0, i * width + 1, getHeight(), hilite);
        }

        // Draw the numbers...

        // Numbers found by the algorithm.
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(getResources().getColor(R.color.puzzle_foreground));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(height * 0.75f);
        textPaint.setTextScaleX(width / height);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Numbers that make the input impossible to solve.
        Paint errorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        errorPaint.setColor(getResources().getColor(R.color.DireOrange));
        errorPaint.setStyle(Paint.Style.FILL);
        errorPaint.setTextSize(height * 0.75f);
        errorPaint.setStrokeWidth(3);
        errorPaint.setTextScaleX(width / height);
        errorPaint.setTextAlign(Paint.Align.CENTER);

        // Correct input that was entered by the user.
        Paint enteredPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        enteredPaint.setColor(getResources().getColor(R.color.RadiantGreen));
        enteredPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        enteredPaint.setTextSize(height * 0.75f);
        enteredPaint.setStrokeWidth(3);
        enteredPaint.setTextScaleX(width / height);
        enteredPaint.setTextAlign(Paint.Align.CENTER);

        // Draw the number in the center of the tile
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        // Centering in X: use alignment (and X at midpoint)
        float x = width / 2;
        // Centering in Y: measure ascent/descent first
        float y = height / 2 - (fm.ascent + fm.descent) / 2;

        if (values != null) {

            Paint paintForSquare = null;
            String textForSquare = "";

            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {

                    // Set correct paint style.
                    if (enteredValues[i][j] == values[i][j]) {
                        paintForSquare = enteredPaint;
                    }
                    if (errors != null && errors[i][j] != 0 && errors[i][j] == values[i][j]) {
                        paintForSquare = errorPaint;
                    }
                    if (paintForSquare == null) {
                        paintForSquare = textPaint;
                    }

                    // Set text for square.
                    if (Integer.toString(values[i][j]).equals("0")) {
                        textForSquare = "";
                    } else {
                        textForSquare = Integer.toString(values[i][j]);
                    }

                    canvas.drawText(textForSquare, i * width + x, j * height + y, paintForSquare);

                    // Reset paint so check will still work for the next square.
                    paintForSquare = null;
                }
            }
        }

        // Draw the selection box.

        Paint selectionPaint = new Paint();
        selectionPaint.setColor(getResources().getColor(R.color.puzzle_selected));
        canvas.drawRect(selRect, selectionPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }
        select((int) (event.getX() / width), (int) (event.getY() / height));

        return true;
    }


    public void setValues(int[][] values, int[][] errors, int[][] enteredValues) {
        this.values = values;
        this.errors = errors;
        this.enteredValues = enteredValues;
        invalidate();
    }

    private void select(int x, int y) {
        invalidate(selRect);
        selectionX = Math.min(Math.max(x, 0), 8);
        selectionY = Math.min(Math.max(y, 0), 8);
        getRect(selectionX, selectionY, selRect);
        invalidate(selRect);
    }


    private void getRect(int x, int y, Rect rect) {
        rect.set((int) (x * width), (int) (y * height), (int) (x * width + width), (int) (y * height + height));
    }

    public int getSelectedX() {
        return selectionX;
    }

    public int getSelectedY() {
        return selectionY;
    }
}

