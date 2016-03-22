import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.datamessages.AppMsg;
import com.dido.pad.datamessages.RequestAppMsg;
import com.google.code.gossip.*;
import org.junit.Assert;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dido-ubuntu on 09/03/16.
 */
public class TestGossipStorageService {

    private static final int NUM_NODES = 3;

    /*
        @Test
        public void testDataInsert(){

            //startup gossip member
            GossipSettings settings = new GossipSettings();
            int seedNodes = 1;
            List<GossipMember> startupMembers = new ArrayList<>();
            for (int i = 1; i < seedNodes+1; ++i) {
                startupMembers.add(new RemoteGossipMember("127.0.0." + i, Helper.GOSSIP_PORT, "node"+i));
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
                    node.startGossipService(LogLevel.DEBUG, startupMembers,settings,node::gossipEvent );
                    node.startStorageService();
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
                    Assert.assertEquals(NUM_NODES-1, clients.get(i).getGossipmanager().getMemberList().size());
                    Assert.assertEquals(NUM_NODES, clients.get(i).get_storageService().getcHasher().getServersMap().values().size());

                }

                String key="Davide";
                AppMsg req = new RequestAppMsg<String>(AppMsg.OPERATION.PUT, key, "Neri");
                clients.get(0).send("127.0.0.3", Helper.STORAGE_PORT, req);

                Thread.sleep(3000);
                //check if 127.0.0.3 has received the key
                Assert.assertTrue(clients.get(2).get_storageService().getStorage().containsKey(key));


                AppMsg req1 = new RequestAppMsg<String>(AppMsg.OPERATION.GET, key, "");
                clients.get(0).send("127.0.0.3",Helper.STORAGE_PORT, req1);

                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        */
/// test with restructurin the node creation
    @Test
    public void testNodeQuorum() {

        //startup gossip member
        GossipSettings settings = new GossipSettings();
        int seedNodes = 1;
        List<GossipMember> startupMembers = new ArrayList<>();
        for (int i = 1; i < seedNodes + 1; ++i) {
            startupMembers.add(new RemoteGossipMember("127.0.0." + i, Helper.GOSSIP_PORT, "node" + i));
        }

        //create three local client
        List<Node> clients = new ArrayList<>();
        int clusterMembers = 3;
        for (int i = 1; i < clusterMembers + 1; ++i) {
            Node node = new Node("127.0.0." + i, "node" + i, Helper.STORAGE_PORT, Helper.GOSSIP_PORT, startupMembers, settings);
            clients.add(node);
        }

        for (Node n : clients) {
            n.start();
        }

        try {
            Thread.sleep(15000);

            //Check if the nodes discovered each other
            for (int i = 0; i < clusterMembers; ++i) {
                Assert.assertEquals((NUM_NODES * (Helper.NUM_NODES_VIRTUALS)) - 1, clients.get(i).getGossipmanager().getMemberList().size());
            }

            String key = "Davide";
            AppMsg req = new RequestAppMsg<>(AppMsg.OPERATION.PUT, key, "Neri");
            clients.get(2).sendToStorage(req);

            Thread.sleep(10000);

            //check if 127.0.0.3 has received the key
            Assert.assertTrue(clients.get(2).get_storageService().getStorage().containsKey(key));

            AppMsg reqUpdate = new RequestAppMsg<>(AppMsg.OPERATION.PUT, key, "giangrande");
            clients.get(2).sendToStorage(reqUpdate);

            Thread.sleep(10000);

            AppMsg req1 = new RequestAppMsg<>(AppMsg.OPERATION.GET, key, "");
            clients.get(2).sendToStorage(req1);


            Thread.sleep(10000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
