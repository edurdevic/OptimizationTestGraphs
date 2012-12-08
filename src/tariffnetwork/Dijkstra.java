/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tariffnetwork;

import tariffnetwork.datastructures.Arc;
import tariffnetwork.datastructures.Graph;
import tariffnetwork.exceptions.ArcNotDefinedException;

/**
 *
 * @author daniel
 */
public class Dijkstra {

    public static int[] dijkstra(Graph graph, int source, boolean tassed) {

        //System.out.println("Nodo definitives iniziale: " + source);

        Arc[][] mat = graph.getGraph();

        boolean[] definitives = new boolean[mat.length];
        int[] potentials = new int[mat.length];

        for (int i = 0; i < mat.length; i++) {
            definitives[i] = false;
            potentials[i] = Integer.MAX_VALUE;
        }

        //Mi serve a impedire che la condzione di exitDijkstra mi faccia uscire
        //la prima volta ancora prima di entrare: tutti i costi 
        //(a parte il primo) sono massimi e tutti i nodi (a parte il primo) sono
        //non definitivi.

        int actualDefinitiveNode = source;
        potentials[source] = 0;

        while (!exitDijkstra(definitives, potentials)) {
            
            //System.out.println("Rendo definitives il nodo: " + actualDefinitiveNode);

            actualDefinitiveNode = indexMinPotential(definitives, potentials);
            definitives[actualDefinitiveNode] = true;

            for (int i = 0; i < definitives.length; i++) {
                if (tassed) {
                    if (i == actualDefinitiveNode || !mat[actualDefinitiveNode][i].exist() || definitives[i]) {
                        continue;
                    }
                } else {
                    try {
                        if (i == actualDefinitiveNode || !mat[actualDefinitiveNode][i].exist() || mat[actualDefinitiveNode][i].getTariffed() || definitives[i]) {
                            continue;
                        }
                    } catch (ArcNotDefinedException e) {
                        System.err.println("mat[" + actualDefinitiveNode + "][" + i + "] non è definita in \"dijkstra\"!");
                    }
                }

                int newPotential = Integer.MAX_VALUE;

                try {
                    newPotential = potentials[actualDefinitiveNode] + mat[actualDefinitiveNode][i].getCost();
                } catch (ArcNotDefinedException e) {
                    System.err.println("mat[" + actualDefinitiveNode + "][" + i + "] non è definita in \"dijkstra\"!");
                }

                if (potentials[i] > newPotential) {
                    potentials[i] = newPotential;
                }
            }
        }

        return potentials;
    }

    /**
     * Restituisco true se valgono una delle due condizioni
     * 1) Tutti i nodi sono stati resi definitivi
     * 2) I nodi non definitivi hanno costo massimo.
     * @return 
     */
    private static boolean exitDijkstra(boolean[] definitives, int[] potentials) {
        boolean allDefinitives = true;
        boolean allNotDefinitivesPotentialAreMax = true;

        for (int i = 0; i < definitives.length; i++) {
            if (definitives[i] == false) {
                allDefinitives = false;
            }

            if (definitives[i] == false && potentials[i] != Integer.MAX_VALUE) {
                allNotDefinitivesPotentialAreMax = false;
            }

            if (!allDefinitives && !allNotDefinitivesPotentialAreMax) {
                return false;
            }
        }

        return (allDefinitives || allNotDefinitivesPotentialAreMax);
    }

    /**
     * Restituisce l'indice del nodo con minor potenziale tra i nodi non resi 
     * definitivi
     * @param definitives array dei nodi: se la posizione è false nodo 
     * corrispondente reso definito, non definitivo altrimenti
     * @param potentials array dei potenziali dei nodi
     * @return l'indice del nodo con minor potenziale tra i nodi non resi 
     * definitivi
     */
    private static int indexMinPotential(boolean[] definitives, int[] potentials) {
        int min = Integer.MAX_VALUE;
        int index = 0;

        for (int i = 0; i < potentials.length; i++) {
            if (!definitives[i] && potentials[i] < min) {
                min = potentials[i];
                index = i;
            }
        }

        return index;
    }
}
