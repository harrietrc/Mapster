package com.mapster.infowindow;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Harriet on 7/29/2015. Represents one dialogue in a sequence of dialogues that are
 * shown one after another (basically a singly linked list).
 */
public abstract class SequentialDialogue {

    // View that represents this dialogue in the UI
    protected AlertDialog _dialogue;

    protected Context _context;
    protected LayoutInflater _inflater;

    private SequentialDialogue _nextInSequence;

    public SequentialDialogue(Context context, LayoutInflater inflater, SequentialDialogue nextInSequence) {
        _context = context;
        _inflater = inflater;
        _nextInSequence = nextInSequence;

        View dialogueContent = inflateContent();
        _dialogue = constructDialogue(dialogueContent);
    }

    protected abstract AlertDialog constructDialogue(View dialogueContent);

    protected abstract View inflateContent();

    public void show() {
        _dialogue.show();
    }

    public void hide() {
        _dialogue.hide();
    }

    public void moveToNext() {
        _dialogue.hide();
    }
}
