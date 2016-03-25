package com.dido.pad;

import com.dido.pad.data.Versioned;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by dido-ubuntu on 10/03/16.
 */
public class PersistentStorage {

    private HashMap<String, Versioned> database;

    public PersistentStorage() {
        this.database = new HashMap<String, Versioned>();
    }

    public HashMap<String, Versioned> getStorage(){
        return  database;
    }

    public boolean isEmpty(){
        return database.isEmpty();
    }

    public void put(Versioned v){
        database.put(v.getData().getKey(), v);
    }

    public boolean containsKey(String key){ return database.containsKey(key);}

    public Versioned get(String key){ return database.get(key); }

    public void update(Versioned v){
        database.put(v.getData().getKey(),v);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : database.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" ");
            }
            String keyValue = database.get(key).getData().toString();
            stringBuilder.append(keyValue);

        }
        return stringBuilder.toString();
    }

}
