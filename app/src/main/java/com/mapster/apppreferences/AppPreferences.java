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

    public static final String USER_CURRENCY = "UserCurrency";

    private SharedPreferences _shared;
    private SharedPreferences.Editor _prefEditor;

    public AppPreferences(Context context) {
        _shared = context.getSharedPreferences(TUTORIAL_PREFERENCES, Activity.MODE_PRIVATE);
        _prefEditor = _shared.edit();
    }

    public String getUserCurrency() {
        return _shared.getString(USER_CURRENCY, "NZD");
    }

    public boolean isDoneInstructionTutorial(){
        return _shared.getBoolean(INSTRUCTION_TUTORIAL, false);
    }

    public boolean isDoneSuggestionTutorial(){
        return _shared.getBoolean(SUGGESTION_TUTORIAL, false);
    }

    public boolean isDoneActionBarMainTutorial(){
        return _shared.getBoolean(ACTION_BAR_MAIN_TUTORIAL, false);
    }

    public boolean isDonePlacesTutorial(){
        return _shared.getBoolean(PLACES_TUTORIAL, false);
    }

    public boolean isDoneItineraryTutorial() {
        return _shared.getBoolean(ITINERARY_TUTORIAL, false);
    }

    public void setDoneInstructionTutorial(){
        _prefEditor.putBoolean(INSTRUCTION_TUTORIAL, true);
        _prefEditor.commit();
    }

    public void setDoneSuggestionTutorial(){
        _prefEditor.putBoolean(SUGGESTION_TUTORIAL, true);
        _prefEditor.commit();
    }

    public void setDoneActionBarMainTutorial(){
        _prefEditor.putBoolean(ACTION_BAR_MAIN_TUTORIAL, true);
        _prefEditor.commit();
    }

    public void setDonePlacesTutorial(){
        _prefEditor.putBoolean(PLACES_TUTORIAL, true);
        _prefEditor.commit();
    }

    public void setDoneItineraryTutorial(){
        _prefEditor.putBoolean(ITINERARY_TUTORIAL, true);
        _prefEditor.commit();
    }

    public void setUserCurrency(String userCurrency) {
        _prefEditor.putString(USER_CURRENCY, userCurrency);
        _prefEditor.commit();
    }
}
