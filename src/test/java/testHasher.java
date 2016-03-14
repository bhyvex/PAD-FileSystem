import com.dido.pad.DataStorage;
import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.consistenthashing.Hasher;
import com.dido.pad.consistenthashing.iHasher;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dido-ubuntu on 08/03/16.
 */
public class testHasher {

    @Test
    public void testOneServer(){
        Hasher<Node> hasher = new Hasher<>(1,iHasher.SHA1,iHasher.getNodeToBytesConverter());

        Node n1 = new Node("127.0.0.1","id1", Helper.STORAGE_PORT, Helper.GOSSIP_PORT);
        hasher.addServer(n1);

        DataStorage d = new DataStorage<String>("AAAA","first data");
        DataStorage d2 = new DataStorage<String>("ZZZZ","second data");

        Assert.assertEquals(hasher.getServerForData(d.getKey()),n1);
        Assert.assertEquals(hasher.getServerForData(d.getKey()),n1);

        n1.shutdown();
    }

    @Test
    public void testMoreServer(){
        Hasher<Node> hasher = new Hasher<>(1,iHasher.SHA1, iHasher.getNodeToBytesConverter() );

        Node n1 = new Node("127.0.0.1","id1", Helper.STORAGE_PORT,Helper.GOSSIP_PORT);
        Node n2 = new Node("127.0.0.2","id2",Helper.STORAGE_PORT,Helper.GOSSIP_PORT);
        Node n3 = new Node("127.0.0.3","id3",Helper.STORAGE_PORT,Helper.GOSSIP_PORT);
        Node n4 = new Node("127.0.0.4","id4",Helper.STORAGE_PORT,Helper.GOSSIP_PORT);

        hasher.addServer(n1);
        hasher.addServer(n2);
        hasher.addServer(n3);
        hasher.addServer(n4);

        DataStorage d = new DataStorage("AAAA","first data");
        Node n = hasher.getServerForData(d.getKey());
        Assert.assertEquals(n,n3);

        DataStorage d2 = new DataStorage("BBBB","second data");
        Node node = hasher.getServerForData(d2.getKey());
        Assert.assertEquals(node,n4);

        n1.shutdown();
        n2.shutdown();
        n3.shutdown();
        n3.shutdown();
        n4.shutdown();

    }

    @Test
    public void testMoreVirtualServer(){
        // Three virtual nodes
        int virtualNodes = 2;
        Hasher<Node> hasher = new Hasher<>(virtualNodes, iHasher.SHA1,iHasher.getNodeToBytesConverter());

        Node n1 = new Node("127.0.0.1","id1", Helper.STORAGE_PORT,Helper.GOSSIP_PORT);
        Node n2 = new Node("127.0.0.2","id2", Helper.STORAGE_PORT,Helper.GOSSIP_PORT);
        Node n3 = new Node("127.0.0.3","id3", Helper.STORAGE_PORT,Helper.GOSSIP_PORT);
        Node n4 = new Node("127.0.0.4","id4", Helper.STORAGE_PORT,Helper.GOSSIP_PORT);

        hasher.addServer(n1);
        hasher.addServer(n2);
        hasher.addServer(n3);
        hasher.addServer(n4);

        DataStorage d = new DataStorage("AAAA","first data");
        Node n = hasher.getServerForData(d.getKey());
        //System.out.print(n);
        Assert.assertEquals(n,n3);

        hasher.removeServer(n3);   //remove physical server
        List<Node> nodes =  hasher.getAllNodes();
        Assert.assertEquals(nodes.size(), virtualNodes*3);


        Node node = hasher.getServerForData(d.getKey());
        Assert.assertEquals(node,n2);

        n1.shutdown();
        n2.shutdown();
        n3.shutdown();
        n3.shutdown();
        n4.shutdown();
    }

}

