package com.dido.pad.VectorClocks;

/**
 * Created by dido-ubuntu on 15/03/16.
 */
public interface Version {

    /**
     * Return whether or not the given version preceeded this one, succeeded it,
     * or is concurrant with it
     *
     * @param v The other version
     */
    VectorClock.OCCUR compare(Version v);

    VectorClock increment( String ip);

    void merge( Version v);
}
