package be.simonraes.sudokusolver.model;

import android.os.AsyncTask;
import be.simonraes.sudokusolver.util.Printer;

import java.util.Arrays;

/**
 * Much faster Sudoku solving algorithm by Kevin Coulombe. (http://byteauthor.com/2010/08/sudoku-solver/)
 * Created by Simon Raes on 20/09/2014.
 */
public class ASyncFasterSolver extends AsyncTask<GridValue[][], int[][], int[][]> {

    private final int[] allowedBitFields = new int[]{
            0,
            1,
            1 << 1,
            1 << 2,
            1 << 3,
            1 << 4,
            1 << 5,
            1 << 6,
            1 << 7,
            1 << 8,
    };
    private final int allAllowed = arraySum(allowedBitFields);

    private FasterSolverListener delegate;

    public interface FasterSolverListener {
        //public void valueAdded(); // Todo: find a way to make an animation based on this faster solver algorithm.

        public void fastSudokuHasNoSolution();

        public void fastSudokuSolved(GridValue[][] gridValues);
    }

    private GridValue[][] gridValues;

    private final long MAX_DURATION = 1000;
    private boolean delegateAlerted = false;
    private boolean sudokuHasSolution = false;


    public ASyncFasterSolver(FasterSolverListener delegate) {
        this.delegate = delegate;
    }

    @Override
    protected int[][] doInBackground(GridValue[][]... ints) {

        gridValues = ints[0];
        int[][] intInputValues = gridValueArrayToIntArray(gridValues);

        final long startTime = System.currentTimeMillis();

        Thread t = new Thread() {
            public void run() {
                while (!delegateAlerted) {
                    if (!isCancelled() && startTime + MAX_DURATION < System.currentTimeMillis() && !delegateAlerted && !sudokuHasSolution) {
                        delegate.fastSudokuHasNoSolution();
                        delegateAlerted = true;
                    }
                }
            }
        };
        t.start();

        final int placedNumbers = solveBoard(intInputValues);

        // Check if a solution was found.
        sudokuHasSolution = (placedNumbers == 81);

        return null;
    }

    @Override
    protected void onPostExecute(int[][] ints) {
        super.onPostExecute(ints);
        delegateAlerted = true;
        if (sudokuHasSolution) {
            delegate.fastSudokuSolved(gridValues);
        } else {
            delegate.fastSudokuHasNoSolution();
        }
    }

    @Override
    protected void onCancelled() {
        clearData();
    }

    public void clearData() {
        gridValues = null;
    }



