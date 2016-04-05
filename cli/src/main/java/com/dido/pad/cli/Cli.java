package com.dido.pad.cli;

import com.dido.pad.Helper;
import com.dido.pad.cli.client.Client;
import com.google.code.gossip.GossipMember;
import com.google.code.gossip.RemoteGossipMember;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Cli{

    public  BufferedReader bufferReader = new BufferedReader(new InputStreamReader(System.in));
    //private Client client;

   public Cli(){
       //this.client = client;
     /* usage = " usage:\n " +
              "\t put key value   : put the <key:value> into the system \n"  +
              "\t get key         : retrieve the value associated to te key \n"+
              "\t list ipAddress  : lists the pairs <key:value> into the database of ipAddress \n"+
              "\t force key value ip : snd to the specidifc ip the key value \n"+
              "\t nodes           : shows the nodes active inthe system \n";*/
   }

  /*  public void printUsage(){
        System.out.print(usage);
    }

    public synchronized String readlLine() throws IOException {
        return bufferReader.readLine();
    }*/
  public synchronized static String scanString() throws IOException{
      //Scanner sc = new Scanner(System.in);
      System.out.println("\nInsert a command or a choice (h for usage message):");
      Scanner sc = new Scanner(new FilterInputStream(System.in){public void close(){}});
      String res = sc.nextLine();
      sc.close();
      return res;
  }

    public static void main(String[] args) {

    int NUM_NODES = 1;
    String ip = "127.0.0.254";
    String id = "client";
    ArrayList<GossipMember> st = new ArrayList<>();

    for(int i = 1 ; i <= NUM_NODES; i++)
            st.add(new RemoteGossipMember("127.0.0."+i, Helper.GOSSIP_PORT, "node"+i));

    Client client = new Client(ip, id, st);
        String usage = " usage:\n " +
                "\t put key value   : put the <key:value> into the system \n"  +
                "\t get key         : retrieve the value associated to te key \n"+
                "\t list ipAddress  : lists the pairs <key:value> into the database of ipAddress \n"+
                "\t force key value ip : snd to the specidifc ip the key value \n"+
                "\t nodes           : shows the nodes active inthe system \n";


        // public void startCli() {
        while (true) {
            String input;
            try {
                input = scanString();

                String[] cmds = input.split("\\s+"); //splits  white spaces

                switch (cmds[0]) {
                    case ("get"):
                        client.get(cmds[1]);
                        break;
                    case ("put"):
                        client.put(cmds[1], cmds[2]);
                        break;
                    case ("list"):
                        client.list(cmds[1]);
                        break;
                    case ("nodes"):
                        System.out.print(client.getClientService().getcHasher().getAllNodes());
                        break;
                    case ("force"):
                        client.force(cmds[1], cmds[2], cmds[3]);
                        break;
                    case ("h"):
                        System.out.println(usage);

                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

       /* public static void main(String[] args) {

        int NUM_NODES = 1;
        //int NUM_NODES = 3

        String ip = "127.0.0.254";
        String id = "client";
        ArrayList<GossipMember> st = new ArrayList<>();

        for(int i = 1 ; i <= NUM_NODES; i++)
            st.add(new RemoteGossipMember("127.0.0."+i, CliHelper.GOSSIP_PORT, "node"+i));

        Client client = new Client(ip, id, st);


        String help = " usage:\n " +
                "\t put key value   : put the <key:value> into the system \n"  +
                "\t get key         : retrieve the value associated to te key \n"+
                "\t list ipAddress  : lists the pairs <key:value> into the database of ipAddress \n"+
                "\t force key value ip : snd to the specidifc ip the key value \n"+
                "\t nodes           : shows the nodes active inthe system \n";

        while(true) {
            System.out.println("\nInsert a command (h for usage message):");
            try {
                synchronized( Cli.bufferReader){
                input =  Cli.bufferReader.readLine();
                }

            String [] cmds = input.split("\\s+"); //splits  white spaces

            switch (cmds[0]) {
                case ("get"):
                    client.get(cmds[1]);
                    break;
                case ("put"):
                    client.put(cmds[1], cmds[2]);
                    break;
                case ("list"):
                    client.list(cmds[1]);
                    break;
                case ("nodes"):
                    System.out.print(client.getClientService().getcHasher().getAllNodes());
                    break;
                case ("force"):
                    client.force(cmds[1],cmds[2],cmds[3]);
                    break;
                case ("h"):
                    System.out.println(help);

            }

            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

        }


    }*/

}