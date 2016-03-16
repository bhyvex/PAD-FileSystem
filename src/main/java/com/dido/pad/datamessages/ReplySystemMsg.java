package com.dido.pad.datamessages;

import com.dido.pad.VectorClocks.Versioned;

/**
 * Created by dido-ubuntu on 16/03/16.
 */
public class ReplySystemMsg extends AppMsg {

    private Versioned data;
    private String msg; //error message

    public ReplySystemMsg(){

    }

    public ReplySystemMsg( OPERATION operation, String ipSender, int portSender, Versioned data) {
        super(TYPE.REPLY, operation, ipSender, portSender);
        this.data = data;
    }
    public ReplySystemMsg( OPERATION operation, String ipSender, int portSender, String msg) {
        super(TYPE.REPLY, operation, ipSender, portSender);
        this.msg = msg;
    }
}
