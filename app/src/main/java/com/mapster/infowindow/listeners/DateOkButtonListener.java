package com.mapster.infowindow.listeners;

import android.content.DialogInterface;
import android.widget.DatePicker;

import com.mapster.infowindow.dialogues.SequentialSuggestionItemDialogue;
import com.mapster.itinerary.SuggestionItem;

/**
 * Created by Harriet on 8/08/2015.
 */
public class DateOkButtonListener extends SequentialDialogueListener {

    private SuggestionItem _item;
    private DatePicker _picker;

    public DateOkButtonListener(SequentialSuggestionItemDialogue dialogue, DatePicker picker, SuggestionItem item) {
        super(dialogue);
        _item = item;
        _picker = picker;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        _item.setDate(_picker.getYear(), _picker.getMonth()+1, _picker.getDayOfMonth());
        _dialogue.moveToNext();
    }
}
