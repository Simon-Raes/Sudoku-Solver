package be.simonraes.sudokusolver.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import be.simonraes.sudokusolver.activity.MainActivity;
import be.simonraes.sudokusolver.model.ASyncSolver;
import be.simonraes.sudokusolver.model.GridLocation;
import be.simonraes.sudokusolver.util.AppPreferences;

import java.util.ArrayList;
import java.util.Random;

/**
 * Fragment without view that holds the logic and a reference to the ASyncTask.
 * This fragment retains instance state on orientation to keep the reference to the ASyncTask.
 * Created by Simon Raes on 28/07/2014.
 */
public class DataFragment extends Fragment implements ASyncSolver.solverListener {

    int[][] originalInputValues = new int[9][9];  // Input the user entered.
    int[][] values = new int[9][9];        // The values to be sent to the UI, can contain less than 81 digits in the case of a hint.
    int[][] errors = new int[9][9];        // All input errors on their location.
    int[][] enteredValues = new int[9][9]; // All values entered by the user on their location.

    private boolean playAnimation = false;

    private MainActivity activity;

    private SolveMode solveMode;
    private enum SolveMode {
        NORMAL, HINT, ANIMATION
    }

    private ASyncSolver solver;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRetainInstance(true);

//        if (solver != null) {
//            solver.attach(this);
//        }
        activity.refreshSudokuView(values, errors, enteredValues);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity.refreshSudokuView(values, errors, enteredValues);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void solve() {

        clearErrors();
        toggleSolveMode(true);

        if (AppPreferences.solutionShouldAnimate(activity)) {
            solveSudokuAnimated();
        } else {
            solveSudokuNormal();
        }
    }

    private void solveSudokuNormal() {
        solveMode = SolveMode.NORMAL;
        startSolver();
    }

    private void solveSudokuAnimated() {
        playAnimation = true;
        solveMode = SolveMode.ANIMATION;
        startSolver();
    }

    public void solveForHint() {
        solveMode = SolveMode.HINT;
        startSolver();
    }

    private void startSolver() {
        solver = new ASyncSolver(getActivity(), this, false);
        originalInputValues = copyArray(values);
        solver.execute(values);
    }

    private void clearErrors() {
        errors = null;
    }

    public void setNumberForSelectedField(int number, int selectedX, int selectedY) {

        if (selectedX >= 0 && selectedY >= 0) {
            values[selectedX][selectedY] = number;
            enteredValues[selectedX][selectedY] = number;
            if (solver == null) {
                solver = new ASyncSolver(getActivity(), this, false);
            }

            // Check if this new input causes any errors.
            if (solver.isErrorFree(values)) {
                errors = new int[9][9];
                enableSolveButtons();
            } else {
                errors = solver.getErrorValues();
                disableSolveButtons();
            }

            refreshSudokuView();
        } else {
            Toast.makeText(getActivity(), "Please select a cell.", Toast.LENGTH_SHORT).show();
        }
    }


    public void clearBoard() {
        cancelASyncTask();

        values = new int[9][9];
        errors = new int[9][9];
        enteredValues = new int[9][9];

        toggleSolveMode(false);
        refreshSudokuView();
    }

    private boolean cancelASyncTask() {
        boolean cancelledTask = false;
        if (solver != null) {
            if (solver.getStatus() == AsyncTask.Status.RUNNING) {
                solver.cancel(true);
                cancelledTask = true;
                solver.clearData();
            }
        }
        return cancelledTask;
    }


    private void refreshSudokuView() {
        activity.refreshSudokuView(values, errors, enteredValues);
    }

    private void enableSolveButtons() {
        activity.toggleSolveButtons(true);
    }

    private void disableSolveButtons() {
        activity.toggleSolveButtons(false);
    }

    private void toggleSolveMode(boolean solveMode) {
        activity.toggleSolveMode(solveMode);
    }


    @Override
    public void valueAdded(int[][] values) {
//        this.solver = solver;
        this.values = values;
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                refreshSudokuView();
//            }
//        });
        activity.refreshSudokuView(values, errors, enteredValues);
    }

    @Override
    public void sudokuHasNoSolution() {
//        this.solver = solver;

        final Context context = getActivity();

        values = originalInputValues;
        cancelASyncTask();
        toggleSolveMode(false);
        Toast.makeText(context, "This Sudoku has no solution.", Toast.LENGTH_SHORT).show();

        activity.refreshSudokuView(values, errors, enteredValues);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // Sudoku has no solution, send the original input to the view.
//
//                refreshSudokuView();
//
//
//            }
//        });
    }

    @Override
    public void sudokuSolved(int[][] values) {
//        this.solver = solver;
        this.values = values;
        toggleSolveMode(false);

        // Decide what to do next based on the solve mode.
        switch (solveMode) {
            case NORMAL:
                refreshSudokuView();
                break;
            case HINT:
                showHint();
                break;
            case ANIMATION:
                if (playAnimation) {
                    startAnimation();
                }
                break;
        }
    }

    private int[][] copyArray(int[][] array) {
        int[][] newArray = new int[array.length][array.length];

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array.length; j++) {
                newArray[i][j] = array[i][j];
            }
        }
        return newArray;
    }

    private void showHint() {
        int[][] hintValues = new int[values.length][values.length];
        ArrayList<GridLocation> openSpaces = new ArrayList<GridLocation>();

        // Copy all values values to the new hintarray.
        for (int i = 0; i < hintValues.length; i++) {
            for (int j = 0; j < hintValues.length; j++) {
                hintValues[i][j] = originalInputValues[i][j];
                if (originalInputValues[i][j] == 0) {
                    // Add the found open cell to the list.
                    openSpaces.add(new GridLocation(i, j));
                }
            }
        }

        int selectedX = activity.getSelectedX();
        int selectedY = activity.getSelectedY();

        // Only search for hint if there is an empty cell available.
        if (openSpaces.size() > 0) {
            if (selectedX >= 0 && selectedY >= 0 && hintValues[selectedX][selectedY] <= 0) {
                // Put the hint at the selected location.
                hintValues[selectedX][selectedY] = values[selectedX][selectedY];
            } else {
                // No location selected, reveal a random cell.
                Random random = new Random();
                GridLocation hintLocation = openSpaces.get(random.nextInt(openSpaces.size()));
                hintValues[hintLocation.getX()][hintLocation.getY()] = values[hintLocation.getX()][hintLocation.getY()];
            }
        }
        values = hintValues;
        refreshSudokuView();
    }

    private void startAnimation() {
        toggleSolveMode(true);
        playAnimation = false;
        solver = new ASyncSolver(getActivity(), this, true);
        solver.execute(originalInputValues);
    }
}
