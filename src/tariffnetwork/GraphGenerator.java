/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tariffnetwork;

import java.util.ArrayList;
import tariffnetwork.datastructures.*;
import tariffnetwork.exceptions.InvalidGraphParamatersException;

/**
 * 
 * @author erni
 */
public class GraphGenerator {

    boolean[] sources;
    boolean[] destinations;
    boolean[] middleNode;

    /**
     * Vincoli sigli input:
     *      z = numero commdity
     *      n = numero nodi
     *      at = archi tassati
     *      a = archi totali
     *      ant = archi non tassati

     *      z*rad(n) < ant < n(n-1)
     *      1 <= at < n(n-1) - ant
     * @param nodeNumber   Numero di archi non tassati
     * @param commodityNumber Numero di Commodity
     * @param nonTariffArcNumber
     * @param tariffArcNumber
     * @return Un grafo semi-casuale in base ai dati di input
     * @throws InvalidGraphParamatersException
     */
    public Graph getGraph(int nodeNumber) throws InvalidGraphParamatersException{
        return getGraph(nodeNumber, (int)(nodeNumber/5), (int)(nodeNumber*nodeNumber/6), (int)(nodeNumber*nodeNumber/25));
    }

    public Graph getGraph(int nodeNumber, int commodityNumber, int nonTariffArcNumber, int tariffArcNumber) throws InvalidGraphParamatersException {

        //TODO: Controllo vincoli
        //z = numero commdity
        //n = numero nodi
        //at = archi tassati
        //a = archi totali
        //ant = archi non tassati
        //
        //z*rad(n) < ant < n(n-1)
        //1 <= at < n(n-1) - ant
        // Se non sono soddisfatti i vincoli il grafo diventa troppo semplice
        if (nonTariffArcNumber < Math.sqrt(nodeNumber) * commodityNumber || nonTariffArcNumber > nodeNumber * (nodeNumber - 1)) {
            throw new InvalidGraphParamatersException();
        }
        if (tariffArcNumber < 1 || tariffArcNumber >= nodeNumber * (nodeNumber - 1) - nonTariffArcNumber) {
            //Non devo andare oltre al limite di capacità del grafo
            //TODO: togliere ancora le impossibilit coppie sorgente-Sorgente e dest-dest
            throw new InvalidGraphParamatersException();
        }
        if (commodityNumber > nodeNumber * nodeNumber / 16) {
            //commodityNumber > nodeNumber/4*nodeNumber/4
            //Non riesco a far stare sorgenti e destinazioni ai lati e avere abbastanza nodi intermedi
            throw new InvalidGraphParamatersException();
        }

        Arc[][] graph = new Arc[nodeNumber][nodeNumber];
        Commodity[] commodities = new Commodity[commodityNumber];
        Graph resultGraph = new Graph(graph, commodities);

        sources = new boolean[nodeNumber];
        destinations = new boolean[nodeNumber];
        middleNode = new boolean[nodeNumber];

        ArrayList<Arc> possibleEmptyArcList = new ArrayList<Arc>();  //Lista di Archi vuoti possibili
        ArrayList<Commodity> possibleCommodityes = new ArrayList<Commodity>();
        
        int maxArcCost = 100;
        int maxCardinality = 20;

        for (int i = 0; i < nodeNumber; i++) {
            sources[i] = false;
            destinations[i] = false;

        }

        //Riempio la lista di possibili commodity:
        for (int i = 0; i < nodeNumber / 4; i++) {
            for (int j = 0; j < nodeNumber / 4; j++) {
                //Il grafo ha sempre sorgenti a sinistra del quarto e destinazioni a destra dei 3/4
                possibleCommodityes.add(new Commodity(i, nodeNumber - j - 1, (int)(Math.random()*(maxCardinality-1))));
            }
        }

        //Scelgo casualmente le Commodity dalla lista di tutte le possibili
        for (int i = 0; i < commodityNumber; i++) {
            //Il grafo ha sempre sorgenti a sinistra del quarto e destinazioni a destra dei 3/4
            //c[i] = new Commodity((int)(Math.random()*(nodeNumber/4-1)), (int)(nodeNumber-Math.random()*(nodeNumber/4-1)));
            int randomCommodityIndex = (int) (Math.random() * (possibleCommodityes.size() - 1));
            commodities[i] = possibleCommodityes.get(randomCommodityIndex);
            possibleCommodityes.remove(randomCommodityIndex);
            sources[commodities[i].getSource()] = true;
            destinations[commodities[i].getDestination()] = true;
        }

        //Inizializza tutto il grafo con archi inesistenti (Vuoti), tranne per i link diretti delle commodity
        for (int i = 0; i < nodeNumber; i++) {
            for (int j = 0; j < nodeNumber; j++) {
                graph[i][j] = new Arc();
                // Non possono esserci archi tra nodo->se_stesso, nodo->sorgente, destinazione->nodo
                if (i != j && !isDestinationNode(i) && !isSourceNode(j)) {
                    //if (isCommodityEndsPath(i, j, commodities)) {
                        //Arco diretto della commodity, creo subito il link (costoso) e non lo metto nella lista
                        //dei possibili archi casuali
                        //graph[i][j].setCost((int) (Math.random() * maxArcCost / 4 + maxArcCost * 3 / 4 - 1));
                        //nonTariffArcNumber--;
                    //} else {
                        possibleEmptyArcList.add(graph[i][j]);
                    //}
                }
            }
        }

        //Lista di tutti i nodi da connettere; una volta connessi posso aggiungere archi a caso
        ArrayList<Integer> nodeToBeConnected = new ArrayList<Integer>();
        for (int i = 0; i < nodeNumber; i++) {
            if (!isSourceNode(i) && !isDestinationNode(i)) {
                nodeToBeConnected.add(i);
            }
        }
        //printGraph(resultGraph);

        //Voglio connettere tutti i nodi, creo dei percorsi per ogni coomodity
        //in modo da coprire tutti i nodi
        int nodesPerCommodity = (int) nodeToBeConnected.size() / commodityNumber;

        //Per tutte le commodity fai un percorso passando per nodesPerCommodity nodi casuali
        for (int i = 0; i < commodityNumber; i++) {
            //da dove partiranno gli archi:
            int lastNode = commodities[i].getSource();
            int nextNode;
            int randomNumber;
            //Se è una commodity intermedia faccio nodesPerCommodity passaggi fino alla destinazione
            if (i != commodityNumber - 1) {
                for (int j = 0; j < nodesPerCommodity; j++) {
                    randomNumber = (int) (Math.random() * (nodeToBeConnected.size() - 1));
                    nextNode = nodeToBeConnected.get(randomNumber);
                    //Setta il costo dell'arco intermedio
                    graph[lastNode][nextNode].setCost((int) (Math.random() * maxArcCost));
                    possibleEmptyArcList.remove(graph[lastNode][nextNode]);
                    nodeToBeConnected.remove(randomNumber);
                    lastNode = nextNode;
                    nonTariffArcNumber--;       //Decremento il numero di archi non tassati da creare
                }
                //Chiudo il percorso arrivando alla destinazione della commodity
                graph[lastNode][commodities[i].getDestination()].setCost((int) (Math.random() * maxArcCost));
                possibleEmptyArcList.remove(graph[lastNode][commodities[i].getDestination()]);
                nonTariffArcNumber--;

                //printGraph(resultGraph);
            } //Altrimenti, se è l'ultima commodity prendi tutti i nodi da connettere rimasti
            else {
                
                while (nodeToBeConnected.size() > 0) {
                    randomNumber = (int) (Math.random() * (nodeToBeConnected.size() - 1));
                    nextNode = nodeToBeConnected.get(randomNumber);
                    //Setta il costo dell'arco intermedio
                    graph[lastNode][nextNode].setCost((int) (Math.random() * maxArcCost));
                    possibleEmptyArcList.remove(graph[lastNode][nextNode]); //Se ho creato l'arco, devo rimuoverlo dalla lista di quelli che è possibile creare
                    nodeToBeConnected.remove(randomNumber);     //Non deve più essere connesso
                    lastNode = nextNode;
                    nonTariffArcNumber--;       //Decremento il numero di archi non tassati da creare
                }
                //Chiudo il percorso arrivando alla destinazione della commodity
                graph[lastNode][commodities[i].getDestination()].setCost((int) (Math.random() * maxArcCost));
                possibleEmptyArcList.remove(graph[lastNode][commodities[i].getDestination()]);
                nonTariffArcNumber--;
            }
        }

        //printGraph(resultGraph);

        //Se ci sono altri archi non tariffati da inserire, li inserisco
        while (nonTariffArcNumber > 0) {
            int randomNumber = (int) (Math.random() * (possibleEmptyArcList.size() - 1));
            possibleEmptyArcList.get(randomNumber).setCost((int) (Math.random() * maxArcCost));
            possibleEmptyArcList.remove(randomNumber);
            nonTariffArcNumber--;
            //printGraph(resultGraph);
        }


        //Se ci sono altri archi tariffati da inserire, li inserisco
        while (tariffArcNumber > 0) {
            int randomNumber = (int) (Math.random() * (possibleEmptyArcList.size() - 1));
            possibleEmptyArcList.get(randomNumber).setCost((int) (Math.random() * maxArcCost / 3) + 1);    //Questi devono costare meno
            try {
                possibleEmptyArcList.get(randomNumber).setTariffed(true);


                possibleEmptyArcList.get(randomNumber).setTariffedLength((int) (Math.random() * maxArcCost / 3) + 1);
                //Se la lunghezza corrisponde al costo:
                //possibleEmptyArcList.get(randomNumber).setTariffedLength(possibleEmptyArcList.get(randomNumber).getCost());


            } catch (Exception e) {
                System.out.println("Oops, non puoi tassare archi che non esistono!");
            }

            possibleEmptyArcList.remove(randomNumber);
            tariffArcNumber--;
            //printGraph(resultGraph);
        }
        //printGraph(resultGraph);
        return resultGraph;
    }

