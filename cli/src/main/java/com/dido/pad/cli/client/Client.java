package com.dido.pad.cli.client;

import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.cli.Cli;
import com.dido.pad.messages.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.code.gossip.GossipMember;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dido-ubuntu on 24/03/16.
 */
public class Client {


    public static final Logger LOGGER = Logger.getLogger(Client.class);

    private ClientService clientService;
    private Cli cli;

    private final Thread _clientThread;   //send periodically a request of the node in the netwoks
    private DatagramSocket clientSocket ;

    private AtomicBoolean keepRunning;

    private String ip;
    private String id;


    public Client(String ip, String id, ArrayList<GossipMember> startupMember) {
        this.ip = ip;
        this.id = id;
        cli = new Cli();
        try {
            clientSocket = new DatagramSocket(Helper.CLIENT_PORT);
        } catch (SocketException e1) {
            Client.LOGGER.info(ip+ "- Error init "+ e1.getMessage());
            //e1.printStackTrace();
            clientSocket = null;
        }
        clientService = new ClientService(this, startupMember, cli);
        keepRunning = new AtomicBoolean(true);

        _clientThread = new Thread(this::gossipClient);
        //TODO create a method fro staritng the service
        _clientThread.start();

        clientService.start();
    }

    public ClientService getClientService() {
        return clientService;
    }

    public void addserver(Node n) {
        clientService.getcHasher().addServer(n);
    }

    public void put(String key, String value) {
        Node n = clientService.getcHasher().getServerForData(key);
        RequestAppMsg msg = new RequestAppMsg<>(AppMsg.OP.PUT, key, value);
        msg.setIpSender(ip);
        n.sendToStorage(msg);
        LOGGER.info(ip + "- sent Put to " + n.getIpAddress());
    }

    public void get(String key) {
        Node n = clientService.getcHasher().getServerForData(key);
        RequestAppMsg msg = new RequestAppMsg<>(AppMsg.OP.GET, key, "");
        msg.setIpSender(ip);
        n.sendToStorage(msg);
        LOGGER.info(ip + "- sent GET to " + n.getIpAddress());
    }

    public void list(String ip) {
        RequestAppMsg msg = new RequestAppMsg<>(AppMsg.OP.LIST, "", "");
        clientService.send(ip, Helper.STORAGE_PORT, msg);
    }

    //only for update a value for testing the versioning without tear down a node
    public void force(String key, String value, String ip) {
        //Node n = null;
        for (Node node:clientService.getcHasher().getAllNodes()) {
            if (node.getIpAddress().equals(ip)){
                RequestAppMsg msg = new RequestAppMsg<>(AppMsg.OP.PUT, key, value);
                msg.setIpSender(ip);
                node.sendToStorage(msg);
                 LOGGER.info(ip + "- sent FORCE to " + node.getIpAddress());
                return;
        }
        }
        LOGGER.info(ip + "- FORCE  " + ip +" not found");

    }

    public String getIpAddress() {
        return ip;
    }


    public String getId() {
        return id;
    }

    private void gossipClient() {
        int interval = 4000; // millisencods

        try{

        while (keepRunning.get()) {

            RequestClientMsg reqNodes = new RequestClientMsg(AppMsg.OP.DSCV, ip, Helper.CLIENT_PORT);
            Node node = clientService.getRandomNode();

            ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
            byte[] jsonByte = mapper.writeValueAsBytes(reqNodes);

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

            InetAddress destAddress = InetAddress.getByName(node.getIpAddress());
            DatagramPacket packet = new DatagramPacket(buf, buf.length, destAddress, Helper.STORAGE_PORT);

            clientSocket.send(packet);

            Client.LOGGER.debug(ip + "- Sent request nodes in the system to " + node.getIpAddress());

            ArrayList<Node> nodesDiscoverd =  _waitRepsonseNodes();

            Thread.sleep(interval);

        }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            clientSocket.close();
        }
        clientSocket.close();
    }

    private ArrayList<Node> _waitRepsonseNodes() {

        ArrayList<Node> nodes = new ArrayList<>();

        try {

            byte[] buff = new byte[clientSocket.getReceiveBufferSize()];
            DatagramPacket p = new DatagramPacket(buff, buff.length);

            ClientService.LOGGER.debug(ip + " - Waiting  nodes in the system ...");

            clientSocket.receive(p);

            int packet_length = 0;
            for (int i = 0; i < 4; i++) {
                int shift = (4 - 1 - i) * 8;
                packet_length += (buff[i] & 0x000000FF) << shift;
            }

            byte[] json_bytes = new byte[packet_length];
            System.arraycopy(buff, 4, json_bytes, 0, packet_length);
            String receivedMessage = new String(json_bytes);

            ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
            ReplyClientMsg msgNodes = mapper.readValue(receivedMessage, ReplyClientMsg.class);
            String nodesIds = msgNodes.getNodesIds();
            ClientService.LOGGER.debug(ip + " - Received Nodes [" + nodesIds+ "] from "+ msgNodes.getIpSender());

            String[] pairsNodeId = nodesIds.split("\\s");
            ArrayList<Node> nodesReceived = new ArrayList<>();
            for (String pair : pairsNodeId) {
                String[] ipId = pair.split(":");   // IP:ID
                Node n = new Node(ipId[0], ipId[1]);
                nodesReceived.add(n);
            }
            clientService.updateNodes(nodesReceived);
            ClientService.LOGGER.debug(ip + " - Nodes has been updated"  );


        } catch (SocketException e) {
            ClientService.LOGGER.error(ip + " - " + e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nodes;
    }


}
