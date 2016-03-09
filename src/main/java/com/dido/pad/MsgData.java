package com.dido.pad;

/**
 * Created by dido-ubuntu on 08/03/16.
 */
public class MsgData<V> {
    private String key;
    private V value;

    public MsgData(String key, V value) {
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
}
