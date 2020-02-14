package cluster_managment;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException ;
import java.io.InterruptedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {

    public static String ZOOKEEPER_ADDRESS = "localhost:2181" ;
    private ZooKeeper zookeeper ;
    private OnElectionCallback onElectionCallback ;
    private static final int SESSION_TIMMEOUT = 3000 ;
//    public static final String Znode_Namespace = "/target_node" ;
    public static String Znode_Namespace = "/election";
    public static String currentNodeName = "" ;

    public LeaderElection(ZooKeeper zooKeeper, OnElectionCallback onElectionCallback){
        this.zookeeper = zooKeeper ;
        this.onElectionCallback = onElectionCallback ;
//        this.Znode_Namespace = "/election" ;
    }

//    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
//        LeaderElection leaderElection = new LeaderElection() ;
////        leaderElection.Znode_Namespace = leaderElection.getClusterParentName() ;
//        leaderElection.ConnectToZookeeper();
//        leaderElection.volunteerForLeadership();
//        leaderElection.electLeader();
////        leaderElection.watchZnode_Namespace();
//        leaderElection.run() ;
//        leaderElection.close() ;
//    }

//    public String getClusterParentName() throws KeeperException, InterruptedException {
//        List<String> parentName = zookeeper.getChildren("/", false) ;
//        return parentName.get(0) ;
//    }

    public void volunteerForLeadership() throws KeeperException, InterruptedException {
        if( zookeeper.exists(Znode_Namespace, false) == null)
            System.out.println("Parent node not found!!!");
        String znodePrefix = Znode_Namespace + "/c_";
        String znodeFullPath = zookeeper.create(znodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        System.out.println("Node created with name : " + znodeFullPath);
        this.currentNodeName = znodeFullPath.replace(Znode_Namespace + "/", "");
    }

    public void electLeader() throws KeeperException, InterruptedException {

        Stat predecessorNode = null ;
        String predecessorName = "" ;
        int predecessorIndex ;

        while(predecessorNode == null){
            List<String> children = zookeeper.getChildren(Znode_Namespace, false);
            Collections.sort(children) ;

            String smallestChild = children.get(0) ;
            if(smallestChild.equals(currentNodeName))
            {
                System.out.println("I am the leader : "+ currentNodeName);
                onElectionCallback.OnelectedAsLeader();
                return ;
            }
            else
            {
                System.out.println("I am no the leader, leader is : " + children.get(0));
                predecessorIndex = Collections.binarySearch(children, currentNodeName) - 1 ;
                predecessorName = children.get(predecessorIndex);
                predecessorNode = zookeeper.exists(Znode_Namespace + "/" + predecessorName, this ) ;
            }
        }
        onElectionCallback.AsWorker();
        System.out.println("Watching : " + predecessorName +"\n" );

    }

//    public void close() throws InterruptedException
//    {
//        zookeeper.close() ;
//        System.out.println("[-]Disconnected from zookeeper server!!!");
//    }

//    public void run() throws InterruptedException
//    {
//        synchronized(zookeeper){
//            zookeeper.wait();
//        }
//    }
//
//    public void ConnectToZookeeper() throws IOException
//    {
//        this.zookeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMMEOUT, this) ;
//    }

//    public void watchZnode_Namespace() throws KeeperException, InterruptedException {
//        Stat stat = zookeeper.exists(Znode_Namespace, this) ;
//        if(stat == null ) {
//            System.out.println("Node not found...");
//            return;
//        }
//        else
//        {
//            byte[] data = zookeeper.getData(Znode_Namespace, this, stat) ;
//            List<String> children = zookeeper.getChildren(Znode_Namespace, this) ;
//            System.out.println("Data : " + new String(data) + "\n Childrens : " + children);
//        }
//    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType())
        {
            case None :
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
                break;
            case NodeChildrenChanged: System.out.println(Znode_Namespace + "'s children changed ");
                break ;
            case NodeCreated: System.out.println(Znode_Namespace + " node created");
                break ;
            case NodeDataChanged: System.out.println(Znode_Namespace + "'s data changed");
                break ;
            case NodeDeleted:
                try{
                    electLeader();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (KeeperException e) {
                    e.printStackTrace();
                }
                break;

        }
        //Following code was for watchers example
//        try{
////            System.out.println("[+] Calling watchZnode_Namespace()...");
//            watchZnode_Namespace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (KeeperException e) {
//            e.printStackTrace();
//        }
    }
}
