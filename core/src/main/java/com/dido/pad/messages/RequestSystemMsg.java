package com.dido.pad.messages;

import com.dido.pad.data.Versioned;

/**
 * Created by dido-ubuntu on 15/03/16.
 */
public class RequestSystemMsg extends AppMsg {

    private Versioned data;
    private String key;  //for GET message

    public RequestSystemMsg() {
    }

    //PUT the versioned storage data
    public  RequestSystemMsg(OP op, String ipSender, int portSender, Versioned data){
        super(TYPE.REQUEST,op,ipSender,portSender);
        this.data = data;
    }

    //GET request System message
    public RequestSystemMsg(OP op, String ipSender, int portSender, String key){
        super(TYPE.REQUEST, op,ipSender,portSender);
        this.data = null;
        this.key = key;
    }


    public Versioned getVersionedData() {
        return data;
    }
    public void setVersionedData(Versioned data) {
        this.data = data;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

}
