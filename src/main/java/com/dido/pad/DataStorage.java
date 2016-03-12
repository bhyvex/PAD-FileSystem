package com.dido.pad;

/**
 * Created by dido-ubuntu on 11/03/16.
 */
public class DataStorage<T>  {

    private String key;
    private T value;


    public DataStorage(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DataStorage {" +
                "key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}
