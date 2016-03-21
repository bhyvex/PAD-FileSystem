import com.dido.pad.StorageData;

import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.VectorClocks.Versioned;
import com.dido.pad.datamessages.AppMsg;
import com.dido.pad.datamessages.RequestSystemMsg;
import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.LogLevel;
import com.google.code.gossip.RemoteGossipMember;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dido-ubuntu on 15/03/16.
 */
public class testVectorClocks {
/*
    @Test
    public void testVectors() throws InterruptedException {

        List<GossipMember> startupMembers = new ArrayList<>();
        startupMembers.add(new RemoteGossipMember("127.0.0.1", Helper.GOSSIP_PORT, "node1"));

        Node n1 = new Node("127.0.0.1","node1", Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startupMembers);
        Node n2 = new Node("127.0.0.2","node2", Helper.STORAGE_PORT, Helper.GOSSIP_PORT ,startupMembers);

        n1.start();
        n2.start();


        RequestSystemMsg msg = new RequestSystemMsg(AppMsg.OPERATION.PUT,"127.0.0.1", Helper.STORAGE_PORT,d1);
        n1.send("127.0.0.2", Helper.STORAGE_PORT, msg);


        Thread.sleep(5000);




    }
*/
}
