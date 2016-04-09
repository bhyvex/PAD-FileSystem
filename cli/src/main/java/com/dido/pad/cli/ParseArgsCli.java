package com.dido.pad.cli;

import com.beust.jcommander.Parameter;
import com.dido.pad.Helper;
import com.google.code.gossip.GossipMember;
import com.google.code.gossip.RemoteGossipMember;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dido-ubuntu on 09/04/16.
 */
public class ParseArgsCli {

    @Parameter(description = "ipSeed:id[:gp] [ipSeed:id[:gp]")
    private ArrayList<String> seedNodes = new ArrayList<>();

    @Parameter(names="--help", help=true, description=" Show this help message")
    private boolean help = false;

    @Parameter(names = "-ip", description = "Ip address of the client")
    private String ip = "127.0.0.254";

    @Parameter(names = "-id", description = "Id name of the client")
    private String id = "client";

    public boolean isHelp() {
        return help;
    }

    public ArrayList<GossipMember> getSeedNodes() {
        ArrayList<GossipMember> startNodes = new ArrayList<>();

        for (String s : seedNodes) {
            String ip,id;
            int portGossip;
            String [] att =s.split(":"); // seedIP:id[:portgossip]
            if(att.length ==2) {
                ip = att[0];
                portGossip = Helper.GOSSIP_PORT;
                id = att[1];
            } else{
                ip = att[0];
                portGossip = Integer.parseInt(att[1]);
                id = att[2];
            }
            startNodes.add(new RemoteGossipMember(ip,portGossip,id));

        }
        return startNodes;
    }

    public String getIp() {
        return ip;
    }

    public String getId() {
        return id;
    }

}
