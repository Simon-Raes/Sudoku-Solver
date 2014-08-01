package be.simonraes.sudokusolver.model;

import android.content.Context;
import android.os.AsyncTask;
import be.simonraes.sudokusolver.exception.SolutionFoundException;
import be.simonraes.sudokusolver.util.AppPreferences;

/**
 * Created by Simon Raes on 28/07/2014.
 */
public class ASyncSolver extends AsyncTask<int[][], int[][], int[][]> {

    private final int MAX_DURATION = 250;

    private int[][] values;
    private int[][] errorValues = new int[9][9];

    private Context context;
    private solverListener delegate;
    private boolean animateSolution, solutionFound, delegateAlerted;

    private long startTime;


    public interface solverListener {
        public void valueAdded(int[][] values);

        public void sudokuHasNoSolution();

        public void sudokuSolved(int[][] values);
    }


    public ASyncSolver(Context context, solverListener delegate, boolean animateSolution) {
        this.context = context;
        this.delegate = delegate;
        this.animateSolution = animateSolution;
        solutionFound = false;
        delegateAlerted = false;
    }


    @Override
    protected int[][] doInBackground(int[][]... ints) {

        this.values = ints[0];

        startTime = System.currentTimeMillis();
        solutionFound = false;

        if (isErrorFree(values)) {
            try {
                solve(0, 0);
            } catch (SolutionFoundException e) {

            }
        }

        return values;
    }

    @Override
    protected void onProgressUpdate(int[][]... values) {
        super.onProgressUpdate(values);
        if (delegate != null) {
            delegate.valueAdded(values[0]);
        }
    }

    @Override
    protected void onPostExecute(int[][] ints) {
        super.onPostExecute(ints);
        delegate.sudokuSolved(ints);
    }

    @Override
    protected void onCancelled() {
        clearData();
    }


    public boolean isErrorFree(int[][] model) {
        this.values = model;

        errorValues = new int[9][9];

        boolean errorFree = true;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model.length; j++) {
                if (model[i][j] != 0) {
                    // Make sure all 3 checks are executed so all errorValues are found.
                    boolean rowHasErrors = rowContainsDuplicates(i, j, model[i][j]);
                    boolean colHasErrors = colContainsDuplicates(i, j, model[i][j]);
                    boolean boxHasErrors = boxContainsDuplicates(i, j, model[i][j]);

                    if (rowHasErrors || colHasErrors || boxHasErrors) {
                        errorFree = false;
                    }
                }
            }
        }
        return errorFree;
    }


    private void solve(int row, int col) throws SolutionFoundException {

        if (!animateSolution && startTime + MAX_DURATION < System.currentTimeMillis() && !solutionFound && !delegateAlerted) {
            delegate.sudokuHasNoSolution();
            delegateAlerted = true;
        }

        if (!isCancelled()) {

            if (row > values.length - 1) {

                // Exception to break out of code if a solution is found.
                solutionFound = true;
                throw new SolutionFoundException();
            }

            // If the cell is not empty, continue with the next cell
            if (values[row][col] != 0) {
                next(row, col);
            } else {
                // Find a valid number for the empty cell
                for (int num = 1; num < values.length + 1; num++) {
                    if (checkRow(row, num) && checkCol(col, num) && checkBox(row, col, num)) {


                        values[row][col] = num;

                        if (animateSolution) {
                            //  alert the listener a new value has been added
//                            delegate.valueAdded(this, values);
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
                values[row][col] = 0;
            }
        }
    }


    /**
     * Checks if num is an acceptable value for the given row
     */
    protected boolean checkRow(int row, int num) {
        for (int col = 0; col < values.length; col++) {
            if (values[row][col] == num) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if num is an acceptable value for the given column
     */
    protected boolean checkCol(int col, int num) {
        for (int row = 0; row < values.length; row++)
            if (values[row][col] == num)
                return false;

        return true;
    }

    /**
     * Checks if num is an acceptable value for the box around row and col
     */
    protected boolean checkBox(int row, int col, int num) {
        row = (row / 3) * 3;
        col = (col / 3) * 3;

        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (values[row + r][col + c] == num)
                    return false;

        return true;
    }

    private boolean rowContainsDuplicates(int row, int column, int num) {
        int counter = 0;
        for (int col = 0; col < values.length; col++) {
            if (col != column) {
                if (values[row][col] == num) {
                    errorValues[row][col] = num;
                    counter++;
                }
            }
        }
        return counter > 0;
    }

    private boolean colContainsDuplicates(int roww, int col, int num) {
        int counter = 0;
        for (int row = 0; row < values.length; row++) {
            if (row != roww) {
                if (values[row][col] == num) {
                    errorValues[row][col] = num;
                    counter++;
                }
            }
        }
        return counter > 0;
    }

    private boolean boxContainsDuplicates(int row, int col, int num) {
        int counter = 0;

        int ogRow = row;
        int ogCol = col;

        row = (row / 3) * 3;
        col = (col / 3) * 3;

        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (r + row != ogRow && c + col != ogCol) {
                    if (values[row + r][col + c] == num) {
                        errorValues[row + r][col + c] = num;
                        counter++;
                    }

                }
        return counter > 0;
    }

    /**
     * Calls solve for the next cell
     */
    public void next(int row, int col) throws SolutionFoundException {
        if (col < values.length - 1)
            solve(row, col + 1);
        else
            solve(row + 1, 0);
    }


    public int[][] getErrorValues() {
        return errorValues;
    }

    public void clearData() {
        values = null;
        errorValues = new int[9][9];
    }

}
