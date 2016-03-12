import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.google.code.gossip.GossipMember;
import com.google.code.gossip.manager.GossipManager;
import com.google.common.collect.LinkedListMultimap;
import org.junit.Test;

import java.util.LinkedList;

/**
 * Created by dido-ubuntu on 09/03/16.
 */
public class testGossipStorageService {

    private static final int NUM_NODES = 3;

    @Test
    public void testDataInsert(){

        LinkedList<Node> nodes = new LinkedList<Node>();
        for(int i=0; i < NUM_NODES; i++)  {
            Node n =new Node("127.0.0."+i,"node"+i, Helper.STORAGE_PORT);
            nodes.add(n);
        }

        LinkedList<GossipMember> startMenber = new LinkedList<>();





    }
}
