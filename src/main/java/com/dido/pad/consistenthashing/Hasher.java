package com.dido.pad.consistenthashing;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by dido-ubuntu on 08/03/16.
 */
public class Hasher<T> implements iHasher<T>{

    public static final Logger LOGGER = Logger.getLogger(Hasher.class);

    private final int startVirtualNodeId, stopVirtualNodeId;
    private final HashFunction hashFunction;
    private final BytesConverter<T> nodeToByteConverter;

    private final NavigableMap<ByteBuffer, T> serversMap;


    public Hasher(final int virtulaNodes,final HashFunction hash, final BytesConverter<T> nodetoByteConverter) {

        Preconditions.checkNotNull(hash,"HashFunction can not be null.");

        this.hashFunction = hash;
        this.nodeToByteConverter = nodetoByteConverter;

        this.startVirtualNodeId = 1;
        this.stopVirtualNodeId = (virtulaNodes > 0) ? virtulaNodes : 1;

        this.serversMap = new ConcurrentSkipListMap<ByteBuffer, T>();

    }
    @Override
    synchronized public void addServer(T server) {
        Preconditions.checkNotNull(server, "Server name can not be null");
        List<ByteBuffer> virtBuckets = new ArrayList<>();
        for (int virtualNodeId = startVirtualNodeId; virtualNodeId <= stopVirtualNodeId; virtualNodeId++) {
            ByteBuffer virtBucket = convertAndApplyHash(virtualNodeId, server);
            serversMap.put(virtBucket, server);
            virtBuckets.add(virtBucket);
        }

    }


    private ByteBuffer convertAndApplyHash(int nodeID, T server){

        byte[] bucketNameInBytes =  hashFunction.hash(nodeToByteConverter.convert(server));
        byte[] bucketNameAndCode = new byte[(Integer.BYTES / Byte.BYTES) + bucketNameInBytes.length];
        ByteBuffer bb = ByteBuffer.wrap(bucketNameAndCode);
        bb.put(bucketNameInBytes);
        bb.putInt(nodeID);
        return ByteBuffer.wrap(hashFunction.hash(bucketNameAndCode));
    }

    @Override
    synchronized public void removeServer(T server) {
        Preconditions.checkNotNull(server, "Server name can not be null");
        for(int virtID= startVirtualNodeId; virtID < startVirtualNodeId+ stopVirtualNodeId; virtID++){
            ByteBuffer bbServerVirtuals = convertAndApplyHash(virtID,server);
            serversMap.remove(bbServerVirtuals);
        }

    }


    synchronized public T getServerForData(String key){
        byte[] bHashData = hashFunction.hash(key.getBytes());
        ByteBuffer bbData = ByteBuffer.wrap(bHashData);
        ByteBuffer nearServer = serversMap.ceilingKey(bbData);
        if(nearServer==null) {
           T server = serversMap.firstEntry().getValue();
           return server;
        }
        else {
            T server = serversMap.get(nearServer);
            return server;
        }
    }

    public List<T> getAllNodes(){
        return new ArrayList<>(serversMap.values());
    }

    public void printkeyValueHash(){
        Iterator<ByteBuffer> iter =  serversMap.navigableKeySet().descendingIterator();

        while(iter.hasNext()){
            ByteBuffer bb = iter.next();
            T server = serversMap.get(bb);
            System.out.println("Hash: "+ byteBufferToLong(bb) +" Value: "+ server);
        }

    }

    private long byteBufferToLong(ByteBuffer bb){
        BigInteger bigint = new BigInteger(1,bb.array());
        return bigint.longValue();
    }


    public NavigableMap<ByteBuffer, T> getServersMap() {
        return serversMap;
    }


}
