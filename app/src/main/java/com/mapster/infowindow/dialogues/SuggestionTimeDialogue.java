package com.mapster.infowindow.dialogues;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import com.mapster.R;
import com.mapster.infowindow.listeners.TimeOkButtonListener;
import com.mapster.itinerary.SuggestionItem;

/**
 * Created by Harriet on 7/29/2015.
 */
public class SuggestionTimeDialogue extends SequentialSuggestionItemDialogue {

    private TimePicker _picker;

    public SuggestionTimeDialogue(Context context, LayoutInflater inflater, SuggestionItem item) {
        super(context, inflater, item);
    }

    @Override
    protected AlertDialog constructDialogue(View dialogueContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle("Arrival time");
        builder.setView(dialogueContent);
        builder.setNegativeButton(R.string.skip, null);

        // Listen for 'ok' and save the time to the item
        TimeOkButtonListener okButtonListener = new TimeOkButtonListener(this, _picker, _item);
        builder.setPositiveButton(R.string.ok, okButtonListener);

        return builder.create();
    }

    @Override
    protected View inflateContent(LayoutInflater inflater) {
        View content = inflater.inflate(R.layout.time_dialogue, null, false);
        _picker = (TimePicker) content.findViewById(R.id.time_picker);
        return content;
    }

    @Override
    protected void processItem() {
        // Nothing to do - no dialogue state needs to be set
    }
}
