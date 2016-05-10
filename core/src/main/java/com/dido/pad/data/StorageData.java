package com.dido.pad.data;

import java.io.Serializable;

/**
 * Created by dido-ubuntu on 11/03/16.
 */
public class StorageData<T>  implements Serializable{

    private String key;
    private T value;

    public StorageData() {
    }

    //copy constructor
    public StorageData(StorageData<T> sd){
        this.key = sd.key;
        this.value = sd.value;
    }

    public StorageData(String key, T value) {
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
        return "StorageData <"+ key + ":" + value + '>';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StorageData<?> that = (StorageData<?>) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
