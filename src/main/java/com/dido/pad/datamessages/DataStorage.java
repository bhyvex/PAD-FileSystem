package com.dido.pad.datamessages;

import com.dido.pad.consistenthashing.HashableDataStorage;

/**
 * Created by dido-ubuntu on 08/03/16.
 */
public class DataStorage<V> implements HashableDataStorage {
    //it is hashed by the Hasher so must be implements HashableDataStorage

    private String key;
    private V value;

    public DataStorage(String key, V value) {
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
        return this.key.getBytes();
    }
}
