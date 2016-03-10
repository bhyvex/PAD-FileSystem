package com.dido.pad;

import com.dido.pad.datamessages.DataStorage;

import java.util.HashMap;

/**
 * Created by dido-ubuntu on 10/03/16.
 */
public class PersisentStorage{

    private HashMap<String, DataStorage<?>> database;

    public PersisentStorage() {
        this.database = new HashMap<String, DataStorage<?>>();
    }

    public void put(DataStorage<?> data){
        database.put(data.getKey(), data);
    }

    public void update(DataStorage<?> data){
        database.put(data.getKey(),data);
    }
}
