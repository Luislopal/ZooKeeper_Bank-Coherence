package es.upm.dit.cnvr_fcon.bank2022.tobedone;

import es.upm.dit.cnvr_fcon.bank2022.bank.MainBank;
import es.upm.dit.cnvr_fcon.bank2022.common.*;
import es.upm.dit.cnvr_fcon.bank2022.interfaces.OperationInterface;
import es.upm.dit.cnvr_fcon.bank2022.interfaces.SendMessages;

import java.util.logging.Logger;

/**
 * 
 * This class provides a set of operations for managing a client.
 * It is aim is communicate the operation for the all servers. 
 * When a client invokes an operation, it is sent to the ZooKeeper ensemble 
 * letting all replicas  to get it.
 * 
 * The operations are not directly processed. Only them can run according 
 * to the order that the operations has been invoked to the ZooKeeper ensemble
 * 
 * Operations are synchronous. The client cannot invoke an operation when the
 * previous has been already processed. Then, the invocation is blocked until
 * the operation has been processed. 
 * 
 * @authors Luis Alberto Lopez Alvarez y Alvaro de Rojas Maraver
 */
public class Operations implements OperationInterface{
	//TO DO
	private Logger LOGGER;
	private SendMessages sendMsgs;	
	private ClientDB clientDB;
	private PendingOpns pendingOpns;
	
	public Operations(SendMessages sendMsgs, ClientDB clientDB, PendingOpns pendingOpns) {
		this.LOGGER = MainBank.LOGGER;
		this.sendMsgs = sendMsgs;
		this.clientDB = clientDB;
		this.pendingOpns = pendingOpns;
	}
	@Override
	public Client put (Client client) {
		String opnId = sendMsgs.sendAdd(client);
		LOGGER.fine("Put");
		if (opnId == null) {
			LOGGER.severe("Valor nulo para el identificador de operacion");
		} else {
			pendingOpns.put(opnId);
		}
		return client;
	}
	// Escribir la operacion en ZK
	@Override
	public Integer get (Integer accNumber) {
		String opnId = sendMsgs.sendRead(accNumber);
        LOGGER.fine("Get " + accNumber);
        if (opnId == null) {
        	LOGGER.severe("Valor nulo para el identificador de operacion");
        }
        else {
        	pendingOpns.put(opnId);
        }
        Client client = clientDB.readClient(accNumber);
        if (client == null) {
            return null;
        } else {
        	return client.getBalance();
        }
	}
	@Override
	public Integer remove(Integer accNumber) {
		String opnId = sendMsgs.sendDelete(accNumber);
        LOGGER.fine("Remove " + accNumber);
        if (opnId == null) {
            LOGGER.severe("Valor nulo para el identificador de operacion");
        }
        else {
        	pendingOpns.put(opnId);
        }
        Client client = clientDB.readClient(accNumber);
        if (client == null) {
            return null;
        } else {
        	return client.getAccountNumber();
        }
	}
	@Override
	public Integer update(Integer accNumber, Integer balance) {
        Client client = new Client(accNumber, "", balance);
        String opnId = sendMsgs.sendUpdate(client);
        LOGGER.fine("Actualizacion -> cuenta:" + accNumber + " , balance: " + balance);
        if (opnId == null) {
        	LOGGER.severe("Valor nulo para el identificador de operacion");
        }
        else {
        	pendingOpns.put(opnId);
        }
        Client clientUpdate = clientDB.readClient(accNumber);
        if (clientUpdate.getBalance() == client.getBalance()) {
            return null;
        } else {
        	return clientUpdate.getBalance();
        }
	}
	// FIN TO DO
}
