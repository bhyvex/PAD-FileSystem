package com.dido.pad;

import com.dido.pad.messages.Msg;
import com.dido.pad.messages.RequestClientMsg;
import com.dido.pad.messages.RequestSystemMsg;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.nio.ByteBuffer;

/**
 * Created by dido-ubuntu on 11/03/16.
 */
public class Helper {

    public static final int STORAGE_PORT = 3000;
    public static final int QUORUM_PORT = 3001;
    public static final int CLIENT_PORT = 3002;
    public static final int CONFLICT_LISTEN_PORT = 3003;

    public static final int GOSSIP_PORT = 2000;


    public static final boolean CLEAR_DATABASE_INTO_NODE = false; //clear all the databases of the node if true


    public static byte[] fromClientMsgtoByte(RequestClientMsg reqNodes) {
        ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
        byte[] jsonByte = new byte[0];
        try {
            jsonByte = mapper.writeValueAsBytes(reqNodes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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

        return buf;
    }

    public static byte[] fromAppMsgtoByte(Msg msg) {
        ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
        byte[] jsonByte = new byte[0];
        try {
            jsonByte = mapper.writeValueAsBytes(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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

        return buf;
    }

    public static byte[] fromReqSystemMsgtoByte(RequestSystemMsg msg) {
        ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
        byte[] jsonByte = new byte[0];
        try {
            jsonByte = mapper.writeValueAsBytes(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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

        return buf;
    }

}
