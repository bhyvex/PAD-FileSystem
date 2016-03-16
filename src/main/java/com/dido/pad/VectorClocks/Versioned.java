package com.dido.pad.VectorClocks;


import com.dido.pad.StorageData;

/**
 * A wrapper for an StorageData object that adds a Version.
 */

//extends StorageData<?>
public class Versioned<T extends StorageData> {

    private T data;
    private  VectorClock vectorclock;
    private String masterNode;  //master node of the data.

    public Versioned() {
        //for jackson JSOn parser
    }

    public Versioned(T data) {
        this.data = data;
        this.vectorclock = new VectorClock();
    }

    public Versioned(T data, Version version) {
        this.vectorclock = version == null ? new VectorClock() : (VectorClock) version;
        this.data = data;
    }

    public String getMasterNode() { return masterNode;  }

    public void setMasterNode(String masterNode) {
        this.masterNode = masterNode;
    }

    public void setgetVectorclock(VectorClock version) {
        this.vectorclock = version;
    }

    public VectorClock getVectorclock() {
        return vectorclock;
    }

    public T getData(){
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * Create a clone of this StorageData object such that the object pointed to
     * is the same, but the VectorClock and StorageData wrapper is a shallow copy.
     */
    public Versioned<T> cloneVersioned() {
        return new Versioned<T>(this.getData(), this.vectorclock.clone());
    }
}

