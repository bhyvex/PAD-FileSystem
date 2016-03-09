package com.dido.pad.consistenthashing;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multiset;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import com.sun.org.apache.xml.internal.serializer.utils.SystemIDResolver;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by dido-ubuntu on 08/03/16.
 */
public class Hasher<S,D> implements iHasher<S,D>{

    private final int startVirtualNodeId, stopVirtualNodeId;
    private final HashFunction hashFunction;
    private final BytesConverter<S> serverToBytesConverter;
    private final BytesConverter<D> dataToBytesConverter;

    private final NavigableMap<ByteBuffer, S> serversMap;

    public Hasher(final int virtulaNodes,
                  final HashFunction hash,
                  final BytesConverter<S> serverToBytesConverter,
                  final BytesConverter<D> dataToBytesConverter) {

        Preconditions.checkNotNull(serverToBytesConverter, "Server data converter can not be null.");
        Preconditions.checkNotNull(dataToBytesConverter, "data converter can not be null.");
        Preconditions.checkNotNull(hash,			   "HashFunction can not be null.");

        this.hashFunction = hash;
        this.serverToBytesConverter = serverToBytesConverter;
        this.dataToBytesConverter = dataToBytesConverter;

        this.startVirtualNodeId = 1;
        this.stopVirtualNodeId = (virtulaNodes > 0) ? virtulaNodes : 1;

        this.serversMap = new ConcurrentSkipListMap<>();

    }
    @Override
    public void addServer(S server) {
        Preconditions.checkNotNull(server, "Server name can not be null");
        List<ByteBuffer> virtBuckets = new ArrayList<>();
        for (int virtualNodeId = startVirtualNodeId; virtualNodeId <= stopVirtualNodeId; virtualNodeId++) {
            ByteBuffer virtBucket = convertAndApplyHash(virtualNodeId, server);
            serversMap.put(virtBucket, server);
            virtBuckets.add(virtBucket);
        }
    }


    private ByteBuffer convertAndApplyHash(int nodeID, S server){

        byte[] bucketNameInBytes =  hashFunction.hash(this.serverToBytesConverter.convert(server));
        byte[] bucketNameAndCode = new byte[(Integer.BYTES / Byte.BYTES) + bucketNameInBytes.length];
        ByteBuffer bb = ByteBuffer.wrap(bucketNameAndCode);
        bb.put(bucketNameInBytes);
        bb.putInt(nodeID);
        return ByteBuffer.wrap(hashFunction.hash(bucketNameAndCode));
    }

    @Override
    public void removeServer(S server) {
        Preconditions.checkNotNull(server, "Server name can not be null");
        for(int virtID= startVirtualNodeId; virtID < startVirtualNodeId+ stopVirtualNodeId; virtID++){
            ByteBuffer bbServerVirtuals = convertAndApplyHash(virtID,server);
            serversMap.remove(bbServerVirtuals);
        }
    }

    private ByteBuffer convertAndApplyHash(D data){
        byte[] bHashData = hashFunction.hash(dataToBytesConverter.convert(data));
        ByteBuffer bbData = ByteBuffer.wrap(bHashData);
        return  bbData;
    }

    @Override
    public S getServerForData(D data) throws Exception {
        ByteBuffer bbData = convertAndApplyHash(data);

        ByteBuffer nearServer = serversMap.ceilingKey(bbData);

        if(nearServer==null) {
            S server = serversMap.firstEntry().getValue();
            return server;
        }
        else {
            S server = serversMap.get(nearServer);
            return server;
        }
    }

    public List<S> getAllNodes(){
        return new ArrayList<>(serversMap.values());
    }

    public void printkeyValueHash(){
        Iterator<ByteBuffer> iter =  serversMap.navigableKeySet().descendingIterator();

        while(iter.hasNext()){
            ByteBuffer bb = iter.next();
            S server = serversMap.get(bb);
            System.out.println("Hash: "+ byteBufferToLong(bb) +" Value: "+ server);
        }

    }

    private long byteBufferToLong(ByteBuffer bb){
        BigInteger bigint = new BigInteger(1,bb.array());
        return bigint.longValue();
    }


    public NavigableMap<ByteBuffer, S> getServersMap() {
        return serversMap;
    }


}
