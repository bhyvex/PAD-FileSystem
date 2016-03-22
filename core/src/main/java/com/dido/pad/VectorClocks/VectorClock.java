package com.dido.pad.VectorClocks;

import com.dido.pad.Node;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dido-ubuntu on 15/03/16.
 */
public class VectorClock  implements Version {


    public enum APPEN {EQUAL,BEFORE,AFTER,CONCURRENT};

    private Map<String, Long> vectorClock;
    private int delta;

    public VectorClock() {
        this.vectorClock = new HashMap<String,Long>();
        this.delta = 1;
    }

    public VectorClock(int delta) {
        this();
        this.delta = delta;
    }

    public VectorClock( Map<String, Long> m){
        this();
        this.vectorClock = m;
    }

    public long getClockFor(String ip){
        return vectorClock.get(ip);
    }

    public Map<String,Long> getVector(){
        return vectorClock;
    }

    public VectorClock incremenetVersion( String ip){
        vectorClock.put(ip, vectorClock.getOrDefault(ip, (long) 0) + delta);
        return this;
    }

    @Override
    public VectorClock clone() {
        return new VectorClock(Maps.newHashMap(vectorClock));
    }

    public void update (VectorClock v2){

    }





    /**
     * Compare two VectorClocks, the outcomes will be one of the following: <br>
     * -- Clock 1 is BEFORE clock 2, if there exists an nodeId such that
     * c1(nodeId) <= c2(nodeId) and there does not exist another nodeId such
     * that c1(nodeId) > c2(nodeId). <br>
     * -- Clock 1 is CONCURRENT to clock 2 if there exists an nodeId, nodeId2
     * such that c1(nodeId) < c2(nodeId) and c1(nodeId2) > c2(nodeId2)<br>
     * -- Clock 1 is AFTER clock 2 otherwise
     *
     * @param version The second VectorClock
     */
    @Override
    public VectorClock.APPEN compare(Version version) {
        Preconditions.checkNotNull(version,"Second Vector clock  name can not be null");

        if (!(version instanceof VectorClock))
            return APPEN.CONCURRENT;

        VectorClock first = this;
        VectorClock second = (VectorClock) version;
        int n = first.getVector().size();
        int numEq=0, numLt=0, numGt = 0;
        for (String ip: first.getVector().keySet()) {
            if(!second.getVector().keySet().contains(ip)){ //if second does not contains the ip the version is 0 so the first is greater
                numGt++;
            }
            else if(first.getClockFor(ip) == second.getClockFor(ip)){
                numEq++; }
            else if(first.getClockFor(ip) < second.getClockFor(ip)){
                numLt++; }
            else{numGt ++;}
        }

        //for each string that are present in the second if is not present in the firsts
        for(String ip: second.getVector().keySet()){
            if(!first.getVector().keySet().contains(ip)){
                numLt ++;
            }
        }

        //int n = first.getVector().keySet().size();
        if(numGt ==0  && n ==(numEq + numLt)) return VectorClock.APPEN.BEFORE;
        else if(numLt > 0 && numGt > 0) return VectorClock.APPEN.CONCURRENT;
        else {return VectorClock.APPEN.AFTER; }

    }


    @Override
    public String toString() {
        return "VectorClock{" +
                "vectorClock=" + vectorClock +
                '}';
    }
}
