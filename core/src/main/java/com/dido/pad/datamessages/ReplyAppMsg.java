package com.dido.pad.datamessages;

/**
 * Created by dido-ubuntu on 11/03/16.
 */
public class ReplyAppMsg extends AppMsg {
    public String msg;

    public ReplyAppMsg(String msg) {
        this.msg = msg;
    }

    public ReplyAppMsg(){

    }

    public ReplyAppMsg(OP operation, String msg) {
        super(TYPE.REPLY, operation);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


}
