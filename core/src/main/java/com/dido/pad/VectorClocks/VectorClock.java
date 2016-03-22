package com.dido.pad.VectorClocks;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dido-ubuntu on 15/03/16.
 */
public class VectorClock  implements Version {
    public enum OCCUR {EQUAL,BEFORE,AFTER,CONCURRENT};

    private Map<String, Long> vectorClock;
    private int delta;

    public VectorClock() {
        this.vectorClock = new HashMap<>();
        this.delta = 1;
    }

    public VectorClock(VectorClock v){
        this.vectorClock = v.vectorClock;
        this.delta = v.delta;
    }

    public VectorClock(int delta) {
        this();
        this.delta = delta;
    }

    public Long getClockFor(String ip){
        return vectorClock.get(ip);
    }

    public Map<String,Long> getVector(){
        return vectorClock;
    }

    public VectorClock increment( String ip){
        vectorClock.put(ip, vectorClock.getOrDefault(ip, (long) 0) + delta);
        return this;
    }

    @Override
    public OCCUR compare(Version v) {
        if (!(v instanceof VectorClock))
            return OCCUR.CONCURRENT;

        VectorClock first = this;
        VectorClock second = (VectorClock) v;
//        for(String ip: first.getVector().keySet()){
//            if(!second.getVector().containsKey(ip))
//                throw  new Exception("The vector clocks must contains the same nodes");
//        }

        int n = first.getVector().size();
        int numEq=0, numLt=0, numGt = 0;
        for (String ip: first.getVector().keySet()) {
            if(first.getClockFor(ip) == second.getClockFor(ip)){ numEq++; }
            else if(first.getClockFor(ip) < second.getClockFor(ip)){ numLt++; }
            else{numGt ++;}
        }
        //int n = first.getVector().keySet().size();
        if(numGt ==0  && n ==(numEq + numLt)) return OCCUR.BEFORE;
        else if(numLt > 0 && numGt > 0) return OCCUR.CONCURRENT;
        else {return OCCUR.AFTER; }
    }

    @Override
    public String toString() {
        return "VectorClock{" +
                "vectorClock=" + vectorClock +
                '}';
    }
}
