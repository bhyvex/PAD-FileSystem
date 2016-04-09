package com.dido.pad;

import com.dido.pad.messages.Msg;
import com.dido.pad.messages.RequestAppMsg;
import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.RemoteGossipMember;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dido-ubuntu on 25/03/16.
 */

public class TestQuorum {

    GossipSettings settings = new GossipSettings();
    List<GossipMember> startupMembers = new ArrayList<>();
    List<Node> clients = new ArrayList<>();
    int clusterMembers = 3;


    public TestQuorum(){
        startupMembers.add(new RemoteGossipMember("127.0.0.1", Helper.GOSSIP_PORT, "node1"));
        for (int i = 1; i < clusterMembers + 1; ++i) {
            Node node = new Node("127.0.0." + i, "node" + i, Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startupMembers, settings);
            clients.add(node);
        }
    }

    @Before
    public void setUP(){
        for (Node n : clients) {
            n.start();
        }
    }


   @Test
    public void testGetMerge() {

       try {
           Thread.sleep(5000);

           //Check if the nodes discovered each other
           for (int i = 0; i < clusterMembers; ++i) {
               Assert.assertEquals(clusterMembers - 1, clients.get(i).getGossipmanager().getMemberList().size());
           }

           String key = "Davide";
           Msg req = new RequestAppMsg<>(Msg.OP.PUT, key, "Neri");
           req.setIpSender("127.0.0.3");
           clients.get(2).sendToStorage(req);

           Thread.sleep(3000);

           //check if 127.0.0.3 has received the key
           Assert.assertTrue(clients.get(2).get_storageService().getStorage().containsKey(key));

           String newValue = "Giangrande";
           //update version with a PUT into backup node (127.0.0.1)
           Msg reqUpdate1 = new RequestAppMsg<>(Msg.OP.PUT, key, newValue);
           clients.get(0).sendToStorage(reqUpdate1);
           Thread.sleep(1000);
           //check if backup has received the new value
           Assert.assertEquals(newValue, clients.get(0).get_storageService().getStorage().get(key).getData().getValue());

           Thread.sleep(1000);

           Msg reqGet = new RequestAppMsg<>(Msg.OP.GET, key, "");
           clients.get(2).sendToStorage(reqGet);

           Thread.sleep(3000);
           //test if 127.0.0.3 has received the value updated (Merge Operation)
           Assert.assertEquals(newValue, clients.get(2).get_storageService().getStorage().get(key).getData().getValue());

           clients.get(0).shutdown();

       } catch (InterruptedException e) {
           e.printStackTrace();
       }
   }
}

