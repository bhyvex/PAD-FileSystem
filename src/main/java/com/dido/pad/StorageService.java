package com.dido.pad;

import com.dido.pad.consistenthashing.HashableData;
import com.dido.pad.consistenthashing.Hasher;
import com.dido.pad.consistenthashing.iHasher;
import com.dido.pad.datamessages.AppMsg;

import java.util.HashMap;

/**
 * Created by dido-ubuntu on 07/03/16.
 */
public class StorageService<V>{

    private Hasher<Node,HashableData> cHasher;
    private HashMap<String,V> _dataStore; //   key:value database
    private Node nodeDataService;
    private String IpAdress;
    private int port;

    public StorageService(Node node){
        this.cHasher  = new Hasher<>(1,iHasher.SHA1,iHasher.getNodeToBytesConverter());
        this.nodeDataService = node;
        _dataStore = new HashMap<String,V>();

    }

    public StorageService(String IpAddress, int port){
        this.IpAdress = IpAddress;
        this.port = port;
    }



    public Hasher<Node, HashableData> getcHasher() {
        return cHasher;
    }

    //TODO open a UDP connection and send messages:  add (data), get (data)
    public void run(){
        // receive a UDP message


    }

    public void receive(AppMsg msg){
        switch (msg.getType()) {
            case REQUEST:
                this.mangageRequest(msg);
                break;
            case REPLY:
                break;
            case CONTROL:
                break;
        }



    }

    private void mangageRequest(AppMsg msg){
        switch (msg.getOperation()) {
            case PUT:
                //insert new data in the store
                Node node=  this.cHasher.getServerForData(msg.getPayload());
                if(node.equals(this.nodeDataService))
                    this._dataStore.put(msg.getPayload().getKey(), (V) msg.getPayload().getValue());
                else
                    send(msg,node);
                break;
            case GET:
                break;
            case LIST:
                break;
        }
    }

    //TODO send a message ( DEST, TYPE MSG, TYPE OP)
    public void send(AppMsg msg, Node n){
        n.get_dataService().receive(msg);
    }

    public V getValueFromKey(String key){
        if(_dataStore.containsKey(key))
         return _dataStore.get(key);
        else
            return null;
    }


}
