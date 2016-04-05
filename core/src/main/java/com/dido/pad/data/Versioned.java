package com.dido.pad.data;


import com.dido.pad.versioning.VectorClock;
import com.dido.pad.versioning.Version;

import java.io.Serializable;

/**
 * A wrapper for an StorageData object that adds a Version.
 */


public class Versioned  implements Serializable{

    private StorageData<?> data;

    private Version version;


    public Versioned() {
        //for jackson JSOn parser
    }

    public Versioned(StorageData<?> data) {
        this.data = data;
        this.version = new VectorClock();
    }


    public Versioned(StorageData<?> data, Version version) {
        this.version = version == null ? new VectorClock() : (VectorClock) version;
        this.data = data;
    }

    public void setVersion(VectorClock version) {
        this.version = version;
    }

    public Version getVersion() {
        return version;
    }

    public StorageData<?> getData(){
        return data;
    }

    public void setData(StorageData<?> data) {
        this.data = data;
    }

    public VectorClock.OCCUR compareTo( Versioned v){
        return version.compare(v.getVersion());
    }

    public void mergeTo(Versioned v){
        data = v.getData();
        version.merge(v.getVersion());
    }

    @Override
    public String toString() {
        return "Versioned {" +
                "data=" + data +
                ", version=" + version +
                '}';
    }
}

