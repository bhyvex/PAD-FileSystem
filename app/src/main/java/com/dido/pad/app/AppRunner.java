package com.dido.pad.app;

import com.dido.pad.Helper;
import com.dido.pad.Node;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.RemoteGossipMember;

import java.io.IOException;
import java.util.ArrayList;
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
        List<Node> clients = new ArrayList<>();
        int clusterMembers = 3;
        for (int i = 1; i < clusterMembers + 1; ++i) {
            Node node = new Node("127.0.0." + i, "node" + i, Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startupMembers, settings);
            clients.add(node);
        }

        for (Node n : clients) {
            n.start();
        }

        Thread.sleep(15000);
     /*   while(true){
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("\nEnter a command (h for help):");
            String read = bufferReader.readLine();

            if(read != null){

            }

        }*/

    /*    try {
            Thread.sleep(15000);

            //Check if the nodes discovered each other
            for (int i = 0; i < clusterMembers; ++i) {
                Assert.assertEquals((NUM_NODES * (Helper.NUM_NODES_VIRTUALS)) - 1, clients.get(i).getGossipmanager().getMemberList().size());
            }

            String key = "Davide";
            AppMsg req = new RequestAppMsg<>(AppMsg.OP.PUT, key, "Neri");
            req.setIpSender("127.0.0.3");
            clients.get(2).sendToStorage(req);

            Thread.sleep(3000);

            //check if 127.0.0.3 has received the key
            Assert.assertTrue(clients.get(2).get_storageService().getStorage().containsKey(key));

            //update version with a PUT
            AppMsg reqUpdate1 = new RequestAppMsg<>(AppMsg.OP.PUT, key, "Giangrande");
            clients.get(2).sendToStorage(reqUpdate1);

            Thread.sleep(1000);

            AppMsg reqGet = new RequestAppMsg<>(AppMsg.OP.GET, key, "");
            clients.get(2).sendToStorage(reqGet);

            Thread.sleep(3000);

            clients.get(0).shutdown();
            //for (Node n : clients) {
             //   n.shutdown();
            //}

        } catch (InterruptedException e) {
            e.printStackTrace();
        */
    }
}
