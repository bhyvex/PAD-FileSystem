package com.dido.pad;

import com.dido.pad.consistenthashing.ConsistentHasher;
import com.google.code.gossip.*;
import com.google.code.gossip.event.GossipListener;
import com.google.code.gossip.event.GossipState;
import com.google.code.gossip.examples.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by dido-ubuntu on 04/03/16.
 */
public class NodeGossipExample {

    /** The number of clients to start. */
    private static final int NUMBER_OF_CLIENTS = 4;

    public static void main(String[] args)
    {
        new Thread(NodeGossipExample::run).start();
        /*new Thread(() -> {).start();*/
    }

    /**
     * Constructor. This will start the this thread.
     */

    /**
     * @see Thread#run()
     */

    public static void run()
    {
        try {
            GossipSettings settings = new GossipSettings();
            List<Node> clients = new ArrayList<Node>();

            // Get my ip address.
            String myIpAddress = InetAddress.getLocalHost().getHostAddress();

            // Create the gossip members and put them in a list and give them a port number starting with 2000.
            List<GossipMember> startupMembers = new ArrayList<GossipMember>();
            for (int i = 0; i < NUMBER_OF_CLIENTS; ++i) {
                    startupMembers.add(new RemoteGossipMember(myIpAddress, 2000 + i, ""));
            }

            // Lets start the gossip clients.
            // Start the clients, waiting cleaning-interval + 1 second between them which will show the
            // dead list handling.
            int i=0;
            for (GossipMember member : startupMembers) {
                Node n = new Node(myIpAddress, "node "+Integer.toString(i));
                i++;
                n.addGossipService(member.getPort(), LogLevel.DEBUG, startupMembers, settings, n::gossipEvent);
                clients.add(n);
                n.startGossipService();
                sleep(settings.getCleanupInterval() + 1000);
            }

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
    }
/*
    public void gossipEvent(GossipMember member, GossipState state) {
        switch (state) {
            case UP:
                n.getConsistenHasher().addBucket(new Node(member));
                System.out.println(" ______________________UP  ------------");
            case DOWN:
                System.out.println(" ______________________DOWN ------------");

        };
    }*/


}
