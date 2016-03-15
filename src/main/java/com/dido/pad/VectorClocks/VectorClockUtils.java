package com.dido.pad.VectorClocks;

import com.google.common.base.Preconditions;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Occurs;
import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;

/**
 * Created by dido-ubuntu on 15/03/16.
 */
public class VectorClockUtils {

    public static  long MAX_VALUE = Long.MAX_VALUE;
    public static int MAX_NODES = 100;

    /**
     * Compare two VectorClocks, the outcomes will be one of the following: <br>
     * -- Clock 1 is BEFORE clock 2, if there exists an nodeId such that
     * c1(nodeId) <= c2(nodeId) and there does not exist another nodeId such
     * that c1(nodeId) > c2(nodeId). <br>
     * -- Clock 1 is CONCURRENT to clock 2 if there exists an nodeId, nodeId2
     * such that c1(nodeId) < c2(nodeId) and c1(nodeId2) > c2(nodeId2)<br>
     * -- Clock 1 is AFTER clock 2 otherwise
     *
     * @param first The first VectorClock
     * @param second The second VectorClock
     */
    public static  VectorClock.APPEN compare(VectorClock first, VectorClock second) throws Exception {

        Preconditions.checkNotNull(first, "First Vector clock  name can not be null");
        Preconditions.checkNotNull(second,"Second Vector clock  name can not be null");

        for(String ip: first.getVector().keySet()){
            if(!second.getVector().containsKey(ip))
                throw  new Exception("The vector clocks must contains the same nodes");
        }

        int n = first.getVector().size();
        int numEq=0, numLt=0, numGt = 0;
        for (String ip: first.getVector().keySet()) {
            if(first.getClockFor(ip) == second.getClockFor(ip)){ numEq++; }
            else if(first.getClockFor(ip) < second.getClockFor(ip)){ numLt++; }
            else{numGt ++;}
        }
        //int n = first.getVector().keySet().size();
        if(numGt ==0  && n ==(numEq + numLt)) return VectorClock.APPEN.BEFORE;
        else if(numLt > 0 && numGt > 0) return VectorClock.APPEN.CONCURRENT;
        else {return VectorClock.APPEN.AFTER; }

    }


}
