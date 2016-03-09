package com.dido.pad.datamessages;

import com.dido.pad.consistenthashing.HashableData;

/**
 * Created by dido-ubuntu on 08/03/16.
 */
public class AppPayload<V> extends HashableData{

    private String key;
    private V value;

    public AppPayload(String key, V value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }


    @Override
    public byte[] convertToBytes() {
        return getKey().getBytes();
    }
}
