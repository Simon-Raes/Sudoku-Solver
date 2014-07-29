package be.simonraes.sudokusolver.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import be.simonraes.sudokusolver.R;
import be.simonraes.sudokusolver.fragment.NumpadFragment;
import be.simonraes.sudokusolver.fragment.SudokuViewFragment;
import be.simonraes.sudokusolver.model.ASyncSolver;
import be.simonraes.sudokusolver.model.GridLocation;
import be.simonraes.sudokusolver.util.AppPreferences;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity implements NumpadFragment.numPadDelegate, ASyncSolver.solverListener {

    int[][] originalInputValues = new int[9][9];  // Input the user entered.
    int[][] values = new int[9][9];        // The values to be sent to the UI, can contain less than 81 digits in the case of a hint.
    int[][] errors = new int[9][9];        // All input errors on their location.
    int[][] enteredValues = new int[9][9]; // All values entered by the user on their location.

    private SudokuViewFragment sudokuViewFragment;
    private NumpadFragment numpadFragment;
    private ASyncSolver solver;

    private boolean playAnimation = false;

    private SolveMode solveMode;

    private enum SolveMode {
        NORMAL, HINT, ANIMATION;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (savedInstanceState != null) {
            originalInputValues = (int[][]) savedInstanceState.getSerializable("originalInputValues");
            values = (int[][]) savedInstanceState.getSerializable("values");
            errors = (int[][]) savedInstanceState.getSerializable("errors");
            enteredValues = (int[][]) savedInstanceState.getSerializable("enteredValues");
        }

        solver = (ASyncSolver) getLastNonConfigurationInstance();

        if (solver != null) {
            System.out.println("solver isn't null, reattaching");
            solver.attach(this);
        } else {
            System.out.println("solver was null");
        }
        //solver = new ASyncSolver(this, this, false);

        sudokuViewFragment = (SudokuViewFragment) getFragmentManager().findFragmentById(R.id.sudokuFragment);
        sudokuViewFragment.setValues(values, errors, enteredValues);
        numpadFragment = (NumpadFragment) getFragmentManager().findFragmentById(R.id.numpadFragment);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("originalInputValues", originalInputValues);
        outState.putSerializable("values", values);
        outState.putSerializable("errors", errors);
        outState.putSerializable("enteredValues", enteredValues);
    }

    // Todo: this should be replaced with a fragment without view that stores the references to the ASyncTask.
    // //Fragment controls the ASyncTask and implements setRetainInstance(true) to save state during reorientations.
    @Override
    public Object onRetainNonConfigurationInstance() {
        if (solver != null) {
            System.out.println("soolver not null, saving");
            solver.detach();
        }

        return solver;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnSettings:
                goToSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    /**
     * Handles button clicks for the numpad fragment.
     */
    @Override
    public void buttonClicked(int id) {
        switch (id) {
            case R.id.btnSolve:
                solve();
                break;
            case R.id.btnHint:
                solveForHint();
                break;
            case R.id.btnDelete:
                setNumberForSelectedField(0);
                break;
            case R.id.btnClear:
                clearBoard();
                break;
            case R.id.btnOne:
                setNumberForSelectedField(1);
                break;
            case R.id.btnTwo:
                setNumberForSelectedField(2);
                break;
            case R.id.btnThree:
                setNumberForSelectedField(3);
                break;
            case R.id.btnFour:
                setNumberForSelectedField(4);
                break;
            case R.id.btnFive:
                setNumberForSelectedField(5);
                break;
            case R.id.btnSix:
                setNumberForSelectedField(6);
                break;
            case R.id.btnSeven:
                setNumberForSelectedField(7);
                break;
            case R.id.btnEight:
                setNumberForSelectedField(8);
                break;
            case R.id.btnNine:
                setNumberForSelectedField(9);
                break;
        }
    }


    public void solve() {

        clearErrors();
        toggleSolveMode(true);
        solveMode = SolveMode.NORMAL;

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("be.simonraes.sudokusolver.preference.animatesolution", false)) {
            solveSudokuAnimated();
        } else {
            solveSudoku();
        }
    }

    private void solveSudoku() {
        solveMode = SolveMode.NORMAL;
        startSolver();
    }

    private void solveSudokuAnimated() {
        playAnimation = true;
        solveMode = SolveMode.ANIMATION;
        startSolver();
    }

    private void solveForHint() {
        solveMode = SolveMode.HINT;
        startSolver();
    }

    private void startSolver() {
        solver = new ASyncSolver(this, this, false);
        originalInputValues = copyArray(values);
        solver.execute(values);
    }

    private void clearErrors() {
        errors = null;
    }

    private void setNumberForSelectedField(int number) {
        int selectedX = sudokuViewFragment.getSelectedX();
        int selectedY = sudokuViewFragment.getSelectedY();

        if (selectedX >= 0 && selectedY >= 0) {
            values[sudokuViewFragment.getSelectedX()][sudokuViewFragment.getSelectedY()] = number;
            enteredValues[sudokuViewFragment.getSelectedX()][sudokuViewFragment.getSelectedY()] = number;
            if (solver == null) {
                solver = new ASyncSolver(this, this, false);
            }

            // Check if this new input caused any errors.
            if (!solver.isErrorFree(values)) {
                errors = solver.getErrorValues();
                disableSolveButtons();
            } else {
                errors = new int[9][9];
                enableSolveButtons();
            }

            refreshSudokuView();
        } else {
            Toast.makeText(this, "Please select a cell.", Toast.LENGTH_SHORT).show();
        }

    }


    private void clearBoard() {
        System.out.println("clearing board");
        cancelASyncTask();

        values = new int[9][9];
        errors = new int[9][9];
        enteredValues = new int[9][9];
//        solver.clearData();
        toggleSolveMode(false);
        refreshSudokuView();
    }

    private void cancelASyncTask() {
        if (solver != null) {
            if (solver.getStatus() == AsyncTask.Status.RUNNING) {
                solver.cancel(true);
                solver.clearData();
            }
        }
    }


    private void refreshSudokuView() {
        sudokuViewFragment.setValues(values, errors, enteredValues);
    }

    private void enableSolveButtons() {
        numpadFragment.enableSolveButton(true);
        numpadFragment.enableHintButton(true);
    }

    private void disableSolveButtons() {
        numpadFragment.enableSolveButton(false);
        numpadFragment.enableHintButton(false);
    }


    private void toggleSolveMode(boolean solveMode) {
        // Disabling the numpad is only necessary when animating the solution.
        if (AppPreferences.solutionShouldAnimate(this)) {
            numpadFragment.toggleSolveMode(solveMode);
        }
    }


    @Override
    public void valueAdded(ASyncSolver solver, int[][] values) {

        this.solver = solver;
        System.out.println("received partial solution");

        this.values = values;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshSudokuView();
            }
        });
    }

    @Override
    public void sudokuHasNoSolution(ASyncSolver solver) {
        this.solver = solver;

        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Sudoku has no values, send the original input to the view.
                values = originalInputValues;
                refreshSudokuView();
                enableSolveButtons();
                cancelASyncTask();
                Toast.makeText(context, "This Sudoku has no solution.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void sudokuSolved(ASyncSolver solver, int[][] values) {
        this.solver = solver;
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
                if(playAnimation){
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

        // Copy all values values to the new hintarray
        for (int i = 0; i < hintValues.length; i++) {
            for (int j = 0; j < hintValues.length; j++) {
                hintValues[i][j] = originalInputValues[i][j];
                if (originalInputValues[i][j] == 0) {
                    // Add the found open cell to the list.
                    openSpaces.add(new GridLocation(i, j));
                }
            }
        }

        int selectedX = sudokuViewFragment.getSelectedX();
        int selectedY = sudokuViewFragment.getSelectedY();

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
        System.out.println("starting animations");

        solver = new ASyncSolver(this, this, true);
        //clearBoard();

        //originalInputValues = copyArray(values);
        //clearBoard();
        solver.execute(originalInputValues);
    }
}
