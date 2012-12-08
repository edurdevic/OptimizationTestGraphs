/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tariffnetwork.datastructures;

/**
 *
 * @author daniel
 */
public class TassedArc {
    
    //Nodi di origine e destinazione
    private int originNode = -1;
    private int destinationNode = -1;
    
    //Label dei nodi originali
    private int labelOriginNode = -1;
    private int labelDestinationNode = -1;
    
    //Arco tra i due nodi
    Arc arc = null;

    /**
     * Definisco un arco tassato
     * @param originNode nodo di origine
     * @param labelOriginNode label del nodo origine
     * @param destinationNode nodo di destinazione
     * @param labelDestinationNode label del nodo di destinazione
     * @param cost costo dell'arco
     */
    public TassedArc(int originNode, int labelOriginNode, int destinationNode, int labelDestinationNode, int cost, int tariffedLenth) {
        this.originNode = originNode;
        this.destinationNode = destinationNode;
        this.labelOriginNode = labelOriginNode;
        this.labelDestinationNode = labelDestinationNode;
        
        arc = new Arc(cost, tariffedLenth, true);
    }

    /**
     * Definisco un arco tassato, le label sono di default uguali ai nodi
     * @param originNode nodo di origine
     * @param destinationNode nodo di destinazione
     * @param cost costo dell'arco
     */
    public TassedArc(int originNode, int destinationNode, int cost, int tariffedLenth) {
        this.originNode = originNode;
        this.destinationNode = destinationNode;
        this.labelOriginNode = originNode;
        this.labelDestinationNode = destinationNode;

        arc = new Arc(cost, tariffedLenth, true);
    }

    /**
     * Restituisce l'"arco" tassato.
     * @return 
     */
    public Arc getArc() {
        return arc;
    }

    /**
     * Restituisce il nodo di destinazione dell'arco.
     * @return nodo di destinazione dell'arco.
     */
    public int getDestinationNode() {
        return destinationNode;
    }

    /**
     * Restituisce la label del nodo di destinazione dell'arco.
     * @return label del nodo di destinazione dell'arco.
     */
    public int getLabelDestinationNode() {
        return labelDestinationNode;
    }

    /**
     * Restituisce la label del nodo sorgente dell'arco.
     * @return label del nodo sorgente dell'arco.
     */
    public int getLabelOriginNode() {
        return labelOriginNode;
    }
    
    /**
     * Restituisce il nodo sorgente dell'arco.
     * @return nodo sorgente dell'arco.
     */
    public int getOriginNode() {
        return originNode;
    }
    
    public void println() {
        System.out.println(originNode + "(" + labelOriginNode + ") -----> " + destinationNode + "(" + labelDestinationNode + ")");
    }
}
