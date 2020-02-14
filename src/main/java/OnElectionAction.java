import cluster_managment.OnElectionCallback;
import cluster_managment.ServiceRegistry;
import org.apache.zookeeper.KeeperException;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class OnElectionAction implements OnElectionCallback {

    public ServiceRegistry service_registry ;
    public int port ;

    public OnElectionAction(ServiceRegistry service_registry, int port){
        this.service_registry = service_registry ;
        this.port = port ;
    }

    @Override
    public void OnelectedAsLeader() throws KeeperException, InterruptedException {
        service_registry.UnregisterFromCluster();
        service_registry.updateAddresses();

    }

    @Override
    public void AsWorker() {
        String currentAddress = null;
        try {
            currentAddress = String.format("http://%s:%d", Inet4Address.getLocalHost().getCanonicalHostName(), port);

            service_registry.RegisterToCluster(currentAddress);
        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
