package com.dido.pad.datamessages;

import com.dido.pad.Node;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;


/**
 * Created by dido-ubuntu on 09/03/16.
 */


@JsonTypeInfo(
        use=JsonTypeInfo.Id.CLASS,
        include=JsonTypeInfo.As.PROPERTY,
        property = "type"
)


@JsonSubTypes(@JsonSubTypes.Type(value=RequestAppMsg.class))

public class AppMsg {

    public enum TYPE {REQUEST, REPLY};
    public enum OPERATION {PUT,GET,LIST, OK, ERR};

    private TYPE type;
    private OPERATION operation;
    private Node originalSender;

    public AppMsg() {
    }

    public AppMsg(TYPE type, OPERATION operation, Node originalSender) {
        this.type = type;
        this.operation = operation;
        this.originalSender = originalSender;
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

    public Node getOriginalSender() {
        return originalSender;
    }

    public void setOriginalSender(Node originalSender) {
        this.originalSender = originalSender;
    }

}
