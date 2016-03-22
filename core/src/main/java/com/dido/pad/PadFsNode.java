package com.dido.pad;

import com.beust.jcommander.JCommander;
import com.google.code.gossip.GossipSettings;


/**
 * Created by dido-ubuntu on 22/03/16.
 */
public class PadFsNode {


    public static void main(String[] args){

        ParseArgs jct = new ParseArgs();
        new JCommander(jct, args);

        Node node = new Node(jct.getIp(), jct.getId(), jct.getStoragePort(), jct.getGossipPort(), jct.getGossipMember(), new GossipSettings());
        node.start();

    }



}
