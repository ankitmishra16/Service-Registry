package cluster_managment;

import org.apache.zookeeper.KeeperException;

public interface OnElectionCallback {
    void OnelectedAsLeader() throws KeeperException, InterruptedException;
    void AsWorker() ;
}
