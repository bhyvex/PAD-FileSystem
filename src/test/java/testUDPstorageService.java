import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.datamessages.AppMsg;
import com.dido.pad.datamessages.RequestAppMsg;
import com.google.code.gossip.GossipMember;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import org.junit.Test;

/**
 * Created by dido-ubuntu on 09/03/16.
 */
public class testUDPstorageService {

    @Test
    public void testStorage(){

        Node n1 = new Node("127.0.0.1","node1", Helper.STORAGE_PORT,Helper.GOSSIP_PORT);
        Node n2 = new Node("127.0.0.2","node2", Helper.STORAGE_PORT, Helper.GOSSIP_PORT);

        n1.startStorageService();
        n2.startStorageService();

        try {
            n1.getConsistentHasher().addServer(n2);
            n1.getConsistentHasher().addServer(n1);
            n2.getConsistentHasher().addServer(n1);
            n2.getConsistentHasher().addServer(n2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
        String key = "k2";
        n1.send("127.0.0.2",Helper.STORAGE_PORT, new RequestAppMsg<String>(AppMsg.OPERATION.PUT,key,"ciao"));
        Thread.sleep(200);
        n1.send("127.0.0.2",Helper.STORAGE_PORT, new RequestAppMsg<String>(AppMsg.OPERATION.GET,key,""));
        Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
