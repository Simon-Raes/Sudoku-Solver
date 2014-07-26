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

    private float width;               // Width of one tile
    private float height;              // Height of one tile
    private int selectionX = -1;       // X index of selection (0-8)
    private int selectionY = -1;       // Y index of selection (0-8)

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
    protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        width = newWidth / 9f;
        height = newHeight / 9f;
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
    }

    /**
     * Makes the view square (whichever dimension is smallest).
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int smallestDimension = getMeasuredWidth() > getMeasuredHeight() ? getMeasuredHeight() : getMeasuredWidth();
        setMeasuredDimension(smallestDimension, smallestDimension);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        drawBackground(canvas);
        drawGridLines(canvas);
        drawSelectionBox(canvas);
        drawNumbers(canvas);
    }

    private void drawBackground(Canvas canvas){
        Paint background = new Paint();
        background.setColor(getResources().getColor(R.color.VeryLightGrey));
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);
    }

    private void drawGridLines(Canvas canvas){

        Paint minorLinesPaint = new Paint();
        minorLinesPaint.setColor(getResources().getColor(R.color.DeleteGreyFocused));

        Paint majorLinesPaint = new Paint();
        majorLinesPaint.setColor(getResources().getColor(R.color.TextGrey));

        // (Slight overdraw here (some small gridlines are overwritten by major gridlines))

        // Draw the minor grid lines
        for (int i = 1; i < values.length; i++) {
            // Horizontal
            canvas.drawLine(0, i * height, getWidth(), i * height, minorLinesPaint);
            // Vertical
            canvas.drawLine(i * width, 0, i * width, getHeight(), minorLinesPaint);
        }

        // Draw the major grid lines
        for (int i = 0; i < 4; i++) {

            if(i<3){
                // Horizontal
                canvas.drawLine(0, i * 3 * height, getWidth(), i*3 * height, majorLinesPaint);
                // Vertical
                canvas.drawLine(i * 3 * width , 0, i *3* width, getHeight(), majorLinesPaint);
            } else {
                // Draw the last major line 1px closer so it doesn't fall outside the canvas.

                // Horizontal
                canvas.drawLine(0, i * 3 * height-1, getWidth(), i*3 * height-1, majorLinesPaint);
                // Vertical
                canvas.drawLine(i * 3 * width - 1, 0, i *3* width - 1, getHeight(), majorLinesPaint);
            }
        }
    }

    private void drawSelectionBox(Canvas canvas){
        if (getSelectedX() >= 0 && getSelectedY() >= 0) {
            Paint selectionPaint = new Paint();
            selectionPaint.setColor(getResources().getColor(R.color.SolveBlueFocused));
            canvas.drawRect(getSelectedX() * width + 5, getSelectedY() * height + 5, getSelectedX() * width + width - 5, getSelectedY() * height + height - 5, selectionPaint);

            Paint fillerPaint = new Paint();
            fillerPaint.setColor(getResources().getColor(R.color.VeryLightGrey));
            canvas.drawRect(getSelectedX() * width + 10, getSelectedY() * height + 10, getSelectedX() * width + width - 10, getSelectedY() * height + height - 10, fillerPaint);
        }
    }

    private void drawNumbers(Canvas canvas){

        // Numbers found by the algorithm.
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(getResources().getColor(R.color.TextGrey));
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextSize(height * 0.75f);
        textPaint.setTextScaleX(width / height);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Correct input that was entered by the user.
        Paint enteredPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        enteredPaint.setColor(getResources().getColor(R.color.RadiantGreen));
        enteredPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        enteredPaint.setTextSize(height * 0.75f);
        enteredPaint.setStrokeWidth(3);
        enteredPaint.setTextScaleX(width / height);
        enteredPaint.setTextAlign(Paint.Align.CENTER);

        // Numbers that make the input impossible to solve.
        Paint errorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        errorPaint.setColor(getResources().getColor(R.color.DireOrange));
        errorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        errorPaint.setTextSize(height * 0.75f);
        errorPaint.setStrokeWidth(3);
        errorPaint.setTextScaleX(width / height);
        errorPaint.setTextAlign(Paint.Align.CENTER);

        // Draw the number in the center of the tile
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        // Centering in X: use alignment (and X at midpoint)
        float x = width / 2;
        // Centering in Y: measure ascent/descent first
        float y = height / 2 - (fm.ascent + fm.descent) / 2;

        if (values != null) {

            Paint paintForSquare = null;
            String textForSquare;

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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }
        select((int) (event.getX() / width), (int) (event.getY() / height));

        return true;
    }

    private void select(int x, int y) {

        int newX = Math.min(Math.max(x, 0), 8);
        int newY = Math.min(Math.max(y, 0), 8);

        if (newX == selectionX && newY == selectionY) {
            selectionX = -1;
            selectionY = -1;
        } else {
            selectionX = newX;
            selectionY = newY;
        }

        invalidate();
    }


    public void setValues(int[][] values, int[][] errors, int[][] enteredValues) {
        this.values = values;
        this.errors = errors;
        this.enteredValues = enteredValues;
        invalidate();
    }

    public int getSelectedX() {
        return selectionX;
    }

    public int getSelectedY() {
        return selectionY;
    }
}