    /**
     * Usa printLn per scrivere la matrice degli archi del grafo e le commodity sullo schermo
     * @param g Grafo da stampare
     */
    public void printGraph(Graph g) {
        System.out.println("Commodityes:");
        for (Commodity c : g.getCommodities()) {
            System.out.println("(" + c.getCardinality() + ")  " + c.getSource() + " --> " + c.getDestination());
        }

        System.out.println();
        System.out.println("Graph:");
        Arc[][] a = g.getGraph();

        for (int i = 0; i < a.length; i++) {
            System.out.println();
            System.out.print(i + "| ");
            for (int j = 0; j < a.length; j++) {
                if (a[i][j].exist()) {
                    try {
                        if (a[i][j].getTariffed()) {
                            System.out.print("*");
                        } else {
                            System.out.print(" ");
                        }
                        System.out.print(a[i][j].getCost());
                    } catch (Exception e) {
                        System.out.print(" -");
                    }
                } else {
                    System.out.print(" -");
                }
            }
        }
        System.out.println();
    }

    private boolean isSourceNode(int n) {
        // Se trovo una commodity con sorgente n ritorno true
        return sources[n];
    }

    private boolean isDestinationNode(int n) {
        return destinations[n];
    }

    /**
     * Data una lista di Commodity risponde true se n è una sorgente
     * @param n nodo da controllare
     * @param commodities array di Commodity
     * @return
     */
    private boolean isSourceNode(int n, Commodity[] commodities) {
        // Se trovo una commodity con sorgente n ritorno true
        for (Commodity c : commodities) {
            if (c.getSource() == n) {
                return true;
            }
        }
        return false;
    }

    /**
     * Data una lista di Commodity risponde true se n è una destinazione
     * @param n nodo da controllare
     * @param commodities array di Commodity
     * @return
     */
    private boolean isDestinationNode(int n, Commodity[] commodities) {
        // Se trovo una commodity con sorgente n ritorno true
        for (Commodity c : commodities) {
            if (c.getDestination() == n) {
                return true;
            }
        }
        return false;
    }

    /**
     * Date sorgente e destinazione risponde true se esiste una commodity tale da avere tali sorgente e destinazione
     * @param source
     * @param destination
     * @param commodities
     * @return
     */
    private boolean isCommodityEndsPath(int source, int destination, Commodity[] commodities) {
        for (Commodity c : commodities) {
            if (c.getDestination() == destination && c.getSource() == source) {
                return true;
            }
        }
        return false;
    }
}
