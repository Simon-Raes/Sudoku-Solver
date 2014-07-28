package be.simonraes.sudokusolver.activity;

import android.app.Activity;
import android.os.Bundle;
import be.simonraes.sudokusolver.fragment.SettingsFragment;


/**
 * Created by Simon Raes on 28/07/2014.
 */
public class SettingsActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

}