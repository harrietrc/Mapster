package com.mapster.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by tommyngo on 10/06/15.
 */
public class TimePickerFragment extends DialogFragment {
    private TimePickerDialog.OnTimeSetListener _timeSetListener;

    public TimePickerFragment(){

    }

    public TimePickerFragment(TimePickerDialog.OnTimeSetListener callback) {
        _timeSetListener = (TimePickerDialog.OnTimeSetListener) callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), _timeSetListener, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }
}