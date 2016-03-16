package com.dido.pad;

import com.dido.pad.VectorClocks.Versioned;

import java.util.HashMap;

/**
 * Created by dido-ubuntu on 10/03/16.
 */
public class PersisentStorage{

    private HashMap<String, Versioned<?>> database;

    public PersisentStorage() {
        this.database = new HashMap<String, Versioned<?>>();
    }

    public HashMap<String, Versioned<?>> getStorage(){
        return  database;
    }

    public void put(Versioned<?> v){
        database.put(v.getData().getKey(), v);
    }

    public boolean containsKey(String key){ return database.containsKey(key);}

    public Versioned<?> get(String key){ return database.get(key); }

    public void update(Versioned<?> v){
        database.put(v.getData().getKey(),v);
    }
}
