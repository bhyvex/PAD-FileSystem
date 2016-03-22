package com.dido.pad;

import com.dido.pad.consistenthashing.DefaultFunctions;
import com.dido.pad.data.StorageData;
import com.dido.pad.data.Versioned;
import com.dido.pad.consistenthashing.Hasher;
import com.dido.pad.datamessages.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.code.gossip.GossipMember;
import org.apache.log4j.Logger;


import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dido-ubuntu on 07/03/16.
 */
public class StorageService extends Thread {


    public static final Logger LOGGER = Logger.getLogger(StorageService.class);

    public int N_REPLICAS = 2;
    public int WRITE_NODES = 1;
    public int READ_NODES = 2;

    private Hasher<Node> cHasher;
    private PersistentStorage storage;
    private DatagramSocket udpServer;
    private AtomicBoolean keepRunning;

    private List<Node> preferenceNodes;
    private Node myNode;


    public StorageService(Node node, List<GossipMember> seedNodes) {

        this.cHasher = new Hasher<>(Helper.NUM_NODES_VIRTUALS, DefaultFunctions::SHA1, DefaultFunctions::BytesConverter);
        this.myNode = node;
        storage = new PersistentStorage();

        // ADD seed nodes to the node storage service
        for (GossipMember member : seedNodes) {
            Node n = new Node(member);
            if (!cHasher.containsNode(n))
                cHasher.addServer(n);
        }

        keepRunning = new AtomicBoolean(true);
        try {
            SocketAddress sAddress = new InetSocketAddress(node.getIpAddress(), Helper.STORAGE_PORT);
            StorageService.LOGGER.info(this.myNode.getIpAddress() + "- initialized on portStorage " + Helper.STORAGE_PORT);
            StorageService.LOGGER.debug("I'm " + node.toString());
            udpServer = new DatagramSocket(sAddress);
        } catch (SocketException e) {
            StorageService.LOGGER.error(this.myNode.getIpAddress() + " - " + e);
            keepRunning.set(false);
            udpServer = null;
        }

    }

    public List<Node> getReplicasNodes(Node server, int replicas) {
        return cHasher.getPreferenceList(server, replicas);
    }

    public PersistentStorage getStorage() {
        return storage;
    }

    public void setPortStorage(int portStorage) {
        this.myNode.setPortStorage(portStorage);
    }

    public int getPortStorage() {
        return this.myNode.getPortStorage();
    }

    @JsonIgnore
    public Hasher<Node> getcHasher() {
        return cHasher;
    }

    public void addServer(Node n) {
        cHasher.addServer(n);
    }

    public void removeServer(Node n) {
        cHasher.removeServer(n);
    }

    public void sendToMe(AppMsg msg) {
        /* send  message to the same storage  node */
        //System.out.println("sendToStorage from same node "+this.ipAddress);
        this.send(myNode.getIpAddress(), getPortStorage(), msg);
    }

