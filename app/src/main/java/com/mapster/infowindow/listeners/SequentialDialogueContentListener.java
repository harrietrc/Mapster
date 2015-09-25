package com.mapster.infowindow.listeners;

import android.view.View;

import com.mapster.infowindow.dialogues.SequentialSuggestionItemDialogue;

/**
 * Created by Harriet on 5/08/2015.
 */
public abstract class SequentialDialogueContentListener implements View.OnClickListener {

    protected SequentialSuggestionItemDialogue _dialogue;

    public SequentialDialogueContentListener(SequentialSuggestionItemDialogue dialogue) {
        _dialogue = dialogue;
    }
}
