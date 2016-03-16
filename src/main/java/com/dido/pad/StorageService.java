package com.dido.pad;

import com.dido.pad.VectorClocks.Versioned;
import com.dido.pad.consistenthashing.Hasher;
import com.dido.pad.consistenthashing.iHasher;
import com.dido.pad.datamessages.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.apache.log4j.Logger;


import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
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
    private int numReplicas;
    private List<Node> backupNodes;
    private Node myNode;

    /*
    public StorageService(Node n, int numReplicas){
        this(n);
        this.numReplicas = numReplicas;

    }
    */

    public StorageService(Node node){
        //TODO number of virtual nodes to be putte into configutaion file
        int numberVirtual = 1;
        this.cHasher  = new Hasher<Node>(numberVirtual,iHasher.SHA1,iHasher.getNodeToBytesConverter());
        this.myNode = node;
        storage = new PersisentStorage();
        //TODO in the configuration file the number of replicas node
        this.numReplicas = 2;
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

    public List<Node> getReplicasNodes(Node server, int replicas){
       return cHasher.getNextServers(server, replicas);
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

                /* Request application message received /*/
                if (msg instanceof RequestAppMsg<?>) {
                    RequestAppMsg requestMsg = (RequestAppMsg) msg;
                    String key = requestMsg.getKey();
                    Node destNode = this.cHasher.getServerForData(requestMsg.getKey());
                    if (destNode.equals(this.myNode)) { /*store in my database, I'm the master*/
                        manageRequest(requestMsg);
                    } else {                            /*forward message to another node*/
                        destNode.sendToStorageNode(requestMsg);
                        StorageService.LOGGER.info( this.myNode.getIpAddress()+" -forwards msg to "+destNode.getIpAddress());

                    }
                }
                /* Request System  message received*/
                else if(msg instanceof RequestSystemMsg) {
                    manageSystemMsg((RequestSystemMsg)msg);
                }
                /* Reply Application message received*/
                else if (msg instanceof  ReplyAppMsg){
                     ReplyAppMsg replyMsg = (ReplyAppMsg) msg;
                     manageReply(replyMsg);
                }
                /* Reply System message received*/
                else if (msg instanceof  ReplySystemMsg){
                    ReplySystemMsg replyMsg = (ReplySystemMsg) msg;
                    manageSystemReply(replyMsg);
                }

            } catch (IOException e) {
                StorageService.LOGGER.error(e);
                udpServer.close();
                keepRunning.set(false);
            }
        }
        shutdown();

    }

    private void manageSystemReply(ReplySystemMsg replyMsg) {
        StorageService.LOGGER.info( this.myNode.getIpAddress()+" - REPLY SYSTEM received from "+ replyMsg.getIpSender());
    }

    private void manageSystemMsg(RequestSystemMsg msg) {

        switch (msg.getOperation()) {
            case PUT:
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - PUT SystemMsg received from ");
                Versioned vData = msg.getVersionedData();
                String key = vData.getData().getKey(); // key of the data
                //if(!this.storage.containsKey(key))
                //TODO never recevied a data that is already present ?
                //TODO set the Primary master node into the vData ???
                    storage.put(vData);
              /*  else{
                   Versioned myVData = storage.get(key); //my version of the data
                   myVData.getVectorclock()
               }*/
                break;
            case GET:
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" -GET SystemMsg received ");
                if(storage.containsKey(msg.getKey())){
                    Versioned myData = storage.get(msg.getKey());
                    ReplySystemMsg reply = new ReplySystemMsg(AppMsg.OPERATION.OK, myNode.getIpAddress(), Helper.STORAGE_PORT,myData);
                    myNode.send(msg.getIpSender(), Helper.STORAGE_PORT,reply);
                }
                else{
                    String info = myNode.getIpAddress()+" - data is not present into my storage";
                    RequestSystemMsg replyErr = new RequestSystemMsg(AppMsg.OPERATION.ERR, msg.getIpSender(),Helper.STORAGE_PORT,info);
                    myNode.send(msg.getIpSender(),Helper.STORAGE_PORT, replyErr);
                }
                break;
            case LIST:
                break;
        }
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

    private void manageRequest(RequestAppMsg<?> msg){
        switch (msg.getOperation()) {
            case PUT:
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - RECEIVED MSG "+msg.getOperation() +" <" + msg.getKey()+":"+msg.getValue()+"> from "+msg.getIpSender());

                // 1)create new versioned (data + vector clock)
                Versioned vData = new Versioned<StorageData>(new StorageData(msg.getKey(), msg.getValue()));
                this.storage.put(vData);
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - Inserted <" + msg.getKey()+":"+msg.getValue()+"> into local database");


                vData.getVectorclock().incremenetVersion(myNode.getIpAddress());
                //2) sent versioned data to backups nodes
                sentToBackupNodes(vData);
                //TODO wait repsonse from backups
                //3) wait response to the backups

                //4) reply succesful to the clent
                myNode.send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.OK, " PUT  <" + msg.getKey() + ":" + msg.getValue() + ">"));

                break;
            case GET:
                String key = msg.getKey();
                StorageService.LOGGER.debug( this.myNode.getIpAddress()+" - RECEIVED MSG "+msg.getOperation()+"<"+ key+">");
                if(storage.containsKey(key)) {
                    //Versioned<?> vdata = storage.get(key);
                    //1) sends read to backup nodes
                    requestToBackupNodes(key);


                    //2) wait response
                    //3)select highest VC from the returned vdata
                    //4) merge version
                    //5) sent reconcilied version
                  //  myNode.send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.OK, " GET "+ vdata.getData().toString()));
                }
                else {
                    myNode.send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.ERR, " GET key not found"));
                }
                break;
            case LIST:
                HashMap<String, Versioned<?>> db = this.storage.getStorage();
                if(!db.isEmpty()) {
                    myNode.send(msg.getIpSender(),Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.OK, " LIST "+ db.toString()));
                }
                else{
                    myNode.send(msg.getIpSender(),Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.ERR, " LIST empty data database"));
                }

                break;
        }
    }

    /**
     * Send a GET message to the backup nodes in order to receive the different versions for a data.
     * @param key
     */
    private void requestToBackupNodes(String key){
        //TODO : maybe is not the right position to get the backups servers ...
        backupNodes = cHasher.getNextServers(this.myNode,numReplicas);
        RequestSystemMsg reqGet = new RequestSystemMsg(AppMsg.OPERATION.GET, myNode.getIpAddress(),Helper.STORAGE_PORT,key);
        for (Node bkup: backupNodes) {
            myNode.send(bkup.getIpAddress(), Helper.STORAGE_PORT, reqGet);
            StorageService.LOGGER.info( this.myNode.getIpAddress()+" - SENT GET to Backup node "+ bkup.getIpAddress());
        }

    }

    private void sentToBackupNodes(Versioned vData) {
        //TODO : maybe is not the right position to get the backups servers ...
        backupNodes = cHasher.getNextServers(this.myNode,numReplicas);

        RequestSystemMsg sysMsg = new RequestSystemMsg(AppMsg.OPERATION.PUT,myNode.getIpAddress(),Helper.STORAGE_PORT,vData);
        for (Node bkup: backupNodes) {
                myNode.send(bkup.getIpAddress(), Helper.STORAGE_PORT, sysMsg);
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - SENT PUT to Backup node "+ bkup.getIpAddress());
            }
    }


    public void shutdown(){
        LOGGER.info(this.myNode.getIpAddress()+"Storage service has been shutdown...");
        this.udpServer.close();
    }


}
