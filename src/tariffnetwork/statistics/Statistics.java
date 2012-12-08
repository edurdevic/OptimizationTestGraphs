/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tariffnetwork.statistics;

import java.util.ArrayList;

/**
 *
 * @author Erni
 */
public class Statistics {
    private ArrayList<Experiment> experimentList;

    public Statistics() {
        experimentList = new ArrayList<Experiment>();
    }

    public void addExperiment(Experiment experiment){
        experimentList.add(experiment);
    }

    public double getAverageRunningTime(){
        double averageRrunTime = 0;
        double totalRunTime = 0;

        //Se non ci sono esperimenti la media è zero
        if(experimentList.size() == 0) return 0;

        //Calcola la media di tutti gli esperimenti
        for(int i=0; i<experimentList.size(); i++){
            totalRunTime += experimentList.get(i).getExecutionTime();
        }
        averageRrunTime = totalRunTime/experimentList.size();

        return averageRrunTime;
    }

    public double getAverageRunningTime(ExperimentRunType type){
        double averageRrunTime = 0;
        double totalRunTime = 0;

        //Se non ci sono esperimenti la media è zero
        if(experimentList.size() == 0) return 0;

        //Calcola la media solo degli esperimenti di un certo tipo
        for(int i=0; i<experimentList.size(); i++){
            if (experimentList.get(i).getRunMode() == type) totalRunTime += experimentList.get(i).getExecutionTime();
        }
        averageRrunTime = totalRunTime/experimentList.size();

        return averageRrunTime;
    }

    public ArrayList<Experiment> getExperimentList() {
        return experimentList;
    }

}
