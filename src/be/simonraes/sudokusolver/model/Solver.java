package be.simonraes.sudokusolver.model;

import be.simonraes.sudokusolver.exception.NoSolutionException;
import be.simonraes.sudokusolver.exception.SolutionFoundException;

/**
 * Created by Simon Raes on 25/07/2014.
 */
public class Solver {

    private int[][] model;
    private int[][] errors = new int[9][9];

    public int[][] solveSudoku(int[][] model) throws NoSolutionException {
        this.model = model;

        if (isErrorFree()) {
            try {
                solve(0, 0);
            } catch (SolutionFoundException e) {

            }
        } else {
            throw new NoSolutionException();
        }

        return model;
    }

    private boolean isErrorFree() {
        boolean errorFree = true;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model.length; j++) {
                if (model[i][j] != 0) {
                    //make sure all 3 checks are executed so all errors are found
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

        // Throw an exception to stop the process if the puzzle is solved
        if (row > 8) {
            // Exception to break out of code
            throw new SolutionFoundException();
        }

        // If the cell is not empty, continue with the next cell
        if (model[row][col] != 0)
            next(row, col);
        else {
            // Find a valid number for the empty cell
            for (int num = 1; num < 10; num++) {
                if (checkRow(row, num) && checkCol(col, num) && checkBox(row, col, num)) {
                    model[row][col] = num;
                    // Delegate work on the next cell to a recursive call
                    next(row, col);
                }
            }

            // No valid number was found, clean up and return to caller
            model[row][col] = 0;
        }
    }

    /**
     * Checks if num is an acceptable value for the given row
     */
    protected boolean checkRow(int row, int num) {
        for (int col = 0; col < 9; col++) {
            if (model[row][col] == num) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if num is an acceptable value for the given column
     */
    protected boolean checkCol(int col, int num) {
        for (int row = 0; row < 9; row++)
            if (model[row][col] == num)
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
                if (model[row + r][col + c] == num)
                    return false;

        return true;
    }

    private boolean rowContainsDuplicates(int row, int column, int num) {
//        System.out.println("checking for row dupes");
        int counter = 0;
        for (int col = 0; col < model.length; col++) {
            if (col != column) {
                if (model[row][col] == num) {
                    errors[row][col] = num;
                    counter++;
                }
            }
        }
        return counter > 0;
    }

    private boolean colContainsDuplicates(int roww, int col, int num) {
//        System.out.println("checking for COL dupes");
        int counter = 0;
        for (int row = 0; row < model.length; row++) {
            if (row != roww) {

                if (model[row][col] == num) {
                    errors[row][col] = num;
                    counter++;
                }
            }
        }
        return counter > 0;
    }

    private boolean boxContainsDuplicates(int row, int col, int num) {
//        System.out.println("checking for BOX dupes");
        int counter = 0;

        int ogRow = row;
        int ogCol = col;

        row = (row / 3) * 3;
        col = (col / 3) * 3;

        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (r + row != ogRow && c + col != ogCol) {
                    if (model[row + r][col + c] == num) {
                        errors[row + r][col + c] = num;
                        counter++;
                    }

                }
        return counter > 0;
    }

    /**
     * Calls solve for the next cell
     */
    public void next(int row, int col) throws SolutionFoundException {
        if (col < 8)
            solve(row, col + 1);
        else
            solve(row + 1, 0);
    }

    public int[][] getErrors() {
        return errors;
    }
}
