package com.dido.pad.consistenthashing;

/**
 * Created by luca on 22/03/16.
 */
public interface IHashFunction{
    byte[] hash( byte[] input);
}