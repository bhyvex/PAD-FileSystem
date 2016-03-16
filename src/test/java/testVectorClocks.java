import com.dido.pad.StorageData;

import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.VectorClocks.Versioned;
import com.dido.pad.datamessages.AppMsg;
import com.dido.pad.datamessages.RequestSystemMsg;
import org.junit.Test;

/**
 * Created by dido-ubuntu on 15/03/16.
 */
public class testVectorClocks {

    @Test
    public void testVectors() throws InterruptedException {

        Node n1 = new Node("127.0.0.1","node1", Helper.STORAGE_PORT,Helper.GOSSIP_PORT);
        Node n2 = new Node("127.0.0.2","node2", Helper.STORAGE_PORT,Helper.GOSSIP_PORT);
        n1._startStorageService();
        n2._startStorageService();

        /*when the node receive a PUT AppMsg and it has not the data.
            1) create message
            2) increment local vector clock for the data
            3) send mesg to destiantion
         */

        Versioned d1 = new Versioned(new StorageData<>("key","value"));
        d1.getVectorclock().incremenetVersion(n1.getId());
        RequestSystemMsg msg = new RequestSystemMsg(AppMsg.OPERATION.PUT,"127.0.0.1", Helper.STORAGE_PORT,d1);
        n1.send("127.0.0.2", Helper.STORAGE_PORT, msg);


        Thread.sleep(5000);





    }
}
