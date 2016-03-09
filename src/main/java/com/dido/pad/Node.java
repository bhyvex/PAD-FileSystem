package com.dido.pad;


import com.dido.pad.consistenthashing.ConsistentHasher;
import com.dido.pad.consistenthashing.ConsistentHasherImpl;

import com.google.code.gossip.*;
import com.google.code.gossip.event.GossipListener;
import com.google.code.gossip.event.GossipState;
import org.apache.log4j.Logger;

import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 * Created by dido-ubuntu on 05/03/16.
 */
public class Node {

    public static final Logger LOGGER = Logger.getLogger(Node.class);

    private GossipService _gossipService;
    private HashingService _hashingService;

    private String ipAddress;
    private String id;


    public Node(String ipAdresss, String id){
        this.ipAddress = ipAdresss;
        this.id = id;
        this._hashingService = new HashingService();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Node(GossipMember member){
        this(member.getAddress(), member.getId());
    }

    public void addGossipService(int port, int logLevel, java.util.List<GossipMember> gossipMembers, GossipSettings settings, GossipListener listener) throws UnknownHostException, InterruptedException {
        _gossipService = new GossipService(this.ipAddress,port,this.id,LogLevel.DEBUG,gossipMembers,settings,listener);
    }

    public void startGossipService(){
        _gossipService.start();
    }

    public void shutdown(){
        _gossipService.shutdown();
    }


    public ConsistentHasher<Node, String> getConsistenHasher(){
        return _hashingService.getHasher();
    }



    /* callback of gossiping procedure append: UP or DOWN member node */
    public void  gossipEvent(GossipMember member, GossipState state) {
        switch (state) {
            case UP:
                this._hashingService.addBucket(new Node(member));
                Node.LOGGER.info("Member "+member.getAddress()+" ADDED in "+this.toString());
                break;
            case DOWN:
                Node.LOGGER.info("Member "+member.getAddress()+"  REMOVED from "+this.toString());
                try {
                    this._hashingService.removeBucket( new Node(member));
                } catch (InterruptedException e) {
                    Node.LOGGER.error(e.getMessage());
                }
                break;

        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (!ipAddress.equals(node.ipAddress)) return false;
        return id.equals(node.id);

    }

    @Override
    public int hashCode() {
        int result = ipAddress.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
