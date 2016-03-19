package com.dido.pad;

import com.dido.pad.VectorClocks.Versioned;
import com.dido.pad.consistenthashing.Hasher;
import com.dido.pad.consistenthashing.iHasher;
import com.dido.pad.datamessages.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.code.gossip.GossipMember;
import org.apache.log4j.Logger;


import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dido-ubuntu on 07/03/16.
 */


public class StorageService extends Thread{


    public static final Logger LOGGER = Logger.getLogger(StorageService.class);

    public int N_REPLICAS = 2;
    public int WRITE_NODES = 2;
    public int READ_NODES = 1;

    private Hasher<Node> cHasher;
    private PersisentStorage storage;
    private DatagramSocket udpServer;
    private AtomicBoolean keepRunning;
   // private int numReplicas;
    private List<Node> preferenceNodes;
    private Node myNode;


    public StorageService(Node node, List<GossipMember> seedNodes){
        //TODO number of virtual nodes to be put into configutaion file
        int numberVirtual = 1;
        this.cHasher  = new Hasher<Node>(numberVirtual,iHasher.SHA1,iHasher.getNodeToBytesConverter());
        this.myNode = node;
        storage = new PersisentStorage();


        // ADD seed nodes to the node storage service
        for (GossipMember member : seedNodes) {
            Node n = new Node(member);
            if(!cHasher.containsNode(n))
                        cHasher.addServer(n);
                //addServer(new Node(member));
        }

        keepRunning = new AtomicBoolean(true);
        try {
            SocketAddress sAddress= new InetSocketAddress(node.getIpAddress(), Helper.STORAGE_PORT);
            StorageService.LOGGER.info(this.myNode.getIpAddress()+ "- Storage Service succesfully initialized on portStorage "+ Helper.STORAGE_PORT);
            StorageService.LOGGER.debug("I'm " + node.toString());
            udpServer = new DatagramSocket(sAddress);
        } catch (SocketException e) {
            StorageService.LOGGER.error(this.myNode.getIpAddress()+ " - "+ e);
            keepRunning.set(false);
            udpServer = null;
        }

    }

