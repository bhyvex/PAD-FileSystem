package com.dido.pad.versioning;


import java.io.Serializable;
import java.util.*;

/**
 * Created by dido-ubuntu on 15/03/16.
 */


public class VectorClock  implements Version, Serializable {
    public enum OCCUR {BEFORE,AFTER,CONCURRENT}; //EQUAL,

    private HashMap<String, Long> vectorClock;
    private int delta;


    public VectorClock() {
        this.vectorClock = new HashMap<>();
        this.delta = 1;
    }

    public VectorClock(VectorClock v){
        if(v.vectorClock != null)
            vectorClock =  (HashMap)v.vectorClock.clone();
        this.delta = v.delta;
    }

    public VectorClock(int delta) {
        this();
        this.delta = delta;
    }

    public HashMap<String, Long> getVectorClock() {
        return vectorClock;
    }

    public void setVectorClock(HashMap<String, Long> vectorClock) {
        this.vectorClock = vectorClock;
    }

    public int getDelta() {
        return delta;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }

    public Long getClockFor(String ip){
        return vectorClock.get(ip);
    }

    private Map<String,Long> getVector(){
        return vectorClock;
    }


    public VectorClock increment( String ip){
        vectorClock.put(ip, vectorClock.getOrDefault(ip, (long) 0) + delta);
        return this;
    }

    /**
     * Compare two versioning, the outcomes will be one of the following: <br>
     * -- clock1 is BEFORE clock 2, if there exists an nodeId such that
     * c1(nodeId) <= c2(nodeId) and there does not exist another nodeId such
     * that c1(nodeId) > c2(nodeId). <br>
     * -- Clock 1 is CONCURRENT to clock 2 if there exists an nodeId, nodeId2
     * such that c1(nodeId) < c2(nodeId) and c1(nodeId2) > c2(nodeId2)<br>
     * -- Clock 1 is AFTER clock 2 otherwise
     *
     * @param v2 The second VectorClock
     */
   // @Override
    public OCCUR compare(Version v2) {
        if (!(v2 instanceof VectorClock))
            return OCCUR.CONCURRENT;

        VectorClock first = this;
        VectorClock second = (VectorClock) v2;

        boolean v1Bigger = false;
        boolean v2Bigger = false;

        HashSet<String> commonNodes =  new HashSet<>();
        commonNodes.addAll(vectorClock.keySet());
        commonNodes.retainAll(second.getVector().keySet());

        // if v1 has more nodes than common nodes
        // v1 has clocks that v2 does not
        if (first.getVector().keySet().size() > commonNodes.size()) {
            v1Bigger = true;
        }
        // if v2 has more nodes than common nodes
        // v2 has clocks that v1 does not
        if (second.getVector().keySet().size() > commonNodes.size()) {
            v2Bigger = true;
        }

        // compare the common parts
        for (String nodeId: commonNodes) {
            // no need to compare more
            if (v1Bigger && v2Bigger) {
                break;
            }
            long v1Version = vectorClock.get(nodeId);
            long v2Version = second.getVector().get(nodeId);
            if (v1Version > v2Version) {
                v1Bigger = true;
            } else if (v1Version < v2Version) {
                v2Bigger = true;
            }
        }
         /*
         * This is the case where they are equal. Consciously return BEFORE, so
         * that the we would throw back an ObsoleteVersionException for online
         * writes with the same clock.
         */
        if (!v1Bigger && !v2Bigger)
            return OCCUR.BEFORE;
        /* This is the case where v1 is a successor clock to v2 */
        else if (v1Bigger && !v2Bigger)
            return OCCUR.AFTER;
        /* This is the case where v2 is a successor clock to v1 */
        else if (!v1Bigger && v2Bigger)
            return OCCUR.BEFORE;
        /* This is the case where both clocks are parallel to one another */
        else
            return OCCUR.CONCURRENT;

    }

    @Override
    public void merge( Version v2){
        if(!(v2 instanceof  VectorClock))
            throw  new ClassCastException(" Version must be a VectorClock");

        VectorClock second = (VectorClock)v2;

      /*  VectorClock newClock = new VectorClock();
        for(Map.Entry<String,Long> entry: vectorClock.entrySet()) {
            newClock.getVector().put(entry.getKey(), entry.getValue());
        }
        */
        for(Map.Entry<String,Long> entry2: second.getVector().entrySet()) {
            Long myValue = vectorClock.containsKey(entry2.getKey())? vectorClock.get(entry2.getKey()):0L;
            vectorClock.put(entry2.getKey(), Math.max(myValue, entry2.getValue()));
        }

    }

    @Override
    public String toString() {
        return "VectorClock " + vectorClock ;
    }
}
