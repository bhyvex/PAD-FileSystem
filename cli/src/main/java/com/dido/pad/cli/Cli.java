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

    public static void main(String[] args) throws InterruptedException, IOException {
        int NUM_NODES = 3;

        String ip = "127.0.0.254";
        String id = "client";
        ArrayList<GossipMember> st = new ArrayList<>();

        for(int i = 1 ; i <= NUM_NODES; i++)
            st.add(new RemoteGossipMember("127.0.0."+i, Helper.GOSSIP_PORT, "node"+i));


        Client c = new Client(ip,id, st);

        Thread.sleep(2000);

        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(System.in));

        String help = " put key value \n get key \n list ipAddress ";
        System.out.println("\nEnter a command (h for help):");
        while(true) {
            String input = bufferReader.readLine();
            String [] cmds = input.split("\\s+"); //slipt on ehite spaces

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

        }


    }

}