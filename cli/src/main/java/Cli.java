import com.dido.pad.Helper;
import com.dido.pad.Node;
import com.dido.pad.StorageService;
import com.dido.pad.consistenthashing.DefaultFunctions;
import com.dido.pad.consistenthashing.Hasher;
import com.dido.pad.datamessages.AppMsg;
import com.dido.pad.datamessages.RequestAppMsg;

import java.util.HashMap;

public class Cli{

    public static void main(String[] args) {

        String ip = "127.0.0.254";
        String id = "client";
        //Hasher<Node> cHasher = new Hasher<>(Helper.NUM_NODES_VIRTUALS, DefaultFunctions::SHA1, DefaultFunctions::BytesConverter);

        Node client = new Node(ip, id, Helper.STORAGE_PORT);
        client.get_storageService().getcHasher().addServer(new Node("127.0.0.1", "node1", Helper.STORAGE_PORT));
        client.get_storageService().getcHasher().addServer(new Node("127.0.0.2", "node2", Helper.STORAGE_PORT));
        client.get_storageService().getcHasher().addServer(new Node("127.0.0.3", "node3", Helper.STORAGE_PORT));

        String key = "davide";
        String value = "neri";

        Node n = client.get_storageService().getcHasher().getServerForData(key);

        n.sendToStorage(new RequestAppMsg<>(AppMsg.OP.PUT, key, value));

    }

}