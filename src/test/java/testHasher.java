import com.dido.pad.AbstractData;
import com.dido.pad.Data;
import com.dido.pad.Node;
import com.dido.pad.consistenthashing.Hasher;
import com.dido.pad.consistenthashing.iHasher;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by dido-ubuntu on 08/03/16.
 */
public class testHasher {

    @Test
    public void testOneServer(){
        Hasher<Node,AbstractData> hasher = new Hasher<>(1,iHasher.SHA1,iHasher.getNodeToBytesConverter());


        Node n1 = new Node("127.0.0.1","id1");
        hasher.addServer(n1);

        try {
            AbstractData d = new Data<String>("AAAA","first data");
            AbstractData d2 = new Data<String>("ZZZZ","second data");

            Assert.assertEquals(hasher.getServerForData(d),n1);
            Assert.assertEquals(hasher.getServerForData(d),n1);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testMoreServer(){
        Hasher<Node,AbstractData> hasher = new Hasher<>(1,iHasher.SHA1, iHasher.getNodeToBytesConverter() );

        Node n1 = new Node("127.0.0.1","id1");
        Node n2 = new Node("127.0.0.2","id2");
        Node n3 = new Node("127.0.0.3","id3");
        Node n4 = new Node("127.0.0.4","id4");

        hasher.addServer(n1);
        hasher.addServer(n2);
        hasher.addServer(n3);
        hasher.addServer(n4);

        try {
            AbstractData d = new Data("AAAA","first data");
            Node n = hasher.getServerForData(d);
            //System.out.print(n);
            Assert.assertEquals(n,n3);

            AbstractData d2 = new Data("BBBB","second data");
            Node node = hasher.getServerForData(d2);
            Assert.assertEquals(node,n4);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Test
    public void testMoreVirtualServer(){
        Hasher<Node,AbstractData> hasher = new Hasher<>(
                3,
                iHasher.SHA1,
                iHasher.getNodeToBytesConverter()
        );

        Node n1 = new Node("127.0.0.1","id1");
        Node n2 = new Node("127.0.0.2","id2");
        Node n3 = new Node("127.0.0.3","id3");
        Node n4 = new Node("127.0.0.4","id4");

        hasher.addServer(n1);
        hasher.addServer(n2);
        hasher.addServer(n3);
        hasher.addServer(n4);

        try {
            AbstractData d = new Data("AAAA","first data");

            Node n = hasher.getServerForData(d);
          //  System.out.print(n);
            Assert.assertEquals(n,n3);

            hasher.removeServer(n3);

            Node node = hasher.getServerForData(d);
            Assert.assertEquals(node,n2);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

