package com.dido.pad.cli;

import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.datamessages.AppMsg;
import com.dido.pad.datamessages.RequestAppMsg;
import com.google.code.gossip.GossipMember;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by dido-ubuntu on 24/03/16.
 */
public class Client {


    public static final Logger LOGGER = Logger.getLogger(Client.class);

    private ClientService clientService;
    private String ip;
    private String id;


    public Client(String ip, String id, ArrayList<GossipMember> startupMember){
        this.ip = ip;
        this.id = id;

        clientService = new ClientService(this, startupMember);
        clientService.start();
    }

    public void addserver(Node n){
        clientService.getcHasher().addServer(n);
    }

    public void put(String key, String value){
        Node n = clientService.getcHasher().getServerForData(key);
        RequestAppMsg msg = new RequestAppMsg<>(AppMsg.OP.PUT, key, value);
        msg.setIpSender(ip);
        n.sendToStorage( msg);
        LOGGER.info(ip +"- sent Put to "+n.getIpAddress());
    }

    public void get(String key){
        Node n = clientService.getcHasher().getServerForData(key);
        RequestAppMsg msg = new RequestAppMsg<>(AppMsg.OP.GET, key, "");
        msg.setIpSender(ip);
        n.sendToStorage(msg);
        LOGGER.info(ip +"- sent GET to "+n.getIpAddress());
    }

    public void list(String ip){
        RequestAppMsg msg = new RequestAppMsg<>(AppMsg.OP.LIST, "", "");
        clientService.send(ip, Helper.STORAGE_PORT, msg);
    }


    public String getIpAddress() {
        return ip;
    }


    public String getId() {
        return id;
    }
}
