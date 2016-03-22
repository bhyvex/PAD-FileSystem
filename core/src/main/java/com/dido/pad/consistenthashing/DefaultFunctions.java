package com.dido.pad.consistenthashing;

import com.dido.pad.Node;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;

/**
 * Created by luca on 22/03/16.
 */
public class DefaultFunctions {

    public static byte[] SHA1(byte[] input) {
        Preconditions.checkNotNull(input);
        return Hashing.sha1().hashBytes(input).asBytes();
    }

    public static byte[] BytesConverter(Node node) {
        return (node.getIpAddress() + node.getId()).getBytes();
    }
}
