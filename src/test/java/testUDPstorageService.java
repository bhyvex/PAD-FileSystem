import com.dido.pad.Node;
import com.dido.pad.datamessages.AppMsg;
import org.junit.Test;

/**
 * Created by dido-ubuntu on 09/03/16.
 */
public class testUDPstorageService {

    @Test
    public void testStorage(){

        Node n1 = new Node("127.0.0.1","node1");
        Node n2 = new Node("127.0.0.1","node2");

        n1.addStorageService(3001);
        n2.addStorageService(3002);

        n2.startStorageService();
        n1.startStorageService();

        n1.send("127.0.0.1",3002, new AppMsg(AppMsg.TYPE.REPLY, AppMsg.OPERATION.GET,n1));
       // n2.send("127.0.0.1",3001, "ciao da 2");
    }
}
