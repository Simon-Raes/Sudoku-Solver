package be.simonraes.sudokusolver.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import be.simonraes.sudokusolver.R;
import be.simonraes.sudokusolver.util.AppPreferences;


/**
 * Created by Simon Raes on 28/07/2014.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        if (getPreferenceScreen() != null && getPreferenceScreen().getSharedPreferences() != null) {
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }
        updateStepTime();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getPreferenceScreen() != null && getPreferenceScreen().getSharedPreferences() != null) {
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getPreferenceScreen() != null && getPreferenceScreen().getSharedPreferences() != null) {
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("be.simonraes.sudokusolver.preference.steptime")) {
            updateStepTime();
        }
//        if(key.equals("be.simonraes.sudokusolver.preference.animatesolution")){
//            updateAnimationToggle();
//        }
    }


//    private void updateAnimationToggle(){
//        Preference prefStepTime = findPreference("be.simonraes.sudokusolver.preference.steptime");
//
//        if (prefStepTime != null && getPreferenceScreen() != null && getPreferenceScreen().getSharedPreferences() != null) {
//            prefStepTime.setEnabled(AppPreferences.solutionShouldAnimate(getActivity()));
//        }
//
//    }

    private void updateStepTime() {
        Preference prefStepTime = findPreference("be.simonraes.sudokusolver.preference.steptime");

        if (prefStepTime != null && getPreferenceScreen() != null && getPreferenceScreen().getSharedPreferences() != null) {

            int stepTime = AppPreferences.getStepTime(getActivity());
            prefStepTime.setDefaultValue(stepTime);

            prefStepTime.setSummary("Showing a step every "
                    + getPreferenceScreen().getSharedPreferences().getString("be.simonraes.sudokusolver.preference.steptime", "")
                    + " milliseconds.");
        }
    }
}
