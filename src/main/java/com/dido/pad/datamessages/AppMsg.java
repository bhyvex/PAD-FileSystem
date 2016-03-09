package com.dido.pad.datamessages;

/**
 * Created by dido-ubuntu on 09/03/16.
 */
public class AppMsg<V> {

    public enum TYPE {REQUEST, REPLY, CONTROL};
    public enum OPERATION {PUT,GET,LIST};

    private TYPE type;
    private OPERATION operation;
    private AppPayload<V> payload;

    public AppMsg(TYPE type, OPERATION op, AppPayload payload) {
        this.operation = op;
        this.type = type;
        this.payload = payload;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public OPERATION getOperation() {
        return operation;
    }

    public void setOperation(OPERATION operation) {
        this.operation = operation;
    }

    public  AppPayload getPayload(){
        return this.payload;
    }
    @Override
    public String toString() {
        return "AppMsg{" +
                ", type=" + type +
                ", operation=" + operation +
                '}';
    }
}
