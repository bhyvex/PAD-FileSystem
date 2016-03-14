package com.dido.pad;

import com.dido.pad.consistenthashing.Hasher;
import com.dido.pad.consistenthashing.iHasher;
import com.dido.pad.datamessages.AppMsg;
import com.dido.pad.datamessages.ReplyAppMsg;
import com.dido.pad.datamessages.RequestAppMsg;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.apache.log4j.Logger;


import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
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

    public StorageService(Node node){
        this.cHasher  = new Hasher<Node>(1,iHasher.SHA1,iHasher.getNodeToBytesConverter());
        this.myNode = node;
        storage = new PersisentStorage();
        keepRunning = new AtomicBoolean(true);
        try {
            SocketAddress sAddress= new InetSocketAddress(node.getIpAddress(), getPortStorage());
            StorageService.LOGGER.info(this.myNode.getIpAddress()+ "- Storage Service succesfully initialized on portStorage "+ getPortStorage());
            StorageService.LOGGER.debug("I'm " + node.toString());
            udpServer = new DatagramSocket(sAddress);
        } catch (SocketException e) {
            StorageService.LOGGER.error(this.myNode.getIpAddress()+ " - "+ e);
            keepRunning.set(false);
            udpServer = null;
        }


    }

    public PersisentStorage getStorage() {
        return storage;
    }

    public void setPortStorage(int portStorage) {
        this.myNode.setPortStorage(portStorage);
    }

    public int getPortStorage(){
        return this.myNode.getPortStorage();
    }

    @JsonIgnore
    public Hasher<Node> getcHasher() {
        return cHasher;
    }

    public void startStorageService(){
        this.start();
    }


    public void addServer(Node n){
        this.cHasher.addServer(n);
    }

    public void removeServer(Node n){
        this.cHasher.removeServer(n);
    }

    @Override
    public void run(){
        while(keepRunning.get()){
            try {
                byte [] buff = new byte[udpServer.getReceiveBufferSize()];
                DatagramPacket p = new DatagramPacket(buff, buff.length);
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - Storage Service waiting messages...");
                udpServer.receive(p);

                int packet_length = 0;
                for (int i = 0; i < 4; i++) {
                    int shift = (4 - 1 - i) * 8;
                    packet_length += (buff[i] & 0x000000FF) << shift;
                }

                byte[] json_bytes = new byte[packet_length];
                System.arraycopy(buff, 4, json_bytes, 0, packet_length);
                String receivedMessage = new String(json_bytes);

                ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
                AppMsg msg = mapper.readValue(receivedMessage, AppMsg.class);

                if (msg instanceof RequestAppMsg<?>) {
                    RequestAppMsg requestMsg = (RequestAppMsg) msg;
                    String key = requestMsg.getKey();
                    Node destNode = this.cHasher.getServerForData(requestMsg.getKey());
                    if (destNode.equals(this.myNode)) { /*store in my database*/
                        mangageRequest(requestMsg);
                    } else {                            /*send mesage to another node*/
                        destNode.sendToStorageNode(requestMsg);
                        StorageService.LOGGER.info( this.myNode.getIpAddress()+" - redicrect msg to "+destNode.getIpAddress());

                    }
                }
                else{       /* reply message*/
                 if(msg instanceof  ReplyAppMsg){
                     ReplyAppMsg replyMsg = (ReplyAppMsg) msg;
                     manageReply(replyMsg);
                 }
                }


            } catch (IOException e) {
                StorageService.LOGGER.error(e);
                udpServer.close();
                keepRunning.set(false);
            }
        }
        shutdown();

    }

    private void manageReply(ReplyAppMsg msg){
        switch (msg.getOperation()) {
            case OK:
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - REPLY  OK "+msg.getMsg());
                break;
            case ERR:
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - REPLY  ERR "+msg.getMsg());
                break;
        }
    }

    private void mangageRequest(RequestAppMsg<?> msg){
        switch (msg.getOperation()) {
            case PUT:
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - RECEIVED MSG "+msg.getOperation() +" <" + msg.getKey()+":"+msg.getValue()+"> from "+msg.getIpSender());
                this.storage.put(new DataStorage(msg.getKey(), msg.getValue()));
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - Inserted <" + msg.getKey()+":"+msg.getValue()+"> into local database");

               // if(!msg.getIpSender().equals(myNode.getIpAddress())){
                    String info = "PUT <" + msg.getKey() + ":" + msg.getValue() + ">";
                    myNode.send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.OK, " PUT " +info));
                //}
                break;
            case GET:
                String key = msg.getKey();
                StorageService.LOGGER.debug( this.myNode.getIpAddress()+" - RECEIVED MSG "+msg.getOperation()+"<"+ key+">");
                if(storage.containsKey(key)) {
                    DataStorage<?> data = storage.get(key);
                    myNode.send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.OK, " GET "+ data.toString()));
                }
                else {
                    myNode.send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.ERR, " GET key not found"));
                }
                break;
            case LIST:
                HashMap<String, DataStorage<?>> db = this.storage.getStorage();
                if(!db.isEmpty()) {
                    myNode.send(msg.getIpSender(),Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.OK, " LIST "+ db.toString()));
                }
                else{
                    myNode.send(msg.getIpSender(),Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.ERR, " LIST empty data databse"));
                }

                break;
        }
    }


    public void shutdown(){
        LOGGER.info(this.myNode.getIpAddress()+"Storage service has been shutdown...");
        this.udpServer.close();
    }


}
