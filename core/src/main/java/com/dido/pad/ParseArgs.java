package com.dido.pad;

/**
 * Created by dido-ubuntu on 22/03/16.
 */

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.IntegerConverter;
import com.google.code.gossip.GossipMember;
import com.google.code.gossip.RemoteGossipMember;

import java.util.ArrayList;
import java.util.List;

public class ParseArgs {

    // main -ip ipAddress m1:gp:id m2:gp:id

    @Parameter(description = "Start up Gossip members ")
    private List<String> gossipMember = new ArrayList<>();

    @Parameter(names = "-ip", description = "Ip address of the machine")
    private String ip = "127.0.0.1";

    @Parameter(names = "-id", description = "Id name of the node")
    private String id = "127.0.0.1";

    @Parameter(names = "-gp", description = "Port of the gossiping service")
    private int gossipPort = 2000;

    @Parameter(names = "-sp", description = "Port of the storage service")
    private int storagePort = 3000;

    public List<GossipMember> getGossipMember() {
        List<GossipMember> startupMembers = new ArrayList<>();
        for (String m : gossipMember) {
            String[]  att = m.split(":");
            startupMembers.add(new RemoteGossipMember(att[0], Integer.parseInt(att[1]), att[2]));
        }
        return startupMembers;
    }

    public String getIp() {
        return ip;
    }

    public String getId() {
        return id;
    }

    public int getGossipPort() {
        return gossipPort;
    }

    public int getStoragePort() {
        return storagePort;
    }
}
