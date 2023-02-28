package es.upm.dit.cnvr_fcon.bank2022.tobedone;

import es.upm.dit.cnvr_fcon.bank2022.bank.MainBank;
import java.util.logging.Logger;

/**
 * It allows for waiting and notifying the required functions for 
 * ensured the synchronized invocation from the clients.
 * 
 * @authors Luis Alberto Lopez Alvarez y Alvaro de Rojas Maraver
 */
public class PendingOpns {
	// TO DO
	private Logger LOGGER;
	private String pendingOpnKey;
	private Integer mutex;
	
	public PendingOpns() {
		this.LOGGER = MainBank.LOGGER;
		this.pendingOpnKey = null;
		this.mutex = -1;
	}
	
	public String get() {
		return this.pendingOpnKey;
	}
	
	public void put(String pendingOpnKey) {
		this.pendingOpnKey = pendingOpnKey;
		try {
			synchronized (mutex) {
				LOGGER.finest("Esperando final de operacion: " + this.pendingOpnKey);
                mutex.wait();
            }
        }
        catch (Exception e) {
            LOGGER.finest("Excepcion en el metodo put(String pendingOpnKey)");
        }
	}
	
	public boolean delete(String pendingOpnKey) {
		if (pendingOpnKey == null || !pendingOpnKey.equals(this.pendingOpnKey)) {
			return false;
		} else {
			this.pendingOpnKey = null; 
			return true;
		}
	}
	
	public boolean unlock(String pendingOpnKey) {
		if (pendingOpnKey == null || !pendingOpnKey.equals(this.pendingOpnKey)) {
            return false;
        }
        synchronized (mutex) {
            LOGGER.finest("Fin de la operacion: " + this.pendingOpnKey);
            mutex.notify();
        }
        return true;
	}
	// FIN TO DO
}
