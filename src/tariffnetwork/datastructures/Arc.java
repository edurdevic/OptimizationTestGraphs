/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tariffnetwork.datastructures;

import tariffnetwork.exceptions.ArcNotDefinedException;
import tariffnetwork.exceptions.ArcNotTariffedException;

/**
 * Definisce un arco con costo e come proprietà se è un arco tariffato o meno.
 * @author daniel
 */
public class Arc {
    
    private int cost;
    private boolean tariffed = false;
    private int tariffedLength;
    
    /**
     * Costruttore: assegna il costo e assegna non tassato.
     * @param cost Costo della tratta.
     */
    public Arc(int cost) {
        this.cost = cost;
    }
    
    /**
     * Costruttore: costruisce un arco "vuoto".
     */
    public Arc() {
        cost = -1;
    }
    
    /**
     * Costruttore.
     * @param cost Costo della tratta.
     * @param tariffedLength Lunghezza dell'arco tassato
     * @param tariffed True se l'arco è tariffato, false altrimenti.
     */
    public Arc(int cost, int tariffedLength, boolean tariffed) {
        this.cost = cost;
        this.tariffed = tariffed;
        this.tariffedLength = tariffedLength;
    }
    
    /**
     * Restituisce true se l'arco esiste, false altrimenti.
     * @return true se l'arco esiste, false altrimenti.
     */
    public boolean exist() {
        return (cost >= 0);
    }
    
    /**
     * Setta la tariffa dell'arco se è definito altrimenti restituisce
     * ArcNotDefinedException.
     * @param tariffed True se è tariffato, false altrimenti.
     * @throws ArcNotDefinedException Eccezione se l'arco non è definito.
     */
    public void setTariffed(boolean tariffed) throws ArcNotDefinedException {
        if (exist())
            this.tariffed = tariffed;
        else throw new ArcNotDefinedException();
    }

    /**
     * Restituisce il costo dell'arco se l'arco è definito altrimenti 
     * restituisce ArcNotDefinedException.
     * @return
     * @throws ArcNotDefinedException Eccezione se l'arco non è definito. 
     */
    public int getCost() throws ArcNotDefinedException {
        if (!exist())
            throw new ArcNotDefinedException();
        return cost;
    }

    /**
     * Setta il costo dell'arco. Se non è definito lo "definisce".
     * @param cost Costo dell'arco.
     */
    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     *
     * @return Lunghezza dell'arco da tassare
     * @throws ArcNotDefinedException Se l'arco non esiste (Costo < 0)
     * @throws ArcNotTariffedException Se l'arco non è tassato
     */
    public int getTariffedLength() throws ArcNotDefinedException, ArcNotTariffedException {
        if (!exist())
            throw new ArcNotDefinedException();
        if (!tariffed){
            throw new ArcNotTariffedException();
        }
        
        return tariffedLength;
        
    }

    /**
     *
     * @param length Lunghezza dell'arco da tassare
     * @throws ArcNotTariffedException Se l'arco non è tassato
     */
    public void setTariffedLength(int length) throws ArcNotTariffedException {
        if (!tariffed){
            throw new ArcNotTariffedException();
        }
        this.tariffedLength = length;
    }


    
    /**
     * Restituisce true se l'arco è tariffato, false altrimenti. Se l'arco non
     * è definito restituisce ArcNotDefinedException.
     * @return
     * @throws ArcNotDefinedException 
     */
    public boolean getTariffed() throws ArcNotDefinedException {
        if (cost >= 0)
            return tariffed;
        else
            throw new ArcNotDefinedException();
    }
}
