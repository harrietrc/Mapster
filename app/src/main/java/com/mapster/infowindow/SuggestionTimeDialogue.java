package com.mapster.infowindow;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Harriet on 7/29/2015.
 */
public class SuggestionTimeDialogue extends SequentialDialogue {

    public SuggestionTimeDialogue(Context context, LayoutInflater inflater, SequentialDialogue nextInSequence) {
        super(context, inflater, nextInSequence);
    }

    @Override
    protected AlertDialog constructDialogue(View dialogueContent) {
        return null;
    }

    @Override
    protected View inflateContent() {
        return null;
    }
}
