package be.simonraes.sudokusolver.model;

import be.simonraes.sudokusolver.exception.NoSolutionException;
import be.simonraes.sudokusolver.exception.SolutionFoundException;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Simon Raes on 25/07/2014.
 */
public class Solver {

    private int[][] model;
    private int[][] hintModel;
    private int[][] errors = new int[9][9];

    /**
     * Returns the values of the fully solved Sudoku.
     */
    public int[][] solveSudoku(int[][] model) throws NoSolutionException {
        this.model = model;

        System.out.println("starting solve, value at 0,1 = "+model[0][1]);
        if (isErrorFree(model)) {
            try {
                solve(0, 0);
            } catch (SolutionFoundException e) {

            }
        } else {
            throw new NoSolutionException();
        }

        return model;
    }

    /**
     * Returns the values with 1 extra field revealed.
     */
    public int[][] hintSudoku(int[][] model, int selectedX, int selectedY) throws NoSolutionException {
        this.model = model;

        ArrayList<GridLocation> openSpaces = new ArrayList<GridLocation>();

        if (isErrorFree(model)) {
            try {
                hintModel = new int[model.length][model.length];

                // Copy all model values to the new hintarray
                for (int i = 0; i < hintModel.length; i++) {
                    for (int j = 0; j < hintModel.length; j++) {
                        hintModel[i][j] = model[i][j];
                        if (model[i][j] == 0) {
                            // Add the found open cell to the list.
                            openSpaces.add(new GridLocation(i, j));
                        }
                    }
                }

                // Only search for hint if there is an empty cell available.
                if (openSpaces.size() > 0) {
                    // Put the full solution in the values array.
                    solve(0, 0);
                }


            } catch (SolutionFoundException e) {

                if (selectedX >= 0 && selectedY >= 0 && hintModel[selectedX][selectedY] <=0) {
                    // Put the hint at the selected location.
                    hintModel[selectedX][selectedY] = model[selectedX][selectedY];
                } else {
                    // No location selected, reveal a random cell.
                    Random random = new Random();
                    GridLocation hintLocation = openSpaces.get(random.nextInt(openSpaces.size()));
                    hintModel[hintLocation.getX()][hintLocation.getY()] = model[hintLocation.getX()][hintLocation.getY()];
                }

            }
        } else {
            throw new NoSolutionException();
        }

        return hintModel;
    }

    public boolean isErrorFree(int[][] model) {
//        if (this.model == null) {
            this.model = model;
//        }

        errors = new int[9][9];

        boolean errorFree = true;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model.length; j++) {
                if (model[i][j] != 0) {
//                    System.out.println("check for number " + model[i][j]);
                    // Make sure all 3 checks are executed so all errors are found.
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

        if (row > 8) {
            // Exception to break out of code if a solution is found.
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
        int counter = 0;
        for (int col = 0; col < model.length; col++) {
            if (col != column) {
//                System.out.println("comparing " + model[row][col] + " to " + num);
                if (model[row][col] == num) {
                    errors[row][col] = num;
                    counter++;
                }
            }
        }
        return counter > 0;
    }

    private boolean colContainsDuplicates(int roww, int col, int num) {
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

    public void clearData() {
        model = null;
        errors = new int[9][9];
        hintModel = new int[9][9];
    }
}

