package com.dido.pad.datamessages;

import com.dido.pad.DataStorage;
import com.dido.pad.VectorClocks.VectorClock;
import com.dido.pad.VectorClocks.Version;
import com.dido.pad.VectorClocks.Versioned;

/**
 * Created by dido-ubuntu on 15/03/16.
 */
public class RequestSystemMsg extends AppMsg {

    private Versioned<DataStorage<?>> data;
    //private String key;

    //PUT the versioned storage data
    public  RequestSystemMsg(OPERATION op, String ipSender, int portSender, Versioned<DataStorage<?>> data){
        super(TYPE.REQUEST,op,ipSender,portSender);
        this.data = data;
    }

    //GET request System message
    public RequestSystemMsg(OPERATION op, String ipSender, int portSender){
        super(TYPE.REQUEST, op,ipSender,portSender);
        this.data = null;
    }

    public RequestSystemMsg() {
    }

    public Versioned<DataStorage<?>> getData() {
        return data;
    }
/*
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
*/
    public void setData(Versioned<DataStorage<?>> data) {
        this.data = data;
    }
}
