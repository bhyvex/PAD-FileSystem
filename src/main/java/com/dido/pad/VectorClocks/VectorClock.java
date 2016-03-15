package com.dido.pad.VectorClocks;

import com.dido.pad.Node;
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
        this.vectorClock = m;
    }

    public Long getClockFor(String ip){
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
    public VectorClock clone()
    {
        return new VectorClock(Maps.newHashMap(vectorClock));
    }


    @Override
    public APPEN compare(Version v) throws Exception {
        if (!(v instanceof VectorClock))
            throw new IllegalArgumentException("Cannot compare Versions of different types.");

        return VectorClockUtils.compare(this, (VectorClock) v);

    }




}
