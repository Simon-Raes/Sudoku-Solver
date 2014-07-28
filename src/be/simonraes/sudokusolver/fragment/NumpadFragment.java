package be.simonraes.sudokusolver.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import be.simonraes.sudokusolver.R;

import java.util.ArrayList;

/**
 * Created by Simon Raes on 26/07/2014.
 */
public class NumpadFragment extends Fragment implements View.OnClickListener {

    private Button btnSolve, btnHint, btnClear;
    private Button btnOne, btnTwo, btnThree, btnFour, btnFive, btnSix, btnSeven, btnEight, btnNine;
    private ArrayList<Button> numberButtons;
    private ImageButton btnDelete;
    private numPadDelegate delegate;

    private boolean btnSolveEnabled, btnHintEnabled, btnDeleteEnabled, btnNumbersEnabled;

    public static interface numPadDelegate {
        public void buttonClicked(int id);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            this.delegate = (numPadDelegate) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("btnSolveEnabled", btnSolveEnabled);
        outState.putBoolean("btnHintEnabled", btnHintEnabled);
        outState.putBoolean("btnNumbersEnabled", btnNumbersEnabled);
        outState.putBoolean("btnDeleteEnabled", btnDeleteEnabled);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);


        numberButtons = new ArrayList<Button>();

        if (savedInstanceState != null) {
            btnSolveEnabled = savedInstanceState.getBoolean("btnSolveEnabled");
            btnHintEnabled = savedInstanceState.getBoolean("btnHintEnabled");
            btnNumbersEnabled = savedInstanceState.getBoolean("btnNumbersEnabled");
            btnDeleteEnabled = savedInstanceState.getBoolean("btnDeleteEnabled");

        } else {
            btnSolveEnabled = true;
            btnHintEnabled = true;
            btnNumbersEnabled = true;
            btnDeleteEnabled = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.numpad_layout, null);
        initButtons(view);
        enableSolveButton(btnSolveEnabled);
        enableHintButton(btnHintEnabled);
        enableDeleteButton(btnDeleteEnabled);
        enableNumPadButtons(btnNumbersEnabled);
        return view;
    }

    private void initButtons(View view) {
        btnSolve = (Button) view.findViewById(R.id.btnSolve);
        btnSolve.setOnClickListener(this);
        btnHint = (Button) view.findViewById(R.id.btnHint);
        btnHint.setOnClickListener(this);
        btnDelete = (ImageButton) view.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(this);
        btnClear = (Button) view.findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);
        btnOne = (Button) view.findViewById(R.id.btnOne);
        btnOne.setOnClickListener(this);
        numberButtons.add(btnOne);
        btnTwo = (Button) view.findViewById(R.id.btnTwo);
        btnTwo.setOnClickListener(this);
        numberButtons.add(btnTwo);
        btnThree = (Button) view.findViewById(R.id.btnThree);
        btnThree.setOnClickListener(this);
        numberButtons.add(btnThree);
        btnFour = (Button) view.findViewById(R.id.btnFour);
        btnFour.setOnClickListener(this);
        numberButtons.add(btnFour);
        btnFive = (Button) view.findViewById(R.id.btnFive);
        btnFive.setOnClickListener(this);
        numberButtons.add(btnFive);
        btnSix = (Button) view.findViewById(R.id.btnSix);
        btnSix.setOnClickListener(this);
        numberButtons.add(btnSix);
        btnSeven = (Button) view.findViewById(R.id.btnSeven);
        btnSeven.setOnClickListener(this);
        numberButtons.add(btnSeven);
        btnEight = (Button) view.findViewById(R.id.btnEight);
        btnEight.setOnClickListener(this);
        numberButtons.add(btnEight);
        btnNine = (Button) view.findViewById(R.id.btnNine);
        btnNine.setOnClickListener(this);
        numberButtons.add(btnNine);
    }

    @Override
    public void onClick(View view) {
        delegate.buttonClicked(view.getId());
    }

    public void enableSolveButton(boolean enabled) {
        btnSolveEnabled = enabled;

        if (btnSolve != null) {
            btnSolve.setEnabled(enabled);
        }
    }

    public void enableHintButton(boolean enabled) {
        btnHintEnabled = enabled;

        if (btnHint != null) {
            btnHint.setEnabled(enabled);

        }
    }

    public void enableDeleteButton(boolean enabled) {
        btnDeleteEnabled = enabled;

        if (btnDelete != null) {
            btnDelete.setEnabled(enabled);
        }
    }

    public void enableNumPadButtons(boolean enabled) {
        btnNumbersEnabled = enabled;

        for (Button b : numberButtons) {
            if (b != null) {
                b.setEnabled(enabled);

            }
        }
    }

    public void toggleSolveMode(boolean solveMode){
        if(solveMode){
            btnClear.setText("Stop");
            enableDeleteButton(false);
            enableHintButton(false);
            enableSolveButton(false);
            enableNumPadButtons(false);
        } else {
            btnClear.setText("Clear");
            enableDeleteButton(true);
            enableHintButton(true);
            enableSolveButton(true);
            enableNumPadButtons(true);
        }
    }
}
