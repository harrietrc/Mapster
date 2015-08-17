package com.mapster.infowindow.dialogues;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import com.mapster.R;
import com.mapster.infowindow.listeners.DateOkButtonListener;
import com.mapster.itinerary.SuggestionItem;

/**
 * Created by Harriet on 7/29/2015.
 */
public class SuggestionDateDialogue extends SequentialSuggestionItemDialogue {

    private DatePicker _picker;

    public SuggestionDateDialogue(Context context, LayoutInflater inflater, SuggestionItem item) {
        super(context, inflater, item);
    }

    @Override
    protected AlertDialog constructDialogue(View dialogueContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);

        builder.setTitle("Arrival date");
        builder.setView(dialogueContent);
        builder.setNegativeButton(R.string.skip, null);

        // Listener saves date to the itinerary item when user clicks 'ok'
        DateOkButtonListener okButtonListener = new DateOkButtonListener(this, _picker, _item);
        builder.setPositiveButton(R.string.ok, okButtonListener);

        return builder.create();
    }

    @Override
    protected View inflateContent(LayoutInflater inflater) {
        View content = inflater.inflate(R.layout.date_dialogue, null, false);
        _picker = (DatePicker) content.findViewById(R.id.date_picker);
        return content;
    }

    // TODO Beware this not getting called - add exceptions or move to constructor.
    @Override
    protected void processItem() {
        // Nothing to do - no dialogue state to be set from item
    }
}
