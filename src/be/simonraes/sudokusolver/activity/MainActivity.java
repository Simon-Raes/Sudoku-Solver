package be.simonraes.sudokusolver.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import be.simonraes.sudokusolver.R;
import be.simonraes.sudokusolver.fragment.DataFragment;
import be.simonraes.sudokusolver.fragment.NumpadFragment;
import be.simonraes.sudokusolver.fragment.SudokuViewFragment;
import be.simonraes.sudokusolver.util.AppPreferences;

public class MainActivity extends FragmentActivity implements NumpadFragment.numPadDelegate {

    // Fragments
    private SudokuViewFragment sudokuViewFragment;
    private NumpadFragment numpadFragment;
    private DataFragment dataFragment;
    private final String DATA_FRAGMENT_TAG = "DATA_FRAGMENT_TAG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("activity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        FragmentManager fm = getSupportFragmentManager();
        sudokuViewFragment = (SudokuViewFragment) fm.findFragmentById(R.id.sudokuFragment);
        numpadFragment = (NumpadFragment) fm.findFragmentById(R.id.numpadFragment);
        dataFragment = (DataFragment) fm.findFragmentByTag(DATA_FRAGMENT_TAG);
        if (dataFragment == null) {
            dataFragment = new DataFragment();
            fm.beginTransaction().add(dataFragment, DATA_FRAGMENT_TAG).commit();
        }
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
                dataFragment.solve();
                break;
            case R.id.btnHint:
                dataFragment.solveForHint();
                break;
            case R.id.btnClear:
                dataFragment.clearBoard();
                break;
            case R.id.btnDelete:
                setNumberForSelectedField(0);
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

    /**
     * Places the tapped number in the selected cell.
     */
    private void setNumberForSelectedField(int number) {
        dataFragment.setNumberForSelectedField(number, sudokuViewFragment.getSelectedX(), sudokuViewFragment.getSelectedY());
    }

    public void refreshSudokuView(int[][] values, int[][] errors, int[][] enteredValues) {
        System.out.println("activity refreshSudokuView");
        sudokuViewFragment.setValues(values, errors, enteredValues);
    }

    public void toggleSolveButtons(boolean enable) {
        numpadFragment.enableSolveButton(enable);
        numpadFragment.enableHintButton(enable);
    }

    public void toggleSolveMode(boolean enable) {
        if (AppPreferences.solutionShouldAnimate(this)) {
            numpadFragment.toggleSolveMode(enable);
        }
    }

    public int getSelectedX() {
        return sudokuViewFragment.getSelectedX();
    }

    public int getSelectedY() {
        return sudokuViewFragment.getSelectedY();
    }

}
