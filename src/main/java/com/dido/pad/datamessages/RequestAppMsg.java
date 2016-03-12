package com.dido.pad.datamessages;

import com.dido.pad.Node;

/**
 * Created by dido-ubuntu on 10/03/16.
 */
public class RequestAppMsg<T> extends AppMsg{

    private String key;
    private T value;


    public RequestAppMsg(OPERATION operation, String key, T value) {
        super(TYPE.REQUEST, operation);
        this.key = key;
        this.value = value;
    }

    public RequestAppMsg() {

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value=value;
    }
}

