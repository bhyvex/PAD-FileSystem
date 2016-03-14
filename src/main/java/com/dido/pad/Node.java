package com.dido.pad;


import com.dido.pad.consistenthashing.Hasher;
import com.dido.pad.datamessages.AppMsg;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.code.gossip.*;
import com.google.code.gossip.event.GossipListener;
import com.google.code.gossip.event.GossipState;
import com.google.code.gossip.manager.GossipManager;
import org.apache.log4j.Logger;


import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by dido-ubuntu on 05/03/16.
 */

public class Node  {

    public static final Logger LOGGER = Logger.getLogger(Node.class);

    private GossipService _gossipService;
    private StorageService _storageService;

    private String ipAddress;
    private String id;

    private int portStorage;
    private int portGossip;

    // Empty constructor for jackson parser to JSON
    public Node(){
    }


    // Node from a GossipMember. Used when a GossipMemeber goes UP.
    public Node(GossipMember member){

        this(member.getHost(), member.getId(), Helper.STORAGE_PORT, member.getPort());
    }

    public Node(String ipAddress, String id, int portStorage, int portGossip){
        this.ipAddress = ipAddress;
        this.id = id;
        this.portStorage = portStorage;
        this.portGossip = portGossip;

    }

    public void start_Gossip_Storage_Service(int logLevel, List<GossipMember> gossipMembers, GossipSettings settings, GossipListener listener)
            throws UnknownHostException, InterruptedException {
        _gossipService = new GossipService(this.ipAddress,this.portGossip,this.id, logLevel,gossipMembers,settings,listener);
        _gossipService.start();

        /*start storage service*/
        //this._startStorageService();
        this._storageService = new StorageService(this);
        this._storageService.addServer(this);

       /* for (GossipMember member : gossipMembers) {
            if(!member.getHost().equals(this.getIpAddress()))
                this._storageService.addServer(new Node(member));
        }*/

        this._storageService.start();
    }

    public GossipManager getGossipmanager(){
        return _gossipService.get_gossipManager();
    }

    private  void _startStorageService(){
        this._storageService = new StorageService(this);
        this._storageService.start();

    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void shutdown(){
        if(_gossipService != null)
            _gossipService.shutdown();
    }


    /* callback of gossiping procedure if a node goes UP or DOWN  */
    public void  gossipEvent(GossipMember member, GossipState state) {
        switch (state) {
            case UP:
                _storageService.addServer(new Node(member));
                Node.LOGGER.info(this.getIpAddress() + "- UP event, node "+member.getHost()+" added to consistent hasher");
                break;
            case DOWN:
                _storageService.removeServer(new Node(member));

                Node.LOGGER.info(this.getIpAddress()+"- DOWN event, node "+member.getHost()+" removed from consistent hasher");
                break;

        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (!ipAddress.equals(node.ipAddress)) return false;
        return id.equals(node.id);

    }

    @Override
    public int hashCode() {
        int result = ipAddress.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }

    public int getPortStorage(){
        return this.portStorage;

    }

    public void setPortStorage( int portStorage){
        this.portStorage = portStorage;

    }

    public void sendToStorageNode(AppMsg msg){
        /* send  message to the same storage  node */
        //System.out.println("sendToStorage from same node "+this.ipAddress);
        this.send(this.ipAddress, this.getPortStorage() ,msg);
    }

    public void send(String destIp, int destPort, AppMsg msg){
        try {

            InetAddress address = InetAddress.getByName(destIp);


            if(msg.getIpSender() == null)
                msg.setIpSender(this.ipAddress);


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

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
