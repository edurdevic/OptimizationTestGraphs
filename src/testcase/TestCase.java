/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testcase;

import java.util.ArrayList;
import tariffnetwork.Dijkstra;
import tariffnetwork.GraphReducer;
import tariffnetwork.datastructures.Arc;
import tariffnetwork.datastructures.Commodity;
import tariffnetwork.datastructures.Graph;
import tariffnetwork.datastructures.TassedArc;
import tariffnetwork.exceptions.ArcNotDefinedException;
import tariffnetwork.exceptions.ArcNotTariffedException;

/**
 *
 * @author daniel
 */
public class TestCase {

    private static final int NODES = 8;

    public static void main(String[] args) {

        Arc[][] mat = new Arc[NODES][NODES];

        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat.length; j++) {
                mat[i][j] = new Arc();
            }
        }

        mat[0][7] = new Arc(2);
        mat[0][2] = new Arc(1);
        mat[0][5] = new Arc(10);

        mat[7][4] = new Arc(3, 3, true);
        mat[2][3] = new Arc(4, 4, true);
        mat[5][6] = new Arc(2, 4, true);

        mat[3][7] = new Arc(4);
        mat[3][5] = new Arc(10);
        mat[4][2] = new Arc(2);

        mat[4][1] = new Arc(2);
        mat[3][1] = new Arc(4);
        mat[6][1] = new Arc(2);

        mat[0][1] = new Arc(11);

        int[] labels = new int[NODES];
        for (int i = 0; i < NODES; i++) {
            labels[i] = i;
        }

        Commodity[] commodities = {new Commodity(0, 1, 7),new Commodity(0, 1, 3)};

        Graph originalGraph = new Graph(mat, commodities, labels);
        Graph addNodesGraph = GraphReducer.addNodesToGraph(originalGraph);

        //Verifico se il grafo originale e quello a cui sono stati aggiunti i
        //nodi siano "equivalenti".
        if (testAddNodes(originalGraph, addNodesGraph)) {
            System.out.println("OK: Il grafo originale e quello modificato hanno gli stessi costi minimi tra i nodi.");
        } else {
            System.out.println("FAIL: Il grafo originale e quello modificato NON hanno gli stessi costi minimi tra i nodi.");
        }

        Graph totalSPGM = GraphReducer.getTotalSPGMWithoutSemplification(addNodesGraph);

        if (testTotalSPGMWithoutReduction(originalGraph, totalSPGM)) {
            System.out.println("OK: Il grafo originale e l'SPGM sono equivalenti.");
        } else {
            System.out.println("FAIL: Il grafo originale e l'SPGM NON sono equivalenti.");
        }
    }

    /**
     * Verifica che i costi minimi tra tutti i nodi siano uguali tra il grafo 
     * originale e il grafo a cui sono stati aggiunti i nodi per fare in modo
     * che non ci siano archi tassati consecutivi o più archi tassati entranti o
     * uscenti dallo stesso nodo e che non ci siano archi tassati che escono da
     * una sorgente o archi tassati che entrano in una destinazione.
     * @param originalGraph grafo originale.
     * @param addNodesGraph grafo con i nodi aggiunti.
     * @return true se i due grafi corrispondono false altrimenti.
     */
    public static boolean testAddNodes(Graph originalGraph, Graph addNodesGraph) {

        //Scorro tutti i nodi
        for (int i = 0; i < NODES; i++) {
            //Applico Dijkstra a entrambe la matrici
            int[] costsOriginalGraph = Dijkstra.dijkstra(originalGraph, i, true);
            int[] costsAddNodesGraph = Dijkstra.dijkstra(addNodesGraph, i, true);

            //Controllo i valori dei primi N nodi
            for (int j = 0; j < costsOriginalGraph.length; j++) {
                if (costsAddNodesGraph[j] != costsOriginalGraph[j]) {
                    System.err.println("Il percorso minimo tra (" + i + "," + j + ") non corrisponde. Archi tassati = true");
                    System.err.println("Orginal graph: " + toStringArray(costsOriginalGraph));
                    System.err.println("Orginal graph: " + toStringArray(originalGraph.getLabels()));
                    System.err.println("Orginal graph: " + toStringArray(costsAddNodesGraph));
                    System.err.println("Orginal graph: " + toStringArray(addNodesGraph.getLabels()));

                    return false;
                }
            }

            int[] addNodeLabels = addNodesGraph.getLabels();

            //Verifico anche quelli che sono stati aggiunti: i nodi nuovi sul grafo
            //"espanso" devono avere lo stesso costo che i avevano i nodi originali
            //prima dell'espansione.
            for (int j = costsOriginalGraph.length; j < costsAddNodesGraph.length; j++) {
                int originalNode = addNodeLabels[j];
                if (costsOriginalGraph[originalNode] != costsAddNodesGraph[j]) {
                    System.err.println("Il percorso minimo tra (" + i + "," + j + ") non corrisponde. Archi tassati = true");
                    System.err.println("Orginal graph: " + toStringArray(costsOriginalGraph));
                    System.err.println("Orginal graph: " + toStringArray(originalGraph.getLabels()));
                    System.err.println("Orginal graph: " + toStringArray(costsAddNodesGraph));
                    System.err.println("Orginal graph: " + toStringArray(addNodesGraph.getLabels()));

                    return false;
                }
            }
        }

        //Scorro tutti i nodi
        for (int i = 0; i < NODES; i++) {
            //Applico Dijkstra a entrambe la matrici
            int[] costsOriginalGraph = Dijkstra.dijkstra(originalGraph, i, false);
            int[] costsAddNodesGraph = Dijkstra.dijkstra(addNodesGraph, i, false);

            //Controllo i valori dei primi N nodi
            for (int j = 0; j < costsOriginalGraph.length; j++) {
                if (costsAddNodesGraph[j] != costsOriginalGraph[j]) {
                    System.err.println("Il percorso minimo tra (" + i + "," + j + ") non corrisponde. Archi tassati = false");
                    System.err.println("Orginal graph: " + toStringArray(costsOriginalGraph));
                    System.err.println("Orginal graph: " + toStringArray(originalGraph.getLabels()));
                    System.err.println("Orginal graph: " + toStringArray(costsAddNodesGraph));
                    System.err.println("Orginal graph: " + toStringArray(addNodesGraph.getLabels()));

                    return false;
                }
            }

            int[] addNodeLabels = addNodesGraph.getLabels();

            //Verifico anche quelli che sono stati aggiunti: i nodi nuovi sul grafo
            //"espanso" devono avere lo stesso costo che i avevano i nodi originali
            //prima dell'espansione.
            for (int j = costsOriginalGraph.length; j < costsAddNodesGraph.length; j++) {
                int originalNode = addNodeLabels[j];
                if (costsOriginalGraph[originalNode] != costsAddNodesGraph[j]) {
                    System.err.println("Il percorso minimo tra (" + i + "," + j + ") non corrisponde. Archi tassati = false");
                    System.err.println("Orginal graph: " + toStringArray(costsOriginalGraph));
                    System.err.println("Orginal graph: " + toStringArray(originalGraph.getLabels()));
                    System.err.println("Orginal graph: " + toStringArray(costsAddNodesGraph));
                    System.err.println("Orginal graph: " + toStringArray(addNodesGraph.getLabels()));

                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Restituisce una stringa che rappresenta l'array passato come parametro.
     * @param array array da "scrivere".
     * @return Stringa che rappresenta l'array.
     */
    private static String toStringArray(int[] array) {
        String aux = "[";

        for (int i = 0; i < array.length; i++) {
            aux += array[i];
            if (i < array.length - 1) {
                aux += ",";
            }
        }

        return aux + "]";
    }

    /**
     * Verifica che il grafo originale sia equivalente all'SPGM "totale".
     * 1) Le commodity siano le stesse (ovviamente con gli indici convertiti per l'SPGM).
     * 2) Gli archi tassati siano gli stessi (ovviamente con gli indici convertiti per l'SPGM).
     * 3) I costi degli archi tassati siano gli stessi.
     * 4) I costi tra sorgenti e nodi da cui iniziano gli archi tassati corrispondano.
     * 5) I costi tra nodi terminali di archi tassati e le destinazioni corrispondano.
     * 6) I costi dei percorsi non tassati tra sorgenti e destinazioni corrispondano.
     * @param originalGraph grafo originale.
     * @param totalSPGM SPGM "totale".
     * @return true se se i due grafi corrispondono, false altrimenti.
     */
    public static boolean testTotalSPGMWithoutReduction(Graph originalGraph, Graph totalSPGM) {
        //Partendo dal SPGM grafo originale estraggo i suoi archi tassati.
        //Estraggo anche gli archi tassati del totalSPGM. Gli archi e i costi 
        //devono coincidere.

        Arc[][] originalMap = originalGraph.getGraph();
        Arc[][] totalSPGMMap = totalSPGM.getGraph();

        int[] totalSPGMLabels = totalSPGM.getLabels();

        ArrayList<TassedArc> originalGraphTassedArcs = new ArrayList<TassedArc>();
        ArrayList<TassedArc> totalSPGMTassedArcs = new ArrayList<TassedArc>();


        for (int i = 0; i < originalMap.length; i++) {
            for (int j = 0; j < originalMap.length; j++) {
                try {
                    if (originalMap[i][j].exist() && originalMap[i][j].getTariffed()) {
                        TassedArc tassedArc = new TassedArc(i, i, j, j, originalMap[i][j].getCost(), originalMap[i][j].getTariffedLength());
                        originalGraphTassedArcs.add(tassedArc);
                    }
                } catch (ArcNotDefinedException e) {
                    System.err.println("originalMap[" + i + "][" + j + "] non è definita in \"testTotalSPGM\"!");
                } catch (ArcNotTariffedException e) {
                    System.err.println("originalMap[" + i + "][" + j + "] non è tassato in \"testTotalSPGM\"!");
                }
            }
        }

        for (int i = 0; i < totalSPGMMap.length; i++) {
            for (int j = 0; j < totalSPGMMap.length; j++) {
                try {
                    if (totalSPGMMap[i][j].exist() && totalSPGMMap[i][j].getTariffed()) {
                        int sourceLabel = totalSPGMLabels[i];
                        int destinationLabel = totalSPGMLabels[j];

                        TassedArc tassedArc = new TassedArc(i, sourceLabel, j, destinationLabel, totalSPGMMap[i][j].getCost(), totalSPGMMap[i][j].getTariffedLength());
                        totalSPGMTassedArcs.add(tassedArc);
                    }
                } catch (ArcNotDefinedException e) {
                    System.err.println("originalMap[" + i + "][" + j + "] non è definita in \"testTotalSPGM\"!");
                } catch (ArcNotTariffedException e) {
                    System.err.println("originalMap[" + i + "][" + j + "] non è tassato in \"testTotalSPGM\"!");
                }
            }
        }

        //Se la dimensione dei due arrayList non corrisponde posso già dire che
        //c'è qualcosa che non è andato per il verso giusto.
        if (originalGraphTassedArcs.size() != totalSPGMTassedArcs.size()) {
            System.err.println("Il numero di archi tassati tra il grafo originale e l'SPGM è diverso.");
            return false;
        }

        TassedArc[][] tassedArcses = new TassedArc[originalGraphTassedArcs.size()][2];

        //Verifico se gli archi tassati corrispondono per sorgente, destinazione
        //e costo. Ne approfitto per salvare le relazioni tra gli archi dei due
        //grafi.
        for (int i = 0; i < originalGraphTassedArcs.size(); i++) {
            TassedArc originalTassedArc = originalGraphTassedArcs.get(i);

            boolean exist = false;

            for (int j = 0; j < totalSPGMTassedArcs.size(); j++) {
                TassedArc totalSPGMTassedArc = totalSPGMTassedArcs.get(j);

                try {
                    if (originalTassedArc.getOriginNode() == totalSPGMTassedArc.getLabelOriginNode()
                            && originalTassedArc.getDestinationNode() == totalSPGMTassedArc.getLabelDestinationNode()
                            && originalTassedArc.getArc().getCost() == totalSPGMTassedArc.getArc().getCost()) {
                        exist = true;

                        tassedArcses[i][0] = originalTassedArc;
                        tassedArcses[i][1] = totalSPGMTassedArc;
                    }
                } catch (ArcNotDefinedException e) {
                    System.err.println("L'arco [" + originalTassedArc.getOriginNode() + "][" + originalTassedArc.getDestinationNode() + "] non è definita in \"testTotalSPGM\"!");
                }
            }

            if (!exist) {
                System.err.println("Non sono riuscito a trovare il nodo tassato equivalente tra il grafo originale e l'SPGM.");
                return false;
            }
        }

        //Calcolo per ogni arco tassato sul grafo originale il costo minimo 
        //tra la fine e l'inizio di due archi tassati e verifico che sia uguale 
        //anche per il totalSPGM.
        for (int i = 0; i < tassedArcses.length; i++) {
            TassedArc originalTassedArc = tassedArcses[i][0];
            TassedArc totalSPGMTassedArc = tassedArcses[i][1];

            for (int j = 0; j < tassedArcses.length; j++) {
                //Salto se stesso
                if (i == j) {
                    continue;
                }

                TassedArc originalTassedArc2 = tassedArcses[j][0];
                TassedArc totalSPGMTassedArc2 = tassedArcses[j][1];

                int[] costsArray = Dijkstra.dijkstra(originalGraph, originalTassedArc.getDestinationNode(), false);
                int costBetweenOriginalTassedArcEndAndOriginalTassedArc2Start = costsArray[originalTassedArc2.getOriginNode()];

                Arc arc = totalSPGMMap[totalSPGMTassedArc.getDestinationNode()][totalSPGMTassedArc2.getOriginNode()];

                if (!arc.exist() && costBetweenOriginalTassedArcEndAndOriginalTassedArc2Start == Integer.MAX_VALUE) {
                    continue;
                }

                try {
                    int costBetweenTotalSPGMTassedArcEndAndTotalSPGMTassedArc2Start = arc.getCost();

                    if (costBetweenTotalSPGMTassedArcEndAndTotalSPGMTassedArc2Start != costBetweenOriginalTassedArcEndAndOriginalTassedArc2Start) {
                        System.err.println("L'arco che collega il nodo " + originalTassedArc.getDestinationNode() + " inizio di un arco tassato al nodo " + originalTassedArc2.getOriginNode() + " non ha lo stesso peso tra il grafo originale e l'SPGM ");
                        return false;
                    }
                } catch (ArcNotDefinedException e) {
                    System.err.println("L'arco [" + totalSPGMTassedArc.getDestinationNode() + "][" + totalSPGMTassedArc2.getOriginNode() + "] non è definita in \"testTotalSPGM\"!");
                }
            }
        }

        //Devo verificare che le commodity corrispondano cioè che siano le 
        //stesse (opportunamente convertite per l'SPGM).
        Commodity[] originalCommodities = originalGraph.getCommodities();
        Commodity[] totalSPGMCommodities = totalSPGM.getCommodities();

        //Se il numero non corrisponde posso già dire che c'è qualcosa che non va
        if (originalCommodities.length != totalSPGMCommodities.length) {
            System.err.print("Il numero delle commodity differisce tra il grafo originale e il totalSPGM");
            return false;
        }

        //Conto sul fatto che per come ho costruito l'SPGM l'ordine delle 
        //commodity delle SPGM non è cambiato rispetto al grafo iniziale.
        for (int i = 0; i < originalCommodities.length; i++) {
            Commodity originalCommodity = originalCommodities[i];
            Commodity totalSPGMCommodity = totalSPGMCommodities[i];

            int originalCommoditySource = originalCommodity.getSource();
            int originalCommodityDestination = originalCommodity.getDestination();

            int totalSPGMCommodityConvertitedSource = totalSPGMLabels[totalSPGMCommodity.getSource()];
            int totalSPGMCommodityConvertitedDestination = totalSPGMLabels[totalSPGMCommodity.getDestination()];

            if (originalCommoditySource != totalSPGMCommodityConvertitedSource
                    || originalCommodityDestination != totalSPGMCommodityConvertitedDestination) {
                System.err.print("Le commodities non corrispondono tra il grafo originale e il totalSPGM");
                return false;
            }
        }

        //Devo verificare per ogni commodity che il costo minimo tra sorgente e 
        //i nodi da cui iniziano gli archi tassati sia uguale al costo degli
        //archi tra la sorgente e il nodo iniziale dell'arco tassato del SPGM.
        //Devo anche verificare che l'arco che collega sorgente e destinazione 
        //del grafo originale abbia lo stesso costo anche per l'SPGM.
        //Scorro tutte le commodity e per ogni commodity tutti gli archi tassati.

        for (int i = 0; i < originalCommodities.length; i++) {

            int originalSource = originalCommodities[i].getSource();
            int originalDestination = originalCommodities[i].getDestination();

            int[] originalCostsBetweenSourceAndOtherNodes = Dijkstra.dijkstra(originalGraph, originalSource, false);

            int sPGMSource = totalSPGMCommodities[i].getSource();
            int sPGMDestination = totalSPGMCommodities[i].getDestination();

            for (int j = 0; j < tassedArcses.length; j++) {
                TassedArc originalTassedArc = tassedArcses[j][0];
                TassedArc sPGMTassedArc = tassedArcses[j][1];

                //Source -> nodo iniziale arco tassato.
                int originalCostBetweenSourceAndTassedArcStartNode = originalCostsBetweenSourceAndOtherNodes[originalTassedArc.getOriginNode()];

                Arc sPGMArcBetweenSPGMSourceAndTassedArcSPGMDestination = totalSPGMMap[sPGMSource][sPGMTassedArc.getOriginNode()];

                if (!sPGMArcBetweenSPGMSourceAndTassedArcSPGMDestination.exist() && originalCostBetweenSourceAndTassedArcStartNode == Integer.MAX_VALUE) {
                    //OK
                } else {
                    try {
                        if (originalCostBetweenSourceAndTassedArcStartNode != sPGMArcBetweenSPGMSourceAndTassedArcSPGMDestination.getCost()) {
                            System.err.println("L'arco che collega il nodo " + originalSource + " inizio di un arco tassato al nodo " + originalTassedArc.getOriginNode() + " non ha lo stesso peso tra il grafo originale e l'SPGM ");
                            return false;
                        }
                    } catch (ArcNotDefinedException e) {
                        System.err.println("L'arco [" + sPGMSource + "][" + sPGMTassedArc.getOriginNode() + "] non è definita in \"testTotalSPGM\"!");
                    }
                }

                //Nodo finale arco tassato -> destination.
                int[] originalCostsBetweenTassedArcEndNodeAndOtherNodes = Dijkstra.dijkstra(originalGraph, originalTassedArc.getDestinationNode(), false);
                int originalCostsBetweenTassedArcEndNodeAndOriginalDestination = originalCostsBetweenTassedArcEndNodeAndOtherNodes[originalDestination];

                Arc sPGMArcBetweenTassedArcEndNodeAndSPGMDestination = totalSPGMMap[sPGMTassedArc.getDestinationNode()][sPGMDestination];

                if (!sPGMArcBetweenTassedArcEndNodeAndSPGMDestination.exist() && originalCostsBetweenTassedArcEndNodeAndOriginalDestination == Integer.MAX_VALUE) {
                    //OK
                } else {
                    try {
                        if (originalCostsBetweenTassedArcEndNodeAndOriginalDestination != sPGMArcBetweenTassedArcEndNodeAndSPGMDestination.getCost()) {
                            System.err.println("L'arco che collega il nodo " + originalTassedArc.getDestinationNode() + " inizio di un arco tassato al nodo " + originalDestination + " non ha lo stesso peso tra il grafo originale e l'SPGM ");
                            return false;
                        }
                    } catch (ArcNotDefinedException e) {
                        System.err.println("L'arco [" + originalTassedArc.getDestinationNode() + "][" + originalDestination + "] non è definita in \"testTotalSPGM\"!");
                    }
                }
            }

            //Verifico anche il percorso non tassato tra sorgente e destinazione
            //della commodity.
            int originalCostsBetweenSourceAndDestination = originalCostsBetweenSourceAndOtherNodes[originalDestination];

            Arc sPGMArcBetweenSPGMSourceAndSPGMDestination = totalSPGMMap[sPGMSource][sPGMDestination];

            if (!sPGMArcBetweenSPGMSourceAndSPGMDestination.exist() && originalCostsBetweenSourceAndDestination == Integer.MAX_VALUE) {
                //OK
            } else {
                try {
                    if (originalCostsBetweenSourceAndDestination != sPGMArcBetweenSPGMSourceAndSPGMDestination.getCost()) {
                        System.err.println("L'arco che collega il nodo " + originalSource + " inizio di un arco tassato al nodo " + originalDestination + " non ha lo stesso peso tra il grafo originale e l'SPGM ");
                        return false;
                    }
                } catch (ArcNotDefinedException e) {
                    System.err.println("L'arco [" + originalSource + "][" + originalDestination + "] non è definita in \"testTotalSPGM\"!");
                }
            }
        }

        return true;
    }
}
