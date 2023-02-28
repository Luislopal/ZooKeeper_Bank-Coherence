package es.upm.dit.cnvr_fcon.bank2022.tobedone;

import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.io.IOException;
import java.util.logging.*;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import es.upm.dit.cnvr_fcon.bank2022.bank.MainBank;
import es.upm.dit.cnvr_fcon.bank2022.common.*;

/**
 * 
 * This class implements Watcher. It is intented to receive the 
 * operations pending to be done. It is associated to a zNode. 
 * When its children is created or deleted, a wather should be received.
 * 
 * @authors Luis Alberto Lopez Alvarez y Alvaro de Rojas Maraver
 */
public class OperationWatcher implements Watcher{
	// TO DO
	private Logger LOGGER;
	private ZooKeeper zk;
	private BankManager managerBank;
	private String opns;
	private ServicesBank servicesBank;
	private PendingOpns pendingOpns;
	private String memberID;
	private String lastOpns;

	public OperationWatcher(ZooKeeper zk, BankManager managerBank, ClientDB clientDB, String opns, PendingOpns pendingOpns, String memberID) {
        this.LOGGER = MainBank.LOGGER;
        this.lastOpns = "";
        this.zk = zk;
        this.managerBank = managerBank;
        this.opns = opns;
        this.pendingOpns = pendingOpns;
        this.memberID = memberID;
        this.servicesBank = new ServicesBank(clientDB);
        try {
            zk.getChildren(this.opns, this);
        }
        catch (Exception E) {
            LOGGER.finest("Excepcion en el constructor OperationsWatcher");
        }
    }
    
	private boolean deleteOpn(String opn, Watcher watcher) {
        try {
            LOGGER.fine("Eliminar la operacion: " + opn);
            List<String> list = zk.getChildren(opn, false);
            for (String node : list) {
                LOGGER.fine(memberID + " Delete: " + opn + "/" + node);
                Stat s = zk.exists(opn + "/" + node, false);
                if (s != null) {
                    try {
                        this.zk.delete(opn + "/" + node, s.getVersion());
                    }
                    catch (KeeperException zk) {
                        LOGGER.fine("El nodo " + opn + "/" + node + " ha sido eliminado");
                    }
                    catch (Exception E) {
                        throw E;
                    }
                }
            }
            Stat s = zk.exists(opn, true);
            if (s != null) {
                this.zk.delete(opn, s.getVersion());
            }
        }
        catch (KeeperException zk2) {
            LOGGER.fine("El nodo " + opn + " ha sido eliminado");
        }
        catch (Exception E2) {
            LOGGER.severe("Excepcion en deleteOpn");
        }
        return false;
    }
    
    public void process(WatchedEvent event) {
        LOGGER.fine("------------------Watcher OPN ------------------");
        try {
            Stat s = zk.exists(opns, false);
            List<String> children = zk.getChildren(opns, this);
            Collections.sort(children);
            if (children == null || children.size() == 0) {
                LOGGER.fine(memberID + " No hay operaciones pendientes");
                return;
            }
            LOGGER.fine(memberID + "Operacion: " + children.get(0));
            String opn = opns + "/" + children.get(0);
            if (lastOpns.equals(children.get(0))) {
                LOGGER.fine(memberID + " La operacion ha sido procesada");
                List<String> miembros = zk.getChildren(opn, this);
                LOGGER.fine(memberID + " Nodo a eliminar " + miembros.size());
                if (miembros.size() + 1 >= managerBank.getQuorum()) {
                    LOGGER.fine(memberID + " Eliminar operaciones pendientes: " + children.get(0));
                    deleteOpn(opn, this);
                }
                return;
            }
            lastOpns = children.get(0);
            byte[] data = zk.getData(opn, true, s);
            Client client = servicesBank.processOpn(data);
            pendingOpns.unlock(opn);
            List<String> miembros2 = zk.getChildren(opn, this);
            LOGGER.fine(memberID + " Numero de operaciones procesadas " + miembros2.size());
            if (miembros2.size() + 1 >= managerBank.getQuorum()) {
                LOGGER.fine(memberID + " Eliminar operaciones pendientes: " + children.get(0) + " " + children.size());
                deleteOpn(opn, this);
            }
            else {
                CreateNode create = new CreateNode();
                create.createNode(zk, opns + "/" + children.get(0), "/" + memberID, CreateMode.PERSISTENT, new byte[0], null);
            }
        }
        catch (Exception e) {
            LOGGER.severe("Excepcion en el Watcher");
        }
    }
    // FIN TO DO
    private void printListMembers(List<String> list) {
        LOGGER.fine("Remaining # members:" + list.size());
        String string = "";
        for (Iterator<String> iterator = list.iterator();iterator.hasNext();) {
            string = string + iterator.next() + " ,";
        }
        LOGGER.fine(string);
    }
}
