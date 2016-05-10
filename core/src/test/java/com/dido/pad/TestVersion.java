package com.dido.pad;

import com.dido.pad.versioning.VectorClock;
import com.dido.pad.data.StorageData;
import com.dido.pad.data.Versioned;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by dido-ubuntu on 15/03/16.
 */
public class TestVersion {

    @Test
    public void testVectors() throws InterruptedException {
        VectorClock vc = new VectorClock();
        vc.increment("ip1");
        vc.increment("ip1");
        vc.increment("ip2");

        //copy from vector clockt
        VectorClock vc2 = new VectorClock(vc);

        Assert.assertEquals(VectorClock.OCCUR.BEFORE, vc.compare(vc2));

        vc2.increment("ip4");

        Assert.assertEquals(VectorClock.OCCUR.BEFORE, vc.compare(vc2));

        vc.increment("ip4");
        vc.increment("ip4");

        Assert.assertEquals(VectorClock.OCCUR.AFTER, vc.compare(vc2));

        vc2.increment("ip6");
        vc2.increment("ip2");
        vc2.increment("ip2");
        Assert.assertEquals(VectorClock.OCCUR.CONCURRENT, vc.compare(vc2));

        //test merge opertaion
        vc2.merge(vc);
        Assert.assertEquals((long)vc2.getClockFor("ip2"),3);
        Assert.assertEquals((long)vc2.getClockFor("ip1"),2);
        Assert.assertEquals((long)vc2.getClockFor("ip4"),2);
        Assert.assertEquals((long)vc2.getClockFor("ip6"),1);
    }

    @Test
    public void  testVersioned(){

        Versioned v = new Versioned (new StorageData<String>("neri","davide"));
        v.getVersion().increment("ip1");

        //a node receive the data (first time) and copy it
        Versioned vCopy = new Versioned(v.getData(), new VectorClock((VectorClock)v.getVersion()));
        vCopy.getVersion().increment("ip2");

        Assert.assertEquals(VectorClock.OCCUR.BEFORE, v.compareTo(vCopy));

        v.getVersion().increment("ip3");
        Assert.assertEquals(VectorClock.OCCUR.CONCURRENT, v.compareTo(vCopy));

        vCopy.mergeTo(v);

        Assert.assertEquals(VectorClock.OCCUR.BEFORE, v.compareTo(vCopy));


    }

    @Test
    public void testCopyConstructor(){
            Versioned v = new Versioned(new StorageData<>("davide", "neri"));
            Versioned copy = new Versioned(v);
        copy.setData(new StorageData<>("davide", "gnagrnde2"));
        copy.getVersion().increment("node");
        Assert.assertNotEquals(v.getData().getValue(),copy.getData().getValue());

    }
}
