import com.dido.pad.Helper;
import com.dido.pad.Node;

/**
 * Created by dido-ubuntu on 24/03/16.
 */
public class Client  {

    private Node client = new Node( "127.0.0.254", "client",Helper.STORAGE_PORT );

    public void sent(){
        client.get_storageService().getcHasher().addServer( new Node("127.0.0.1","node1",Helper.STORAGE_PORT));

    }
}
