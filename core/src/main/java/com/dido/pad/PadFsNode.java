package com.dido.pad;

import com.beust.jcommander.JCommander;
import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;

import java.util.ArrayList;


/**
 * Created by dido-ubuntu on 22/03/16.
 */
public class PadFsNode {


    public static void main(String[] args){

        ParseArgs jct = new ParseArgs();

        JCommander jCommander= new JCommander(jct, args);
        jCommander.setProgramName("PadFsNode");
        if(jct.isHelp()){
            jCommander.usage();
            return;
        }

        Node node = new Node(jct.getIp(), jct.getId(), jct.getStoragePort(), jct.getGossipPort(), jct.getGossipMember(), new GossipSettings());
        node.start();

    }



}
