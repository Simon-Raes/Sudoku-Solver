package be.simonraes.sudokusolver.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import be.simonraes.sudokusolver.R;
import be.simonraes.sudokusolver.ui.SudokuView;

/**
 * Created by Simon Raes on 26/07/2014.
 */
public class SudokuViewFragment extends Fragment {

    private SudokuView sudokuView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sudokuview_layout, null);

        //setRetainInstance(true);


        sudokuView = (SudokuView) view.findViewById(R.id.sudokuView);
        return view;
    }

    public int getSelectedX() {
        return sudokuView.getSelectedRow();
    }

    public int getSelectedY() {
        return sudokuView.getSelectedCol();
    }

    public void setValues(int[][] values, int[][] errors, int[][] enteredValues) {
        sudokuView.setValues(values, errors, enteredValues);
    }
}
