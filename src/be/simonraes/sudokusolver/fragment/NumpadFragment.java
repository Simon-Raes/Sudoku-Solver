package be.simonraes.sudokusolver.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import be.simonraes.sudokusolver.R;

import java.util.ArrayList;

/**
 * Fragment that holds the buttons.
 * Created by Simon Raes on 26/07/2014.
 */
public class NumpadFragment extends Fragment implements View.OnClickListener {

    private Button btnSolve, btnHint, btnClear;
    private ArrayList<Button> numberButtons;
    private ImageButton btnDelete;
    private numPadDelegate delegate;

    private boolean btnSolveEnabled, btnHintEnabled, btnDeleteEnabled, btnNumbersEnabled, btnClearIsStop;

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
        outState.putBoolean("btnClearIsStop", btnClearIsStop);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        numberButtons = new ArrayList<Button>();

        if (savedInstanceState != null) {
            btnSolveEnabled = savedInstanceState.getBoolean("btnSolveEnabled");
            btnHintEnabled = savedInstanceState.getBoolean("btnHintEnabled");
            btnNumbersEnabled = savedInstanceState.getBoolean("btnNumbersEnabled");
            btnDeleteEnabled = savedInstanceState.getBoolean("btnDeleteEnabled");
            btnClearIsStop = savedInstanceState.getBoolean("btnClearIsStop");
        } else {
            btnSolveEnabled = true;
            btnHintEnabled = true;
            btnNumbersEnabled = true;
            btnDeleteEnabled = true;
            btnClearIsStop = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.numpad_layout, null);
        initButtons(view);

        enableSolveButton(btnSolveEnabled);
        enableHintButton(btnHintEnabled);
        enableDeleteButton(btnDeleteEnabled);
        enableNumberButtons(btnNumbersEnabled);
        setClearButtonText(btnClearIsStop);

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

        Button btnOne = (Button) view.findViewById(R.id.btnOne);
        btnOne.setOnClickListener(this);
        numberButtons.add(btnOne);

        Button btnTwo = (Button) view.findViewById(R.id.btnTwo);
        btnTwo.setOnClickListener(this);
        numberButtons.add(btnTwo);

        Button btnThree = (Button) view.findViewById(R.id.btnThree);
        btnThree.setOnClickListener(this);
        numberButtons.add(btnThree);

        Button btnFour = (Button) view.findViewById(R.id.btnFour);
        btnFour.setOnClickListener(this);
        numberButtons.add(btnFour);

        Button btnFive = (Button) view.findViewById(R.id.btnFive);
        btnFive.setOnClickListener(this);
        numberButtons.add(btnFive);

        Button btnSix = (Button) view.findViewById(R.id.btnSix);
        btnSix.setOnClickListener(this);
        numberButtons.add(btnSix);

        Button btnSeven = (Button) view.findViewById(R.id.btnSeven);
        btnSeven.setOnClickListener(this);
        numberButtons.add(btnSeven);

        Button btnEight = (Button) view.findViewById(R.id.btnEight);
        btnEight.setOnClickListener(this);
        numberButtons.add(btnEight);

        Button btnNine = (Button) view.findViewById(R.id.btnNine);
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

    public void enableNumberButtons(boolean enabled) {
        btnNumbersEnabled = enabled;

        for (Button b : numberButtons) {
            if (b != null) {
                b.setEnabled(enabled);
            }
        }
    }

    private void setClearButtonText(boolean btnClearIsStop) {
        this.btnClearIsStop = btnClearIsStop;
        if (btnClear != null) {
            btnClear.setText(btnClearIsStop ? "Stop" : "Clear");
        }
    }

    public void toggleSolveMode(boolean solveMode) {
        if (solveMode) {
            setClearButtonText(true);
            enableDeleteButton(false);
            enableHintButton(false);
            enableSolveButton(false);
            enableNumberButtons(false);
        } else {
            setClearButtonText(false);
            enableDeleteButton(true);
            enableHintButton(true);
            enableSolveButton(true);
            enableNumberButtons(true);
        }
    }
}
