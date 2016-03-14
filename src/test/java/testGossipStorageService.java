import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.datamessages.AppMsg;
import com.dido.pad.datamessages.RequestAppMsg;
import com.google.code.gossip.*;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.text.html.HTMLEditorKit;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dido-ubuntu on 09/03/16.
 */
public class testGossipStorageService {

    private static final int NUM_NODES = 3;

    @Test
    public void testDataInsert(){

        //starup gossi member
        GossipSettings settings = new GossipSettings();
        int seedNodes = 1;
        List<GossipMember> startupMembers = new ArrayList<>();
        for (int i = 1; i < seedNodes+1; ++i) {
            startupMembers.add(new RemoteGossipMember("127.0.0." + i, Helper.GOSSIP_PORT, ""+i));
        }

        //create three local client
        List<Node> clients = new ArrayList<>();
        int clusterMembers = 3;
        for (int i = 1; i < clusterMembers+1; ++i) {
            Node node = new Node("127.0.0." + i, "node" + i, Helper.STORAGE_PORT, Helper.GOSSIP_PORT);
            clients.add(node);
        }

        for(int i=0; i < clients.size(); i++){
            Node node = clients.get(i);
            try {
                node.start_Gossip_Storage_Service(LogLevel.DEBUG,startupMembers,settings,node::gossipEvent );
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(10000);
            //Check if the nodes discovered each other
            for (int i = 0; i < clusterMembers; ++i) {
                Assert.assertEquals(2, clients.get(i).getGossipmanager().getMemberList().size());
            }

            String key="Davide";

            AppMsg req = new RequestAppMsg<String>(AppMsg.OPERATION.PUT,key,"Neri");
            clients.get(0).send("127.0.0.3", Helper.STORAGE_PORT, req);

            AppMsg req1 = new RequestAppMsg<String>(AppMsg.OPERATION.GET,key,"");
            clients.get(0).send("127.0.0.3",Helper.STORAGE_PORT, req1);

            clients.get(0).send("127.0.0.3", Helper.STORAGE_PORT,new RequestAppMsg<String>(AppMsg.OPERATION.LIST,"",""));


            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
