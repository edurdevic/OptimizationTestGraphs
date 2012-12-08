/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tariffnetwork.datastructures;


/**
 *
 * @author Erni
 */
public class PathInfo {
    private int cost;
    private int tariffedLength;
    private boolean[] tariffArcs;
    private int[] path;
    private int source;
    private int destination;

    
    public PathInfo(int[] path, int source, int destination) {
        this.path = path;
        this.source = source;
        this.destination = destination;
    }

    public int getTariffedLength() {
        return tariffedLength;
    }

    public void setTariffedLength(int tariffedLength) {
        this.tariffedLength = tariffedLength;
    }


    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int[] getPath() {
        return path;
    }

    public void setPath(int[] path) {
        this.path = path;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public boolean[] getTariffArcs() {
        return tariffArcs;
    }

    public void setTariffArcs(boolean[] tariffArcs) {
        this.tariffArcs = tariffArcs;
    }


}
