package com.dido.pad.messages;

/**
 * Created by dido-ubuntu on 26/03/16.
 */
public class ReplyClientMsg  extends AppMsg{

   // ArrayList<Node> nodes;
    String nodesIds; // String of the form " ip1:id1 ip2:id2 "

    public ReplyClientMsg(OP operation, String ipSender, int portSender, String nodesIds) {
        super(TYPE.REPLY, operation, ipSender,portSender);
        this.nodesIds= nodesIds;
    }

    public ReplyClientMsg() {

    }

    public String getNodesIds() {
        return nodesIds;
    }

    public void setNodesIds(String  nodesIds) {
        this.nodesIds = nodesIds;
    }
}
