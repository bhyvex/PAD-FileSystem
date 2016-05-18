package com.dido.pad;

import com.beust.jcommander.JCommander;
import com.google.code.gossip.GossipSettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Created by dido-ubuntu on 22/03/16.
 */
public class PadFsNode {


    public static void main(String[] args){

        ParseArgs jct = new ParseArgs();

        JCommander jCommander= new JCommander(jct, args);
        jCommander.setProgramName("PadFsNode");
        if(jct.isHelp()){
            jCommander.usage();
            return;
        }

        Node node = new Node(jct.getIp(), jct.getId(), jct.getStoragePort(), jct.getGossipPort(), jct.getGossipMember(), new GossipSettings());
        node.start();


        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(System.in));

        String help = "ID "+ node.getId() + " usage: \n " +
                      "\tdisconnect : disconnect the node from the network (it will not respond to message)\n" +
                      "\tconnect    : connect the node into the network ";

        while(true) {
            System.out.println("\n Insert a command [h for usage message]...");
            String input = null;
            try {
                input = bufferReader.readLine();
                String [] cmds = input.split("\\s+"); //splits white spaces and return a list of strings

                switch (cmds[0]) {
                    case ("disconnect"):
                        node.disconnect();
                        break;
                    case ("connect"):
                        node.connect();
                        break;
                    case ("h"):
                        System.out.println(help);
                        break;
                    default:
                        System.out.println(help);
                }

            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

        }


    }



}
