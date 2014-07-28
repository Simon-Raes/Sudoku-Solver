package be.simonraes.sudokusolver.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import be.simonraes.sudokusolver.R;
import be.simonraes.sudokusolver.exception.NoSolutionException;
import be.simonraes.sudokusolver.fragment.NumpadFragment;
import be.simonraes.sudokusolver.fragment.SudokuViewFragment;
import be.simonraes.sudokusolver.model.ASyncSolver;

public class MainActivity extends Activity implements NumpadFragment.numPadDelegate, ASyncSolver.solverListener {

    int[][] solvedSudoku = new int[9][9];  // Fully solved Sudoku with all 81 digits.
    int[][] values = new int[9][9];        // The values to be sent to the UI, can contain less than 81 digits in the case of a hint.
    int[][] errors = new int[9][9];        // All input errors on their location.
    int[][] enteredValues = new int[9][9]; // All values entered by the user on their location.
//    int[][] valuesForVisualMode = new int[9][9];

//    private boolean animatedSolveMode = false;

    private SudokuViewFragment sudokuViewFragment;
    private NumpadFragment numpadFragment;
    private ASyncSolver solver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (savedInstanceState != null) {
            solvedSudoku = (int[][]) savedInstanceState.getSerializable("solvedSudoku");
            values = (int[][]) savedInstanceState.getSerializable("values");
            errors = (int[][]) savedInstanceState.getSerializable("errors");
            enteredValues = (int[][]) savedInstanceState.getSerializable("enteredValues");
        }

        //solver = new ASyncSolver(this, this, false);

        sudokuViewFragment = (SudokuViewFragment) getFragmentManager().findFragmentById(R.id.sudokuFragment);
        sudokuViewFragment.setValues(values, errors, enteredValues);
        numpadFragment = (NumpadFragment) getFragmentManager().findFragmentById(R.id.numpadFragment);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("solvedSudoku", solvedSudoku);
        outState.putSerializable("values", values);
        outState.putSerializable("errors", errors);
        outState.putSerializable("enteredValues", enteredValues);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        solver.detach();
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
                hint();
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

    private void goToSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    public void solve() {

        clearErrors();
        toggleSolveMode(true);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("be.simonraes.sudokusolver.preference.animatesolution", false)) {
            solveSudokuAnimated();
        } else {
            solveSudoku();
            //refreshSudokuView();
        }


//        } catch (NoSolutionException e) {
//
////            Toast.makeText(this, "Please fix input errors.", Toast.LENGTH_SHORT).show();
////            errors = solver.getErrors();
////            sudokuViewFragment.setValues(values, errors, enteredValues);
//        }
    }

    private void hint() {

        try {
            solver = new ASyncSolver(this, this, false);

            values = solver.hintSudoku(values, sudokuViewFragment.getSelectedX(), sudokuViewFragment.getSelectedY());
            clearErrors();
            refreshSudokuView();
        } catch (NoSolutionException e) {
            Toast.makeText(this, "Please fix all errors.", Toast.LENGTH_SHORT).show();
            errors = solver.getErrors();
            sudokuViewFragment.setValues(values, errors, enteredValues);
        }
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
            if (!solver.isErrorFree(values)) {
                errors = solver.getErrors();
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
        System.out.println(solver);
        if (solver != null) {
            System.out.println("cancelling solver task");
            solver.cancel(true);
        }
        values = new int[9][9];
        errors = new int[9][9];
        enteredValues = new int[9][9];
        solver.clearData();
        toggleSolveMode(false);
//        System.out.println("cleared all 3 arrays");
//        System.out.println("value at errors 0,0 = "+errors[0][0]);
        refreshSudokuView();
    }

    private void solveSudoku() {
        solver = new ASyncSolver(this, this, false);
        solver.execute(values);
    }

    private void solveSudokuAnimated() {
        solver = new ASyncSolver(this, this, true);
        solver.execute(values);
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
        numpadFragment.toggleSolveMode(solveMode);

    }



    @Override
    public void valueAdded(ASyncSolver solver, int[][] values) {
//        System.out.println(solver);
        this.solver = solver;
        System.out.println("received partial solution");

        this.values = values;
        //if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("be.simonraes.sudokusolver.preference.animatesolution", false)) {
//            final int[][] finalValues = values;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshSudokuView();
//                    sudokuViewFragment.setValues(finalValues, errors, enteredValues);
            }
        });
        //}
    }

    @Override
    public void sudokuSolved(ASyncSolver solver, int[][] values) {
        this.solver = solver;
        System.out.println("received full solution");
        this.values = values;
        toggleSolveMode(false);
        refreshSudokuView();
    }
}
