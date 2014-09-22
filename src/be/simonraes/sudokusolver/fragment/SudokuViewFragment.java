package be.simonraes.sudokusolver.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import be.simonraes.sudokusolver.R;
import be.simonraes.sudokusolver.model.GridValue;
import be.simonraes.sudokusolver.ui.SudokuGrid;

/**
 * Fragment that holds the Sudoku grid.
 * Created by Simon Raes on 26/07/2014.
 */
public class SudokuViewFragment extends Fragment {

    private SudokuGrid sudokuGrid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sudokuview_layout, null);

        sudokuGrid = (SudokuGrid) view.findViewById(R.id.sudokuView);
        return view;
    }

    public int getSelectedX() {
        return sudokuGrid.getSelectedRow();
    }

    public int getSelectedY() {
        return sudokuGrid.getSelectedCol();
    }

    public void setValues(GridValue[][] values) {
        sudokuGrid.setValues(values);
    }
}
