package com.dido.pad;

import com.dido.pad.hashing.DefaultFunctions;
import com.dido.pad.hashing.Hasher;
import com.dido.pad.data.StorageData;
import com.google.code.gossip.GossipMember;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dido-ubuntu on 08/03/16.
 */
public class TestHasher {

    private  List<GossipMember> startNodes;
    private Node n1 ;
    private  Node n2 ;
    private  Node n3 ;
    private  Node n4 ;

    /**
     * Sets up the test fixture.
     * (Called before every test case method.)
     */
    @Before
    public void setUp(){
        startNodes =  new ArrayList<>();
         n1 = new Node("127.0.0.1","id1");//, Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startNodes);
         n2 = new Node("127.0.0.2","id2");//, Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startNodes);
         n3 = new Node("127.0.0.3","id3");//, Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startNodes);
         n4 = new Node("127.0.0.4","id4");//, Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startNodes);

    }

    /**
     * Tears down the test fixture.
     * (Called after every test case method.)
     */
    @After
    public void tearDown(){
       // n1.shutdown();
    }

    @Test
    public void testOneServer(){
        Hasher<Node> hasher = new Hasher<>(1, DefaultFunctions::SHA1, DefaultFunctions::BytesConverter);

        hasher.addServer(n1);
        StorageData d = new StorageData<>("AAAA","first data");
        StorageData d2 = new StorageData<>("ZZZZ","second data");
        Assert.assertEquals(hasher.getServerForData(d.getKey()),n1);
    }

    @Test
    public void testMoreServer(){
        Hasher<Node> hasher = new Hasher<>(1,DefaultFunctions::SHA1, DefaultFunctions::BytesConverter);

        hasher.addServer(n1);
        hasher.addServer(n2);
        hasher.addServer(n3);
        hasher.addServer(n4);

        StorageData d = new StorageData("pad","filesystem");
        Node n = hasher.getServerForData(d.getKey());
        Assert.assertEquals(n,n4);

        StorageData d2 = new StorageData("dynamo","filesystem");
        Node node = hasher.getServerForData(d2.getKey());
        Assert.assertEquals(node,n1);


    }

    @Test
    public void testMoreVirtualServer(){
        // Three virtual nodes
        int virtualNodes = 3;
        Hasher<Node> hasher = new Hasher<>(virtualNodes,DefaultFunctions::SHA1, DefaultFunctions::BytesConverter);


        hasher.addServer(n1);
        hasher.addServer(n2);
        hasher.addServer(n3);
        hasher.addServer(n4);

        StorageData d = new StorageData("pad","filesystem");
        Node node = hasher.getServerForData(d.getKey());
        Assert.assertEquals(node,n1);


        StorageData d1 = new StorageData("dynamo","filesystem");
        Node node1 = hasher.getServerForData(d1 .getKey());
        Assert.assertEquals(node1,n2);

    }

    @Test
    public void testRemoveAddServers(){
        Hasher<Node> hasher = new Hasher<>(1,DefaultFunctions::SHA1, DefaultFunctions::BytesConverter);

        hasher.addServer(n1);
        hasher.addServer(n2);
        hasher.addServer(n3);
        hasher.addServer(n4);

        StorageData d = new StorageData("pad","filesystem");
        Node node = hasher.getServerForData(d.getKey());
        Assert.assertEquals(node,n4);

        hasher.removeServer(n4);

        Node newNode = hasher.getServerForData(d.getKey());
        Assert.assertEquals(newNode,n2);

        hasher.addServer(n4);
        Node addNode = hasher.getServerForData(d.getKey());
        Assert.assertEquals(addNode,n4);


    }

}

