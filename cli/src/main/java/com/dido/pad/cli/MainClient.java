package com.dido.pad.cli;

import com.beust.jcommander.JCommander;
import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.ParseArgs;
import com.dido.pad.cli.client.Client;
import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.RemoteGossipMember;


import java.util.ArrayList;


public class MainClient {


    public static void main(String[] args) {

        ParseArgsCli jct = new ParseArgsCli();

        JCommander jCommander = new JCommander(jct, args);
        jCommander.setProgramName("Client");
        if (jct.isHelp()) {
            jCommander.usage();
            return;
        }

        Client client = new Client(jct.getIp(), jct.getId(), jct.getSeedNodes());
        client.start();
    }

}