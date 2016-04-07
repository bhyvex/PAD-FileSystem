package com.dido.pad.hashing;

import com.dido.pad.Node;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;

/**
 * Created by dido-ubuntu on 08/03/16.
 */
public interface iHasher<S> {

    interface BytesConverter<T>{
      byte[] convert( T data);
    }

    interface HashFunction{
         byte[] hash( byte[] input);
    }


    static class SHA1HashFunction implements HashFunction
    {
        @Override
        public byte[] hash(byte[] input)
        {
            Preconditions.checkNotNull(input);
            return Hashing.sha1().hashBytes(input).asBytes();
        }
    }
    // Helper implementations

    public static final HashFunction SHA1 = new SHA1HashFunction();

    static HashFunction getSHA1HashFunction()
    {
        return SHA1;
    }



    static BytesConverter<Node> getNodeToBytesConverter(){
        return new BytesConverter<Node>() {
            @Override
            public byte[] convert(Node node) {
                return (node.getIpAddress()+node.getId()).getBytes();
            }
        };
    }

/*
    public static BytesConverter<StorageData> getDataToBytesConverter(){

        return new BytesConverter<StorageData>() {
            @Override
            public byte[] convert(StorageData d) {
                return d.getKey().getBytes();
            }
        };
    }

}*/
}