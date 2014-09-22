package be.simonraes.sudokusolver.model;

import android.content.Context;
import android.os.AsyncTask;
import be.simonraes.sudokusolver.exception.SolutionFoundException;
import be.simonraes.sudokusolver.util.AppPreferences;

/**
 * Solver using the backtracking algorithm.
 * Created by Simon Raes on 28/07/2014.
 */
public class ASyncBackTrackSolver extends AsyncTask<GridValue[][], GridValue[][], GridValue[][]> {

    private GridValue[][] values;

    private Context context;
    private solverListener delegate;
    private boolean animateSolution;

    public interface solverListener {
        public void valueAdded();
    }

    public ASyncBackTrackSolver(Context context, solverListener delegate, boolean animateSolution) {
        this.context = context;
        this.delegate = delegate;
        this.animateSolution = animateSolution;
    }

    @Override
    protected GridValue[][] doInBackground(GridValue[][]... ints) {

        this.values = ints[0];

        try {
            solve(0, 0);
        } catch (SolutionFoundException e) {
            // No longer used. BackTrack solver is only used for animation.
        }

        return values;
    }

    @Override
    protected void onProgressUpdate(GridValue[][]... values) {
        super.onProgressUpdate(values);
        delegate.valueAdded();
    }

    @Override
    protected void onCancelled() {
        clearData();
    }

    public boolean isErrorFree(GridValue[][] model) {
        this.values = model;

        clearErrors();
        boolean errorFree = true;

        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model.length; j++) {
                if (model[i][j] != null && model[i][j].getValue() != 0) {
                    // Make sure all 3 checks are executed so all errors are found.
                    boolean rowHasErrors = rowContainsDuplicates(i, j, model[i][j].getValue());
                    boolean colHasErrors = colContainsDuplicates(i, j, model[i][j].getValue());
                    boolean boxHasErrors = boxContainsDuplicates(i, j, model[i][j].getValue());

                    if (rowHasErrors || colHasErrors || boxHasErrors) {
                        model[i][j].setError(true);
                        errorFree = false;
                    }
                }
            }
        }
        return errorFree;
    }

    /**
     * Marks every cell as error free.
     */
    private void clearErrors() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (values[i][j] != null) {
                    values[i][j].setError(false);
                }
            }
        }
    }

    private void solve(int row, int col) throws SolutionFoundException {

        if (!isCancelled()) {

            if (row > values.length - 1) {

                // Exception to break out of code if a solution is found.
                throw new SolutionFoundException();
            }

            // If the cell is not empty, continue with the next cell
            if (values[row][col] != null && values[row][col].getValue() != 0) {
                next(row, col);
            } else {
                // Find a valid number for the empty cell
                for (int num = 1; num < values.length + 1; num++) {
                    if (checkRow(row, num) && checkCol(col, num) && checkBox(row, col, num)) {
                        GridValue newValue = new GridValue(num);
                        newValue.setSolution(true);
                        values[row][col] = newValue;

                        if (animateSolution) {
                            //  alert the listener a new value has been added
                            publishProgress(values);

                            // Wait x ms before searching for the next value.
                            try {
                                Thread.sleep(AppPreferences.getStepTime(context));
                            } catch (InterruptedException e) {

                            }
                        }

                        // Delegate work on the next cell to a recursive call
                        if (!isCancelled()) {
                            next(row, col);
                        }
                    }
                }

                // No valid number was found, clean up and return to caller
                values[row][col] = null;
            }
        }
    }


    /**
     * Checks if num is an acceptable value for the given row
     */
    protected boolean checkRow(int row, int num) {
        for (int col = 0; col < values.length; col++) {
            if (values[row][col] != null && values[row][col].getValue() == num) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if num is an acceptable value for the given column
     */
    protected boolean checkCol(int col, int num) {
        for (GridValue[] value : values) {
            if (value[col] != null && value[col].getValue() == num) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if num is an acceptable value for the box around row and col
     */
    protected boolean checkBox(int row, int col, int num) {
        row = (row / 3) * 3;
        col = (col / 3) * 3;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (values[row + r][col + c] != null && values[row + r][col + c].getValue() == num)
                    return false;
            }
        }
        return true;
    }

    private boolean rowContainsDuplicates(int row, int column, int num) {
        int errorCounter = 0;
        for (int col = 0; col < values.length; col++) {
            if (col != column) {
                if (values[row][col] != null) {
                    if (values[row][col].getValue() == num) {
                        values[row][col].setError(true);
                        errorCounter++;
                    }
                }
            }
        }
        return errorCounter > 0;
    }

    private boolean colContainsDuplicates(int roww, int col, int num) {
        int errorCounter = 0;
        for (int row = 0; row < values.length; row++) {
            if (row != roww) {
                if (values[row][col] != null) {
                    if (values[row][col].getValue() == num) {
                        values[row][col].setError(true);
                        errorCounter++;
                    }
                }
            }
        }
        return errorCounter > 0;
    }

    private boolean boxContainsDuplicates(int row, int col, int num) {
        int errorCounter = 0;

        int ogRow = row;
        int ogCol = col;

        row = (row / 3) * 3;
        col = (col / 3) * 3;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (r + row != ogRow && c + col != ogCol) {
                    if (values[row + r][col + c] != null) {
                        if (values[row + r][col + c].getValue() == num) {
                            values[row + r][col + c].setError(true);
                            errorCounter++;
                        }
                    }
                }
            }
        }
        return errorCounter > 0;
    }

    /**
     * Calls solve for the next cell.
     */
    public void next(int row, int col) throws SolutionFoundException {
        if (col < values.length - 1)
            solve(row, col + 1);
        else
            solve(row + 1, 0);
    }

    public void clearData() {
        values = null;
    }
}
