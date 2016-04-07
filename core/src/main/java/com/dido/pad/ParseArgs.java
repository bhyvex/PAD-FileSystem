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
import java.util.regex.PatternSyntaxException;

public class ParseArgs {

    // main -ip ipAddress m1:gp:id m2:gp:id

    @Parameter(description = "ipGossipMember:id[:gp]")
    private List<String> gossipMember = new ArrayList<>();

    @Parameter(names="--help", help=true, description=" Show this help message")
    private boolean help = false;

    @Parameter(names = "-ip", description = "Ip address of the machine")
    private String ip = "127.0.0.1";

    @Parameter(names = "-id", description = "Id name of the node")
    private String id = "127.0.0.1";

    @Parameter(names = "-gp", description = "Port of the gossiping service")
    private int gossipPort = Helper.GOSSIP_PORT ;//2000;

    @Parameter(names = "-sp", description = "Port of the storage service")
    private int storagePort = Helper.STORAGE_PORT; //3000;

    public ArrayList<GossipMember> getGossipMember() {
        ArrayList<GossipMember> startupMembers = new ArrayList<>();

        for (String m : gossipMember) {
                String ip,id;
                int portGossip;
                String [] att = m.split(":");
                if(att.length ==2) {
                    ip = att[0];
                    portGossip = Helper.GOSSIP_PORT;
                    id = att[0];
                } else{
                    ip = att[0];
                    portGossip = Integer.parseInt(att[1]);
                    id = att[2];
                }
                startupMembers.add(new RemoteGossipMember(ip,portGossip,id));
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

    public boolean isHelp() {
        return help;
    }
}
