package es.upm.dit.cnvr_fcon.bank2022.interfaces;

import es.upm.dit.cnvr_fcon.bank2022.common.Client;

public interface OperationInterface {

	public Client put (Client client);
	
	public Integer get (Integer accNumber);

	public Integer remove(Integer accNumber);

	public Integer update(Integer accNumber, Integer balance);

	// public Integer GetClientDB();

}