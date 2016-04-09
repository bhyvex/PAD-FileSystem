package com.dido.pad.cli.client;

import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.cli.ClientHelper;
import com.dido.pad.messages.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.code.gossip.GossipMember;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dido-ubuntu on 24/03/16.
 */
public class Client {


    public static final Logger LOGGER = Logger.getLogger(Client.class);

    private String ip;
    private String id;

    private Cli clientService;

    private final Thread _clientThread;   //send periodically a request of the nodes in the system
    private DatagramSocket clientSocket;

    private AtomicBoolean keepRunning;



    public Client(String ip, String id, ArrayList<GossipMember> startupMember) {
        this.ip = ip;
        this.id = id;
        try {
            clientSocket = new DatagramSocket(Helper.CLIENT_PORT);
        } catch (SocketException e1) {
            Client.LOGGER.info(ip+ "- Error init "+ e1.getMessage());
            clientSocket = null;
        }
        clientService = new Cli(this, startupMember);
        keepRunning = new AtomicBoolean(true);
        _clientThread = new Thread(this::gossipClient);

    }

    public void start(){
        _clientThread.start();
        clientService.runCli();

    }

    public Cli getClientService() {
        return clientService;
    }

    public void addserver(Node n) {
        clientService.getcHasher().addServer(n);
    }

    public String getIpAddress() {
        return ip;
    }


    public String getId() {
        return id;
    }

    private void gossipClient() {
        int interval = ClientHelper.INTERVAL_DISCOVER;

        while (keepRunning.get()) {
            try{
                RequestClientMsg reqNodes = new RequestClientMsg(Msg.OP.DSCV, ip, Helper.CLIENT_PORT);
                Node node = clientService.getRandomNode();
                byte buf[] = Helper.fromClientMsgtoByte(reqNodes);
                InetAddress destAddress = InetAddress.getByName(node.getIpAddress());
                DatagramPacket packet = new DatagramPacket(buf, buf.length, destAddress, Helper.STORAGE_PORT);
                clientSocket.send(packet);

                LOGGER.debug(ip + " - Sent request nodes in the system to " + node.getIpAddress());

                ArrayList<Node> received =  _waitRepsonseNodes();
                if(!received.isEmpty())
                    clientService.updateNodes(received);

                LOGGER.debug(ip + " - Nodes has been updated");

                Thread.sleep(interval);

            } catch (InterruptedException | IOException e) {
                LOGGER.error(ip + " - "+e.getMessage());
            }
        }
        clientSocket.close();
    }

    private ArrayList<Node> _waitRepsonseNodes() {
        ArrayList<Node> nodesReceived = new ArrayList<>();

        try {

            byte[] buff = new byte[clientSocket.getReceiveBufferSize()];
            DatagramPacket p = new DatagramPacket(buff, buff.length);

            clientSocket.setSoTimeout(ClientHelper.TIMEOUT_INTERVAL);

            LOGGER.debug(ip + " - Waiting list of nodes in the system ...");

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
            LOGGER.debug(ip + " - Received Nodes [" + nodesIds + "] from " + msgNodes.getIpSender());

            String[] pairsNodeId = nodesIds.split("\\s");

            for (String pair : pairsNodeId) {
                String[] ipId = pair.split(":");   // IP:ID
                Node n = new Node(ipId[0], ipId[1]);
                nodesReceived.add(n);
            }

        } catch (SocketException e) {
            LOGGER.error(ip + " - " + e);
        } catch (IOException e) {
            LOGGER.error(ip + " - "+e);
        }
        return nodesReceived;
    }


}
