package com.dido.pad;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dido-ubuntu on 10/03/16.
 */
public class PersisentStorage{

    private HashMap<String, DataStorage<?>> database;

    public PersisentStorage() {
        this.database = new HashMap<String, DataStorage<?>>();
    }

    public HashMap<String, DataStorage<?>> getStorage(){
        return  database;
    }

    public void put(DataStorage<?> data){
        database.put(data.getKey(), data);
    }

    public boolean containsKey(String key){ return database.containsKey(key);}

    public DataStorage<?> get(String key){ return database.get(key); }

    public void update(DataStorage<?> data){
        database.put(data.getKey(),data);
    }
}
