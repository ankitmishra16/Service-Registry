import cluster_managment.LeaderElection;
import cluster_managment.ServiceRegistry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class Application implements Watcher {
    public static String ZOOKEEPER_ADDRESS = "localhost:2181" ;
    private ZooKeeper zookeeper ;
    private static final int SESSION_TIMMEOUT = 3000 ;
    private static final int DEFAULT_PORT = 8080 ;
    public static String Zeenode_namespace = "/service_registry" ;

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        int currentPort = args.length == 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT ;
        Application application = new Application() ;
        ZooKeeper zooKeeper = application.ConnectToZookeeper();

        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper, Zeenode_namespace) ;
        OnElectionAction onElectionAction = new OnElectionAction(serviceRegistry, currentPort) ;

        LeaderElection leaderElection = new LeaderElection(zooKeeper, onElectionAction ) ;

        leaderElection.volunteerForLeadership();
        leaderElection.electLeader();

        application.run() ;
        application.close();
        System.out.println("[-]Disconnected from Zookeeper server, exiting apllication!!!");

    }
    public void close() throws InterruptedException
    {
        zookeeper.close() ;
        System.out.println("[-]Disconnected from zookeeper server!!!");
    }

    public void run() throws InterruptedException
    {
        synchronized(zookeeper){
            zookeeper.wait();
        }
    }

    public ZooKeeper ConnectToZookeeper() throws IOException
    {
        this.zookeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMMEOUT, this) ;
        return this.zookeeper ;
    }

    @Override
    public void process(WatchedEvent event) {
        if(event.getState()==Event.KeeperState.SyncConnected)
        {
            System.out.println("[+}Connected successfully to zookeeper");
        }
        else{
            synchronized (zookeeper){
                System.out.println("[-]Disconnected from zookeeper event");
                zookeeper.notifyAll();
            }
        }
    }
}
