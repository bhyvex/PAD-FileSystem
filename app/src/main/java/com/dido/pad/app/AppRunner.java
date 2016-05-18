package com.dido.pad.app;


import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.RemoteGossipMember;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dido-ubuntu on 22/03/16.
 */
public class AppRunner {

    private static final int NUM_NODES = 3;

    public static void main(String[] args) throws IOException, InterruptedException {
        //startup gossip member
        GossipSettings settings = new GossipSettings();
        int seedNodes = 1;
        List<GossipMember> startupMembers = new ArrayList<>();
        for (int i = 1; i < seedNodes + 1; ++i) {
            startupMembers.add(new RemoteGossipMember("127.0.0." + i, Helper.GOSSIP_PORT, "node" + i));
        }

        //create three local client
        HashMap<String,Node> clients = new HashMap<>();
        int clusterMembers = 4;
        for (int i = 1; i < clusterMembers + 1; ++i) {
            String ip = "127.0.0." + i;
                Node node = new Node(ip, "node" + i, Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startupMembers, settings);
                clients.put(ip, node);

        }

        for (Node n : clients.values()) {
            n.start();
        }

        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(System.in));

        String help = "AppRunner usage: \n " +
                "\tdown ip  : the node with ip  goes down\n" +
                "\tup  x    : the node with ip (127.0.0.x) goes up\n"+
                "\tdisconnect  ip  : disconnect the node with ip from the network\n"+
                "\tconnect ip    : connect the node ip fro the network";

        while(true) {
            System.out.println("\nApp Runner : insert a command [h for usage message]...\n");
            String input = null;
            try {
                input = bufferReader.readLine();

                String [] cmds = input.split("\\s+"); //slipts  white spaces

                switch (cmds[0]) {
                    case ("down"):
                        String ip = cmds[1];
                        if(clients.containsKey(ip))
                            clients.get(ip).shutdown();
                        break;
                    case ("up"):
                        String lastAddress = cmds[1]; // is the last number of ip  (127.0.0.X)
                        Node node = new Node("127.0.0."+lastAddress, "node" + lastAddress, Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startupMembers, settings);
                        clients.put("127.0.0."+lastAddress,node);
                        node.start();
                        break;
                    case ("disconnect"):
                        String nodeDisconnect= cmds[1];
                        if(clients.containsKey(nodeDisconnect)){
                            (clients.get(nodeDisconnect)).disconnect();
                        } else
                        System.out.println("node is not present");
                        break;
                    case ("connect"):
                        String nodeConnect= cmds[1];
                        if(clients.containsKey(nodeConnect)){
                            (clients.get(nodeConnect)).connect();
                        }else
                            System.out.println("node is not present");

                        break;
                    case ("h"):
                        System.out.println(help);

                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

        }

        //Thread.sleep(15000);

    }
}
