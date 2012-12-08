/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tariffnetwork.statistics;

/**
 * Contiene le informazioni di ogni esecuzione del grafo
 * @author Erni
 */
public class Experiment {
    private double executionTime;
    private int nodeNumber;
    private int reducedNodeNumber;
    private int reducedPathNumber;
    private int nonTariffArcNumber;
    private int tariffArcNumber;
    private int commodityNumber;
    private ExperimentRunType runMode;

    public Experiment(double executionTime, int nodeNumber, int reducedNodeNumber, int reducedPathNumber, int nonTariffArcNumber, int tariffArcNumber, int commodityNumber, ExperimentRunType runMode) {
        this.executionTime = executionTime;
        this.nodeNumber = nodeNumber;
        this.reducedNodeNumber = reducedNodeNumber;
        this.reducedPathNumber = reducedPathNumber;
        this.nonTariffArcNumber = nonTariffArcNumber;
        this.tariffArcNumber = tariffArcNumber;
        this.commodityNumber = commodityNumber;
        this.runMode = runMode;
    }

    public ExperimentRunType getRunMode() {
        return runMode;
    }

    public int getCommodityNumber() {
        return commodityNumber;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public int getNonTariffArcNumber() {
        return nonTariffArcNumber;
    }

    public int getReducedNodeNumber() {
        return reducedNodeNumber;
    }

    public int getReducedPathNumber() {
        return reducedPathNumber;
    }

    public int getTariffArcNumber() {
        return tariffArcNumber;
    }

    

}

