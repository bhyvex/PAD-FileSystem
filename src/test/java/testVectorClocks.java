import com.dido.pad.DataStorage;

import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.VectorClocks.Versioned;
import com.dido.pad.datamessages.AppMsg;
import com.dido.pad.datamessages.RequestSystemMsg;
import org.junit.Test;

import javax.swing.text.html.HTMLEditorKit;

/**
 * Created by dido-ubuntu on 15/03/16.
 */
public class testVectorClocks {

    @Test
    public void testVectors(){

        Node n1 = new Node("127.0.0.1","node1", Helper.STORAGE_PORT,Helper.GOSSIP_PORT);
        Node n2 = new Node("127.0.0.2","node1", Helper.STORAGE_PORT,Helper.GOSSIP_PORT);
        n1._startStorageService();
        n2._startStorageService();


        Versioned<DataStorage<?>> d1 = new Versioned<>(new DataStorage<>("key","value"));
        d1.getVersion().incremenetVersion("node1");

        RequestSystemMsg msg = new RequestSystemMsg(AppMsg.OPERATION.PUT,"127.0.0.1", Helper.STORAGE_PORT,d1);
        n1.send("127.0.0.2", Helper.STORAGE_PORT, msg);





    }
}
