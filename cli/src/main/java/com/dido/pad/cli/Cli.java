package com.dido.pad.cli;

import com.dido.pad.Helper;
import com.google.code.gossip.GossipMember;
import com.google.code.gossip.RemoteGossipMember;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Cli{

    public static void main(String[] args) {
        int NUM_NODES = 3;

        String ip = "127.0.0.254";
        String id = "client";
        ArrayList<GossipMember> st = new ArrayList<>();

        for(int i = 1 ; i <= NUM_NODES; i++)
            st.add(new RemoteGossipMember("127.0.0."+i, Helper.GOSSIP_PORT, "node"+i));
        Client c = new Client(ip,id, st);


        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(System.in));

        String help = " usage: \n put key value \n get key \n list ipAddress ";

        while(true) {
            System.out.println("\nEnter a command (h for help):");
            String input = null;
            try {
                input = bufferReader.readLine();

            String [] cmds = input.split("\\s+"); //slipts  white spaces

            switch (cmds[0]) {
                case ("get"):
                    c.get(cmds[1]);
                    break;
                case ("put"):
                    c.put(cmds[1], cmds[2]);
                    break;
                case ("list"):
                    c.list(cmds[1]);
                    break;

                case ("h"):
                    System.out.println(help);

            }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

        }


    }

}