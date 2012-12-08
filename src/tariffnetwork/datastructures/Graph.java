/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tariffnetwork.datastructures;

/**
 * Cotenitore per il grafo e le commodity.
 * @author daniel
 */
public class Graph {
    
    private Arc [][] graph;
    private Commodity [] commodities;
    private int [] labels;
    
    /**
     * Setta il grafo e le commodity
     * @param graph Grafo
     * @param commodities Commodities
     * @param labels Etichette (interi) dei nodi del grafo.
     */
    public Graph(Arc [][] graph, Commodity[] commodities, int [] labels) {
        this.graph = graph;
        this.commodities = commodities;
        this.labels = labels;
    }

    /**
     * Setta il grafo e le commodity, autogenera le labels
     * @param graph Grafo
     * @param commodities Commodities
     */
    public Graph(Arc [][] graph, Commodity[] commodities) {
        this.graph = graph;
        this.commodities = commodities;
        this.labels = new int[graph.length];
        for (int i=0; i<graph.length; i++){
            this.labels[i] = i;
        }
    }

    /**
     * Restituisce il vettore di commodity.
     * @return vettore di commodity.
     */
    public Commodity[] getCommodities() {
        return commodities;
    }

    /**
     * Restituisce il vettore delle Etichette dei nodi del grafo.
     * @return Etichette (interi) dei nodi del grafo.
     */
    public int[] getLabels() {
        return labels;
    }

    /**
     * Setta il vettore delle Etichette dei nodi del grafo.
     * @param labels Etichette dei nodi del grafo.
     */
    public void setLabels(int[] labels) {
        this.labels = labels;
    }

    /**
     * Restituisce il grafo sotto forma di matrice.
     * @return Matrice di Arc.
     */
    public Arc[][] getGraph() {
        return graph;
    }
}
