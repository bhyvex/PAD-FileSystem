package Client;
import com.dido.pad.Node;
import com.dido.pad.StorageService;

import java.util.List;

/**
 * Created by dido-ubuntu on 24/03/16.
 */
public class NodeClient {

    private StorageService _storageService;
    private String ip ;
    private int portStorage;


    public NodeClient(String ipaddress, int portStorage, List<Node> startupNode){
        this.ip = ipaddress;
        this.portStorage = portStorage;
        //_storageService = new StorageService(this,

    }










}
