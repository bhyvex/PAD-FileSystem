import com.dido.pad.Node;
import com.dido.pad.datamessages.AppMsg;
import com.dido.pad.datamessages.DataStorage;
import org.junit.Test;

/**
 * Created by dido-ubuntu on 09/03/16.
 */
public class testDataStore {

    @Test
    public void testDataInsert(){
        Node n1 = new Node("127.0.0.1","id1");
        Node n2 = new Node("127.0.0.2","id2");
        n1.addStorageService(3001);
//        n2.addStorageService(3002);

        //n1.getHasher().addServer(n);
        n1.getHasher().addServer(n1);

      /*  DataStorage<String> pay = new DataStorage<String>("key1", "value1");
        AppMsg<String> msg = new AppMsg<String>(AppMsg.TYPE.REQUEST, AppMsg.OPERATION.PUT, pay);

        n1.get_storageService().receive(msg);
        String v =(String) n1.get_storageService().getValueFromKey("key1");

        System.out.println(v);
*/
    }
}
