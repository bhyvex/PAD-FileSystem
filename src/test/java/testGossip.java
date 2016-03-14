import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.datamessages.AppMsg;
import com.dido.pad.datamessages.RequestAppMsg;
import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.LogLevel;
import com.google.code.gossip.RemoteGossipMember;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by dido-ubuntu on 09/03/16.
 */
public class testGossip {

    @Test
    public void testGossipUP(){

/*        try {

            int NUMBER_OF_CLIENTS = 2;
            GossipSettings settings = new GossipSettings();
            List<Node> clients = new ArrayList<Node>();

            // Get my ip address.
            String myIpAddress = InetAddress.getLocalHost().getHostAddress();

            // Create the gossip members and put them in a list and give them a port number starting with 2000.
            List<GossipMember> startupMembers = new ArrayList<GossipMember>();
            for (int i = 0; i < NUMBER_OF_CLIENTS; ++i) {
                startupMembers.add(new RemoteGossipMember("127.0.0."+i, 2000 + i, ""));
            }

            // Lets start the gossip clients.
            // Start the clients, waiting cleaning-interval + 1 second between them which will show the
            // dead list handling.
            int i=1;
            for (GossipMember member : startupMembers) {
                Node n = new Node("127.0.0."+i, "node "+Integer.toString(i), Helper.STORAGE_PORT);
                n.start_Gossip_Storage_Service(member.getPort(), LogLevel.DEBUG, startupMembers, settings, n::gossipEvent);
                n.startStorageService();
                clients.add(n);
                i++;
                sleep(settings.getCleanupInterval() + 1000);
            }
            clients.get(0).send("127.0.0.1",Helper.STORAGE_PORT, new RequestAppMsg<String>(AppMsg.OPERATION.PUT,"key1"," ciao"));

            // After starting all gossip clients, first wait 10 seconds and then shut them down.
            sleep(10000);
            System.err.println("Going to shutdown all services...");
            // Since they all run in the same virtual machine and share the same executor, if one is shutdown they will all stop.
            clients.get(0).shutdown();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
   }

}
