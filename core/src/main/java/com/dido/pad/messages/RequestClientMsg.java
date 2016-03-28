package com.dido.pad.messages;

/**
 * Created by dido-ubuntu on 26/03/16.
 */
public class RequestClientMsg extends AppMsg {

    public RequestClientMsg(OP op, String ipSender, int port){
        super(TYPE.REQUEST, op, ipSender, port);
    }

    public RequestClientMsg(){

    }

}
