package com.dido.pad;

/**
 * Created by dido-ubuntu on 09/03/16.
 */
public class Data<T> extends AbstractData<T> {


    public Data(String key, T value) {
        super(key, value);
    }


    @Override
    public byte[] convertToBytes() {
        return this.getKey().getBytes();
    }
}
