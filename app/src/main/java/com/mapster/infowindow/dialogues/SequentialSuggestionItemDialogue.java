package com.mapster.infowindow.dialogues;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.mapster.itinerary.SuggestionItem;

/**
 * Created by Harriet on 7/29/2015. Represents one dialogue in a sequence of dialogues that are
 * shown one after another (basically a singly linked list).
 * Intended to base its state off or modify the state of a single SuggestionItem.
 *
 * Not really necessary but might be better design to put those last 2 methods in another
 * class that extends this - i.e. remove references to SuggestionItem and change this into a
 * SequentialDialogue abstraction.
 */
public abstract class SequentialSuggestionItemDialogue {
    // View that represents this dialogue in the UI
    protected AlertDialog _dialogue;
    protected Context _context;
    protected SequentialSuggestionItemDialogue _nextInSequence;
    protected SuggestionItem _item;

    /**
     * Bit of a template method (although I've tried to restrict the logic to only setting up
     * dialogue state (listeners, references to buttons, visibility of views, etc.). Still seems
     * a bit dangerous to have these 'hooks' into the constructor.
     */
    public SequentialSuggestionItemDialogue(Context context, LayoutInflater inflater, SuggestionItem item) {
        _context = context;
        _item = item;
        View dialogueContent = inflateContent(inflater);
        _dialogue = constructDialogue(dialogueContent);
        processItem();
    }

    /**
     * @return The dialogue so you can have nice pretty one-liner call chains for the whole sequence
     * (not sure if bad practice)
     */
    public SequentialSuggestionItemDialogue setNextDialogue(SequentialSuggestionItemDialogue dialogue) {
        _nextInSequence = dialogue;
        return _nextInSequence;
    }

    /**
     * @param dialogueContent View that acts as the contents of the dialogue
     */
    protected abstract AlertDialog constructDialogue(View dialogueContent);

    /**
     * @return A view with content for the dialogue
     */
    protected abstract View inflateContent(LayoutInflater inflater);

    public void show() {
        _dialogue.show();
    }

    public void dismiss() {
        _dialogue.dismiss();
    }

    public void moveToNext() {
        dismiss();

        if (_nextInSequence != null)
            _nextInSequence.show();
    }

    public View findViewById(int id) {
        return _dialogue.findViewById(id);
    }

    protected abstract void processItem();
}
