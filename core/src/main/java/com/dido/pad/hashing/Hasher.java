package com.dido.pad.hashing;

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

    private final int startVirtualNodeId, stopVirtualNodeId;
    private final IHashFunction hashFunction;
    private final IByteConverter<T> nodeToByteConverter;

    private final NavigableMap<ByteBuffer, T> serversMap;
    private HashMap<T, ArrayList<ByteBuffer>> virtualForServer; // <Node :list< vitualNodes>> :for each Node list its virtual nodes


    public Hasher(final int virtulaNodes, final IHashFunction hash, final IByteConverter<T> nodetoByteConverter) {

        Preconditions.checkNotNull(hash, "HashFunction can not be null.");

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
        virtualForServer.put(server, virtBuckets); //first entry is the bytebuffer of the server itself.
    }

    @Override
    synchronized public void removeServer(T server) {
        Preconditions.checkNotNull(server, "Server name can not be null");
        for (int virtID = startVirtualNodeId; virtID <= stopVirtualNodeId; virtID++) {
            ByteBuffer bbServerVirtuals = convertAndApplyHash(virtID, server);
            serversMap.remove(bbServerVirtuals);
        }
        virtualForServer.remove(server);

    }


    private ByteBuffer convertAndApplyHash(int nodeID, T server) {
        byte[] bucketNameInBytes = hashFunction.hash(nodeToByteConverter.convert(server));
        byte[] bucketNameAndCode = new byte[(Integer.BYTES / Byte.BYTES) + bucketNameInBytes.length];
        ByteBuffer bb = ByteBuffer.wrap(bucketNameAndCode);
        bb.put(bucketNameInBytes);
        bb.putInt(nodeID);
        return ByteBuffer.wrap(hashFunction.hash(bucketNameAndCode));
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

    public ArrayList<T> getAllNodes() {
        ArrayList<T> nodes = new ArrayList<>();
        nodes.addAll(virtualForServer.keySet());
        return nodes;
    }

    public boolean containsNode(T node) {
        ByteBuffer bbServerVirtuals = convertAndApplyHash(startVirtualNodeId, node);
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
     * Get next physical servers associated with next virtaul nodes.
     * @param server starting node server
     * @param number nnumber of successive server to be found
     * @return
     */
    public ArrayList<T> getNextServers(T server, int number) {
        Preconditions.checkArgument(number <= virtualForServer.keySet().size(), "The number of node present is less than the preference list size required");

        ArrayList<ByteBuffer> virtuals = virtualForServer.get(server);

        ByteBuffer bbNext = virtuals.get(0);     // first entry is the Bytebuffer of the first physical server.
        ArrayList<T> nexts = new ArrayList<>();

        while (number > 0) {
            bbNext = serversMap.higherKey(bbNext);
            if (bbNext == null) { // there is no they greater than the server key.
                bbNext = serversMap.firstKey();
            }
            if (!virtuals.contains(bbNext) && !nexts.contains(serversMap.get(bbNext))) {
                nexts.add(serversMap.get(bbNext));
                number--;
            }
        }
        return nexts;
    }


    public ArrayList<T> getPreviousServer(T server, int number){
        ArrayList<ByteBuffer> virtuals = virtualForServer.get(server);

        ByteBuffer bbPrevious = virtuals.get(0);     // first entry is the Bytebuffer of the  physical server.
        ArrayList<T> privious = new ArrayList<>();

        while (number > 0) {
            bbPrevious = serversMap.lowerKey(bbPrevious);
            if (bbPrevious == null) { // there is no they less than the server key.
                bbPrevious = serversMap.lastKey();
            }
            if (!virtuals.contains(bbPrevious) && !privious.contains(serversMap.get(bbPrevious))) {
                privious.add(serversMap.get(bbPrevious));
                number--;
            }
        }
        return privious;
    }


}
