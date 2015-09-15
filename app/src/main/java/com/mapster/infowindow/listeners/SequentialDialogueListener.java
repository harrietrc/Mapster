package com.mapster.infowindow.listeners;

import android.content.DialogInterface;

import com.mapster.infowindow.dialogues.SequentialSuggestionItemDialogue;

/**
 * Created by Harriet on 8/08/2015.
 */
public abstract class SequentialDialogueListener implements DialogInterface.OnClickListener,
        DialogInterface.OnCancelListener {

    protected SequentialSuggestionItemDialogue _dialogue;

    public SequentialDialogueListener(SequentialSuggestionItemDialogue dialogue) {
        _dialogue = dialogue;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        _dialogue.moveToNext();
    }
}
