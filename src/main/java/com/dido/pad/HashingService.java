package com.dido.pad;

import com.dido.pad.consistenthashing.ConsistentHasher;
import com.dido.pad.consistenthashing.ConsistentHasherImpl;

/**
 * Created by dido-ubuntu on 07/03/16.
 */
public class HashingService {

    private ConsistentHasher<Node, String> cHasher;

    public HashingService(){
        this.cHasher  = new ConsistentHasherImpl<>(
                1,
                ConsistentHasher.getNodeToBytesConverter(),
                ConsistentHasher.getStringToBytesConverter(),
                ConsistentHasher.SHA1);

    }

    //TODO open a UDP connection and send messages:  add (data), get (data)
    public void run(){

        // receive a UDP message

    }

    //TODO send a message ( DEST, TYPE MSG, TYPE OP)
    public void send(){


    }

    public ConsistentHasher<Node, String> getHasher(){
        return cHasher;
    }

    public void addBucket(Node node){
        this.cHasher.addBucket(node);
    }


    public void removeBucket(Node node) throws InterruptedException {
        this.cHasher.removeBucket(node);
    }
}
