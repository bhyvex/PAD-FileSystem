package com.dido.pad.hashing;

import com.dido.pad.Node;
import com.dido.pad.data.Versioned;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by dido-ubuntu on 08/03/16.
 */
public class Hasher<T> implements IHasher<T> {

//    public static final Logger LOGGER = Logger.getLogger(Hasher.class);

    private final IHashFunction hashFunction;
    private final IByteConverter<T> nodeToByteConverter;

    private final NavigableMap<ByteBuffer, T> serversMap;

    public Hasher(final IHashFunction hash, final IByteConverter<T> nodetoByteConverter) {
        Preconditions.checkNotNull(hash, "HashFunction can not be null.");

        this.hashFunction = hash;
        this.nodeToByteConverter = nodetoByteConverter;

        this.serversMap = new ConcurrentSkipListMap<>();

    }

    @Override
    synchronized public void addServer(T server) {
        Preconditions.checkNotNull(server, "Server name can not be null");

        ByteBuffer virtBucket = convertAndApplyHash(server);
        serversMap.put(virtBucket, server);
    }

    @Override
    synchronized public void removeServer(T server) {
        Preconditions.checkNotNull(server, "Server name can not be null");
        ByteBuffer bbServerVirtuals = convertAndApplyHash(server);
        serversMap.remove(bbServerVirtuals);
    }


    private ByteBuffer convertAndApplyHash(T server) {
        byte[] bucketNameInBytes = hashFunction.hash(nodeToByteConverter.convert(server));
        return ByteBuffer.wrap(hashFunction.hash(bucketNameInBytes));
    }


    synchronized public T getServerForData(String key) {
        byte[] bHashData = hashFunction.hash(key.getBytes());
        ByteBuffer bbData = ByteBuffer.wrap(bHashData);
        ByteBuffer nearServer = serversMap.ceilingKey(bbData);
        if (nearServer == null) {
            return serversMap.firstEntry().getValue();
        } else {
            return serversMap.get(nearServer);
        }
    }

    synchronized  public ArrayList<Versioned> getDataInterval(Collection<Versioned> data, T start, T end){
        ArrayList<Versioned> interval = new ArrayList<>();
        ByteBuffer s = convertAndApplyHash(start);
        ByteBuffer e = convertAndApplyHash(end);

        for(Versioned v : data){
            byte[] bHashData = hashFunction.hash(v.getData().getKey().getBytes());
            ByteBuffer bbData = ByteBuffer.wrap(bHashData);
            if( bbData.compareTo(s) >0  && bbData.compareTo(e)<= 0 ) // negative = less; zero = eqaul; positive = greater
                interval.add(v);
        }
        return interval;

    }

    synchronized  public ArrayList<T> getAllNodes() {
        return  new ArrayList<>(serversMap.values());
    }

    public boolean containsNode(T node) {
        ByteBuffer bbServerVirtuals = convertAndApplyHash(node);
        return this.serversMap.containsKey(bbServerVirtuals);
    }


    private long byteBufferToLong(ByteBuffer bb) {
        BigInteger bigint = new BigInteger(1, bb.array());
        return bigint.longValue();
    }


    synchronized public NavigableMap<ByteBuffer, T> getServersMap() {
        return serversMap;
    }

    /**
     * Get next physical servers associated after.
     * @param server starting node server
     * @return list of Nodes
     */
    public ArrayList<T> getNextServers(T server, int number) {
        Preconditions.checkArgument(number > 0 , "number of next servers cannot be negative");
        ByteBuffer myByte = convertAndApplyHash(server);
        ByteBuffer bbNext= convertAndApplyHash(server);
        ArrayList<T> nexts = new ArrayList<>();
        if(serversMap.containsKey(bbNext)) { // contains the bytebuffer of the server passes as argument
            while (number > 0) {
                bbNext = serversMap.higherKey(bbNext);
                if (bbNext == null ){//& !bbNext.equals(myByte) ){ // there is no keys greater than the server key.
                    bbNext = serversMap.firstKey();
                    if( !nexts.contains(serversMap.get(bbNext))) {//!bbNext.equals(myByte) &&){ //if is not the node server passed as argument
                        nexts.add(serversMap.get(bbNext));
                        number--;
                    }else {  //if is the last server, and the next if myself
                        bbNext = serversMap.higherKey(bbNext);
                    }
                }
                else if (!nexts.contains(serversMap.get(bbNext))&& !bbNext.equals(myByte)) {
                    nexts.add(serversMap.get(bbNext));
                    number--;
                }
            }
        }
        return nexts;
    }


    public ArrayList<T> getPreviousServer(T server, int number){
       // ArrayList<ByteBuffer> virtuals = virtualForServer.get(server);

        ByteBuffer bbPrevious = convertAndApplyHash(server);     // first entry is the Bytebuffer of the  physical server.
        ArrayList<T> privious = new ArrayList<>();

        if(serversMap.containsKey(bbPrevious)) {
            while (number > 0) {
                bbPrevious = serversMap.lowerKey(bbPrevious);
                if (bbPrevious == null) { // there is no they less than the server key.
                    bbPrevious = serversMap.lastKey();
                }
                if ( !privious.contains(serversMap.get(bbPrevious))) {
                    privious.add(serversMap.get(bbPrevious));
                    number--;
                }
            }
        }
        return privious;
    }


}
