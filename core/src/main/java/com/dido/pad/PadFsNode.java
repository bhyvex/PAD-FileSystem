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

        System.out.println("\nInsert a command [h for usage message]...\n");

        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(System.in));

        String help = "\n["+ node.getId() + "] usage: \n" +
                      "\tdisconnect : disconnect the node from the network (it will not respond to message)\n" +
                      "\tconnect    : connect the node into the network \n"+
                      "\th          : show this help";

        while(true) {
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
