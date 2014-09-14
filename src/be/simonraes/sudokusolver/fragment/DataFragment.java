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
import be.simonraes.sudokusolver.model.GridValue;
import be.simonraes.sudokusolver.util.AppPreferences;

import java.util.ArrayList;
import java.util.Random;

/**
 * Fragment without view that holds the logic and a reference to the ASyncTask.
 * This fragment retains instance state on orientation to keep the reference to the ASyncTask.
 * Created by Simon Raes on 28/07/2014.
 */
public class DataFragment extends Fragment implements ASyncSolver.solverListener {

    GridValue[][] gridValues = new GridValue[9][9];
    GridValue[][] originalValues = new GridValue[9][9];

    private boolean currentlyAnimating = false;

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

    /**
     * onCreate will only be called the first time.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // onCreateView will still be called after every re-orientation. Use this to send the latest values to the activity.
        activity.refreshSudokuView(gridValues);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void solve() {
        toggleSolveMode(true);

        if (AppPreferences.solutionShouldAnimate(activity)) {
            solveMode = SolveMode.ANIMATION;
        } else {
            solveMode = SolveMode.NORMAL;
        }
        startSolver();
    }


    public void solveForHint() {
        solveMode = SolveMode.HINT;
        startSolver();
    }

    private void startSolver() {
        // Store the currently visible values in a separate array.
        // These original values will be restored if the solver can not find a solution and
        // are also used to find and empty cell to place a hint in.
        originalValues = copyArray(gridValues);

        solver = new ASyncSolver(getActivity(), this, false);
        solver.clearData();
        solver.execute(gridValues);
    }

    public void setNumberForSelectedField(int number, int selectedX, int selectedY) {

        if (selectedX >= 0 && selectedY >= 0) {
            if (number > 0) {
                GridValue addedValue = new GridValue(number);
                addedValue.setInput(true);
                gridValues[selectedX][selectedY] = addedValue;
            } else {
                gridValues[selectedX][selectedY] = null;
            }

            if (solver == null) {
                solver = new ASyncSolver(getActivity(), this, false);
            }

            // Check if this new input causes any errors.
            if (solver.isErrorFree(gridValues)) {
                enableSolveButtons();
            } else {
                disableSolveButtons();
            }

            refreshSudokuView();
        } else {
            Toast.makeText(getActivity(), "Please select a cell.", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearBoard() {
        cancelASyncTask();

        if (!currentlyAnimating) {
            gridValues = new GridValue[9][9];
        } else {
            currentlyAnimating = false;
        }

        toggleSolveMode(false);
        refreshSudokuView();
    }

    private boolean cancelASyncTask() {
        boolean cancelledTask = false;
        if (solver != null && solver.getStatus() == AsyncTask.Status.RUNNING) {
            solver.cancel(true);
            cancelledTask = true;
            solver.clearData();
        }
        return cancelledTask;
    }

    private void refreshSudokuView() {
        activity.refreshSudokuView(gridValues);
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
    public void valueAdded() {
        activity.refreshSudokuView(gridValues);
    }

    @Override
    public void sudokuHasNoSolution() {
        final Context context = getActivity();

        // Display the original values (from before solver tried to find a solution).
        gridValues = originalValues;
        originalValues = null;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleSolveMode(false);
                Toast.makeText(context, "This Sudoku has no solution.", Toast.LENGTH_SHORT).show();
                cancelASyncTask();

                activity.refreshSudokuView(gridValues);
            }
        });
    }


    @Override
    public void sudokuSolved() {

        toggleSolveMode(false);

        // Decide what to do based on the active solve mode.
        switch (solveMode) {
            case NORMAL:
                // In case of animation: check if last cell has been found (= end of animation).
                if (gridValues[8][8] != null) {
                    currentlyAnimating = false;
                }
                refreshSudokuView();
                break;
            case HINT:
                showHint();
                break;
            case ANIMATION:
                startAnimation();
                break;
        }
    }

    /**
     * Returns a copy (new object) of the supplied array.
     */
    private GridValue[][] copyArray(GridValue[][] array) {
        GridValue[][] newArray = new GridValue[array.length][array.length];

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array.length; j++) {
                GridValue oldValue = array[i][j];
                if (oldValue == null) {
                    newArray[i][j] = null;
                } else {
                    GridValue newValue = new GridValue(oldValue.getValue());

                    newValue.setError(oldValue.isError());
                    newValue.setSolution(oldValue.isSolution());
                    newValue.setHint(oldValue.isHint());
                    newValue.setInput(oldValue.isInput());

                    newArray[i][j] = newValue;
                }
            }
        }
        return newArray;
    }

    private void showHint() {
        ArrayList<GridLocation> openSpaces = new ArrayList<GridLocation>();

        // Copy all values values to the new hintarray.
        for (int i = 0; i < originalValues.length; i++) {
            for (int j = 0; j < originalValues.length; j++) {

                // Make a list of all empty cells.
                if (originalValues[i][j] == null) {
                    openSpaces.add(new GridLocation(i, j));
                }
            }
        }

        int selectedX = activity.getSelectedX();
        int selectedY = activity.getSelectedY();

        // Only search for hint if there is an empty cell available.
        if (openSpaces.size() > 0) {
            if (selectedX >= 0 && selectedY >= 0 && originalValues[selectedX][selectedY] == null) {
                // Put the hint at the selected location.
                originalValues[selectedX][selectedY] = gridValues[selectedX][selectedY];
            } else {
                // No location selected, reveal a random cell.
                Random random = new Random();
                GridLocation hintLocation = openSpaces.get(random.nextInt(openSpaces.size()));
                originalValues[hintLocation.getX()][hintLocation.getY()] = gridValues[hintLocation.getX()][hintLocation.getY()];
            }
        }
        gridValues = originalValues;
        originalValues = null;
        refreshSudokuView();
    }

    private void startAnimation() {
        toggleSolveMode(true);
        solveMode = SolveMode.NORMAL;   // Values will be coming in one by one and can be normally displayed.
        currentlyAnimating = true;

        solver = new ASyncSolver(getActivity(), this, true);
        gridValues = originalValues;
        solver.execute(gridValues);
    }
}
