package com.dido.pad;

import com.dido.pad.messages.Msg;
import com.google.code.gossip.*;
import com.google.code.gossip.event.GossipState;
import com.google.code.gossip.manager.GossipManager;
import com.google.code.gossip.GossipService;
import org.apache.log4j.Logger;


import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dido-ubuntu on 05/03/16.
 */

public class Node {

    public static final Logger LOGGER = Logger.getLogger(Node.class);

    private GossipService _gossipService;
    private StorageService _storageService;

    private String ipAddress;
    private String id;

    private int portStorage;
    private int portGossip;
    private int numReplicas;


    // Empty constructor for jackson parser to JSON
    public Node() {

    }

    public int getNumReplicas() {
        return numReplicas;
    }

    public void setNumReplicas(int numReplicas) {
        this.numReplicas = numReplicas;
    }

    public Node(String ipAddress, String id, int portStorage, int portGossip, List<GossipMember> gossipMembers) {
        this(ipAddress, id, portStorage, portGossip, gossipMembers, new GossipSettings());
    }

    public Node(String ipAddress, String id, int portStorage, int portGossip, List<GossipMember> gossipMembers, GossipSettings settings) {
        this.ipAddress = ipAddress;
        this.id = id;
        this.portStorage = portStorage;
        this.portGossip = portGossip;
        try {
            _gossipService = new GossipService(ipAddress, this.portGossip, id, gossipMembers, settings, this::gossipEvent);
            _storageService = new StorageService(this, gossipMembers);
            _storageService.addServer(this);

        } catch (InterruptedException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    // Creates a Node from a GossipMember.
    // Used when a GossipMemeber goes UP, or create a Node from a GossipMember for the Hasher
    public Node(GossipMember member) {
        this.ipAddress = member.getHost();
        this.id = member.getId();
        this.portStorage = Helper.STORAGE_PORT;
        this.portGossip = member.getPort();
        _storageService = new StorageService(this); //used to sendToStorage() method
    }


    //used in the Client Cli
    public Node(String ipAddress, String id) {
        this.ipAddress = ipAddress;
        this.id = id;
        this.portStorage = Helper.STORAGE_PORT;
        _storageService = new StorageService(this);
    }


    public void start() {
        _gossipService.start();
        _storageService.start();
    }

    public GossipManager getGossipmanager() {
        return _gossipService.get_gossipManager();
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

    public void shutdown() {
        if (_gossipService != null)
            _gossipService.shutdown();
        if (_storageService != null)
            _storageService.shutdown();
    }

    public void sendToStorage(Msg msg){
        _storageService.sendToMyStorage(msg);
    }


    /* callback of gossiping procedure if a node goes UP or DOWN  */
    private void gossipEvent(GossipMember member, GossipState state) {
        switch (state) {
            case UP:
                Node nodeUP = new Node(member);
                _storageService.addServer(nodeUP);
                Node.LOGGER.info(getIpAddress() + "- UP event, node " + member.getHost() + " added to consistent hasher");
                //_storageService.manageUP(nodeUP);
                break;
            case DOWN:
                Node n = new Node(member);
                _storageService.removeServer(n);
                Node.LOGGER.info(getIpAddress() + "- DOWN event, node " + member.getHost() + " removed from consistent hasher");
                break;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return ipAddress.equals(node.ipAddress) && id.equals(node.id);

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

    public int getPortStorage() {
        return this.portStorage;

    }

    public void setPortStorage(int portStorage) {
        this.portStorage = portStorage;

    }

    public StorageService get_storageService() {
        return _storageService;
    }
}
