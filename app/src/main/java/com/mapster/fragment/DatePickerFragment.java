package com.mapster.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import java.util.Calendar;

/**
 * Created by tommyngo on 10/06/15.
 */
public class DatePickerFragment extends DialogFragment {

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    public DatePickerFragment(){

    }

    public DatePickerFragment(DatePickerDialog.OnDateSetListener callback) {
        mDateSetListener = (DatePickerDialog.OnDateSetListener) callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar cal = Calendar.getInstance();

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(),
                mDateSetListener, cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

    }
}
