import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.datamessages.AppMsg;
import com.dido.pad.datamessages.RequestAppMsg;
import com.google.code.gossip.*;
import com.google.code.gossip.manager.GossipManager;
import com.google.common.collect.LinkedListMultimap;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
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
        int seedNodes = 2;
        List<GossipMember> startupMembers = new ArrayList<>();
        for (int i = 1; i < seedNodes+1; ++i) {
            startupMembers.add(new RemoteGossipMember("127.0.0." + i, Helper.GOSSIP_PORT, i + ""));
        }

        //create three local client
        List<Node> clients = new ArrayList<>();
        int clusterMembers = 3;
        for (int i = 1; i < clusterMembers+1; ++i) {
            Node node = new Node("127.0.0." + i, "node" + i, Helper.STORAGE_PORT, Helper.GOSSIP_PORT);
            //GossipService gossipService = new GossipService("127.0.0." + i, 2000, i + "",
            //        LogLevel.DEBUG, startupMembers, settings, null);
            clients.add(node);
        }

        for(int i=0; i < clients.size(); i++){
            Node node = clients.get(i);
            try {
                node.startGossipService(LogLevel.DEBUG,startupMembers,settings,node::gossipEvent );
                node.startStorageService();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(5000);
            clients.get(0).send ("127.0.0.3",Helper.STORAGE_PORT, new RequestAppMsg<String>(AppMsg.OPERATION.PUT,"keyprova","hello"));
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
