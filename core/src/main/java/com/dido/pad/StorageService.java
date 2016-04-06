package com.dido.pad;

import com.dido.pad.hashing.DefaultFunctions;
import com.dido.pad.data.StorageData;
import com.dido.pad.data.Versioned;
import com.dido.pad.hashing.Hasher;
import com.dido.pad.messages.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.code.gossip.GossipMember;
import org.apache.log4j.Logger;


import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dido-ubuntu on 07/03/16.
 */
public class StorageService extends Thread {

    public static final Logger LOGGER = Logger.getLogger(StorageService.class);

    public int N_REPLICAS = 2;  //  include also the node master ( N=2 , mode master + successive node)
    public int WRITE_NODES = 2; //1 //number of nodes after the master tha must return a write response
    public int READ_NODES = 1;   //2  //number of nodes that must read that must return a read response

    private Hasher<Node> cHasher;
    private PersistentStorage storage;
    private DatagramSocket udpServer;
    private AtomicBoolean keepRunning;

    private List<Node> preferenceNodes; // preference nodes = list of backup nodes (xclude the node itself)
                                        //list of nodes that is responsible for storing a particular key is
    private Node myNode;


    public StorageService(Node node){ // only for the creation of new node from a GossipMember
        this.myNode = node;
    }

    public StorageService(Node node, List<GossipMember> seedNodes) {

        this.cHasher = new Hasher<>(Helper.NUM_NODES_VIRTUALS, DefaultFunctions::SHA1, DefaultFunctions::BytesConverter);
        this.myNode = node;

        storage = new PersistentStorage(myNode.getId(),Helper.CLEAR_DATABASE_INTO_NODE);

        // ADD seed nodes to the node storage service
        for (GossipMember member : seedNodes) {
            Node n = new Node(member);
            //TODO problem change saGossipMember to a Node
          //  if (!cHasher.containsNode(n))
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
        return cHasher.getNextServers(server, replicas);
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


    @Override
    public void run() {
        while (N_REPLICAS > cHasher.getAllNodes().size()) {
            StorageService.LOGGER.info(this.myNode.getIpAddress() + " -  Required " + N_REPLICAS + " backup node, found " + cHasher.getAllNodes().size());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        preferenceNodes = cHasher.getNextServers(myNode, N_REPLICAS);

        while (keepRunning.get()) {
           /* while (N_REPLICAS > cHasher.getAllNodes().size()) {
                StorageService.LOGGER.info(this.myNode.getIpAddress() + " -  Required " + N_REPLICAS + " backup node, found " + cHasher.getAllNodes().size());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/

           // preferenceNodes = cHasher.getNextServers(myNode, N_REPLICAS);

            try {
                byte[] buff = new byte[udpServer.getReceiveBufferSize()];
                DatagramPacket p = new DatagramPacket(buff, buff.length);
                StorageService.LOGGER.debug(myNode.getIpAddress() + " - waiting messages...");
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
                    manageAppRequest((RequestAppMsg) msg);
                }
                 /* Reply Application message received*/
                else if (msg instanceof ReplyAppMsg) {
                    ReplyAppMsg replyMsg = (ReplyAppMsg) msg;
                    manageAppReply(replyMsg);
                }
                /* Request System  message received*/
                else if (msg instanceof RequestSystemMsg) {
                    manageSystemRequest((RequestSystemMsg) msg);
                }
                /* Request Client message received*/
                else if(msg instanceof RequestClientMsg){
                    manageClientRequest((RequestClientMsg) msg);
                }
                // Reply System message received
                else if (msg instanceof ReplySystemMsg) {
                    ReplySystemMsg replyMsg = (ReplySystemMsg) msg;
                    manageSystemReply(replyMsg);
                }

            } catch (IOException e) {
                StorageService.LOGGER.info(myNode.getIpAddress()+"- IOExcpetion "+e.getMessage());
                keepRunning.set(false);
            }
        }
       shutdown();
    }


    private void manageClientRequest(RequestClientMsg msg) {
      //  StorageService.LOGGER.info(myNode.getIpAddress()+"- MSG OPERAORTN  "+msg.getOperation());
        switch (msg.getOperation()) {
            case DSCV:
                StorageService.LOGGER.debug(myNode.getIpAddress()+"- Received DSCV message from "+msg.getIpSender());
                ArrayList<Node> myNodes = cHasher.getAllNodes();
                ReplyClientMsg msgreply;
                if(!myNodes.isEmpty()) {
                    StringBuilder strBuilder = new StringBuilder();
                    for (Node n : myNodes) {
                        strBuilder.append(n.getIpAddress() + ":" + n.getId());
                        strBuilder.append(" ");
                    }

                    msgreply = new ReplyClientMsg(AppMsg.OP.OK, myNode.getIpAddress(), myNode.getPortStorage(), strBuilder.toString());
                }
                else{
                    msgreply = new ReplyClientMsg(AppMsg.OP.ERR, myNode.getIpAddress(), myNode.getPortStorage(), " I have no Nodes in my view");
                }
                send(msg.getIpSender(), Helper.CLIENT_PORT, msgreply);
                StorageService.LOGGER.debug(myNode.getIpAddress()+"- SENT reply to DSCV message to "+msg.getIpSender());
                break;
        }


    }

    private void manageSystemReply(ReplySystemMsg replyMsg) {
        StorageService.LOGGER.info(myNode.getIpAddress() + " - Reply SystemMsg received from " + replyMsg.getIpSender());
    }


    private void manageSystemRequest(RequestSystemMsg msg) {

        switch (msg.getOperation()) {
            case PUT:
                Versioned vData = msg.getVersionedData();
                String key = vData.getData().getKey();
                Versioned mergeData = new Versioned(vData.getData());
                mergeData.mergeTo(vData);// merge VersionData received with my data
                if(!storage.containsKey(key)) {
                    storage.put(mergeData);
                    StorageService.LOGGER.info(myNode.getIpAddress() + " - PUT key <" + key + "> version: " + mergeData.getVersion());
                }
                else {
                    storage.update(mergeData);
                    StorageService.LOGGER.info(myNode.getIpAddress() + " - UPDATED  key <" + key + "> version: " + mergeData.getVersion());
                }

                send(msg.getIpSender(), Helper.QUORUM_PORT, new ReplySystemMsg(AppMsg.OP.OK, myNode.getIpAddress(), Helper.QUORUM_PORT, "PUT Succesfully "));
                break;
            case GET:
                LOGGER.info(myNode.getIpAddress() + " - GET key <"+msg.getKey()+"> SystemMsg received  from "+msg.getIpSender());
                if (storage.containsKey(msg.getKey())) {
                    Versioned myData = storage.get(msg.getKey());
                    ReplySystemMsg reply = new ReplySystemMsg(AppMsg.OP.OK, myNode.getIpAddress(), Helper.STORAGE_PORT, myData);
                    send(msg.getIpSender(), Helper.QUORUM_PORT, reply);
                    LOGGER.info(myNode.getIpAddress() + " - SENT <"+msg.getKey()+","+myData.getData().getValue()+"> to"+msg.getIpSender());

                } else {
                    String err = myNode.getIpAddress() + " - Data is not present into my storage";
                    ReplySystemMsg replyErr = new ReplySystemMsg(AppMsg.OP.ERR, msg.getIpSender(), Helper.STORAGE_PORT, err);
                    send(msg.getIpSender(), Helper.QUORUM_PORT, replyErr);
                    LOGGER.info(myNode.getIpAddress() + " - SENT "+err+" to "+ msg.getIpSender());
                }
                break;
        }
    }

    /**
     * The coordinator of the request manages the  message sent by a client.
     *
     * @param msg
     */

    private void manageAppRequest(RequestAppMsg<?> msg) {
        switch (msg.getOperation()) {
            case PUT:
                StorageService.LOGGER.info(myNode.getIpAddress() + " - Received  AppMsg " + msg.getOperation() + " <" + msg.getKey() + ":" + msg.getValue() + ">");// from " + msg.getIpSender());
                if (storage.containsKey(msg.getKey())) { // UPDATE data and increment version
                    Versioned d = storage.get(msg.getKey());
                    d.setData(new StorageData<>(msg.getKey(), msg.getValue()));
                    d.getVersion().increment(myNode.getId());
                    storage.update(d);
                    send(msg.getIpSender(),Helper.STORAGE_PORT,new ReplyAppMsg(AppMsg.OP.OK, " UPDATE <" + msg.getKey()+":"+msg.getValue()+"> "+d.getVersion()));

               /*     //sent to all WRITE_NODES
                    List<ReplySystemMsg> rep = askQuorum(d, Helper.QUORUM_PORT, AppMsg.OP.PUT);
                    if(rep.size() < WRITE_NODES)
                        send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.ERR, " Error: PUT not all the WRITE NODES  have responded"));
                    else
                        send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.OK, " PUT  <" + msg.getKey() + ":" + msg.getValue() + ">"));
                        */
                } else { // PUT new object
                    Versioned vData = new Versioned(new StorageData<>(msg.getKey(), msg.getValue()));
                    vData.getVersion().increment(myNode.getId());
                    this.storage.put(vData);
                    StorageService.LOGGER.info(this.myNode.getIpAddress() + " - Inserted <" + msg.getKey() + ":" + msg.getValue() + "> into local database");

                    //sent to all WRITE_NODES the new object received and wait the selection
                    List<ReplySystemMsg> rep = askQuorum(vData, Helper.QUORUM_PORT, AppMsg.OP.PUT);
                    if(rep.size() < WRITE_NODES)//-1
                        send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.ERR, " Error: PUT not all the WRITE NODES  have responded"));
                    else
                        send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.OK, " PUT  <" + msg.getKey() + ":" + msg.getValue() + ">"));
                }
                break;
            case GET:
                String key = msg.getKey();
                StorageService.LOGGER.info(myNode.getIpAddress() + " - Received AppMsg " + msg.getOperation() + "<" + key + ">");
                if (storage.containsKey(key)) {
                    Versioned myData = storage.get(key);
                    List<ReplySystemMsg> replies = askQuorum(myData,Helper.QUORUM_PORT, AppMsg.OP.GET);

                    if(replies.isEmpty() || replies.size() < READ_NODES) {
                        send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.ERR, " Error: GET has note received version from all the backups"));
                        StorageService.LOGGER.info(myNode.getIpAddress() + " - ERR  has not received all the backup nodes version to"+msg.getIpSender());
                    }

                    else {
                        for (ReplySystemMsg msgReply :replies) {  //for all READ_NODES version
                            if(!msgReply.getOperation().equals(AppMsg.OP.ERR)) {
                                Versioned bkuData = msgReply.getData();
                                switch (bkuData.compareTo(myData)) {
                                    case BEFORE: //my data version is newer than the backup version
                                        LOGGER.info(myNode.getIpAddress() + " - BEFORE version: <" + msgReply.getData().getData().getKey() + "> version: " + msgReply.getData().getVersion() + " from " + msgReply.getIpSender());
                                        send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.OK, " GET " + myData.getData().toString()));
                                        for (Node backup : preferenceNodes) {
                                            backup.sendToStorage(new RequestSystemMsg(AppMsg.OP.PUT, myNode.getIpAddress(), Helper.STORAGE_PORT, myData));
                                        }
                                        break;
                                    case AFTER: //my data version is older than the backup version
                                        LOGGER.info(myNode.getIpAddress() + " - AFTER version: <" + msgReply.getData().getData().getKey() + "> version: " + msgReply.getData().getVersion() + " from " + msgReply.getIpSender());
                                        myData.mergeTo(bkuData);
                                        send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.OK, " GET " + bkuData.getData().toString()));
                                        break;
                                    case CONCURRENT: //concurrent version must be resolved by the client
                                        LOGGER.info(myNode.getIpAddress() + " - CONCURRENT version: <" + msgReply.getData().getData().getKey() + "> version: " + msgReply.getData().getVersion() + " from " + msgReply.getIpSender());
                                        String selection = " 1 : " + myData.getData().toString() + " \n 2 : " + bkuData.getData().toString();
                                        AppMsg conflict = new RequestConflictMsg(AppMsg.TYPE.REQUEST, AppMsg.OP.GET, myNode.getIpAddress(), Helper.STORAGE_PORT, selection);
                                        sendConflict(msg.getIpSender(), conflict, Helper.CONFLICT_LISTEN_PORT);
                                        int sel = waitConflictResponse(Helper.CONFLICT_LISTEN_PORT);
                                        LOGGER.info(myNode.getIpAddress() + " - Received selection " + sel);
                                        switch (sel) {
                                            case (1): //my data is chosen from the client
                                                for (Node backup : preferenceNodes) {
                                                    backup.sendToStorage(new RequestSystemMsg(AppMsg.OP.PUT, myNode.getIpAddress(), Helper.STORAGE_PORT, myData));
                                                }
                                                send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.OK, " GET " + myData.getData().toString()));
                                            case (2): {//backup  is chosen from the client
                                                sendToMyStorage(new RequestSystemMsg(AppMsg.OP.PUT, myNode.getIpAddress(), Helper.STORAGE_PORT, bkuData));
                                                send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.OK, " GET " + bkuData.getData().toString()));
                                            }
                                        }
                                        break;
                                }
                            }
                            else {  //Get operation from READ nodes has returned an ERR message
                                LOGGER.info(myNode.getIpAddress() + " - Received ERR "+msgReply.getMsg()+" from  " +msg.getIpSender());
                            }
                        }
                    }
                }
                    else {
                    send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.ERR, " GET key not found"));
                }
                break;
            case LIST: //list command from client
                if (!storage.isEmpty()) {
                    send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.OK, " LIST " + storage.toString()));
                    StorageService.LOGGER.info(myNode.getIpAddress() + " - Sent LIST of my database to " + msg.getIpSender());
                } else {
                    send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.ERR, " LIST: empty database"));
                    StorageService.LOGGER.info(myNode.getIpAddress() + " - LIST : my database is empty");
                }

                break;
        }
    }

    private void manageAppReply(ReplyAppMsg msg) {
        switch (msg.getOperation()) {
            case OK:
                StorageService.LOGGER.info(myNode.getIpAddress() + " - REPLY  OK  from " + msg.getIpSender() + msg.getMsg()  );
                break;
            case ERR:
                StorageService.LOGGER.info(myNode.getIpAddress() + " - REPLY  ERR " + msg.getMsg());
                break;
        }
    }

    public void sendToMyStorage(AppMsg msg) {
        /* send  message to the storage  node */
        this.send(myNode.getIpAddress(), getPortStorage(), msg);
    }

    private void send(String destIp, int destPort, AppMsg msg) {
        try {
            InetAddress address = InetAddress.getByName(destIp);

            if (msg.getIpSender() == null)
                msg.setIpSender(myNode.getIpAddress());

            ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());

            byte[] buf = Helper.fromAppMsgtoByte(msg);

            // / Initialize a datagram packet with data and address
            DatagramSocket dsocket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, destPort);
            dsocket.send(packet);
            dsocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<ReplySystemMsg> askQuorum(Versioned vData, int listenPort, AppMsg.OP op) {
        RequestSystemMsg reqQuorum;

        if (op.equals(AppMsg.OP.PUT))
            reqQuorum = new RequestSystemMsg(op, myNode.getIpAddress(), 0, vData);
        else{ //GET method
            reqQuorum = new RequestSystemMsg(op, myNode.getIpAddress(), 0, vData.getData().getKey());
        }

        try {
            DatagramSocket dsocket = new DatagramSocket(listenPort);

            // send msg to all the nodes in the preference list
            for (Node backup : preferenceNodes) {
                byte [] buf = Helper.fromReqSystemMsgtoByte(reqQuorum);

                InetAddress destAddress = InetAddress.getByName(backup.getIpAddress());
                DatagramPacket packet = new DatagramPacket(buf, buf.length, destAddress, Helper.STORAGE_PORT);

                dsocket.send(packet);
                StorageService.LOGGER.info(this.myNode.getIpAddress() + " - Sent "+op+"  SystemMsg  to "+backup.getIpAddress() );
            }
            dsocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        //wait quorum selection
        return _waitQuorum(reqQuorum.getOperation(), listenPort);
    }

    private List<ReplySystemMsg> _waitQuorum(AppMsg.OP op, int listenPort) {

        DatagramSocket udpQuorum;  //server listen Quorum selection


        int numResponses = (op == AppMsg.OP.PUT) ? WRITE_NODES : READ_NODES;
        ArrayList<ReplySystemMsg> replies = new ArrayList<>();

        try {
            udpQuorum = new DatagramSocket(listenPort);
            //TODO insert timeout into configuration file
            udpQuorum.setSoTimeout(3000);

            //wait the selection
            for (int j = 0; j < numResponses; j++) {
                byte[] buff = new byte[udpQuorum.getReceiveBufferSize()];
                DatagramPacket p = new DatagramPacket(buff, buff.length);
                StorageService.LOGGER.debug(this.myNode.getIpAddress() + " - Waiting " + numResponses + " quorum msg selection...");

                try {
                    udpQuorum.receive(p);
                }
                catch (SocketTimeoutException e) { //timeout exception
                    StorageService.LOGGER.info(this.myNode.getIpAddress() + " - Timeout reached, waiting quorum Msg...");
                    udpQuorum.close();
                    return replies;
                }

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
                StorageService.LOGGER.info(myNode.getIpAddress() + " - Received quorum Msg from " + msgQuorum.getIpSender());
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

    // Resole conflict message
    private void sendConflict(String destIp, AppMsg msg , int listenPort) {
        try {

            InetAddress address = InetAddress.getByName(destIp);

            if (msg.getIpSender() == null)
                msg.setIpSender(myNode.getIpAddress());

            byte[] buf = Helper.fromAppMsgtoByte(msg);

            // / Initialize a datagram packet with data and address
            DatagramSocket dsocket = new DatagramSocket(listenPort);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, Helper.STORAGE_PORT);
            dsocket.send(packet);
            dsocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private int waitConflictResponse(int listenPort) {

        int response = -1;
        try {
            DatagramSocket udpConflict = new DatagramSocket(listenPort); //server listen for conflict message
            //TODO insert timeout into configuration file
            //udpConflict.setSoTimeout(5000);

            byte[] buff = new byte[udpConflict.getReceiveBufferSize()];
            DatagramPacket p = new DatagramPacket(buff, buff.length);

            try {
                StorageService.LOGGER.info(myNode.getIpAddress() + " - waiting conflict Msg selection from client...");
                udpConflict.receive(p);
            } catch (SocketTimeoutException e) { //timeout exception
                StorageService.LOGGER.info(myNode.getIpAddress() + " - Timeout reached, waiting conflict Msg...");
                return response;
            }

            int packet_length = 0;
            for (int i = 0; i < 4; i++) {
                int shift = (4 - 1 - i) * 8;
                packet_length += (buff[i] & 0x000000FF) << shift;
            }

            byte[] json_bytes = new byte[packet_length];
            System.arraycopy(buff, 4, json_bytes, 0, packet_length);
            String receivedMessage = new String(json_bytes);

            ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
            ReplyConflictMsg msgQuorum = mapper.readValue(receivedMessage, ReplyConflictMsg.class);
            response = msgQuorum.getSelection();


        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return response;
    }


    public void shutdown() {
        LOGGER.info(this.myNode.getIpAddress() + "-  Storage service has been shutdown...");
        udpServer.close();
    }


    public void manageUP(Node nodeUp) {
        ArrayList<Node> nexts = cHasher.getNextServers(myNode,1);
        ArrayList<Node> previous = cHasher.getPreviousServer(myNode,1);

        if(nexts.contains(nodeUp) && previous.contains(nodeUp)){
            for (Versioned vdata : storage.getStorage().values()) {
                RequestSystemMsg msg = new RequestSystemMsg(AppMsg.OP.PUT, myNode.getIpAddress(), myNode.getPortStorage(), vdata);
                StorageService.LOGGER.info(this.myNode.getIpAddress() + " - UP node " + nodeUp.getIpAddress() + ", Sent data " + vdata.getData());
                nodeUp.sendToStorage(msg);
            }
        }

    }
}
