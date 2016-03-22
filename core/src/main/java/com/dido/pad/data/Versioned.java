package com.dido.pad.data;


import com.dido.pad.VectorClocks.VectorClock;
import com.dido.pad.VectorClocks.Version;

/**
 * A wrapper for an StorageData object that adds a Version.
 */

//extends StorageData<?>
public class Versioned {

    private StorageData<?> data;
    private Version vectorClock;
   // private String masterNode;  //master node of the data.

    public Versioned() {
        //for jackson JSOn parser
    }

    public Versioned(StorageData<?> data) {
        this.data = data;
        this.vectorClock = new VectorClock();
    }

    public Versioned(StorageData<?> data, Version version) {
        this.vectorClock = version == null ? new VectorClock() : (VectorClock) version;
        this.data = data;
    }

    public void setVectorClock(VectorClock version) {
        this.vectorClock = version;
    }

    public Version getVersion() {
        return vectorClock;
    }

    public StorageData<?> getData(){
        return data;
    }

    public void setData(StorageData<?> data) {
        this.data = data;
    }

    /**
     * Create a clone of this StorageData object such that the object pointed to
     * is the same, but the VectorClock and StorageData wrapper is a shallow copy.
     */
//    public Versioned cloneVersioned() {
//        return new Versioned(this.getData(), this.vectorClock.clone());
//    }
}