    private int solveBoard(final int[][] board) {
        final int[][] allowedValues = new int[9][9];
        int placedNumberCount = 0;

        for (int[] allowedValuesRow : allowedValues) {
            Arrays.fill(allowedValuesRow, allAllowed);
        }

        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                if (board[x][y] > 0 && !isCancelled()) {
                    allowedValues[x][y] = 0;
                    applyAllowedValuesMask(board, allowedValues, x, y);
                    placedNumberCount++;
                }
            }
        }
        return solveBoard(board, allowedValues, placedNumberCount);
    }

    private int solveBoard(final int[][] board, final int[][] allowedValues, int placedNumberCount) {

        int lastPlacedNumbersCount = 0;

        while (placedNumberCount - lastPlacedNumbersCount > 3 && placedNumberCount < 68 && placedNumberCount > 10) {
            lastPlacedNumbersCount = placedNumberCount;
            placedNumberCount += moveNothingElseAllowed(board, allowedValues);
            placedNumberCount += moveNoOtherRowOrColumnAllowed(board, allowedValues);
            placedNumberCount += moveNothingElseAllowed(board, allowedValues);

            if (placedNumberCount < 35) {
                applyNakedPairs(allowedValues);
                applyLineCandidateConstraints(allowedValues);
            }
        }

        if (placedNumberCount < 81) {
            final int[][] bruteForcedBoard = attemptBruteForce(board, allowedValues, placedNumberCount);

            if (bruteForcedBoard != null) {
                placedNumberCount = 0;

                for (int x = 0; x < 9; x++) {
                    for (int y = 0; y < 9; y++) {
                        board[x][y] = bruteForcedBoard[x][y];

                        if (bruteForcedBoard[x][y] > 0) {
                            placedNumberCount++;
                        }
                    }
                }
            }
        }

        return placedNumberCount;
    }

    private int[][] attemptBruteForce(final int[][] board, final int[][] allowedValues, final int placedNumberCount) {

        if (!isCancelled()) {


            for (int x = 0; x < 9; x++) {
                final int[] allowedValuesRow = allowedValues[x];
                final int[] boardRow = board[x];

                for (int y = 0; y < 9; y++) {
                    if (boardRow[y] == 0) {
                        for (int value = 1; value <= 9; value++) {
                            if ((allowedValuesRow[y] & allowedBitFields[value]) > 0) {
                                final int[][] testBoard = copyGameMatrix(board);
                                final int[][] testAllowedValues = copyGameMatrix(allowedValues);
                                setValue(testBoard, testAllowedValues, value, x, y);

                                final int placedNumbers = solveBoard(testBoard, testAllowedValues, placedNumberCount + 1);

                                if (placedNumbers == 81) {
                                    return testBoard;
                                }
                            }
                        }

                        return null;
                    }
                }
            }
        }

        return null;
    }

    private int moveNoOtherRowOrColumnAllowed(final int[][] board, final int[][] allowedValues) {

        int moveCount = 0;

        for (int value = 1; value <= 9; value++) {
            final int allowedBitField = allowedBitFields[value];

            for (int x = 0; x < 9; x++) {
                int allowedY = -1;
                final int[] allowedValuesRow = allowedValues[x];

                for (int y = 0; y < 9; y++) {
                    if ((allowedValuesRow[y] & allowedBitField) > 0) {
                        if (allowedY < 0) {
                            allowedY = y;
                        } else {
                            allowedY = -1;
                            break;
                        }
                    }
                }

                if (allowedY >= 0) {
                    setValue(board, allowedValues, value, x, allowedY);
                    moveCount++;
                }
            }

            for (int y = 0; y < 9; y++) {
                int allowedX = -1;

                for (int x = 0; x < 9; x++) {
                    if ((allowedValues[x][y] & allowedBitField) > 0) {
                        if (allowedX < 0) {
                            allowedX = x;
                        } else {
                            allowedX = -1;
                            break;
                        }
                    }
                }

                if (allowedX >= 0) {
                    setValue(board, allowedValues, value, allowedX, y);
                    moveCount++;
                }
            }
        }

        return moveCount;
    }

    private int moveNothingElseAllowed(final int[][] board,
                                       final int[][] allowedValues) {

        int moveCount = 0;

        for (int x = 0; x < 9; x++) {
            final int[] allowedValuesRow = allowedValues[x];

            for (int y = 0; y < 9; y++) {
                final int currentAllowedValues = allowedValuesRow[y];
                if (countSetBits(currentAllowedValues) == 1) {
                    setValue(board, allowedValues, getLastSetBitIndex(currentAllowedValues), x, y);
                    moveCount++;
                }
            }
        }

        return moveCount;
    }

    private void applyNakedPairs(final int[][] allowedValues) {
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                final int value = allowedValues[x][y];

                if (countSetBits(value) == 2) {
                    for (int scanningY = y + 1; scanningY < 9; scanningY++) {
                        if (allowedValues[x][scanningY] == value) {
                            final int removeMask = ~value;

                            for (int applyY = 0; applyY < 9; applyY++) {
                                if (applyY != y && applyY != scanningY) {
                                    allowedValues[x][applyY] &= removeMask;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                final int value = allowedValues[x][y];

                if (value != 0 && countSetBits(value) == 2) {
                    for (int scanningX = x + 1; scanningX < 9; scanningX++) {
                        if (allowedValues[scanningX][y] == value) {
                            final int removeMask = ~value;

                            for (int applyX = 0; applyX < 9; applyX++) {
                                if (applyX != x && applyX != scanningX) {
                                    allowedValues[applyX][y] &= removeMask;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void applyLineCandidateConstraints(final int[][] allowedValues) {
        for (int value = 1; value <= 9; value++) {
            final int valueMask = allowedBitFields[value];
            final int valueRemoveMask = ~valueMask;
            final int[] sectionAvailabilityColumn = new int[9];

            for (int x = 0; x < 9; x++) {

                for (int y = 0; y < 9; y++) {
                    if ((allowedValues[x][y] & valueMask) != 0) {
                        sectionAvailabilityColumn[x] |= (1 << (y / 3));
                    }
                }

                if (x == 2 || x == 5 || x == 8) {
                    for (int scanningX = x - 2; scanningX <= x; scanningX++) {
                        final int bitCount = countSetBits(sectionAvailabilityColumn[scanningX]);

                        if (bitCount == 1) {
                            for (int applyX = x - 2; applyX <= x; applyX++) {
                                if (scanningX != applyX) {
                                    for (int applySectionY = 0; applySectionY < 3; applySectionY++) {
                                        if ((sectionAvailabilityColumn[scanningX] & (1 << applySectionY)) != 0) {
                                            for (int applyY = applySectionY * 3; applyY < (applySectionY + 1) * 3; applyY++) {
                                                allowedValues[applyX][applyY] &= valueRemoveMask;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (bitCount == 2 && scanningX < x) {
                            for (int scanningSecondPairX = scanningX + 1; scanningSecondPairX <= x; scanningSecondPairX++) {
                                if (sectionAvailabilityColumn[scanningX] == sectionAvailabilityColumn[scanningSecondPairX]) {
                                    final int applyX;

                                    if (scanningSecondPairX != x) {
                                        applyX = x;
                                    } else if (scanningSecondPairX - scanningX > 1) {
                                        applyX = scanningSecondPairX - 1;
                                    } else {
                                        applyX = scanningX - 1;
                                    }

                                    for (int applySectionY = 0; applySectionY < 3; applySectionY++) {
                                        if ((sectionAvailabilityColumn[scanningX] & (1 << applySectionY)) != 0) {
                                            for (int applyY = applySectionY * 3; applyY < (applySectionY + 1) * 3; applyY++) {
                                                allowedValues[applyX][applyY] &= valueRemoveMask;
                                            }
                                        }
                                    }

                                    break;
                                }
                            }
                        }
                    }
                }
            }

            final int[] sectionAvailabilityRow = new int[9];

            for (int y = 0; y < 9; y++) {

                for (int x = 0; x < 9; x++) {
                    if ((allowedValues[x][y] & valueMask) != 0) {
                        sectionAvailabilityRow[y] |= (1 << (x / 3));
                    }
                }

                if (y == 2 || y == 5 || y == 8) {
                    for (int scanningY = y - 2; scanningY <= y; scanningY++) {
                        final int bitCount = countSetBits(sectionAvailabilityRow[scanningY]);

                        if (bitCount == 1) {
                            for (int applyY = y - 2; applyY <= y; applyY++) {
                                if (scanningY != applyY) {
                                    for (int applySectionX = 0; applySectionX < 3; applySectionX++) {
                                        if ((sectionAvailabilityRow[scanningY] & (1 << applySectionX)) != 0) {
                                            for (int applyX = applySectionX * 3; applyX < (applySectionX + 1) * 3; applyX++) {
                                                allowedValues[applyX][applyY] &= valueRemoveMask;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (bitCount == 2 && scanningY < y) {
                            for (int scanningSecondPairY = scanningY + 1; scanningSecondPairY <= y; scanningSecondPairY++) {
                                if (sectionAvailabilityRow[scanningY] == sectionAvailabilityRow[scanningSecondPairY]) {
                                    final int applyY;

                                    if (scanningSecondPairY != y) {
                                        applyY = y;
                                    } else if (scanningSecondPairY - scanningY > 1) {
                                        applyY = scanningSecondPairY - 1;
                                    } else {
                                        applyY = scanningY - 1;
                                    }

                                    for (int applySectionX = 0; applySectionX < 3; applySectionX++) {
                                        if ((sectionAvailabilityRow[scanningY] & (1 << applySectionX)) != 0) {
                                            for (int applyX = applySectionX * 3; applyX < (applySectionX + 1) * 3; applyX++) {
                                                allowedValues[applyX][applyY] &= valueRemoveMask;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void setValue(final int[][] board, final int[][] allowedValues, final int value, final int x, final int y) {

        if (gridValues != null) {
            GridValue newValue = new GridValue(value);
            newValue.setSolution(true);
            gridValues[x][y] = newValue;
        }

        board[x][y] = value;
        allowedValues[x][y] = 0;
        applyAllowedValuesMask(board, allowedValues, x, y);
    }

    private int getLastSetBitIndex(int value) {
        int bitIndex = 0;

        while (value > 0) {
            bitIndex++;
            value >>= 1;
        }

        return bitIndex;
    }

    private void applyAllowedValuesMask(final int[][] board, final int[][] allowedValues, final int x, final int y) {

        final int mask = ~allowedBitFields[board[x][y]];

        for (int maskApplyX = 0; maskApplyX < 9; maskApplyX++) {
            allowedValues[maskApplyX][y] &= mask;
        }

        final int[] allowedValuesRow = allowedValues[x];

        for (int maskApplyY = 0; maskApplyY < 9; maskApplyY++) {
            allowedValuesRow[maskApplyY] &= mask;
        }

        int sectionX1 = 0;
        int sectionX2 = 0;

        switch (x) {
            case 0:
                sectionX1 = x + 1;
                sectionX2 = x + 2;
                break;
            case 1:
                sectionX1 = x - 1;
                sectionX2 = x + 1;
                break;
            case 2:
                sectionX1 = x - 2;
                sectionX2 = x - 1;
                break;
            case 3:
                sectionX1 = x + 1;
                sectionX2 = x + 2;
                break;
            case 4:
                sectionX1 = x - 1;
                sectionX2 = x + 1;
                break;
            case 5:
                sectionX1 = x - 2;
                sectionX2 = x - 1;
                break;
            case 6:
                sectionX1 = x + 1;
                sectionX2 = x + 2;
                break;
            case 7:
                sectionX1 = x - 1;
                sectionX2 = x + 1;
                break;
            case 8:
                sectionX1 = x - 2;
                sectionX2 = x - 1;
                break;

        }

        int sectionY1 = 0;
        int sectionY2 = 0;

        switch (y) {
            case 0:
                sectionY1 = y + 1;
                sectionY2 = y + 2;
                break;
            case 1:
                sectionY1 = y - 1;
                sectionY2 = y + 1;
                break;
            case 2:
                sectionY1 = y - 2;
                sectionY2 = y - 1;
                break;
            case 3:
                sectionY1 = y + 1;
                sectionY2 = y + 2;
                break;
            case 4:
                sectionY1 = y - 1;
                sectionY2 = y + 1;
                break;
            case 5:
                sectionY1 = y - 2;
                sectionY2 = y - 1;
                break;
            case 6:
                sectionY1 = y + 1;
                sectionY2 = y + 2;
                break;
            case 7:
                sectionY1 = y - 1;
                sectionY2 = y + 1;
                break;
            case 8:
                sectionY1 = y - 2;
                sectionY2 = y - 1;
                break;
        }

        final int[] allowedValuesRow1 = allowedValues[sectionX1];
        final int[] allowedValuesRow2 = allowedValues[sectionX2];

        allowedValuesRow1[sectionY1] &= mask;
        allowedValuesRow1[sectionY2] &= mask;
        allowedValuesRow2[sectionY1] &= mask;
        allowedValuesRow2[sectionY2] &= mask;
    }

    private int countSetBits(int value) {
        int count = 0;

        while (value > 0) {
            value = value & (value - 1);
            count++;
        }

        return count;
    }

    private int arraySum(final int[] array) {
        int sum = 0;

        for (int value : array) {
            sum += value;
        }

        return sum;
    }

    private int[][] copyGameMatrix(final int[][] matrix) {
        return new int[][]{
                Arrays.copyOf(matrix[0], 9),
                Arrays.copyOf(matrix[1], 9),
                Arrays.copyOf(matrix[2], 9),
                Arrays.copyOf(matrix[3], 9),
                Arrays.copyOf(matrix[4], 9),
                Arrays.copyOf(matrix[5], 9),
                Arrays.copyOf(matrix[6], 9),
                Arrays.copyOf(matrix[7], 9),
                Arrays.copyOf(matrix[8], 9),
        };
    }

    /**
     * Creates a new int[][] with the supplied values. Also stores
     */
    private int[][] gridValueArrayToIntArray(GridValue[][] gridArray) {

        int[][] newArray = new int[gridArray.length][gridArray.length];

        for (int i = 0; i < gridArray.length; i++) {
            for (int j = 0; j < gridArray.length; j++) {
                if (gridArray[i][j] != null) {
                    newArray[i][j] = gridArray[i][j].getValue();
                } else {
                    newArray[i][j] = 0;
                }
            }
        }

        System.out.println("Gridvalues pre-solver");
        Printer.printArray(gridValues);

        return newArray;
    }
}
