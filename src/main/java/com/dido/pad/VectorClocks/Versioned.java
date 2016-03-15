package com.dido.pad.VectorClocks;


import com.dido.pad.DataStorage;

/**
 * A wrapper for an DataStorage object that adds a Version.
 */

//extends DataStorage<?>
public class Versioned<T > {

    private T data;
    private final VectorClock version;

    public Versioned(T data) {
        this.data = data;
        this.version = new VectorClock();
    }

    public Versioned(T data, Version version) {
        this.version = version == null ? new VectorClock() : (VectorClock) version;
        this.data = data;
    }

    public T getData(){
        return data;
    }

    public VectorClock getVersion() {
        return version;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * Create a clone of this Versioned object such that the object pointed to
     * is the same, but the VectorClock and Versioned wrapper is a shallow copy.
     */
    public Versioned<T> cloneVersioned() {
        return new Versioned<T>(this.getData(), this.version.clone());
    }
}

