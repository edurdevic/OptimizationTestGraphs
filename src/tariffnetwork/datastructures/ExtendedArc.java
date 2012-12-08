/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tariffnetwork.datastructures;

/**
 *
 * @author Erni
 */
public class ExtendedArc extends Arc {
    private int source, destination;

    public ExtendedArc(int source, int destination) {
        this.source = source;
        this.destination = destination;
    }

    public ExtendedArc(int cost, int source, int destination) {
        super(cost);
        this.source = source;
        this.destination = destination;
    }

    public ExtendedArc(int cost, int tariffedLenth, boolean tariffed, int source, int destination) {
        super(cost, tariffedLenth, tariffed);
        this.source = source;
        this.destination = destination;
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

    @Override
    public boolean equals(Object o){
        try{
            ExtendedArc other = (ExtendedArc)o;
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
        hash = 37 * hash + this.source;
        hash = 37 * hash + this.destination;
        return hash;
    }
}
