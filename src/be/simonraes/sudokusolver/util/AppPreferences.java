package be.simonraes.sudokusolver.util;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Simon Raes on 28/07/2014.
 */
public class AppPreferences {

    public static boolean solutionShouldAnimate(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("be.simonraes.sudokusolver.preference.animatesolution", false);
    }

    public static int getStepTime(Context context){
        String stepTimeString = PreferenceManager.getDefaultSharedPreferences(context).getString("be.simonraes.sudokusolver.preference.steptime", "");

        int stepTime;

        if (stepTimeString.equals("")) {
            stepTime = 0;
        } else {
            stepTime = Integer.parseInt(stepTimeString);
        }

        if (stepTime <= 0) {
            stepTime = 100;
        }
        return stepTime;
    }
}
