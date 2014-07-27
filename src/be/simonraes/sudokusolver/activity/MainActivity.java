package be.simonraes.sudokusolver.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import be.simonraes.sudokusolver.R;
import be.simonraes.sudokusolver.exception.NoSolutionException;
import be.simonraes.sudokusolver.fragment.NumpadFragment;
import be.simonraes.sudokusolver.fragment.SudokuViewFragment;
import be.simonraes.sudokusolver.model.Solver;

public class MainActivity extends Activity implements NumpadFragment.numPadDelegate {

    int[][] solvedSudoku = new int[9][9];  // Fully solved Sudoku with all 81 digits.
    int[][] values = new int[9][9];        // The values to be sent to the UI, can contain less than 81 digits in the case of a hint.
    int[][] errors = new int[9][9];        // All input errors on their location.
    int[][] enteredValues = new int[9][9]; // All values entered by the user on their location.


    private SudokuViewFragment sudokuViewFragment;
    private Solver solver;

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

        solver = new Solver();

        sudokuViewFragment = (SudokuViewFragment) getFragmentManager().findFragmentById(R.id.sudokuFragment);
        sudokuViewFragment.setValues(values, errors, enteredValues);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("solvedSudoku", solvedSudoku);
        outState.putSerializable("values", values);
        outState.putSerializable("errors", errors);
        outState.putSerializable("enteredValues", enteredValues);
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

    public void solve() {

        try {
            //todo: put solver in asynctask so the UI thread doesn't lock up with hard/impossible inputs
            //stop searching after x seconds?

            //todo: display errors realtime

            //todo: option to solve step by step
            //fix the row colum switching first then
            values = solver.solveSudoku(values);
            clearErrors();
            refreshSudokuView();
        } catch (NoSolutionException e) {
            Toast.makeText(this, "Invalid input.", Toast.LENGTH_SHORT).show();
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
            if(!solver.isErrorFree(values)){
                errors = solver.getErrors();
            } else {
                errors = new int[9][9];
            }

            refreshSudokuView();
        } else {
            //todo: some form of feedback to let user know he needs to select a grid square
            Toast.makeText(this, "Select a cell.",Toast.LENGTH_SHORT).show();
        }

    }

    private void hint() {

        try {
            values = solver.hintSudoku(values, sudokuViewFragment.getSelectedX(), sudokuViewFragment.getSelectedY());
            clearErrors();
            refreshSudokuView();
        } catch (NoSolutionException e) {
            Toast.makeText(this, "Invalid input.", Toast.LENGTH_SHORT).show();
            errors = solver.getErrors();
            sudokuViewFragment.setValues(values, errors, enteredValues);
        }
    }

    private void clearBoard() {
        values = new int[9][9];
        errors = new int[9][9];
        enteredValues = new int[9][9];
        solver.clearData();
        System.out.println("cleared all 3 arrays");
        System.out.println("value at errors 0,0 = "+errors[0][0]);
        refreshSudokuView();
    }

    private void refreshSudokuView() {
        sudokuViewFragment.setValues(values, errors, enteredValues);
    }
}
