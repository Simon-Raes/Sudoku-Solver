package be.simonraes.sudokusolver.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import be.simonraes.sudokusolver.model.ASyncSolver;

/**
 * Fragment without view that holds a reference to the ASyncTask and retains instance state on orientation.
 * Created by Simon Raes on 28/07/2014.
 */
public class DataFragment extends Fragment implements ASyncSolver.solverListener {

    int testValue;
    private ASyncSolver solver;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (solver != null) {
            solver.attach(this);
        }
    }

    public int getTestValue() {
        return testValue;
    }

    public void setTestValue(int testValue) {
        this.testValue = testValue;
    }

    @Override
    public void valueAdded(ASyncSolver solver, int[][] values) {

    }

    @Override
    public void sudokuHasNoSolution(ASyncSolver solver) {

    }

    @Override
    public void sudokuSolved(ASyncSolver solver, int[][] values) {

    }
}
