package com.dido.pad.datamessages;

import com.dido.pad.Node;

/**
 * Created by dido-ubuntu on 10/03/16.
 */
public class RequestAppMsg extends AppMsg{

    private String payload;

    public RequestAppMsg(OPERATION operation, Node originalSender, String payload) {
        super(TYPE.REQUEST, operation, originalSender);
        this.payload = payload;
    }
}
