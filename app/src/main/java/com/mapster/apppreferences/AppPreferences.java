package com.mapster.apppreferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by tommyngo on 6/08/15.
 */
public class AppPreferences {
    public static final String TUTORIAL_PREFERENCES = "Tutorial";
    public static final String INSTRUCTION_TUTORIAL = "InstructionTutorial";
    public static final String SUGGESTION_TUTORIAL = "SuggestionTutorial";
    public static final String ACTION_BAR_MAIN_TUTORIAL = "ActionBarMainTutorial";
    public static final String PLACES_TUTORIAL = "PlacesTutorial";
    public static final String ITINERARY_TUTORIAL = "ItineraryTutorial";

    private SharedPreferences mShared;
    private SharedPreferences.Editor prefEditor;

    public AppPreferences(Context context) {
        this.mShared = context.getSharedPreferences(TUTORIAL_PREFERENCES, Activity.MODE_PRIVATE);
        this.prefEditor = mShared.edit();
    }

    public boolean isDoneInstructionTutorial(){
        return mShared.getBoolean(INSTRUCTION_TUTORIAL, false);
    }

    public boolean isDoneSuggestionTutorial(){
        return mShared.getBoolean(SUGGESTION_TUTORIAL, false);
    }

    public boolean isDoneActionBarMainTutorial(){
        return mShared.getBoolean(ACTION_BAR_MAIN_TUTORIAL, false);
    }

    public boolean isDonePlacesTutorial(){
        return mShared.getBoolean(PLACES_TUTORIAL, false);
    }

    public boolean isDoneItineraryTutorial() {
        return mShared.getBoolean(ITINERARY_TUTORIAL, false);
    }

    public void setDoneInstructionTutorial(){
        prefEditor.putBoolean(INSTRUCTION_TUTORIAL, true);
        prefEditor.commit();
    }

    public void setDoneSuggestionTutorial(){
        prefEditor.putBoolean(SUGGESTION_TUTORIAL, true);
        prefEditor.commit();
    }

    public void setDoneActionBarMainTutorial(){
        prefEditor.putBoolean(ACTION_BAR_MAIN_TUTORIAL, true);
        prefEditor.commit();
    }

    public void setDonePlacesTutorial(){
        prefEditor.putBoolean(PLACES_TUTORIAL, true);
        prefEditor.commit();
    }

    public void setDoneItineraryTutorial(){
        prefEditor.putBoolean(ITINERARY_TUTORIAL, true);
        prefEditor.commit();
    }
}
