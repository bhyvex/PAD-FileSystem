package com.dido.pad.consistenthashing;

import com.dido.pad.Node;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by dido-ubuntu on 08/03/16.
 */
public class Hasher<T> implements iHasher<T>{

    public static final Logger LOGGER = Logger.getLogger(Hasher.class);

    private final int startVirtualNodeId, stopVirtualNodeId;
    private final HashFunction hashFunction;
    private final BytesConverter<T> nodeToByteConverter;

    private final NavigableMap<ByteBuffer, T> serversMap;
    private  HashMap<T, ArrayList<ByteBuffer>> virtualForServer; // <Node :list< vitualNodes>> :for each Node list its virtual nodes


    public Hasher(final int virtulaNodes,final HashFunction hash, final BytesConverter<T> nodetoByteConverter) {

        Preconditions.checkNotNull(hash,"HashFunction can not be null.");

        this.hashFunction = hash;
        this.nodeToByteConverter = nodetoByteConverter;

        this.startVirtualNodeId = 1;
        this.stopVirtualNodeId = (virtulaNodes > 0) ? virtulaNodes : 1;

        this.serversMap = new ConcurrentSkipListMap<ByteBuffer, T>();
        this.virtualForServer = new HashMap<>();

    }

    @Override
    synchronized public void addServer(T server) {
        Preconditions.checkNotNull(server, "Server name can not be null");
        ArrayList<ByteBuffer> virtBuckets = new ArrayList<>();
        for (int virtualNodeId = startVirtualNodeId; virtualNodeId <= stopVirtualNodeId; virtualNodeId++) {
            ByteBuffer virtBucket = convertAndApplyHash(virtualNodeId, server);
            serversMap.put(virtBucket, server);
            virtBuckets.add(virtBucket);
        }
        virtualForServer.put(server,virtBuckets);
    }

    @Override
    synchronized public void removeServer(T server) {
        Preconditions.checkNotNull(server, "Server name can not be null");
        for(int virtID= startVirtualNodeId; virtID <= stopVirtualNodeId; virtID++){
            ByteBuffer bbServerVirtuals = convertAndApplyHash(virtID,server);
            serversMap.remove(bbServerVirtuals);
        }
        virtualForServer.remove(server);

    }


    private ByteBuffer convertAndApplyHash(int nodeID, T server){

        byte[] bucketNameInBytes =  hashFunction.hash(nodeToByteConverter.convert(server));
        byte[] bucketNameAndCode = new byte[(Integer.BYTES / Byte.BYTES) + bucketNameInBytes.length];
        ByteBuffer bb = ByteBuffer.wrap(bucketNameAndCode);
        bb.put(bucketNameInBytes);
        bb.putInt(nodeID);
        return ByteBuffer.wrap(hashFunction.hash(bucketNameAndCode));
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
        List<T> nodes = new ArrayList<>();
        nodes.addAll(virtualForServer.keySet());
        return nodes;
    }

    public boolean containsNode(T node){
        return this.serversMap.containsKey( nodeToByteConverter.convert(node));
    }

    public void printkeyValueHash(){
        SortedMap<ByteBuffer, T> sorted = serversMap.tailMap(serversMap.firstKey());
        Iterator<ByteBuffer> iter =   sorted.keySet().iterator();
        while(iter.hasNext()){
            ByteBuffer bb = iter.next();
            T server = serversMap.get(bb);
            System.out.println("Hash: "+ byteBufferToLong(bb)+" Value: "+ server); // byteBufferToLong(bb)
        }
        System.out.println(" ");

    }

    private long byteBufferToLong(ByteBuffer bb){
        BigInteger bigint = new BigInteger(1,bb.array());
        return bigint.longValue();
    }


    synchronized public NavigableMap<ByteBuffer, T> getServersMap() {
        return serversMap;
    }

    public ArrayList<T> getPreferenceList(T server, int number){
        Preconditions.checkArgument(number <= virtualForServer.keySet().size(), "The number of node present is less than the preference list size required");

        ArrayList<ByteBuffer> virtuals = virtualForServer.get(server);

        ByteBuffer bbNext = virtuals.get(0);     // first entry is the Bytebuffer of the first physical server.
        ArrayList<T> nexts = new ArrayList<>();

        while(number > 0) {
            bbNext = serversMap.higherKey(bbNext);
            if (bbNext == null) { // there is no they greater than the server key.
                bbNext = serversMap.firstKey();
            }
            if(!virtuals.contains(bbNext) && !nexts.contains(serversMap.get(bbNext))){
                nexts.add(serversMap.get(bbNext));
                number--;
            }
        }
        return nexts;
    }


}