    public List<Node> getReplicasNodes(Node server, int replicas){
       return cHasher.getPreferenceList(server, replicas);
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
        while(N_REPLICAS > cHasher.getAllNodes().size() ){
            StorageService.LOGGER.info( this.myNode.getIpAddress()+" - Waiting ... Required "+N_REPLICAS+" backup node, Have " +cHasher.getAllNodes().size());
            try {
               Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        StorageService.LOGGER.info( this.myNode.getIpAddress()+" - FOUND Required "+N_REPLICAS+" backup node, Have " +cHasher.getAllNodes().size());
        preferenceNodes = cHasher.getPreferenceList(this.myNode, N_REPLICAS);

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

                /* Request application message received */
                if (msg instanceof RequestAppMsg<?>) {
                    RequestAppMsg requestMsg = (RequestAppMsg) msg;
                    String key = requestMsg.getKey();
                    Node destNode = this.cHasher.getServerForData(requestMsg.getKey());
                    if (destNode.equals(this.myNode)) { /*store in my database, I'm the master*/
                        manageAppRequest(requestMsg);
                    } else {                            /*forward message to another node*/
                        destNode.sendToStorageNode(requestMsg);
                        StorageService.LOGGER.info( this.myNode.getIpAddress()+" -forwards msg to "+destNode.getIpAddress());

                    }
                }
                /* Request System  message received*/
                else if(msg instanceof RequestSystemMsg) {
                    manageSystemRequest((RequestSystemMsg)msg);
                }
                /* Reply Application message received*/
                else if (msg instanceof  ReplyAppMsg){
                     ReplyAppMsg replyMsg = (ReplyAppMsg) msg;
                     manageAppReply(replyMsg);
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


    private void manageSystemRequest(RequestSystemMsg msg) {

        switch (msg.getOperation()) {
            case PUT:
                Versioned vData = msg.getVersionedData();
                String key = vData.getData().getKey(); // key of the data
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - PUT SystemMsg received key:" +key);

                //if(!this.storage.containsKey(key))
                //TODO never recevied a data that is already present ?
                //TODO set the Primary master node into the vData ???
                storage.put(vData);
                myNode.send(msg.getIpSender(),Helper.STORAGE_PORT+1, new ReplySystemMsg(AppMsg.OPERATION.OK,myNode.getIpAddress(),Helper.STORAGE_PORT+1,"PUT SUccesful QUIRUM"));
                    /*  else{
                   Versioned myVData = storage.get(key); //my version of the data
                   myVData.getVectorclock()
                    }*/
                break;
            case GET:
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - GET SystemMsg received ");
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

    private void manageAppReply(ReplyAppMsg msg){
        switch (msg.getOperation()) {
            case OK:
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - REPLY  OK "+msg.getMsg()+" from "+msg.getIpSender());
                break;
            case ERR:
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - REPLY  ERR "+msg.getMsg());
                break;
        }
    }

    /**
     * The coordinator of the request manages the application message
     * sent by a client.
     * @param msg
     */

    private void manageAppRequest(RequestAppMsg<?> msg){
        switch (msg.getOperation()) {
             case PUT: // PUT or UPDATE a data
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - RECEIVED MSG "+msg.getOperation() +" <" + msg.getKey()+":"+msg.getValue()+"> from "+msg.getIpSender());
                 if(storage.containsKey(msg.getKey())){// UPDATE data and Version
                    Versioned d = storage.get(msg.getKey());
                    d.getData().setValue(msg.getValue());
                    d.getVectorclock().incremenetVersion(myNode.getIpAddress());
                    myNode.sendToStorageNode(new ReplyAppMsg(AppMsg.OPERATION.OK, " Update succesfully key:"+msg.getKey()));
                }
                else{ // PUT new object
                     Versioned vData = new Versioned<>(new StorageData(msg.getKey(), msg.getValue()));
                     vData.getVectorclock().incremenetVersion(myNode.getIpAddress());
                     this.storage.put(vData);
                     StorageService.LOGGER.info(this.myNode.getIpAddress() + " - Inserted <" + msg.getKey() + ":" + msg.getValue() + "> into local database");

                     sentPutQuorum(vData);  //with quorum (at least W_NODES must write the data)

                     myNode.send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.OK, " PUT  <" + msg.getKey() + ":" + msg.getValue() + ">"));
                }
                break;
            case GET:
                String key = msg.getKey();
                StorageService.LOGGER.debug( this.myNode.getIpAddress()+" - RECEIVED MSG "+msg.getOperation()+"<"+ key+">");
                if(storage.containsKey(key)) {
                    //Versioned<?> vdata = storage.get(key);
                    //1) sends read to backup nodes
                    sentGetQuorum(key);


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
    private void sentGetQuorum(String key){
        RequestSystemMsg reqGet = new RequestSystemMsg(AppMsg.OPERATION.GET, myNode.getIpAddress(),Helper.STORAGE_PORT,key);
        for (Node bkup: preferenceNodes) {
            myNode.send(bkup.getIpAddress(), Helper.STORAGE_PORT, reqGet);
            StorageService.LOGGER.info( this.myNode.getIpAddress()+" - SENT GET to Backup node "+ bkup.getIpAddress());
        }

    }

    private void sentPutQuorum(Versioned vData) {

        RequestSystemMsg sysMsg = new RequestSystemMsg(AppMsg.OPERATION.PUT, myNode.getIpAddress(), Helper.STORAGE_PORT, vData);

        askReadQuorum(sysMsg, Helper.STORAGE_PORT + 1);

    }

    public boolean askReadQuorum(RequestSystemMsg msg, int listenPort) {


        try {
            DatagramSocket dsocket = new DatagramSocket(listenPort);

            // send msg to all the  nodes in the preference list
            for (Node backup : preferenceNodes) {
                ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
                byte[] jsonByte = mapper.writeValueAsBytes(msg);

                int packet_length = jsonByte.length;
                // Convert the packet length to the byte representation of the int.
                byte[] length_bytes = new byte[4];
                length_bytes[0] = (byte) (packet_length >> 24);
                length_bytes[1] = (byte) ((packet_length << 8) >> 24);
                length_bytes[2] = (byte) ((packet_length << 16) >> 24);
                length_bytes[3] = (byte) ((packet_length << 24) >> 24);

                ByteBuffer byteBuffer = ByteBuffer.allocate(4 + jsonByte.length);
                byteBuffer.put(length_bytes);
                byteBuffer.put(jsonByte);
                byte[] buf = byteBuffer.array();

                InetAddress destAddress = InetAddress.getByName(backup.getIpAddress());
                DatagramPacket packet = new DatagramPacket(buf, buf.length, destAddress, Helper.STORAGE_PORT);

                dsocket.send(packet);
                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - SENT to Backup for Quorum "+msg.getType() +"  "+ backup.getIpAddress());

            }
            dsocket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DatagramSocket udpQuorum;
        try {
            udpQuorum= new DatagramSocket(listenPort);
            udpQuorum.setSoTimeout(5000);

        //wait the response
        for(int j =0; j < this.WRITE_NODES; j++){
            byte [] buff = new byte[udpQuorum.getReceiveBufferSize()];
            DatagramPacket p = new DatagramPacket(buff, buff.length);
            StorageService.LOGGER.info( this.myNode.getIpAddress()+" - Storage Service waiting QUORUM msg response...");

            udpQuorum.receive(p);

            int packet_length = 0;
            for (int i = 0; i < 4; i++) {
                int shift = (4 - 1 - i) * 8;
                packet_length += (buff[i] & 0x000000FF) << shift;
            }

            byte[] json_bytes = new byte[packet_length];
            System.arraycopy(buff, 4, json_bytes, 0, packet_length);
            String receivedMessage = new String(json_bytes);

            ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
            AppMsg msgQuorum = mapper.readValue(receivedMessage, AppMsg.class);
            StorageService.LOGGER.info( this.myNode.getIpAddress()+" - RECEIVED QUORUM MSG from "+msgQuorum.getIpSender());

        }
        } catch (SocketException e) {
            StorageService.LOGGER.error(this.myNode.getIpAddress()+ " - "+ e);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void shutdown(){
        LOGGER.info(this.myNode.getIpAddress()+"Storage service has been shutdown...");
        this.udpServer.close();
    }


}
