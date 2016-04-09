package com.dido.pad.messages;

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

@JsonSubTypes({
        @JsonSubTypes.Type(value = RequestAppMsg.class, name = "reqAppMsg"),
        @JsonSubTypes.Type(value = ReplyAppMsg.class, name = "repAppMsg"),
        @JsonSubTypes.Type(value = RequestSystemMsg.class, name = "reqCtrMsg"),
        @JsonSubTypes.Type(value = ReplySystemMsg.class, name = "repCtrMsg"),
        @JsonSubTypes.Type(value = RequestConflictMsg.class, name = "conflictMsg"),
        @JsonSubTypes.Type(value = ReplyConflictMsg.class, name = "replyconflictMsg"),
        @JsonSubTypes.Type(value = RequestClientMsg.class, name = "reqClient"),
        @JsonSubTypes.Type(value = ReplyClientMsg.class, name = "replyClient")
}
)

public class Msg {

    public enum TYPE {REQUEST, REPLY}
    public enum OP {PUT, GET, LIST, OK, ERR, DSCV ,RM}  //DSCV = discovering for client request of gossipMembers in a SystemMsg

    private TYPE type;
    private OP operation;
    private String ipSender;
    private int portSender;

    public Msg(TYPE type, OP operation, String ipSender, int portSender) {
        this.type = type;
        this.operation = operation;
        this.ipSender = ipSender;
        this.portSender = portSender;
    }
    public Msg(TYPE type, OP operation){
        this.type = type;
        this.operation = operation;
    }
    public Msg() {

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

    public OP getOperation() {
        return operation;
    }

    public void setOperation(OP operation) {
        this.operation = operation;
    }

}
