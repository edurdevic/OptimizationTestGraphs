/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tariffnetwork.datastructures;

/**
 * Definisce la coppia sorgente/destinazione della Commodity.
 * @author daniel
 */
public class Commodity {
    
    private final int source;
    private final int destination;
    private int cardinality;



    /**
     * Costruttore della commodity: indici sergente e destinazione.
     * @param source Indice del nodo sorgente.
     * @param destination Indice del nodo destinazione.
     */
    public Commodity(int source, int destination) {
        this.source = source;
        this.destination = destination;
        this.cardinality = 1;
    }

    /**
     * Costruttore della commodity: indici sergente e destinazione.
     * @param source Indice del nodo sorgente.
     * @param destination Indice del nodo destinazione.
     * @param cardinality Cardinalità della commodity
     */
    public Commodity(int source, int destination, int cardinality) {
        this.source = source;
        this.destination = destination;
        this.cardinality = cardinality;
    }

    /**
     * Resituisce l'indice del nodo destinazione.
     * @return indice nodo destinazione.
     */
    public int getDestination() {
        return destination;
    }

    /**
     * Resituisce l'indice del nodo sorgente.
     * @return indice nodo sorgente.
     */
    public int getSource() {
        return source;
    }

    public int getCardinality() {
        return cardinality;
    }

    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }
    @Override
    public boolean equals(Object o){
        try{
            Commodity other = (Commodity)o;
            if (other.source == this.source && other.destination == this.destination) return true;
            return false;
        }
        catch(Exception e){
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.source;
        hash = 29 * hash + this.destination;
        return hash;
    }


}
