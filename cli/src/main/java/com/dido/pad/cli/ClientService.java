package com.dido.pad.cli;

import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.PersistentStorage;
import com.dido.pad.consistenthashing.DefaultFunctions;
import com.dido.pad.consistenthashing.Hasher;
import com.dido.pad.data.StorageData;
import com.dido.pad.data.Versioned;
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
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dido-ubuntu on 07/03/16.
 */
public class ClientService extends Thread {


    public static final Logger LOGGER = Logger.getLogger(ClientService.class);

    public int N_REPLICAS = 2;  //  include olso the node master ( N=2 , mode master + successive node)
    public int WRITE_NODES = 1;
    public int READ_NODES = 2;

    private Hasher<Node> cHasher;
    private PersistentStorage storage;
    private DatagramSocket udpServer;
    private AtomicBoolean keepRunning;

    private List<Node> preferenceNodes; // preference nodes = list of backup nodes (xclude the node itself)
                                        //list of nodes that is responsible for storing a particular key is
   private Client client;


   // public ClientService(Node client, List<GossipMember> seedNodes) {
   public ClientService(Client client, List<GossipMember> seedNodes) {

        this.cHasher = new Hasher<>(Helper.NUM_NODES_VIRTUALS, DefaultFunctions::SHA1, DefaultFunctions::BytesConverter);
        this.client = client;
        storage = new PersistentStorage();

        // ADD seed nodes to the node storage service
        for (GossipMember member : seedNodes) {
            Node n = new Node(member);
            //TODO problem trasportin s GossipMember to a Node
          //  if (!cHasher.containsNode(n))
                cHasher.addServer(n);
        }

        keepRunning = new AtomicBoolean(true);
        try {
            SocketAddress sAddress = new InetSocketAddress(client.getIpAddress(), Helper.STORAGE_PORT);
            ClientService.LOGGER.info(client.getIpAddress() + "- initialized on portStorage " + Helper.STORAGE_PORT);
            ClientService.LOGGER.debug("I'm " + client.toString());
            udpServer = new DatagramSocket(sAddress);
        } catch (SocketException e) {
            ClientService.LOGGER.error(this.client.getIpAddress() + " - " + e);
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


    @JsonIgnore
    public Hasher<Node> getcHasher() {
        return cHasher;
    }

    public void addServer(Node n) {
       // if(!cHasher.containsNode(n)) {
            cHasher.addServer(n);
        //    return true;
        //}else
        //    return false;
    }

    //update nodes of the storage service received from the selected node.
    public void updateNodes(ArrayList<Node> nodes){
        for (Node n : nodes) {
            if (!cHasher.containsNode(n))
                cHasher.addServer(n);
        }
        for (Node myNode : cHasher.getAllNodes()){
            if(!nodes.contains(myNode))
                cHasher.removeServer(myNode);
        }

    }

    public void removeServer(Node n) {
        cHasher.removeServer(n);
    }


    @Override
    public void run() {
        while (keepRunning.get()) {
            try {
                byte[] buff = new byte[udpServer.getReceiveBufferSize()];
                DatagramPacket p = new DatagramPacket(buff, buff.length);
                ClientService.LOGGER.debug(client.getIpAddress() + " - waiting messages...");
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
                  //  Node destNode = this.cHasher.getServerForData(requestMsg.getKey());
                   // if (destNode.equals(this.client)) { /*store in my database, I'm the master*/
                        manageAppRequest(requestMsg);
                  //  } else {                            /*forward message to another node*/
                  //      destNode.sendToStorage(requestMsg);
                 //       StorageService.LOGGER.info(this.client.getIpAddress() + " -forwards msg to " + destNode.getIpAddress());
                  //  }
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

                else if(msg instanceof RequestConflictMsg){
                    manageConflictMessage((RequestConflictMsg) msg);
                }


            } catch (IOException e) {
               // StorageService.LOGGER.debug(client.getIpAddress()+"- has beeen shutdown ...");
                keepRunning.set(false);
            }
        }
       shutdown();
    }


    private void manageConflictMessage( RequestConflictMsg msg) {
        switch (msg.getType()) {
            case REQUEST:
                System.out.println("CLIENT Insert a choice"+msg.getSelection());
                break;
            case REPLY:
                break;
        }
    }

    private void manageSystemReply(ReplySystemMsg replyMsg) {
        ClientService.LOGGER.info(client.getIpAddress() + " - Reply SystemMsg received from " + replyMsg.getIpSender());
    }


    private void manageSystemRequest(RequestSystemMsg msg) {

        switch (msg.getOperation()) {
            case PUT:
                Versioned vData = msg.getVersionedData();
                String key = vData.getData().getKey();
                Versioned mergeData = new Versioned(vData.getData());
                mergeData.mergeTo(vData);                   // merge VersionData received with my data
                storage.put(mergeData);
                ClientService.LOGGER.info(client.getIpAddress() + " - PUT SystemMsg <" + key + "> version: "+ mergeData.getVersion());
                send(msg.getIpSender(), Helper.QUORUM_PORT, new ReplySystemMsg(AppMsg.OP.OK, client.getIpAddress(), Helper.QUORUM_PORT, "PUT Succesfully "));
                break;
            case GET:
                ClientService.LOGGER.info(client.getIpAddress() + " - GET SystemMsg received ");
                if (storage.containsKey(msg.getKey())) {
                    Versioned myData = storage.get(msg.getKey());
                    ReplySystemMsg reply = new ReplySystemMsg(AppMsg.OP.OK, client.getIpAddress(), Helper.STORAGE_PORT, myData);
                    send(msg.getIpSender(), Helper.QUORUM_PORT, reply);

                } else {
                    String info = client.getIpAddress() + " - Data is not present into my storage";
                    RequestSystemMsg replyErr = new RequestSystemMsg(AppMsg.OP.ERR, msg.getIpSender(), Helper.STORAGE_PORT, info);
                    send(msg.getIpSender(), Helper.QUORUM_PORT, replyErr);
                }
                break;
        }
    }

    private void manageAppReply(ReplyAppMsg msg) {
        switch (msg.getOperation()) {
            case OK:
                ClientService.LOGGER.info(client.getIpAddress() + " - REPLY  OK " + msg.getMsg() + " from " + msg.getIpSender());
                break;
            case ERR:
                ClientService.LOGGER.info(client.getIpAddress() + " - REPLY  ERR " + msg.getMsg());
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
                ClientService.LOGGER.info(this.client.getIpAddress() + " - Received  AppMsg " + msg.getOperation() + " <" + msg.getKey() + ":" + msg.getValue() + ">");// from " + msg.getIpSender());
                if (storage.containsKey(msg.getKey())) { // UPDATE data and increment version
                    Versioned d = storage.get(msg.getKey());
                    d.setData(new StorageData<>(msg.getKey(), msg.getValue()));
                    d.getVersion().increment(client.getId());
                    send(msg.getIpSender(),Helper.STORAGE_PORT,new ReplyAppMsg(AppMsg.OP.OK, " UPDATE <" + msg.getKey()+":"+msg.getValue()+"> "+d.getVersion()));
                    //client.get_storageService().sendToMyStorage(new ReplyAppMsg(AppMsg.OP.OK, " Updated succesfully key:" + msg.getKey()));
                } else { // PUT new object
                    Versioned vData = new Versioned(new StorageData<>(msg.getKey(), msg.getValue()));
                    vData.getVersion().increment(client.getId());
                    this.storage.put(vData);
                    ClientService.LOGGER.info(this.client.getIpAddress() + " - Inserted <" + msg.getKey() + ":" + msg.getValue() + "> into local database");

                    //sent to all WRITE_NODES the new object received and wait the response
                    List<ReplySystemMsg> rep = askQuorum(vData, Helper.QUORUM_PORT, AppMsg.OP.PUT);
                    if(rep.size() < WRITE_NODES-1)
                        send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.ERR, " Error: PUT has note received version from allthe backups"));
                    else
                        send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.OK, " PUT  <" + msg.getKey() + ":" + msg.getValue() + ">"));
                }
                break;
            case GET:
                String key = msg.getKey();
                ClientService.LOGGER.info(this.client.getIpAddress() + " - Received AppMsg " + msg.getOperation() + "<" + key + ">");
                if (storage.containsKey(key)) {
                    Versioned myData = storage.get(key);
                    List<ReplySystemMsg> replies = askQuorum(myData,Helper.QUORUM_PORT, AppMsg.OP.GET);
                    //Merge version
                    if(replies.isEmpty() && replies.size() < READ_NODES-1)
                        send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.ERR, " Error: GET has note received version from all the backups"));
                    else{
                      //  for (ReplySystemMsg msgReply :replies) {  //suppose only one single versioned received
                            ReplySystemMsg reply =replies.get(0);
                            Versioned bkuData = reply.getData();
                          ClientService.LOGGER.info(this.client.getIpAddress() + " - Received Versioned: <"+reply.getData().getData().getKey()+"> version: "+ reply.getData().getVersion()+" from "+reply.getIpSender());
                            switch (bkuData.compareTo(myData)) {
                                case BEFORE: //my data version is newer than the backup version
                                    send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.OK, " GET "+ myData.getData().toString()));
                                    for (Node backup: preferenceNodes){
                                        backup.sendToStorage(new RequestSystemMsg(AppMsg.OP.PUT, client.getIpAddress(),Helper.STORAGE_PORT,myData));
                                    }
                                    break;
                                case AFTER: //my data version is older than the backup version
                                    myData.mergeTo(bkuData);
                                    send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.OK, " GET "+ bkuData.getData().toString()));
                                    break;
                                case CONCURRENT: //concurrent version must be resolved by the client
                                    String selection = "1: "+myData.getData().toString()+ "2: "+bkuData.getData().toString();
                                    // TODO message to send to the client in order to resolve the concurrent version
                                    send(msg.getIpSender(),Helper.STORAGE_PORT, new RequestConflictMsg(AppMsg.TYPE.REQUEST, AppMsg.OP.GET, client.getIpAddress(),Helper.STORAGE_PORT, selection));
                                    break;
                            }
                      //  }
                    }

                    //5) sent reconcilied version
                    send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.OK, " GET "+ myData.getData().toString()));
                }
                    else {
                    send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.ERR, " GET key not found"));
                }
                break;
            case LIST:
                HashMap<String, Versioned> db = this.storage.getStorage();
                if (!db.isEmpty()) {
                    send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.OK, " LIST " + db.toString()));
                } else {
                    send(msg.getIpSender(), Helper.STORAGE_PORT, new ReplyAppMsg(AppMsg.OP.ERR, " LIST empty data database"));
                }

                break;
        }
    }


    public void sendToMyStorage(AppMsg msg) {
        /* send  message to the storage  node */
        this.send(client.getIpAddress(), Helper.STORAGE_PORT, msg);
    }

    protected void send(String destIp, int destPort, AppMsg msg) {
        try {

            InetAddress address = InetAddress.getByName(destIp);

            if (msg.getIpSender() == null)
                msg.setIpSender(client.getIpAddress());

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


    public List<ReplySystemMsg> askQuorum(Versioned vData, int listenPort, AppMsg.OP op) {
        RequestSystemMsg reqQuorum;

        if (op.equals(AppMsg.OP.PUT))
            reqQuorum = new RequestSystemMsg(op, client.getIpAddress(), 0, vData);
        else {
            reqQuorum = new RequestSystemMsg(op, client.getIpAddress(), 0, vData.getData().getKey());
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
                ClientService.LOGGER.info(this.client.getIpAddress() + " - Sent "+op+" SystemMsg  "+backup.getIpAddress() );
            }
            dsocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        //wait quorum response
        return _waitQuorum(reqQuorum.getOperation(), listenPort);
    }

    private List<ReplySystemMsg> _waitQuorum(AppMsg.OP op, int listenPort) {

        DatagramSocket udpQuorum;  //server listen Quorum response

        //int numResponses = (op == AppMsg.OP.PUT) ? WRITE_NODES : READ_NODES;
        int numResponses = (op == AppMsg.OP.PUT) ? WRITE_NODES-1 : READ_NODES-1;
        ArrayList<ReplySystemMsg> replies = new ArrayList<>();

        try {
            udpQuorum = new DatagramSocket(listenPort);
            //TODO insert timeout into configuration file
            udpQuorum.setSoTimeout(3000);

            //wait the response
            for (int j = 0; j < numResponses; j++) {
                byte[] buff = new byte[udpQuorum.getReceiveBufferSize()];
                DatagramPacket p = new DatagramPacket(buff, buff.length);
                ClientService.LOGGER.debug(this.client.getIpAddress() + " - Waiting " + numResponses + " quorum msg response...");

                try {
                    udpQuorum.receive(p);
                }
                catch (SocketTimeoutException e) { //timeout exception
                    ClientService.LOGGER.info(this.client.getIpAddress() + " - Timeout reached, waiting quorum Msg...");
                    //udpQuorum.close();
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
                ClientService.LOGGER.info(this.client.getIpAddress() + " - Received quorum Msg from " + msgQuorum.getIpSender());
                replies.add(msgQuorum);

            }

            udpQuorum.close();

        } catch (SocketException e) {
            ClientService.LOGGER.error(this.client.getIpAddress() + " - " + e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return replies;
    }

    public void shutdown() {
        LOGGER.info(this.client.getIpAddress() + "-  Storage service has been shutdown...");
        udpServer.close();
    }

    public Node getRandomNode(){
        ArrayList<Node> nodes = cHasher.getAllNodes();
        Random rand = new Random();
        int random = rand.nextInt(nodes.size());
        return nodes.get(random);
    }

  /*  public void manageUP(Node nodeUp) {
        ArrayList<Node> nexts = cHasher.getNextServers(client,1);
        ArrayList<Node> previous = cHasher.getPreviousServer(client,1);

        if(nexts.contains(nodeUp) && previous.contains(nodeUp)){
            for (Versioned vdata : storage.getStorage().values()) {
                RequestSystemMsg msg = new RequestSystemMsg(AppMsg.OP.PUT, client.getIpAddress(), Helper.STORAGE_PORT, vdata);
                ClientService.LOGGER.info(this.client.getIpAddress() + " - UP node " + nodeUp.getIpAddress() + ", Sent data " + vdata.getData());
                nodeUp.sendToStorage(msg);
            }
        }

    }*/
}
