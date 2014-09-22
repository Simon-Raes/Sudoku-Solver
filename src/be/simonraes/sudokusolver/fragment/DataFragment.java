package be.simonraes.sudokusolver.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import be.simonraes.sudokusolver.model.ASyncBackTrackSolver;
import be.simonraes.sudokusolver.model.ASyncFasterSolver;
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
public class DataFragment extends Fragment implements ASyncBackTrackSolver.solverListener, ASyncFasterSolver.FasterSolverListener {

    GridValue[][] gridValues = new GridValue[9][9];
    GridValue[][] originalValues = new GridValue[9][9];

    private boolean currentlyAnimating = false; // Used to switch between the two different Clear-button modes. (Clear or Stop)
    private boolean currentlySolving = false; // Set to true if a solver is currently working on a solution.


    public DataFragmentDelegate delegate;

    public interface DataFragmentDelegate {
        public void updateValues(GridValue[][] values);

        public void toggleSolveMode(boolean solveModeActive);

        public void toggleSolveButtons(boolean buttonsActive);

        int getSelectedX();

        int getSelectedY();
    }

    private SolveMode solveMode;

    private enum SolveMode {
        NORMAL, HINT, ANIMATION
    }

    private ASyncBackTrackSolver solver;
    private ASyncFasterSolver fastSolver;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        delegate = (DataFragmentDelegate) getActivity();
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
        delegate.updateValues(gridValues);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        delegate = null;
    }

    public void solve() {
        toggleSolveMode(true);

        if (AppPreferences.solutionShouldAnimate(getActivity())) {
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
        // Don't allow a solver to start if the previous one is still running.
        if (!currentlySolving) {
            // Store the currently visible values in a separate array.
            // These original values will be restored if the solver can not find a solution and
            // are also used to find and empty cell to place a hint in.
            originalValues = copyArray(gridValues);
            currentlySolving = true;

            fastSolver = new ASyncFasterSolver(this);
            fastSolver.execute(gridValues);
        }
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
                solver = new ASyncBackTrackSolver(getActivity(), this, false);
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

        if (currentlyAnimating) {
            // Clear the solution cells
            clearSolutionCells();
            currentlyAnimating = false;
        } else {
            // Clear all cells
            gridValues = new GridValue[9][9];
        }

        toggleSolveMode(false);
        refreshSudokuView();
    }

    /**
     * Removes all cells found as solution from the array. Hints will not be removed.
     */
    private void clearSolutionCells() {
        for (int i = 0; i < gridValues.length; i++) {
            for (int j = 0; j < gridValues.length; j++) {
                if (gridValues[i][j] != null && gridValues[i][j].isSolution()) {
                    gridValues[i][j] = null;
                }
            }
        }
    }

    private boolean cancelASyncTask() {
        boolean cancelledTask = false;
        if (solver != null && solver.getStatus() == AsyncTask.Status.RUNNING) {
            solver.cancel(true);
            cancelledTask = true;
            solver.clearData();
        }
        if (fastSolver != null && fastSolver.getStatus() == AsyncTask.Status.RUNNING) {
            fastSolver.cancel(true);
            cancelledTask = true;

        }
        return cancelledTask;
    }

    private void refreshSudokuView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                delegate.updateValues(gridValues);
            }
        });
    }

    /**
     * Disables the "Solve" button.
     */
    private void enableSolveButtons() {
        delegate.toggleSolveButtons(true);
    }

    /**
     * Enables the "Solve" button.
     */
    private void disableSolveButtons() {
        delegate.toggleSolveButtons(false);
    }

    /**
     * Toggles all buttons except "Stop" on or off.
     */
    private void toggleSolveMode(boolean solveMode) {
        delegate.toggleSolveMode(solveMode);
    }

    @Override
    public void valueAdded() {
        // In case of animation: check if last cell has been found (= end of animation).
        if (gridValues[8][8] != null) {
            currentlyAnimating = false;
            toggleSolveMode(false);
        }
        delegate.updateValues(gridValues);
    }

    @Override
    public void fastSudokuHasNoSolution() {
        currentlySolving = false;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleSolveMode(false);
                Toast.makeText(getActivity(), "This Sudoku has no solution.", Toast.LENGTH_SHORT).show();
                cancelASyncTask();
                gridValues = originalValues;
                delegate.updateValues(gridValues);
            }
        });
    }

    @Override
    public void fastSudokuSolved(GridValue[][] gridValues) {
        currentlySolving = false;
        this.gridValues = gridValues;
        toggleSolveMode(false);

        // Decide what to do based on the active solve mode.
        switch (solveMode) {
            case NORMAL:

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

    /**Reveals a single cell.*/
    private void showHint() {

        // Make a list of all empty cells.
        ArrayList<GridLocation> openSpaces = new ArrayList<GridLocation>();

        for (int i = 0; i < originalValues.length; i++) {
            for (int j = 0; j < originalValues.length; j++) {
                if (originalValues[i][j] == null) {
                    openSpaces.add(new GridLocation(i, j));
                }
            }
        }

        int selectedX = delegate.getSelectedX();
        int selectedY = delegate.getSelectedY();

        // Only search for hint if there is an empty cell available.
        if (openSpaces.size() > 0) {
            if (selectedX < 0 || selectedY < 0 || originalValues[selectedX][selectedY] != null) {
                // No location selected or selection already has a value, reveal a random cell.
                Random random = new Random();
                GridLocation hintLocation = openSpaces.get(random.nextInt(openSpaces.size()));

                selectedX = hintLocation.getX();
                selectedY = hintLocation.getY();
            }
            if (gridValues[selectedX][selectedY] != null) {
                GridValue hintValue = gridValues[selectedX][selectedY];
                hintValue.setHint(true);
                hintValue.setSolution(false);
                originalValues[selectedX][selectedY] = hintValue;

                gridValues = originalValues;
                originalValues = null;
                refreshSudokuView();
            }
        }
    }

    private void startAnimation() {
        toggleSolveMode(true);
        solveMode = SolveMode.NORMAL;   // Values will be coming in one by one and can be displayed as a normal Sudoku.
        currentlyAnimating = true;

        solver = new ASyncBackTrackSolver(getActivity(), this, true);
        gridValues = originalValues;
        solver.execute(gridValues);
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
}
