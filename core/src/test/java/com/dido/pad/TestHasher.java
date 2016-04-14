package com.dido.pad;

import com.dido.pad.hashing.DefaultFunctions;
import com.dido.pad.hashing.Hasher;
import com.dido.pad.data.StorageData;
import com.dido.pad.hashing.IHasher;
import com.dido.pad.hashing.iHasher;
import com.google.code.gossip.GossipMember;
import junit.framework.*;
import org.junit.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dido-ubuntu on 08/03/16.
 */
public class TestHasher {

    //private  List<GossipMember> startNodes;
    private Hasher<Node> hasher;
    private  Node n1 ;
    private  Node n2 ;
    private  Node n3 ;
    private  Node n4 ;

    /**
     * Sets up the test fixture.
     * (Called before every test case method.)
     */
    @Before
    public void setUp(){

        hasher= new Hasher<>(DefaultFunctions::SHA1, DefaultFunctions::BytesConverter);
         n1 = new Node("127.0.0.1","node1");//, Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startNodes);
         n2 = new Node("127.0.0.2","node2");//, Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startNodes);
         n3 = new Node("127.0.0.3","node3");//, Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startNodes);
         n4 = new Node("127.0.0.4","node4");//, Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startNodes);
    }

    @Test
    public void testHashing(){
        hasher.addServer(n1);
        hasher.addServer(n2);
        hasher.addServer(n3);

        System.out.print(hasher.containsNode(n1));

        Assert.assertTrue(hasher.containsNode(n1));
        Assert.assertTrue(hasher.containsNode(n2));
        Assert.assertTrue(hasher.containsNode(n2));

        Assert.assertEquals(hasher.getAllNodes().size(),3);
    }

    @Test
    public void testOneServer(){
        hasher.addServer(n1);
        StorageData d = new StorageData<>("AAAA","first data");
        Assert.assertEquals(hasher.getServerForData(d.getKey()),n1);
    }

    @Test
    public void testMoreServer(){
        hasher.addServer(n1);
        hasher.addServer(n2);
        hasher.addServer(n3);
        hasher.addServer(n4);

        StorageData d = new StorageData<>("pad","filesystem");
        Node n = hasher.getServerForData(d.getKey());
        Assert.assertEquals(n3,n);

        StorageData d2 = new StorageData<>("dynamo","filesystem");
        Node node = hasher.getServerForData(d2.getKey());
        Assert.assertEquals(n2,node);
    }


    @Test
    public void testRemoveAddServers(){

        Hasher<Node> hasher = new Hasher<>(DefaultFunctions::SHA1, DefaultFunctions::BytesConverter);
        hasher.addServer(n1);
        hasher.addServer(n2);
        hasher.addServer(n3);
        hasher.addServer(n4);

        StorageData d = new StorageData<>("dido","filesystem");
        Node node = hasher.getServerForData(d.getKey());
        Assert.assertEquals(n3,node);

        hasher.removeServer(n3);

        Node newNode = hasher.getServerForData(d.getKey());
        Assert.assertEquals(n4,newNode);

        hasher.addServer(n3);
        Node addNode = hasher.getServerForData(d.getKey());
        Assert.assertEquals(n3,addNode);


    }

    @Test
    public void testNextPreviousServer(){
        hasher= new Hasher<>(DefaultFunctions::SHA1, DefaultFunctions::BytesConverter);

        hasher.addServer(n1);
        hasher.addServer(n2);
        hasher.addServer(n3);
        hasher.addServer(n4);

        Assert.assertEquals(hasher.getNextServers(n1,1).get(0),n3);
        Assert.assertEquals(hasher.getNextServers(n2,1).get(0),n1);

        Assert.assertEquals(hasher.getNextServers(n3,1).get(0),n4);

        Assert.assertEquals(hasher.getNextServers(n4,1).get(0),n2);

        String key = "dido";
        Node n =hasher.getServerForData(key);

        hasher.removeServer(n3);
        Node node1 = hasher.getServerForData(key);
        Assert.assertEquals(node1,n4);

        hasher.addServer(n3);

        Assert.assertEquals(hasher.getPreviousServer(n2,1).get(0),n4);

        Assert.assertEquals(hasher.getNextServers(n1,1).get(0), n3); //n4 is last node, n2 is the first
        Assert.assertEquals(hasher.getPreviousServer(n3,1).get(0), n1); //n2 is the first key, previous is n4

    }


}

