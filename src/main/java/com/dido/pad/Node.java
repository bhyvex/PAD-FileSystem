package com.dido.pad;


import com.dido.pad.consistenthashing.HashableDataStorage;
import com.dido.pad.consistenthashing.Hasher;
import com.dido.pad.datamessages.AppMsg;
import com.google.code.gossip.*;
import com.google.code.gossip.event.GossipListener;
import com.google.code.gossip.event.GossipState;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

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


    public Node(String ipAddresss, String id){
        this.ipAddress = ipAddresss;
        this.id = id;
    }

    public StorageService get_storageService() {
        return _storageService;
    }

    public Node(GossipMember member){
        this(member.getAddress(), member.getId());
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

    public void addStorageService(int port){
        this._storageService = new StorageService(this, port);
    }


    public void addGossipService(int port, int logLevel, List<GossipMember> gossipMembers, GossipSettings settings, GossipListener listener)
            throws UnknownHostException, InterruptedException {
        _gossipService = new GossipService(this.ipAddress,port,this.id,LogLevel.DEBUG,gossipMembers,settings,listener);
    }

    public void startGossipService(){
        _gossipService.start();
    }

    public void startStorageService(){this._storageService.start();}

    public void shutdown(){
        _gossipService.shutdown();
    }



    /* callback of gossiping procedure if a node goes UP or DOWN  */
    public void  gossipEvent(GossipMember member, GossipState state) {
        switch (state) {
            case UP:
                getHasher().addServer(new Node(member));
                Node.LOGGER.info("Node "+this.toString()+" ADDS  "+member.getAddress());
                break;
            case DOWN:
                getHasher().removeServer(new Node(member));
                Node.LOGGER.info("Node "+this.toString()+"  REMOVES "+member.getAddress());
                break;

        };
    }

    public Hasher<Node> getHasher(){
        return _storageService.getcHasher();
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

    public void send(String ip, int port, AppMsg msg){
        try {
            InetAddress address = null;
            address = InetAddress.getByName(ip);

            ObjectMapper mapper = new ObjectMapper();
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
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
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
