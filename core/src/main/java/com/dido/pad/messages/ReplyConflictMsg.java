package com.dido.pad.messages;

/**
 * Created by dido-ubuntu on 04/04/16.
 */
public class ReplyConflictMsg  extends Msg {

    public int selection;

    public ReplyConflictMsg(TYPE type, OP operation, int response) {
        super(type, operation);
        this.selection = response;

    }

    public ReplyConflictMsg(){

    }
    public int getSelection() {
        return selection;
    }

    public void setSelection(int selection) {
        this.selection = selection;
    }
}
