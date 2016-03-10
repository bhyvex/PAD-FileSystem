package com.dido.pad;

import com.dido.pad.consistenthashing.Hasher;
import com.dido.pad.consistenthashing.iHasher;
import com.dido.pad.datamessages.DataStorage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dido-ubuntu on 07/03/16.
 */
public class StorageService extends Thread{


    public static final Logger LOGGER = Logger.getLogger(StorageService.class);

    private Hasher<Node> cHasher;
    private PersisentStorage storage;
    private DatagramSocket udpServer;
    private AtomicBoolean keepRunning;

    private Node myNode;
    private int port;

    public StorageService(Node node, int port){
        this.cHasher  = new Hasher<>(1,iHasher.SHA1,iHasher.getNodeToBytesConverter());
        this.myNode = node;
        storage = new PersisentStorage();
        this.port = port;
        keepRunning = new AtomicBoolean(true);
        try {
            SocketAddress sAddress= new InetSocketAddress(node.getIpAddress(), port);
            StorageService.LOGGER.info("Storage Service succesfully initialized on port "+ port);
            StorageService.LOGGER.debug("I'm " + node.toString());
            udpServer = new DatagramSocket(sAddress);
        } catch (SocketException e) {
            StorageService.LOGGER.error(e);
            udpServer = null;
        }


    }

    public Hasher<Node> getcHasher() {
        return cHasher;
    }

    public void startStorageService(){
        this.start();
    }


    //TODO open a UDP connection and send messages:  add (data), get (data)
    @Override
    public void run(){
        while(keepRunning.get()){
            try {
                byte [] buff = new byte[udpServer.getReceiveBufferSize()];
                DatagramPacket p = new DatagramPacket(buff, buff.length);
                StorageService.LOGGER.debug("Storage Service waiting messages...");
                udpServer.receive(p);

                int packet_length = 0;
                for (int i = 0; i < 4; i++) {
                    int shift = (4 - 1 - i) * 8;
                    packet_length += (buff[i] & 0x000000FF) << shift;
                }

                byte[] json_bytes = new byte[packet_length];
                System.arraycopy(buff, 4, json_bytes, 0, packet_length);

                String receivedMessage = new String(json_bytes);
                StorageService.LOGGER.debug("Storage Service message Received from "+p.getAddress()+":"+p.getPort());
                System.out.println(receivedMessage);



                //TODO marshal message object
                //AppMsg<String> msg = new AppMsg<>(AppMsg.TYPE.REPLY,)


            } catch (IOException e) {
                StorageService.LOGGER.error(e);
                keepRunning.set(false);
            }



        }



    }
/*
    public void receive(AppMsg msg){
        switch (msg.getType()) {
            case REQUEST:
                this.mangageRequest(msg);
                break;
            case REPLY:
                break;
            case CONTROL:
                break;
        }



    }

    private void mangageRequest(AppMsg msg){
        switch (msg.getOperation()) {
            case PUT:
                //insert new data in the store
                Node node=  this.cHasher.getServerForData(msg.getPayload());
                if(node.equals(this.myNode))
                    this._dataStore.put(msg.getPayload().getKey(), (V) msg.getPayload().getValue());
                else
                    send(msg,node);
                break;
            case GET:
                break;
            case LIST:
                break;
        }
    }
*/
    //TODO send a message ( DEST, TYPE MSG, TYPE OP)
    /*
    public void send(AppMsg msg, Node n){
        n.get_storageService() receive(msg);
    }*/




}
