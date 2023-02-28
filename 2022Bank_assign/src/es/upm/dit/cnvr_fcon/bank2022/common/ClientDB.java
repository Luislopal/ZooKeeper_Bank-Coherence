package es.upm.dit.cnvr_fcon.bank2022.common;

import java.io.Serializable;


/**
 * The dababase in the bank with the clients
 * @author aalonso
 */
public class ClientDB implements Serializable {

	private static final long serialVersionUID = 1L;

	private java.util.HashMap <Integer, Client> clientDB; 

	/**
	 * Start a dababase of clients, starting from one existing
	 * @param clientDB the database of clients
	 */
	public ClientDB (ClientDB clientDB) {
		this.clientDB = clientDB.getClientDB();
	}
	
	/**
	 * Start an empty database of clients
	 */
	public ClientDB() {
		clientDB = new java.util.HashMap <Integer, Client>();
	}

	/**
	 * Get the datababase of clients
	 * @return the database
	 */
	public java.util.HashMap <Integer, Client> getClientDB() {
		return this.clientDB;
	}
	
	/**
	 * Includes a client in the database. 
	 * @param client the client
	 * @return true, if the client has been included successfully. 
	 * false, if the client exists already in the database
	 */
	public boolean createClient(Client client) {		
		if (clientDB.containsKey(client.getAccountNumber())) {
			return false;
		} else {
			clientDB.put(client.getAccountNumber(), client);
			return true;
		}		
	}

	/**
	 * Get a client in the databse
	 * @param accountNumber the account number of the client
	 * @return the client. If the client does not exists in the
	 * database, null is returned
	 */
	public Client readClient(Integer accountNumber) {
		if (clientDB.containsKey(accountNumber)) {
			return clientDB.get(accountNumber);
		} else {
			return null;
		}		
	}

	/**
	 * Update the balance of a client in the database.
	 * @param accNumber the account number of the client
	 * @param balance the balanced
	 * @return true if the bajlance has been updated successfully.
	 * false, if the client does not exist in the database 
	 */
	public boolean updateClient (int accNumber, int balance) {
		if (clientDB.containsKey(accNumber)) {
			Client client = clientDB.get(accNumber);
			client.setBalance(balance);
			clientDB.put(client.getAccountNumber(), client);
			return true;
		} else {
			return false;
		}	
	}

	/**
	 * Delete a client in the database.
	 * @param accountNumber the account number of the client 
	 * @return the client deleted. null, if the client does not 
	 * exist in the database 
	 */
	public Client deleteClient(Integer accountNumber) {
		if (clientDB.containsKey(accountNumber)) {
			return clientDB.remove(accountNumber);
		} else {
			return null;
		}	
	}

	/**
	 * Change the current database
	 * @param clientDB the new database
	 * @return true if the database has been processed successfully.
	 * null, if the database is not valid
	 */
	public boolean createBank(ClientDB clientDB) {
		
		if (clientDB != null) {
			System.out.println("createBank");
			this.clientDB = clientDB.getClientDB();
			System.out.println(clientDB.toString());
			return true;			
		} else {
			return false;
		}
		
	}
	
	public String toString() {
		String aux = new String();

		for (java.util.HashMap.Entry <Integer, Client>  entry : clientDB.entrySet()) {
			aux = aux + entry.getValue().toString() + "\n";
		}
		return aux;
	}
}



