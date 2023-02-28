package es.upm.dit.cnvr_fcon.bank2022.tobedone;

import es.upm.dit.cnvr_fcon.bank2022.bank.*;
import es.upm.dit.cnvr_fcon.bank2022.common.*;
import es.upm.dit.cnvr_fcon.bank2022.interfaces.*;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
//import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * This class creates and initiate the main clases in the system. 
 * In addition, it connectates to the ZooKeeper ensemble and
 * creates the basic zNodes required
 *
 * @authors Luis Alberto Lopez Alvarez y Alvaro de Rojas Maraver
 */
public class BankManager implements Watcher {

	private static final int SESSION_TIMEOUT = 5000;
	private static final int CONNECTION_TIMEOUT = 2000;

	//TODO Las tres son de ExploreZk
	private static Integer mutex        = -1;
	private static Integer mutexBarrier = -2;
	private static Integer mutexMember  = -3;
	private static int nMembers  = 0;

	private static String rootMembers = "/members";
	private static String member      = "/member-";
	private String opns = "/opns";
	private String opn  = "/opn-";
	
	private Logger LOGGER;
	private int quorum = 2;
	private String memberID;
	// TO DO
	private ZooKeeper zk;
	private SendMessages sendMsgs;
	private QuorumManager quorumManager;
	private ClientDB clientDB;
	private OperationWatcher operationWatcher;
	private PendingOpns pendingOpns;
	private Operations operations;
	
	Watcher memberWatcher;
	Watcher memberWatcherFT;
	// FIN TO DO
	public BankManager(int serverID) {
		// TO DO
		this.opns = "/opns";
        this.opn = "/opn-";
        this.LOGGER = MainBank.LOGGER;
        this.quorum = 2;
        this.memberWatcher = new Watcher() {public void process(WatchedEvent event) {}};
        this.memberWatcherFT = new Watcher() {public void process(WatchedEvent event) {
            System.out.println("------------------Watcher MEMBER ------------------");
            System.out.println("Member: " + event.getType() + ", " + event.getPath());
            try {
            	if (event.getPath() != null && rootMembers != null && event.getPath().equals(rootMembers)) {
                    synchronized (mutexMember) {
                        nMembers += 1;
                        LOGGER.finest("Numero de Watcher MEMBERS: " + nMembers);
                        mutexMember.notify();
                    }
                }
                if (event.getPath() == null) {
                    LOGGER.finest("SyncConnected");
                    synchronized (mutex) {
                        mutex.notify();
                        return;
                    }
                }
            } catch (Exception e) {
                System.out.println("Unexpected Exception process Watcher PROCESS");
            }
        }};
        this.clientDB = new ClientDB();
        LOGGER.finest("Server ID: " + serverID);
        // FIN TO DO
        configureZK();
	}

	public void configureZK() {
		// TO DO
		String[] nodos = {"127.0.0.1:2181","127.0.0.1:2181"};
        Random aleat = new Random();
        int nodo = aleat.nextInt(nodos.length);
        try {
            if (zk == null) {
                zk = new ZooKeeper(nodos[nodo], 5000, this); // Con this paso el valor del watcher actual
                try {
                    synchronized (mutex) {
                        mutex.wait();
                    }
                    zk.exists("/", false);
                }
                catch (Exception e) {
                    LOGGER.severe("Excepcion configurando zookeeper");
                }
            }
        }
        catch (Exception e) {
            LOGGER.severe("Excepcion configurando zookeeper");
        }
        if (zk != null) {
            try {
                Stat s = zk.exists(rootMembers, memberWatcher);
                memberID = zk.create(String.valueOf(rootMembers) + member, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                memberID = memberID.replace(String.valueOf(rootMembers) + "/", "");
                List<String> list = zk.getChildren(rootMembers, memberWatcher, s);
                printListMembers(list);
            }
            catch (Exception e) {
                LOGGER.severe("La sesion ha fallado. Cerrando...");
            }
        }
        if (zk != null) {
            try {
                Stat s2 = zk.exists(opns, memberWatcher);
                if (s2 == null) {
                    zk.create(opns, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            }
            catch (Exception e) {
                LOGGER.severe("Excepcion configurando zookeeper");
            }
        }
        quorumManager = new QuorumManager(zk, quorum, rootMembers);
        pendingOpns = new PendingOpns();
        operationWatcher = new OperationWatcher(zk, this, clientDB, opns, pendingOpns, memberID);
        sendMsgs = new SendMessagesBank(zk, opns, opn, operationWatcher);
        operations = new Operations(sendMsgs, clientDB, pendingOpns);
        // TO DO
	}
	// TO DO
	public Operations getOperations() {
		return this.operations;
	}
	// FIN TO DO
	@Override
	public void process(WatchedEvent event) {
		System.out.println("------------------Watcher PROCESS ------------------");
		System.out.println("Member: " + event.getType() + ", " + event.getPath());
		try {
			if (event.getPath() == null) {			
				if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
					this.LOGGER.finest("SyncConnected");
					synchronized (mutex) {
						mutex.notify();
					}
				}
			}
		System.out.println("-----------------------------------------------");
		} catch (Exception e) {
			System.out.println("Unexpected Exception process Watcher PROCESS");
		}
	}

	public boolean isQuorum () {
		return this.quorumManager.isQuorum();
	}

	public int getQuorum() {
		return this.quorum;
	}

		
	public String clientDBString () {
		return this.clientDB.toString();
	}

	private void printListMembers (List<String> list) {
		LOGGER.fine("Remaining # members:" + list.size());
		String string = "";
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
			string = string + (String) iterator.next() + " ,";
		}
		LOGGER.fine(string);				
	}

}
