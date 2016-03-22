package com.dido.pad.consistenthashing;

/**
 * Created by luca on 22/03/16.
 */
public interface IByteConverter<T>{
    byte[] convert( T data);
}
