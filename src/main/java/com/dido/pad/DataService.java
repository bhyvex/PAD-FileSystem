package com.dido.pad;

import com.dido.pad.consistenthashing.Hasher;
import com.dido.pad.consistenthashing.iHasher;

/**
 * Created by dido-ubuntu on 07/03/16.
 */
public class DataService<D extends AbstractData>{

    private Hasher<Node,D> cHasher;

    public DataService(){
        this.cHasher  = new Hasher<>(1,iHasher.SHA1,iHasher.getNodeToBytesConverter());

    }


    //TODO open a UDP connection and send messages:  add (data), get (data)
    public void run(){

        // receive a UDP message

    }

    //TODO send a message ( DEST, TYPE MSG, TYPE OP)
    public void send(){

    }

    public Hasher<Node, D> getHasher(){
        return cHasher;
    }


}
