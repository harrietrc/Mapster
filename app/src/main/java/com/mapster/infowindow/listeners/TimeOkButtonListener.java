package com.mapster.infowindow.listeners;

import android.content.DialogInterface;
import android.widget.TimePicker;

import com.mapster.infowindow.dialogues.SequentialSuggestionItemDialogue;
import com.mapster.itinerary.SuggestionItem;

/**
 * Created by Harriet on 8/08/2015.
 */
public class TimeOkButtonListener extends SequentialDialogueListener {

    private TimePicker _picker;
    private SuggestionItem _item;

    public TimeOkButtonListener(SequentialSuggestionItemDialogue dialogue, TimePicker picker, SuggestionItem item) {
        super(dialogue);
        _picker = picker;
        _item = item;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        _item.setTime(_picker.getCurrentHour(), _picker.getCurrentMinute());
        _dialogue.dismiss(); // Need to do this or dismiss(), else the dialogue leaks
    }
}
