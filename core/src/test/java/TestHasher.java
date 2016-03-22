import com.dido.pad.StorageData;
import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.consistenthashing.DefaultFunctions;
import com.dido.pad.consistenthashing.Hasher;
import com.google.code.gossip.GossipMember;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dido-ubuntu on 08/03/16.
 */
public class TestHasher {

    @Test
    public void testOneServer(){
//        Hasher<Node> hasher = new Hasher<>(1, DefaultFunctions::SHA1, DefaultFunctions::BytesConverter);
//        List<GossipMember> l =  new ArrayList<>();
//
//        Node n1 = new Node("127.0.0.1","id1", Helper.STORAGE_PORT, Helper.GOSSIP_PORT,l);
//        hasher.addServer(n1);
//        n1.start();
//
//        StorageData d = new StorageData<>("AAAA","first data");
//        StorageData d2 = new StorageData<>("ZZZZ","second data");
//
//        Assert.assertEquals(hasher.getServerForData(d.getKey()),n1);
//        n1.shutdown();

    }
/*
    @Test
    public void testMoreServer(){
        Hasher<Node> hasher = new Hasher<>(1,iHasher.SHA1, iHasher.getNodeToBytesConverter() );

        List<GossipMember> l = new ArrayList<>();
        Node n1 = new Node("127.0.0.1","id1", Helper.STORAGE_PORT,Helper.GOSSIP_PORT,l);
        Node n2 = new Node("127.0.0.2","id2",Helper.STORAGE_PORT,Helper.GOSSIP_PORT,l);
        Node n3 = new Node("127.0.0.3","id3",Helper.STORAGE_PORT,Helper.GOSSIP_PORT,l);
        Node n4 = new Node("127.0.0.4","id4",Helper.STORAGE_PORT,Helper.GOSSIP_PORT,l);

        hasher.addServer(n1);
        hasher.addServer(n2);
        hasher.addServer(n3);
        hasher.addServer(n4);

        StorageData d = new StorageData("AAAA","first data");
        Node n = hasher.getServerForData(d.getKey());
        Assert.assertEquals(n,n3);

        StorageData d2 = new StorageData("BBBB","second data");
        Node node = hasher.getServerForData(d2.getKey());
        Assert.assertEquals(node,n4);


    }

    @Test
    public void testMoreVirtualServer(){
        // Three virtual nodes
        int virtualNodes = 2;
        Hasher<Node> hasher = new Hasher<>(virtualNodes, iHasher.SHA1,iHasher.getNodeToBytesConverter());

        List<GossipMember> l = new ArrayList<>();

        Node n1 = new Node("127.0.0.1","id1", Helper.STORAGE_PORT,Helper.GOSSIP_PORT,l);
        Node n2 = new Node("127.0.0.2","id2", Helper.STORAGE_PORT,Helper.GOSSIP_PORT,l);
        Node n3 = new Node("127.0.0.3","id3", Helper.STORAGE_PORT,Helper.GOSSIP_PORT,l);
        Node n4 = new Node("127.0.0.4","id4", Helper.STORAGE_PORT,Helper.GOSSIP_PORT,l);

        hasher.addServer(n1);
        hasher.addServer(n2);
        hasher.addServer(n3);
        hasher.addServer(n4);

        StorageData d = new StorageData("AAAA","first data");
        Node n = hasher.getServerForData(d.getKey());
        //System.out.print(n);
        Assert.assertEquals(n,n3);

        // get Next nodes froma serve
        //hasher.printkeyValueHash();
        ArrayList<Node> nexts = hasher.getPreferenceList(n3,2);  //next(n3) = [id2, id1]
        ArrayList<Node> list =new ArrayList<Node>();
        list.add(n2);
        list.add(n1);
        Assert.assertEquals(nexts, list);

        //remove physical server
        hasher.removeServer(n3);   //remove physical server
        List<Node> nodes =  hasher.getAllNodes();
        Assert.assertEquals(nodes.size(), virtualNodes*3);

        Node node = hasher.getServerForData(d.getKey());
        Assert.assertEquals(node,n2);

    }
*/

}

