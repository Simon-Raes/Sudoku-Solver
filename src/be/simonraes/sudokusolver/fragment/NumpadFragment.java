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

/**
 * Created by Simon Raes on 26/07/2014.
 */
public class NumpadFragment extends Fragment implements View.OnClickListener {

    private Button btnSolve, btnHint, btnClear, btnOne, btnTwo, btnThree, btnFour, btnFive, btnSix, btnSeven, btnEight, btnNine;
    private ImageButton btnDelete;
    private numPadDelegate delegate;

    private boolean btnSolveEnabled, btnHintEnabled;

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("reading numpad savedstate");

        if (savedInstanceState != null) {
            System.out.println("btnsolveenabled "+btnSolveEnabled);
            btnSolveEnabled = savedInstanceState.getBoolean("btnSolveEnabled");
            btnHintEnabled = savedInstanceState.getBoolean("btnHintEnabled");
        } else {
            btnSolveEnabled = true;
            btnHintEnabled = true;
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.numpad_layout, null);
        initButtons(view);
        enableSolveButton(btnSolveEnabled);
        enableHintButton(btnHintEnabled);
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
        btnTwo = (Button) view.findViewById(R.id.btnTwo);
        btnTwo.setOnClickListener(this);
        btnThree = (Button) view.findViewById(R.id.btnThree);
        btnThree.setOnClickListener(this);
        btnFour = (Button) view.findViewById(R.id.btnFour);
        btnFour.setOnClickListener(this);
        btnFive = (Button) view.findViewById(R.id.btnFive);
        btnFive.setOnClickListener(this);
        btnSix = (Button) view.findViewById(R.id.btnSix);
        btnSix.setOnClickListener(this);
        btnSeven = (Button) view.findViewById(R.id.btnSeven);
        btnSeven.setOnClickListener(this);
        btnEight = (Button) view.findViewById(R.id.btnEight);
        btnEight.setOnClickListener(this);
        btnNine = (Button) view.findViewById(R.id.btnNine);
        btnNine.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        delegate.buttonClicked(view.getId());
    }

    public void enableSolveButton(boolean enabled) {

        btnSolveEnabled = enabled;
        if(btnSolve!=null){
            btnSolve.setEnabled(enabled);
        }

    }

//    public void disableSolveButton() {
//        btnSolveEnabled = false;
//        btnSolve.setEnabled(false);
//    }

    public void enableHintButton(boolean enabled) {
        btnHintEnabled = enabled;
        if(btnHint!=null){
            btnHint.setEnabled(enabled);

        }
    }

//    public void disableHintButton() {
//        btnHintEnabled = false;
//        btnHint.setEnabled(false);
//    }


}
