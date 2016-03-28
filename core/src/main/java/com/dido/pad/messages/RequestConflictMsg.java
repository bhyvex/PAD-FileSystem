package com.dido.pad.messages;

/**
 * Created by dido-ubuntu on 24/03/16.
 */
public class RequestConflictMsg extends AppMsg {

    String selection;

    public RequestConflictMsg(TYPE type, OP operation, String ipSender, int portSender, String msg){
        super(type, operation,ipSender,portSender);
        this.selection = msg;

    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }
}
