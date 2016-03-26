package com.dido.pad;

import com.dido.pad.data.Versioned;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dido-ubuntu on 10/03/16.
 */
public class PersistentStorage {

    private HTreeMap<String, Versioned> _treeMap;
    private DB _db;

    public PersistentStorage() {
  //      this.database = new HashMap<String, Versioned>();
        this("prova", false);
    }

    public PersistentStorage(String fileName){
        this(fileName,false);
    }

    public PersistentStorage(String fileName, boolean erase){
        String dir = System.getProperty("java.io.tmpdir");
        File filePath = new File(dir+"/"+fileName+".pad");
        if(erase){
            try {
                Files.deleteIfExists(filePath.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        _db = DBMaker.newFileDB(filePath)
                .snapshotEnable()
                .closeOnJvmShutdown()
                .make();
        _treeMap= _db.getHashMap("storage");
        _db.commit();

    }
    synchronized public void close() {
        if(!_db.isClosed()) _db.close();
    }

    synchronized public boolean put(Versioned v){
        if(!_treeMap.containsKey(v.getData().getKey())) {
            _treeMap.put(v.getData().getKey(), v);
            _db.commit();
            return true;
        }
        return false;
    }


    synchronized public Versioned get(String key){
         Versioned v = _treeMap.get(key);
         return v;
    }



    public boolean isEmpty(){
        return _treeMap.isEmpty();
    }

    synchronized public Map<String, Versioned> getStorage(){
        return  _treeMap.snapshot();
    }


    public boolean containsKey(String key){
        return _treeMap.containsKey(key);}


    public boolean  update(Versioned v){
        if(_treeMap.containsKey(v.getData().getKey())){
            _treeMap.put(v.getData().getKey(), v);
            _db.commit();
            return true;
        }
        return false;
        //database.put(v.getData().getKey(),v);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : _treeMap.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" ");
            }
            String keyValue = _treeMap.get(key).getData().toString();
            stringBuilder.append(keyValue);

        }
        return stringBuilder.toString();
    }

}
