package com.dido.pad.datamessages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Created by dido-ubuntu on 09/03/16.
 */


@JsonTypeInfo(
        use=JsonTypeInfo.Id.CLASS,
        include=JsonTypeInfo.As.PROPERTY,
        property = "type"
)

@JsonSubTypes( {
        @JsonSubTypes.Type(value=RequestAppMsg.class,name ="requestMsg"),
        @JsonSubTypes.Type(value=ReplyAppMsg.class,name ="replyMsg"),
        @JsonSubTypes.Type(value=RequestSystemMsg.class,name ="cotrolMsg")
})

public class AppMsg {

    public enum TYPE {REQUEST, REPLY}
    public enum OPERATION {PUT,GET, LIST, OK, ERR}

    private TYPE type;
    private OPERATION operation;
    private String ipSender;
    private int portSender;

    public AppMsg(TYPE type, OPERATION operation, String ipSender, int portSender) {
        this.type = type;
        this.operation = operation;
        this.ipSender = ipSender;
        this.portSender = portSender;
    }
    public AppMsg(TYPE type, OPERATION operation){
        this.type = type;
        this.operation = operation;
    }
    public AppMsg() {

    }


    public String getIpSender() {
        return ipSender;
    }

    public void setIpSender(String ipSender) {
        this.ipSender = ipSender;
    }

    public int getPortSender() {
        return portSender;
    }

    public void setPortSender(int portSender) {
        this.portSender = portSender;
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

}
