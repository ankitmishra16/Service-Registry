package cluster_managment;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher{

    public String SERVIC_REGISTRY_NODE = "" ;
    public ZooKeeper zooKeeper ;
    public String currentNode = null ;
    public List<String> allRegisteredNodes = null ;

    public ServiceRegistry(ZooKeeper zooKeeper, String zeenode) throws KeeperException, InterruptedException {
        this.zooKeeper = zooKeeper ;
        this.SERVIC_REGISTRY_NODE = zeenode ;
        this.CreateServiceRegistryNode() ;

    }

    public void CreateServiceRegistryNode() throws KeeperException, InterruptedException {
        if(zooKeeper.exists(this.SERVIC_REGISTRY_NODE, false) == null){
            System.out.println("[+]Creating parent node!!!");
            String parentNode = zooKeeper.create(this.SERVIC_REGISTRY_NODE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT) ;
            System.out.println("[+] Craeted parent node as : "+ parentNode);
            return ;
        }
        System.out.println("[-]Parent node is already created!!!");
    }

    public void RegisterToCluster(String metadata) throws KeeperException, InterruptedException {
        if(this.currentNode != null ){
            System.out.println("[-]Already registered!!!");
            return ;
        }
        this.currentNode = zooKeeper.create(SERVIC_REGISTRY_NODE + "/c_", metadata.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL) ;
        System.out.println("[+] " + this.currentNode + " registered for service!!!");

    }

    public void UnregisterFromCluster() throws KeeperException, InterruptedException {
        if (this.currentNode != null && zooKeeper.exists(this.currentNode, false) != null) {
            zooKeeper.delete(this.currentNode, -1);
            System.out.println("[+]Node deleted");
        }
        System.out.println("Current node is : "+ this.currentNode);
        updateAddresses();
    }

    public List<String> GetAllServiceAddresses() throws KeeperException, InterruptedException {
        if(this.allRegisteredNodes == null)
            updateAddresses();
        return this.allRegisteredNodes ;
    }

    public void updateAddresses() throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(SERVIC_REGISTRY_NODE, this) ;
        List<String> addresses = new ArrayList<>(children.size()); ;
        System.out.println("Childrens are : "+ children);
        for(String address : children){
            address = SERVIC_REGISTRY_NODE + "/" + address ;
            Stat stat = zooKeeper.exists(address, false) ;
            if( stat == null)
                continue ;
            byte[] addressBytes = zooKeeper.getData(address, false, stat) ;
            addresses.add(new String(addressBytes)) ;
        }
        this.allRegisteredNodes = Collections.unmodifiableList(addresses) ;
        System.out.println("Service addresses are : " + this.allRegisteredNodes);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            updateAddresses();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