    private void send(String destIp, int destPort, AppMsg msg) {
        try {

            InetAddress address = InetAddress.getByName(destIp);

            if (msg.getIpSender() == null)
                msg.setIpSender(myNode.getIpAddress());

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

            // / Initialize a datagram packet with data and address
            DatagramSocket dsocket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, destPort);
            dsocket.send(packet);
            dsocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void run() {

        while (keepRunning.get()) {

            while (N_REPLICAS > cHasher.getAllNodes().size()) {
                StorageService.LOGGER.info(this.myNode.getIpAddress() + " -  Required " + N_REPLICAS + " backup node, found " + cHasher.getAllNodes().size());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //StorageService.LOGGER.info( this.myNode.getIpAddress()+" - FOUND Required "+N_REPLICAS+" backup node, Have " +cHasher.getAllNodes().size());
            preferenceNodes = cHasher.getPreferenceList(this.myNode, N_REPLICAS);

            try {
                byte[] buff = new byte[udpServer.getReceiveBufferSize()];
                DatagramPacket p = new DatagramPacket(buff, buff.length);
                StorageService.LOGGER.info(this.myNode.getIpAddress() + " - waiting messages...");
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
                        destNode.get_storageService().sendToMe(requestMsg);
                        StorageService.LOGGER.info(this.myNode.getIpAddress() + " -forwards msg to " + destNode.getIpAddress());

                    }
                }
                /* Request System  message received*/
                else if (msg instanceof RequestSystemMsg) {
                    manageSystemRequest((RequestSystemMsg) msg);
                }
                /* Reply Application message received*/
                else if (msg instanceof ReplyAppMsg) {
                    ReplyAppMsg replyMsg = (ReplyAppMsg) msg;
                    manageAppReply(replyMsg);
                }
                /* Reply System message received*/
                else if (msg instanceof ReplySystemMsg) {
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
        StorageService.LOGGER.info(this.myNode.getIpAddress() + " - REPLY SYSTEM received from " + replyMsg.getIpSender());
    }


    private void manageSystemRequest(RequestSystemMsg msg) {

        switch (msg.getOperation()) {
            case PUT:
                Versioned vData = msg.getVersionedData();
                String key = vData.getData().getKey(); // key of the data
                StorageService.LOGGER.info(this.myNode.getIpAddress() + " - PUT SystemMsg received key:" + key);

                //TODO set the Primary master node into the vData ???
                storage.put(vData);

                send(msg.getIpSender(), Helper.QUORUM_PORT, new ReplySystemMsg(AppMsg.OPERATION.OK, myNode.getIpAddress(), Helper.STORAGE_PORT + 1, "PUT Succesfully QUORUM"));
                    /*  else{
                   Versioned myVData = storage.get(key); //my version of the data
<<<<<<< HEAD:src/main/java/com/dido/pad/StorageService.java
                   myVData.getVersion()
=======
                   myVData.getVersion()
>>>>>>> e3a879c91af528482e9b717aa9af5d0780951f7f:core/src/main/java/com/dido/pad/StorageService.java
                    }*/

                break;
            case GET:
                StorageService.LOGGER.info(this.myNode.getIpAddress() + " - GET SystemMsg received ");
                if (storage.containsKey(msg.getKey())) {
                    Versioned myData = storage.get(msg.getKey());
                    ReplySystemMsg reply = new ReplySystemMsg(AppMsg.OPERATION.OK, myNode.getIpAddress(), Helper.STORAGE_PORT, myData);
                    //TODO change from Storage-Port to Quorum Port (Read quorum)
                    send(msg.getIpSender(), Helper.QUORUM_PORT, reply);
                } else {
                    String info = myNode.getIpAddress() + " - data is not present into my storage";
                    RequestSystemMsg replyErr = new RequestSystemMsg(AppMsg.OPERATION.ERR, msg.getIpSender(), Helper.STORAGE_PORT, info);
                    send(msg.getIpSender(), Helper.QUORUM_PORT, replyErr);
                }
                break;
            case LIST:
                break;
        }
    }

    private void manageAppReply(ReplyAppMsg msg) {
        switch (msg.getOperation()) {
            case OK:
                StorageService.LOGGER.info(this.myNode.getIpAddress() + " - REPLY  OK " + msg.getMsg() + " from " + msg.getIpSender());
                break;
            case ERR:
                StorageService.LOGGER.info(this.myNode.getIpAddress() + " - REPLY  ERR " + msg.getMsg());
                break;
        }
    }

    /**
     * The coordinator of the request manages the application message
     * sent by a client.
     *
     * @param msg
     */

    private void manageAppRequest(RequestAppMsg<?> msg) {
        switch (msg.getOperation()) {
//<<<<<<< HEAD:src/main/java/com/dido/pad/StorageService.java
            case PUT:
                StorageService.LOGGER.info(this.myNode.getIpAddress() + " - RECEIVED MSG " + msg.getOperation() + " <" + msg.getKey() + ":" + msg.getValue() + "> from " + msg.getIpSender());
                if (storage.containsKey(msg.getKey())) {// UPDATE data  Version
                    Versioned d = storage.get(msg.getKey());
                    d.setData(new StorageData<>(msg.getKey(), msg.getValue()));
                    d.getVersion().increment(myNode.getIpAddress());
                    myNode.get_storageService().sendToMe(new ReplyAppMsg(AppMsg.OPERATION.OK, " Updated succesfully key:" + msg.getKey()));
                } else { // PUT new object
                    Versioned vData = new Versioned(new StorageData<>(msg.getKey(), msg.getValue()));
                    vData.getVersion().increment(myNode.getIpAddress());
                    this.storage.put(vData);
                    StorageService.LOGGER.info(this.myNode.getIpAddress() + " - Inserted <" + msg.getKey() + ":" + msg.getValue() + "> into local database");

                    //TODO send ok before Quorum Request :  writable first policy
                    askQuorum(vData, Helper.QUORUM_PORT, AppMsg.OPERATION.PUT);

                    send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.OK, " PUT  <" + msg.getKey() + ":" + msg.getValue() + ">"));
//=======
//             case PUT:
//                StorageService.LOGGER.info( this.myNode.getIpAddress()+" - RECEIVED MSG "+msg.getOperation() +" <" + msg.getKey()+":"+msg.getValue()+"> from "+msg.getIpSender());
//
//                 if(storage.containsKey(msg.getKey())){// UPDATE data  Version
//                    Versioned d = storage.get(msg.getKey());
//                    d.getData().setValue(msg.getValue());
//                    d.getVersion().incremenetVersion(myNode.getIpAddress());
//                    myNode.sendToStorageNode(new ReplyAppMsg(AppMsg.OPERATION.OK, " Updated succesfully <key:"+msg.getKey() +":"+msg.getValue()+">"));
//                }
//                else{ // PUT new object
//                     Versioned vData = new Versioned<>(new StorageData(msg.getKey(), msg.getValue()));
//                     vData.getVersion().incremenetVersion(myNode.getIpAddress());
//                     this.storage.put(vData);
//                     StorageService.LOGGER.info(this.myNode.getIpAddress() + " - Inserted <" + msg.getKey() + ":" + msg.getValue() + "> into local database");
//
//                     askQuorum(vData, Helper.QUORUM_PORT, AppMsg.OPERATION.PUT);
//
//                     myNode.send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.OK, " PUT  <" + msg.getKey() + ":" + msg.getValue() + ">"));
//>>>>>>> e3a879c91af528482e9b717aa9af5d0780951f7f:core/src/main/java/com/dido/pad/StorageService.java
                }
                break;
            case GET:
                String key = msg.getKey();
                StorageService.LOGGER.debug(this.myNode.getIpAddress() + " - RECEIVED MSG " + msg.getOperation() + "<" + key + ">");
                if (storage.containsKey(key)) {
                    Versioned vdata = storage.get(key);

                    List<ReplySystemMsg> replies = askQuorum(vdata,Helper.QUORUM_PORT, AppMsg.OPERATION.GET);
                    for (ReplySystemMsg r :
                            replies) {
                        System.out.print(r.getData().getVersion());

                    }
                    //4) merge version
                    //5) sent reconcilied version
                    //  myNode.send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.OK, " GET "+ vdata.getData().toString()));
                } else {
                    send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.ERR, " GET key not found"));
                }
                break;
            case LIST:
                HashMap<String, Versioned> db = this.storage.getStorage();
                if (!db.isEmpty()) {
                    send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.OK, " LIST " + db.toString()));
                } else {
                    send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OPERATION.ERR, " LIST empty data database"));
                }

                break;
        }
    }


    public List<ReplySystemMsg> askQuorum(Versioned vData, int listenPort, AppMsg.OPERATION op) {
        RequestSystemMsg reqQuorum;

        if (op.equals(AppMsg.OPERATION.PUT))
            reqQuorum = new RequestSystemMsg(op, myNode.getIpAddress(), 0, vData);
        else {
            reqQuorum = new RequestSystemMsg(op, myNode.getIpAddress(), 0, vData.getData().getKey());
        }

        try {
            DatagramSocket dsocket = new DatagramSocket(listenPort);

            // send msg to all the  nodes in the preference list
            for (Node backup : preferenceNodes) {

                ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
                byte[] jsonByte = mapper.writeValueAsBytes(reqQuorum);

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
                StorageService.LOGGER.info(this.myNode.getIpAddress() + " - SENT to Backup for Quorum " + reqQuorum.getType() + "  " + backup.getIpAddress());
            }
            dsocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return _waitQuorum(reqQuorum.getOperation(), listenPort);
    }

    private List<ReplySystemMsg> _waitQuorum(AppMsg.OPERATION op, int listenPort) {

        DatagramSocket udpQuorum;  //server listen Quorum response

        int numResponses = (op == AppMsg.OPERATION.PUT) ? WRITE_NODES : READ_NODES;
        ArrayList<ReplySystemMsg> replies = new ArrayList<>();

        try {
            udpQuorum = new DatagramSocket(listenPort);
            //TODO insert timeout into configuration file
            udpQuorum.setSoTimeout(5000);

            //wait the response
            for (int j = 0; j < numResponses; j++) {
                byte[] buff = new byte[udpQuorum.getReceiveBufferSize()];
                DatagramPacket p = new DatagramPacket(buff, buff.length);
                StorageService.LOGGER.info(this.myNode.getIpAddress() + " - waiting " + numResponses + " QUORUM msg response...");

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
                ReplySystemMsg msgQuorum = mapper.readValue(receivedMessage, ReplySystemMsg.class);
                StorageService.LOGGER.info(this.myNode.getIpAddress() + " - RECEIVED QUORUM MSG from " + msgQuorum.getIpSender());
                replies.add(msgQuorum);

            }
            udpQuorum.close();
        } catch (SocketException e) {
            StorageService.LOGGER.error(this.myNode.getIpAddress() + " - " + e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return replies;
    }

    public void shutdown() {
        LOGGER.info(this.myNode.getIpAddress() + "Storage service has been shutdown...");
        this.udpServer.close();
    }


}
