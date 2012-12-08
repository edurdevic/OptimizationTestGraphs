/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tariffnetwork;

import java.util.ArrayList;
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
public class GraphReducer {

    private static final int NODES = 8;
    private static final int SPACE = 10;

    public static void main(String[] arg) {

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

        Commodity[] commodities = {new Commodity(0, 1, 6)};

        Graph graph = new Graph(mat, commodities, labels);

        printGraph(graph, SPACE);

        Graph newGraph = reduceGraphToSPGM(graph);
        /*
        System.out.println();
        
        int source = 9;
        int [] costs = Dijkstra.dijkstra(graph, source, false);
        
        for (int i = 0; i < costs.length; i ++) {
        System.out.println("Costo del percorso (" + source + "," + i + "): " + costs[i]);
        }
         */

        printGraph(newGraph, SPACE);

        //printGraph(getTotalSPGM(newGraph), SPACE);

        printGraph(getTotalSPGM(newGraph), SPACE);

        /*
        for (Graph aux : getSPGMs(newGraph)) {
        printGraph(aux, SPACE);
        System.out.println();
        }*/
    }

    public static Graph addNodesToGraph(Graph graph) {

        Graph newGraph = null;

        do {
            newGraph = addNodes(graph);
            if (newGraph != null) {
                graph = newGraph;
            }
        } while (newGraph != null);


        return graph;
    }

    public static Graph reduceGraphToSPGM(Graph graph) {

        Graph newGraph = null;

        do {
            newGraph = addNodes(graph);
            if (newGraph != null) {
                graph = newGraph;
            }
        } while (newGraph != null);


        return graph;
    }

    public static Graph reduceSPGMToFinalSPGM(Graph graph) {
        return null;
    }

    private static Graph addNodes(Graph graph) {

        Arc[][] mat = graph.getGraph();
        int[] labels = graph.getLabels();
        Commodity[] commodities = graph.getCommodities();

        //Devo verificare che dalle sorgenti non partano archi tassati e alle
        //destinazioni non arrivino archi tassati.
        for (Commodity commodity : commodities) {
            int commoditySource = commodity.getSource();
            int commodityDestination = commodity.getDestination();

            //Scorro gli archi che partono dalla sorgente alla ricerca di archi tassati
            for (int i = 0; i < mat.length; i++) {
                if (commoditySource == i) {
                    continue;
                }

                try {
                    if (mat[commoditySource][i].exist() && mat[commoditySource][i].getTariffed()) {
                        //C'è un arco tassato che parte dalla sorgente: devo creare un nuovo nodo,
                        //fare in modo che il vecchio nodo punti a quello nuovo con costo 0 e 
                        //devo connettere l'arco tassato dal nuovo nodo alla vecchia destinazione.
                        Arc[][] newMat = addNode(mat);

                        newMat[mat.length][i] = mat[commoditySource][i];
                        newMat[commoditySource][i] = new Arc();
                        newMat[commoditySource][mat.length] = new Arc(0);
                        newMat[mat.length][commoditySource] = new Arc(0);

                        int[] newLabels = addToLabels(labels, commoditySource);

                        return new Graph(newMat, commodities, newLabels);
                    }
                } catch (ArcNotDefinedException e) {
                    System.err.println("mat[" + commoditySource + "][" + i + "] non è definita in \"addNodes\"!");
                }
            }

            //Scorro gli archi che arrivano alla destinazione alla ricerca di archi tassati
            for (int i = 0; i < mat.length; i++) {
                if (commodityDestination == i) {
                    continue;
                }

                try {
                    if (mat[i][commodityDestination].exist() && mat[i][commodityDestination].getTariffed()) {
                        //C'è un arco tassato arriva alla destinazione: devo creare un nuovo nodo,
                        //fare in modo che il vecchio nodo punti a quello nuovo con costo 0 e 
                        //devo connettere l'arco tassato dal nuovo nodo alla vecchia destinazione.
                        Arc[][] newMat = addNode(mat);

                        newMat[i][mat.length] = mat[i][commodityDestination];
                        newMat[i][commodityDestination] = new Arc();
                        newMat[commodityDestination][mat.length] = new Arc(0);
                        newMat[mat.length][commodityDestination] = new Arc(0);

                        int[] newLabels = addToLabels(labels, commodityDestination);

                        return new Graph(newMat, commodities, newLabels);
                    }
                } catch (ArcNotDefinedException e) {
                    System.err.println("mat[" + commoditySource + "][" + i + "] non è definita in \"addNodes\"!");
                }
            }

        }

        //Devo scorrere tutta la matrice e verificare se ci sono nodi in cui 
        //entrano ed escono archi tassati oppure entrano due archi tassati nello
        //stesso arco o escono due archi tassati dallo stesso nodo
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat.length; j++) {

                try {
                    //Verifico se l'arco esiste e se è tessato. Se lo è
                    //1) se esiste un arco tassato che esce dal nodo di destinazione
                    //2) se un altro arco tassato entra nel nodo destinazione
                    //3) se un altro arco tassato esce dal nodo corrente
                    if (mat[i][j].exist() && mat[i][j].getTariffed()) {

                        //1) Scorro la destinazione alla ricerca di archi tassati
                        for (int k = 0; k < mat.length; k++) {
                            try {
                                if (mat[j][k].exist() && mat[j][k].getTariffed()) {
                                    //Devo aggiungere un nuovo nodo, fare in modo che il vecchio 
                                    //nodo punti a quello nuovo con costo 0 
                                    //e che l'arco tassato parta dal nuovo nodo. 
                                    //Devo creare anche l'arco di costo 0 che collega il nuovo nodo al vecchio
                                    Arc[][] newMat = addNode(mat);

                                    newMat[mat.length][k] = mat[j][k];
                                    newMat[j][k] = new Arc();
                                    newMat[j][mat.length] = new Arc(0);
                                    newMat[mat.length][j] = new Arc(0);

                                    int[] newLabels = addToLabels(labels, j);

                                    return new Graph(newMat, commodities, newLabels);
                                }
                            } catch (ArcNotDefinedException e) {
                                System.err.println("mat[" + j + "][" + k + "] non è definita in \"addNodes\"!");
                            }
                        }

                        //2) Scorro la colonna j-esima alla ricerca di altri
                        //archi tassati. Salto la posizione i-esima perchè
                        //altrimenti beccherei lo stesso arco tassato.
                        for (int k = 0; k < mat.length; k++) {

                            if (k == i) {
                                continue;
                            }

                            try {
                                if (mat[k][j].exist() && mat[k][j].getTariffed()) {
                                    //Devo aggiungere un nuovo nodo, fare in modo che l'arco tassato 
                                    //tra (k,j) sia ora tra (k,nuovoNodo),
                                    //il nuovo nodo punti al nodo j con costo 0. Devo anche creare un collegamento
                                    //di costo 0 tra il vecchio nodo e il nodo nuovo.
                                    Arc[][] newMat = addNode(mat);

                                    newMat[k][mat.length] = mat[k][j];
                                    newMat[k][j] = new Arc();
                                    newMat[mat.length][j] = new Arc(0);
                                    newMat[j][mat.length] = new Arc(0);

                                    int[] newLabels = addToLabels(labels, j);

                                    return new Graph(newMat, commodities, newLabels);
                                }
                            } catch (ArcNotDefinedException e) {
                                System.err.println("mat[" + k + "][" + j + "] non è definita in \"addNodes\"!");
                            }
                        }

                        //3) Scorro la riga i-esima saltando la posizione (i,j) alla ricerca 
                        //di altri archi tassati
                        for (int k = 0; k < mat.length; k++) {

                            if (k == j) {
                                continue;
                            }

                            try {
                                if (mat[i][k].exist() && mat[i][k].getTariffed()) {
                                    //Creo un nuovo nodo, devo fare in modo che l'arco 
                                    //tassato (i,k) sia ora (nuovoNodo,k), che il nodo 
                                    //i-esimo punti al nuovoNodo con costo 0.
                                    Arc[][] newMat = addNode(mat);

                                    newMat[mat.length][k] = mat[i][k];
                                    newMat[i][k] = new Arc();
                                    newMat[i][mat.length] = new Arc(0);
                                    newMat[mat.length][i] = new Arc(0);

                                    int[] newLabels = addToLabels(labels, i);

                                    return new Graph(newMat, commodities, newLabels);
                                }
                            } catch (ArcNotDefinedException e) {
                                System.err.println("mat[" + i + "][" + k + "] non è definita in \"addNodes\"!");
                            }
                        }

                    }
                } catch (ArcNotDefinedException e) {
                    System.err.println("mat[" + i + "][" + j + "] non è definita in \"addNodes\"!");
                }
            }
        }

        return null;
    }

    /**
     * Aggiunge un nuovo nodo non connesso all'ultima riga e colonna della 
     * matrice
     * @param mat matrice di partenza
     * @return nuova matrice con n+1 nodi con i primi n nodi connessi come per
     * la matrice originale.
     */
    private static Arc[][] addNode(Arc[][] mat) {

        Arc[][] newMat = new Arc[mat.length + 1][mat.length + 1];

        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat.length; j++) {
                newMat[i][j] = mat[i][j];
            }
        }

        for (int i = 0; i < mat.length + 1; i++) {
            newMat[i][mat.length] = new Arc();
            newMat[mat.length][i] = new Arc();
        }

        return newMat;
    }

    /**
     * Stampa il grafo su schermo
     * @param mat matrice di archi
     * @param space spazio tra una colonna e l'altra
     */
    public static void printGraph(Graph graph, int space) {

        Arc[][] mat = graph.getGraph();
        int[] labels = graph.getLabels();

        String separator = "";
        String fixed = "";
        for (int i = 0; i < space + 1; i++) {
            fixed += "-";
        }

        System.out.print(normalizeString("", space) + "|");

        for (int i = 0; i < mat.length; i++) {
            System.out.print(normalizeString(i + "(" + labels[i] + ")", space) + "|");
            separator += fixed;
        }

        separator += fixed;
        separator += fixed;

        System.out.println();
        System.out.println(separator);

        for (int i = 0; i < mat.length; i++) {

            if (labels[i] == i) {
                System.out.print(normalizeString(i + "", space) + "|");
            } else {
                System.out.print(normalizeString(i + "(" + labels[i] + ")", space) + "|");
            }

            for (int j = 0; j < mat.length; j++) {

                if (mat[i][j].exist()) {
                    try {
                        System.out.print(normalizeString("(" + mat[i][j].getCost() + "," + mat[i][j].getTariffed() + ")", space) + "|");
                    } catch (ArcNotDefinedException e) {
                        System.err.println("mat[" + i + "][" + j + "] non è definita in \"printGraph\"!");
                    }
                } else {
                    System.out.print(normalizeString("", space) + "|");
                }
            }

            System.out.println();
            System.out.println(separator);
        }

    }

    /**
     * Riporta una stringa in input alla dimensione fissata da length. 
     * Se la stringa è di dimensione minore di lenght vengono aggiunti 
     * caratteri " "
     * @param input Stringa in input
     * @param length Lunghezza della stringa finale
     * @return stringa "normalizzata"
     */
    private static String normalizeString(String input, int length) {

        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            stringBuffer.append(" ");
        }

        String fixed = stringBuffer.toString();

        return (input + fixed).substring(0, length);
    }

    /**
     * Restituisce lo stesso vettore allungato di una unità riempita con label.
     * @param labels Etichette dei nodi.
     * @param label Nuova etichetta da aggiungere.
     * @return Nuovo vettore delle etichette.
     */
    private static int[] addToLabels(int[] labels, int label) {
        int[] newLabels = new int[labels.length + 1];

        for (int i = 0; i < labels.length; i++) {
            newLabels[i] = labels[i];
        }

        newLabels[labels.length] = label;

        return newLabels;
    }

    /**
     * A partire dal grafo (rielaborato perchè 
     * 1) non ci siano due archi tassati entranti o uscenti dallo stesso nodo 
     * 2) che non ci siano archi tassati consecutivi
     * 3) che non ci siano archi tassati che partono da una sorgente
     * 4) che non ci siano archi tassati che arrivano a una destinazione)
     * genera tutti gli SPGM e li restituisce.
     * @param graph Grafo da "semplificare".
     * @return vettore di SPGM (un grafo per ogni SPGM).
     */
    private static Graph[] getSPGMs(Graph graph) {

        ArrayList<TassedArc> tassedArrayList = new ArrayList<TassedArc>();
        Arc[][] mat = graph.getGraph();
        Commodity[] commodities = graph.getCommodities();

        Graph[] spgmArray = new Graph[commodities.length];

        //Per prima cosa devo "estrarre" gli archi tassati.
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat.length; j++) {
                try {
                    if (mat[i][j].exist() && mat[i][j].getTariffed()) {
                        int[] labels = graph.getLabels();

                        TassedArc tassedArc = new TassedArc(i, labels[i], j, labels[j], mat[i][j].getCost(), mat[i][j].getTariffedLength());
                        tassedArrayList.add(tassedArc);
                    }
                } catch (ArcNotDefinedException e) {
                    System.err.println("mat[" + i + "][" + j + "] non è definita in \"getSPGMs\"!");
                } catch (ArcNotTariffedException e) {
                    System.err.println("mat[" + i + "][" + j + "] non è tassato in \"getSPGMs\"!");
                }
            }
        }

        //Creo per ogni arco tassato il vettore dei costi minimi tra il nodo di 
        //fine arco tassato e tutti gli altri nodi non passando per gli archi 
        //tassati. Così facendo creo in un'unica "passata" di Dijkstra i
        //costi minimi tra il nodo finale e l'arco tassato corrente 
        //de tutte le destinazioni delle commodity e i costi minimi tra il 
        //nodo di destinazione dell'arco tassato corrente e il nodo iniziale
        //di tutti gli archi tassati.
        //PS: non posso fare lo stesso lavoro dentro il secondo ciclo precedente
        //perchè non so a priori il numero di archi tassati per generare la 
        //matrice di costi: potrei usare un altro ArrayList ma sarei obbligato
        //ad usare oggetti.
        int[][] matrixCosts = new int[tassedArrayList.size()][];

        for (int i = 0; i < tassedArrayList.size(); i++) {
            int endTassedArcNode = tassedArrayList.get(i).getDestinationNode();
            matrixCosts[i] = Dijkstra.dijkstra(graph, endTassedArcNode, false);
        }

        //Devo creare tanti SPGM quante sono le commodites.
        //I nodi con archi tassati sono "fissi" e a quelli devo aggiungere altri
        //due nodi che sono le estremità della commodities.
        int nodesNumber = (tassedArrayList.size() * 2) + 2;
        Arc[][] spgmMat = new Arc[nodesNumber][nodesNumber];

        //"Azzero" la matrice
        for (int i = 0; i < nodesNumber; i++) {
            for (int j = 0; j < nodesNumber; j++) {
                spgmMat[i][j] = new Arc();
            }
        }

        int[] labels = new int[nodesNumber];

        //Metto i nodi da cui iniziano gli archi tassati all'inizio e quelli in 
        //cui finiscono subito dopo.
        //es:
        //0 ---> 3
        //1 ---> 4
        //2 ---> 5
        //Salvo anche le etichette per ricordarmi qual era il nodo originale.
        //Inoltre per ogni arco tassato creo un arco di costo minimo tra il nodo
        //finale dell'arco tasato corrente e i nodi iniziali degli altri archi 
        //tassati se ovviamente esiste un percorso con archi non tassati che li
        //unisce.
        int j = tassedArrayList.size();
        for (int i = 0; i < tassedArrayList.size(); i++) {
            TassedArc tassedArc = tassedArrayList.get(i);
            spgmMat[i][j] = tassedArc.getArc();
            labels[i] = tassedArc.getLabelOriginNode();
            labels[j] = tassedArc.getLabelDestinationNode();

            //int originalEndNode = tassedArc.getDestinationNode();

            for (int k = 0; k < tassedArrayList.size(); k++) {

                //Salto l'arco tassato corrente
                if (k == i) {
                    continue;
                }

                TassedArc otherTassedArc = tassedArrayList.get(k);
                int originalStartNodeOtherNode = otherTassedArc.getOriginNode();

                int costBetweenOriginalEndNodeAndOriginalStartOtherNode = matrixCosts[i][originalStartNodeOtherNode];

                //Se il costo è massimo (Integer.MAX_VALUE) significa che non esiste 
                //un percorso che non comprenda archi tassati tra il nodo finale
                //dell'arco tassato i-esimo e il nodo iniziale dell'arco tassato k-esimo.
                //Se così è Continuo il ciclo senza aggiungere alcun arco.
                if (costBetweenOriginalEndNodeAndOriginalStartOtherNode == Integer.MAX_VALUE) {
                    continue;
                }

                //C'è un percorso: aggiungo l'arco. Il primo indice dell'array è j 
                //perchè l'arco tassato per costruzione termina con il nodo j. 
                //Il secondo indice invece è k perchè sempre per costruzione l'arco 
                //tassato k-esimo ha come nodo iniziale il nodo k.
                spgmMat[j][k] = new Arc(costBetweenOriginalEndNodeAndOriginalStartOtherNode);
            }

            j++;
        }

        int sPGMSource = j;
        int sPGMDestination = j + 1;

        //Devo copiare l'SPGM che ho creato in precedenza (a cui mancano le varie
        //commodity) tante volte quante sono le commodity, aggiungere per ognuno
        //una commodity e poi creare gli archi di costo minimo tra sorgente e 
        //inizio arco tassato e fine arco tassato e destinazione.
        for (int k = 0; k < commodities.length; k++) {

            Arc[][] newMat = copyMat(spgmMat);
            int[] newLabels = copyLabels(labels);

            newLabels[sPGMSource] = commodities[k].getSource();
            newLabels[sPGMDestination] = commodities[k].getDestination();

            //Calcolo i costi dal nodo sorgente a tutti gli altri nodi senza
            //considerare gli archi tassati.
            int[] costs = Dijkstra.dijkstra(graph, commodities[k].getSource(), false);

            //Scorro tutti gli archi tassati, creo un arco tra la sorgente e il
            //nodo iniziale dell'arco tassato considerato (se il costo non è 
            //massimo) e creo un arco dal nodo finale dell'arco tassato alla 
            //destinazione (sempre se il costo non è massimo).
            for (int i = 0; i < tassedArrayList.size(); i++) {
                TassedArc tassedArc = tassedArrayList.get(i);

                //Sorgente -> nodo inizio arco tassato.
                int startOriginalArcTassedNode = tassedArc.getOriginNode();
                int costBetweenSourceAndTassedArcStartNode = costs[startOriginalArcTassedNode];

                //Se il costo non è massimo significa che c'è un percorso non tassato
                //che collega la sorgente al nodo d'inizio del percorso tassato
                //considerato. Se è così devo creare l'arco altrimenti non faccio nulla.
                if (costBetweenSourceAndTassedArcStartNode != Integer.MAX_VALUE) {
                    newMat[sPGMSource][i] = new Arc(costBetweenSourceAndTassedArcStartNode);
                }

                //Nodo fine arco tassato -> destinazione.
                //int endOriginalArcTassedNode = tassedArc.getDestinationNode();
                int costBetweenTassedArcNodeAndDestination = matrixCosts[i][commodities[k].getDestination()];

                //Per come è stata costruita la matrice il nodo finale dell'arco
                //ha sempre l'indice nodo_iniziale + numero_archi_tassati.
                int endArcTassedNode = i + tassedArrayList.size();

                //Se il costo non è massimo significa che c'è un percorso non tassato
                //che collega la fine dell'arco tassato considerato al nodo destinazione.
                //Se è così creo l'arco altrimenti non faccio nulla.
                if (costBetweenTassedArcNodeAndDestination != Integer.MAX_VALUE) {
                    newMat[endArcTassedNode][sPGMDestination] = new Arc(costBetweenTassedArcNodeAndDestination);
                }
            }

            //Devo tracciare l'arco diretto (se esiste) tra sorgente e destinazione.
            //Se non è massimo traccio l'arco altrimenti no. Teoricamente dovrebbe
            //sempre esserci un percorso non tassato tra sorgente e destinazione
            //per come è stato costruito il grafo.
            int sourceToDestinationCost = costs[commodities[k].getDestination()];

            if (sourceToDestinationCost != Integer.MAX_VALUE) {
                newMat[sPGMSource][sPGMDestination] = new Arc(sourceToDestinationCost);
            }

            //Creo un array di un solo elemento che è la commodity a cui è 
            //associato l'SPGM. Devo però prima convertire la commodity: la 
            //sorgente e la destinazione si riferivano al grafo originale non a
            //quello che è stato appena creato.
            Commodity[] newCommodities = {new Commodity(nodesNumber - 2, nodesNumber - 1, commodities[k].getCardinality())};

            spgmArray[k] = new Graph(newMat, newCommodities, newLabels);
        }

        return spgmArray;
    }

    /**
     * A partire dal grafo originale crea gli SPGM, NON li riduce ulteriormente
     * e li riunisce in un unico grafo.
     * @param graph grafo originale.
     * @return nuovo grafo.
     */
    public static Graph getTotalSPGMWithoutSemplification(Graph graph) {
        //Genero tutti gli SPGM
        Graph[] sPGMArray = getSPGMs(graph);

        //Verifico se getSPGMs mi ha restituito qualcosa di "sensato"
        if (sPGMArray == null || sPGMArray.length == 0) {
            System.err.println("Il metodo \"getSPGMs\" ha restituito al metodo \"getTotalSPGM\" un array nullo o vuoto");
            return null;
        }

        //Se c'è un'unica commodity ne consegue che l'SPGM sarà l'unico: mi 
        //limito a restituirlo.
        if (sPGMArray.length == 1) {
            return sPGMArray[0];
        }

        Commodity[] commodities = graph.getCommodities();

        //Il nuovo grafo deve avere il numero di nodi pari a due volte il numero 
        //di archi tassati più il numero di sorgenti e destinazioni delle 
        //commodity.
        int commonNodesNumber = sPGMArray[0].getLabels().length - 2;
        int newNodeNumbers = commonNodesNumber + (2 * commodities.length);

        Arc[][] newMat = new Arc[newNodeNumbers][newNodeNumbers];
        int[] newLabels = new int[newNodeNumbers];

        //Copio le etichette fino al numero di (nodi_comuni -1 + 2). Questo 
        //perchè i nodi sorgenti e destinazione della prima SPGM rimarranno
        //nella stessa posizione che occupano nel SPGM riferito alla prima 
        //commodity.
        int[] firstSPGMLabels = sPGMArray[0].getLabels();
        for (int i = 0; i < commonNodesNumber + 2; i++) {
            newLabels[i] = firstSPGMLabels[i];
        }

        //Per comodità riempio la parte della matrice che sicuramente avrà 
        //pochissimi archi con archi non definiti. Sarà il quadrato compreso tra
        //la colonna commonNodesNumber e la colonna (newNodeNumbers - 1) delle
        //righe da commonNodesNumber a (newNodeNumbers - 1). Le uniche 
        //informazioni che verranno inserite in questo blocco saranno per ogni 
        //commodity un arco non tassato tra la sorgente della commodity stessa e
        //la sua destinazione.
        for (int i = commonNodesNumber; i < newNodeNumbers; i++) {
            for (int j = commonNodesNumber; j < newNodeNumbers; j++) {
                newMat[i][j] = new Arc();
            }
        }

        //Copio così com'è il primo SPGM: in pratica copio i nodi "comuni" a 
        //tutti gli SPGM e i nodi riferiti alla prima commodity.
        Arc[][] firstSPGMMat = sPGMArray[0].getGraph();

        for (int i = 0; i < commonNodesNumber + 2; i++) {
            for (int j = 0; j < commonNodesNumber + 2; j++) {
                newMat[i][j] = firstSPGMMat[i][j];
            }
        }

        //Per le altre commodity invece copio nelle colonne successive la parte
        //riferita ai percorsi tra nodi di fine archi tassati e le destinazioni (1)
        //e le righe riferite ai percorsi tra sorgenti e nodi di inizio archi tassati (2).
        //Copio inoltre l'arco non tassato tra sorgente e destinazione della commodity (3).
        //Devo anche sistemare le etichette: per ogni commodity inserisco come 
        //etichetta del nodo sorgente e della destinazione l'indice del nodo 
        //sorgente e l'indice del nodo di destinazione (4).
        int j = commonNodesNumber + 2;
        for (int i = 1; i < commodities.length; i++) {
            Arc[][] commoditySPGM = sPGMArray[i].getGraph();

            for (int k = 0; k < commonNodesNumber; k++) {
                //(1)
                newMat[k][j] = commoditySPGM[k][commonNodesNumber];
                newMat[k][j + 1] = commoditySPGM[k][commonNodesNumber + 1];

                //(2)
                newMat[j][k] = commoditySPGM[commonNodesNumber][k];
                newMat[j + 1][k] = commoditySPGM[commonNodesNumber + 1][k];
            }

            //(3)
            newMat[j][j + 1] = commoditySPGM[commonNodesNumber][commonNodesNumber + 1];

            //(4)
            newLabels[j] = commodities[i].getSource();
            newLabels[j + 1] = commodities[i].getDestination();

            j += 2;
        }

        //Devo creare il nuovo vettore di Commodity: quello originale non va più
        //bene perchè negli SPGM sono stati cambiati gli indici.
        Commodity[] newCommodities = new Commodity[sPGMArray.length];
        int row = commonNodesNumber;
        for (int i = 0; i < sPGMArray.length; i++) {
            newCommodities[i] = new Commodity(row, row + 1, commodities[i].getCardinality());

            row += 2;
        }

        return new Graph(newMat, newCommodities, newLabels);
    }
    
    /**
     * A partire dal grafo originale crea gli SPGM, li riduce ulteriormente
     * e li riunisce in un unico grafo.
     * @param graph grafo originale.
     * @return nuovo grafo.
     */
    public static Graph getTotalSPGM(Graph graph) {
        //Genero tutti gli SPGM
        Graph[] sPGMArray = getSPGMs(graph);

        //Scrivo tutti gli archi tassati in un ArrayList tanto sono uguali per
        //tutti gli SPGM almeno all'inizio visto che poi possono anche essere
        //eliminati. Il numero di nodi comuni sono quelli che sono sorgente
        //e destinazione degli archi tassati. POichè li abbiamo separati in
        //modo da non avere archi tassati che entrano o escono dalla stesso nodo
        //oppure che non ci siano archi tassati consecutivi il numero di archi
        //tassati sarà la metà di questo numero di nodi.
        int commonNodesNumber = sPGMArray[0].getLabels().length - 2;
        int commonTassedArcsNumber = commonNodesNumber / 2;
        ArrayList<TassedArc> tassedArcsArray = new ArrayList<TassedArc>();

        for (int i = 0; i < commonTassedArcsNumber; i++) {
            int labelNodeI = sPGMArray[0].getLabels()[i];
            int labelNodeIPlusCommonTassedArcsNumber = sPGMArray[0].getLabels()[i + commonTassedArcsNumber];
            try {
                int cost = sPGMArray[0].getGraph()[i][i + commonTassedArcsNumber].getCost();
                int tariffedLength = sPGMArray[0].getGraph()[i][i + commonTassedArcsNumber].getTariffedLength();
                TassedArc tassedArc = new TassedArc(i, labelNodeI, i + commonTassedArcsNumber, labelNodeIPlusCommonTassedArcsNumber, cost, tariffedLength);

                tassedArcsArray.add(tassedArc);
            } catch (ArcNotDefinedException e) {
                System.err.println("L'arco (" + i + "," + (i + commonNodesNumber) + ") dell'SPGM di indice [" + i + "] nel metodo \"getTotalSPGM\" dovrebbe essere tassato!");
            } catch (ArcNotTariffedException e) {
                System.err.println("L'arco (" + i + "," + (i + commonNodesNumber) + ") dell'SPGM di indice [" + i + "] non è tassato in \"getTotalSPGM\"!");
            }
        }

        //Semplifico tutti gli SPGM applicando i criteri di riduzione.
        for (int i = 0; i < sPGMArray.length; i++) {
            sPGMArray[i] = applyReductPrepositions(sPGMArray[i], tassedArcsArray);
        }

        //Verifico se getSPGMs mi ha restituito qualcosa di "sensato"
        if (sPGMArray == null || sPGMArray.length == 0) {
            System.err.println("Il metodo \"applyReductPrepositions\" ha restituito al metodo \"getTotalSPGM\" un array nullo o vuoto");
            return null;
        }

        //Se c'è un'unica commodity ne consegue che l'SPGM sarà l'unico: mi 
        //limito a restituirlo.
        if (sPGMArray.length == 1) {
            //return sPGMArray[0];
        }

        Commodity[] commodities = graph.getCommodities();

        //Il nuovo grafo deve avere il numero di nodi pari a due volte il numero 
        //di archi tassati più il numero di sorgenti e destinazioni delle 
        //commodity.
        int newNodeNumbers = commonNodesNumber + (2 * commodities.length);

        Arc[][] newMat = new Arc[newNodeNumbers][newNodeNumbers];
        int[] newLabels = new int[newNodeNumbers];

        //Copio le etichette fino al numero di (nodi_comuni -1 + 2). Questo 
        //perchè i nodi sorgenti e destinazione della prima SPGM rimarranno
        //nella stessa posizione che occupano nel SPGM riferito alla prima 
        //commodity.
        int[] firstSPGMLabels = sPGMArray[0].getLabels();
        for (int i = 0; i < commonNodesNumber + 2; i++) {
            newLabels[i] = firstSPGMLabels[i];
        }

        //Per comodità riempio la parte della matrice che sicuramente avrà 
        //pochissimi archi con archi non definiti. Sarà il quadrato compreso tra
        //la colonna commonNodesNumber e la colonna (newNodeNumbers - 1) delle
        //righe da commonNodesNumber a (newNodeNumbers - 1). Le uniche 
        //informazioni che verranno inserite in questo blocco saranno per ogni 
        //commodity un arco non tassato tra la sorgente della commodity stessa e
        //la sua destinazione.
        for (int i = commonNodesNumber; i < newNodeNumbers; i++) {
            for (int j = commonNodesNumber; j < newNodeNumbers; j++) {
                newMat[i][j] = new Arc();
            }
        }

        //######################################################################

        //Copio la parte compresa tra (0,commonNodesNumber) e 
        //(commonNodesNumber+1,commonNodesNumber+1) e la parte compresa tra 
        //(commonNodesNumber,0) e (commonNodesNumber+1,commonNodesNumber-1)
        //della matrice del primo SPGM direttamente nella nuova matrice.
        //Queste due parti contengono i soli archi tra sorgente e archi tassati
        //e archi tassati e destinazione della sola prima commodity.
        for (int i = 0; i < commonNodesNumber + 2; i++) {
            for (int j = commonNodesNumber; j < commonNodesNumber + 2; j++) {
                newMat[i][j] = sPGMArray[0].getGraph()[i][j];
            }
        }
        for (int i = commonNodesNumber; i < commonNodesNumber + 2; i++) {
            for (int j = 0; j < commonNodesNumber; j++) {
                newMat[i][j] = sPGMArray[0].getGraph()[i][j];
            }
        }

        //Il quadrato tra la posizione (0,0) e 
        //(commonTassedArcsNumber-1,commonTassedArcsNumber-1) sarò sicuramente
        //vuoto (per costruzione gli archi tassati partono dall'indice 0 e
        //terminano con l'indice commonTassedArcsNumber-1 e non hanno collegamenti
        //diretti tra loro).
        for (int i = 0; i < commonTassedArcsNumber; i++) {
            for (int j = 0; j < commonTassedArcsNumber; j++) {
                newMat[i][j] = sPGMArray[0].getGraph()[i][j];
            }
        }

        //Il quadrato tra la posizione (commonTassedArcsNumber,commonTassedArcsNumber)
        //e (commonTassedArcsNumber*2 -1, commonTassedArcsNumber*2 -1) sarà
        //sicuramente vuoto perchè (sempre per costruzione) gli archi dalla
        //posizione commonTassedArcsNumber a commonTassedArcsNumber*2 -1 sono
        //connessi solo alle sorgenti degli archi tassati.
        for (int i = commonTassedArcsNumber; i < commonNodesNumber; i++) {
            for (int j = commonTassedArcsNumber; j < commonNodesNumber; j++) {
                newMat[i][j] = sPGMArray[0].getGraph()[i][j];
            }
        }

        //Il quadrato tra la posizione (0,commonTassedArcsNumber) e 
        //(commonTassedArcsNumber,commonTassedArcsNumber*2-1) avrà i soli archi 
        //tassati sulla diagonale purchè non siano stati eliminati da tutti gli
        //SPGM.
        for (int i = 0; i < commonTassedArcsNumber; i++) {
            for (int j = commonTassedArcsNumber; j < commonNodesNumber; j++) {
                //Se i != j significa che non sono sulla diagonale.
                if (i != j - commonTassedArcsNumber) {
                    newMat[i][j] = sPGMArray[0].getGraph()[i][j];
                } else {
                    Arc arcBetweenIAndJ = arcBetweenXAndYExistInSPGMs(sPGMArray, i, j);
                    if (arcBetweenIAndJ != null) {
                        newMat[i][j] = arcBetweenIAndJ;
                    } else {
                        newMat[i][j] = sPGMArray[0].getGraph()[i][j];
                    }
                }
            }
        }

        //Il quadrato tra la posizione (commonTassedArcsNumber,0) e 
        //(commonNodesNumber,commonTassedArcsNumber) contiene tutti gli archi
        //che collegano tra loro archi tassati (sempre dalla destinazione di un 
        //arco tassato alla partenza di un altro arco tassato). Se in una data 
        //posizione almeno un SPGM ha l'arco devo copiarlo.
        for (int i = commonTassedArcsNumber; i < commonNodesNumber; i++) {
            for (int j = 0; j < commonTassedArcsNumber; j++) {
                Arc arcBetweenIAndJ = arcBetweenXAndYExistInSPGMs(sPGMArray, i, j);
                if (arcBetweenIAndJ != null) {
                    newMat[i][j] = arcBetweenIAndJ;
                } else {
                    newMat[i][j] = sPGMArray[0].getGraph()[i][j];
                }
            }
        }

        //######################################################################

        //Per le altre commodity invece copio nelle colonne successive la parte
        //riferita ai percorsi tra nodi di fine archi tassati e le destinazioni (1)
        //e le righe riferite ai percorsi tra sorgenti e nodi di inizio archi tassati (2).
        //Copio inoltre l'arco non tassato tra sorgente e destinazione della commodity (3).
        //Devo anche sistemare le etichette: per ogni commodity inserisco come 
        //etichetta del nodo sorgente e della destinazione l'indice del nodo 
        //sorgente e l'indice del nodo di destinazione (4).
        {
            int j = commonNodesNumber + 2;
            for (int i = 1; i < commodities.length; i++) {
                Arc[][] commoditySPGM = sPGMArray[i].getGraph();

                for (int k = 0; k < commonNodesNumber; k++) {
                    //(1)
                    newMat[k][j] = commoditySPGM[k][commonNodesNumber];
                    newMat[k][j + 1] = commoditySPGM[k][commonNodesNumber + 1];

                    //(2)
                    newMat[j][k] = commoditySPGM[commonNodesNumber][k];
                    newMat[j + 1][k] = commoditySPGM[commonNodesNumber + 1][k];
                }

                //(3)
                newMat[j][j + 1] = commoditySPGM[commonNodesNumber][commonNodesNumber + 1];

                //(4)
                newLabels[j] = commodities[i].getSource();
                newLabels[j + 1] = commodities[i].getDestination();

                j += 2;
            }
        }

        //Adesso elimino tutti i nodi inutili: ci possonon essere dei nodi infatti 
        //che non sono più connessi a nulla: è inutile tenerli ad appesantire il grafo.
        //Creo una struttura ArrayList<ArrayList<Arc>> che contiene tutta la 
        //matrice così posso facilmente eliminare righe e colonne. Devo anche
        //sistemare l'array di etichette ogni volta che elimino un nodo.
        ArrayList<ArrayList<Arc>> arrayListOfArraysListsOfArcs = new ArrayList<ArrayList<Arc>>();
        for (int i = 0; i < newMat.length; i++) {
            ArrayList<Arc> arcsArrayList = new ArrayList<Arc>();

            for (int j = 0; j < newMat.length; j++) {
                Arc arcBetweenIAndJ = newMat[i][j];
                arcsArrayList.add(arcBetweenIAndJ);
            }
            
            arrayListOfArraysListsOfArcs.add(arcsArrayList);
        }
        
        //Scorro la matrice: se tutta la riga i-esima e la colonna i-esima non
        //ha alcun arco allora elimino la riga i-esima e la colonna i-esima.
        //Devo eliminare anche la posizione i-esima dell'array delle label.
        for (int i = 0; i < arrayListOfArraysListsOfArcs.size(); i ++) {
            boolean delete = true;
            
            ArrayList<Arc> rowI = arrayListOfArraysListsOfArcs.get(i);
            
            for (int j = 0; j < arrayListOfArraysListsOfArcs.size(); j ++) {
                Arc arcBetweenIAndJ = rowI.get(j);
                Arc arcBetweenJAndI = arrayListOfArraysListsOfArcs.get(j).get(i);
                
                if (arcBetweenIAndJ.exist() || arcBetweenJAndI.exist()) {
                    delete = false;
                    break;
                }
            }
            
            //Se mi è stato dato il "permesso" elimino riga e colonna e sistemo
            //le label.
            if (delete) {
                for (int j = 0; j < arrayListOfArraysListsOfArcs.size(); j ++) {
                    arrayListOfArraysListsOfArcs.get(j).remove(i);
                }
                
                arrayListOfArraysListsOfArcs.remove(i);
                
                newLabels = deleteIndexFromArray(newLabels, i);
                
                //Se c'è stata un eliminazione devo tornare a scorrere la stessa
                //riga e la stessa colonna che in verità adesso sono la riga e
                //la colonna successiva.
                i --;
            }
        }
        
        //Devo ricostruire newMat
        int newNodesNumber = arrayListOfArraysListsOfArcs.size();
        newMat = new Arc[newNodesNumber][newNodesNumber];
        
        for (int i = 0; i < newNodesNumber; i ++) {
            for (int j = 0; j < newNodesNumber; j ++) {
                newMat[i][j] = arrayListOfArraysListsOfArcs.get(i).get(j);
            }
        }

        //Devo creare il nuovo vettore di Commodity: quello originale non va più
        //bene perchè negli SPGM sono stati cambiati gli indici.
        Commodity[] newCommodities = new Commodity[sPGMArray.length];
        int row = newMat.length - (sPGMArray.length * 2);
        for (int i = 0; i < sPGMArray.length; i++) {
            newCommodities[i] = new Commodity(row, row + 1, commodities[i].getCardinality());

            row += 2;
        }
        
        for (int i = 0; i < newCommodities.length; i ++) {
            Commodity commodity = newCommodities[i];
            
            //System.out.println("Commodity [" + i + "] : " + commodity.getSource() + " -> " + commodity.getDestination());
        }
            

        return new Graph(newMat, newCommodities, newLabels);
    }
    
    /**
     * Elimina dall'array di interi che ha ricevuto come parametro l'intero di
     * indice index.
     * @param intsArray array di interi su cui eliminare l'indice.
     * @param index indice da eliminare.
     * @return array di interi con l'indice eliminato
     */
    private static int[] deleteIndexFromArray(int [] intsArray, int index) {
        
        if (index < 0 || index >= intsArray.length) {
            System.err.println("Al metodo \"deleteIndexFromArray\" e' stato passato un indice (" + index +
                    ") non compreso nella lunghezza dell'array (0,"+ (intsArray.length - 1) +")");
        }
        
        int [] newIntsArray = new int[intsArray.length - 1];
        int i = 0;
        int j = 0;
        
        while (i < newIntsArray.length) {
            if (i == index) {
                j ++;
            }
            
            newIntsArray[i] = intsArray[j];
            
            i ++;
            j ++;
        }
        
        return newIntsArray;
    }

    /**
     * Verifica che su almeno un SPGM dell'array passato per parametro l'arco
     * di coordinate (x,y) esista e in caso positivo restituisce l'arco.
     * @param sPGMArray array di Graph SPGM.
     * @param x coordinata x dell'arco.
     * @param y coordinata y dell'arco.
     * @return null se non esiste l'arco in nessun SPGM, l'arco altrimenti.
     */
    private static Arc arcBetweenXAndYExistInSPGMs(Graph[] sPGMArray, int x, int y) {
        //Scorro tutti gli SPGM e verifico se in almeno un grafo esiste l'arco
        //in posizione (x,y)

        for (int i = 0; i < sPGMArray.length; i++) {
            if (sPGMArray[i].getGraph()[x][y].exist()) {
                return sPGMArray[i].getGraph()[x][y];
            }
        }

        return null;
    }

    private static Graph applyReductPrepositions(Graph graph, ArrayList<TassedArc> tassedArcsArrayP) {

        //Copio l'ArrayList perchè su quello "locale" potrebbero venir fatte 
        //cancellazioni degli archi tassati.
        ArrayList<TassedArc> tassedArcsArray = new ArrayList<TassedArc>();
        for (int i = 0; i < tassedArcsArrayP.size(); i++) {
            tassedArcsArray.add(tassedArcsArrayP.get(i));
        }

        int commoditySource = graph.getCommodities()[0].getSource();
        int commodityDestination = graph.getCommodities()[0].getDestination();

        //Applico i criteri di riduzione uno alla volta
        Graph preposition1Result = reductPropositionOne(graph, tassedArcsArray);
        Graph preposition2Result = reductPropositionTwo(preposition1Result, tassedArcsArray);
        Graph preposition3Result = reductPropositionThree(preposition2Result, tassedArcsArray);
        Graph preposition4Result = reductPropositionFour(preposition3Result, tassedArcsArray);
        Graph preposition5Result = reductPropositionFive(preposition4Result, tassedArcsArray);
        Graph preposition6Result = reductPropositionSix(preposition5Result, tassedArcsArray);
        Graph finalResult = reductPropositionSix(preposition6Result, tassedArcsArray);

        Arc[][] mat = finalResult.getGraph();

        //Elimino gli archi "orfani".

        //1) Elimino tutti gli archi che escono dalla sorgente della commodity 
        //che arrivavano ad un arco tassato che è stato eliminato.
        //Scorro la riga della sorgente per il numero di archi tassati e verifico
        //che l'arco tassato corrispondente esista. Se non esite posso eliminare
        //l'arco.
        int tassedArcsNumber = tassedArcsArrayP.size();
        for (int i = 0; i < tassedArcsNumber; i++) {
            //Per prima cosa verifico se l'arco tra la sorgente e il nodo da cui
            //dovrebbe partire l'arco tassato esiste o meno.
            if (mat[commoditySource][i].exist()) {
                //Verifico se non esiste l'arco tassato. In caso positivo elimino
                //l'arco tra sorgente ed ex origine arco tassato.
                if (!mat[i][i + tassedArcsNumber].exist()) {
                    mat[commoditySource][i] = new Arc();
                }
            }
        }

        //2) Elimino tutti gli archi che entrano nella destinazione partendo
        //dalla fine di un arco tassato che è stato eliminato.
        //Scorro la colonna della destinazione dal numero archi tassati per il
        //numero degli archi tassati e verifico se l'arco tassato corrispondente
        //esiste. Se non esiste posso eliminare l'arco.
        for (int i = 0; i < tassedArcsNumber; i++) {
            //Per prima cosa verifico se l'arco tra la la fine dell'arco tassato
            //e il nodo destinazione esiste.
            if (mat[i + tassedArcsNumber][commodityDestination].exist()) {
                //Verifico se esiste o meno l'arco tassato. Se non esiste posso 
                //eliminare l'arco.
                if (!mat[i][i + tassedArcsNumber].exist()) {
                    mat[i + tassedArcsNumber][commodityDestination] = new Arc();
                }
            }
        }

        //Elimino gli archi che partivano da un arco tassato che non esiste più 
        //ed arrivano ad un arco tassato che esiste ancora, elimino gli archi 
        //tassati che partono da un arco tassato che esiste ancora e arrivano ad
        //un arco tassato che non esiste più, elimino l'arco che parte da un 
        //arco tassato che non esiste più e che arriva ad un arco tassato che 
        //non esiste più.
        //Per costruzione dell'SPGM gli archi "compresi" tra archi tassati stanno
        //nella parte della matrice che cosrrisponde agli archi tra 
        //(0,tassedArcsNumber) a (tassedArcsNumber, tassedArcsNumber*2).
        //Scorro la matrice degli archi dal nodo (0,tassedArcsNumber) a 
        //(tassedArcsNumber, tassedArcsNumber*2), verifico se l'arco esiste e se
        //esiste devo accertarmi se c'è o meno un arco successivo tassato e se c'è
        //o meno un arco tassato precedente. Se manca uno dei due elimino l'arco.
        for (int i = tassedArcsNumber; i < tassedArcsNumber * 2; i++) {
            for (int j = 0; j < tassedArcsNumber; j++) {
                //Verifico se esiste l'arco.
                if (!mat[i][j].exist()) {
                    continue;
                }

                //Verifico se eliminarlo o no.
                if (!mat[j][i].exist() || !mat[i][i - tassedArcsNumber].exist()) {
                    mat[i][j] = new Arc();
                }
            }
        }

        return finalResult;
    }

    /**
     * Elimina dall'SPGM per la singola commodity gli archi secondo 
     * la Proposizione 1.
     * @param graph SPGM
     * @param tassedArcsArray Array degli archi tassati.
     * @return il grafo con gli archi eliminati qualora sia possibile.
     */
    private static Graph reductPropositionOne(Graph graph, ArrayList<TassedArc> tassedArcsArray) {

        //Ponendo j l'indice del nodo finale di un arco tassato e t l'indice
        //del nodo destinazione della commodity scorro tutti gli archi tassati 
        //e verifico se l(j,t) == u(j,t): se è così posso eliminare tutti gli 
        //archi uscenti dal nodo di indice j tranne ovviamente quello che arriva
        //alla destinazione.

        int t = graph.getCommodities()[0].getDestination();
        Arc[][] mat = graph.getGraph();

        for (TassedArc tassedArc : tassedArcsArray) {
            int j = tassedArc.getDestinationNode();
            Arc arcBetweenTassedArcEndNodeAndCommodityDestinationNode = mat[j][t];

            //Devo verificare se esiste
            if (!arcBetweenTassedArcEndNodeAndCommodityDestinationNode.exist()) {
                continue;
            }

            int arcCostBetweenTassedArcEndNodeAndCommodityDestinationNode = 0;

            try {
                arcCostBetweenTassedArcEndNodeAndCommodityDestinationNode =
                        arcBetweenTassedArcEndNodeAndCommodityDestinationNode.getCost();
            } catch (ArcNotDefinedException e) {
                System.err.println("Il metodo \"reductPropositionOne\" ha restituito un arco che non dovrebbe essere null");
            }

            //Calcolo il costo minimo tra l'indice del nodo finale dell'arco 
            //tassato e t, nodo destinazione della commodity
            int[] costsArray = Dijkstra.dijkstra(graph, j, true);

            //Se il costo minimo è uguale significa che posso eliminare tutti 
            //gli archi uscenti da j tranne l'arco tra j e t.
            if (arcCostBetweenTassedArcEndNodeAndCommodityDestinationNode == costsArray[t]) {
                //Scorro la riga j ed elimino tutti gli archi uscenti dal nodo
                //tranne quello in posizione t
                for (int i = 0; i < mat.length; i++) {
                    if (i != t) {
                        mat[j][i] = new Arc();
                    }

                }
            }
        }

        return graph;
    }

    /**
     * Elimina dall'SPGM per la singola commodity gli archi secondo 
     * la Proposizione 2.
     * @param graph SPGM
     * @param tassedArcsArray Array degli archi tassati.
     * @return il grafo con gli archi eliminati qualora sia possibile.
     */
    private static Graph reductPropositionTwo(Graph graph, ArrayList<TassedArc> tassedArcsArray) {

        //Ponendo s l'indice del nodo sorgente della commodity e i l'indice del
        //nodo di partenza di un arco tassato devo scorrere tutti gli archi
        //tassati e verificare se l(s,i) == u(s,i): se è così posso eliminare
        //tutti gli archi entranti nel nodo di indice i tranne quello che parte
        //dalla sorgente della commodity.

        int s = graph.getCommodities()[0].getSource();
        Arc[][] mat = graph.getGraph();

        for (TassedArc tassedArc : tassedArcsArray) {
            int i = tassedArc.getOriginNode();
            Arc arcBetweenOriginNodeAndTassedArcEndNode = mat[s][i];

            //Devo verificare se esiste
            if (!arcBetweenOriginNodeAndTassedArcEndNode.exist()) {
                continue;
            }

            int arcCostBetweenOriginNodeAndTassedArcEndNode = 0;

            try {
                arcCostBetweenOriginNodeAndTassedArcEndNode =
                        arcBetweenOriginNodeAndTassedArcEndNode.getCost();
            } catch (ArcNotDefinedException e) {
                System.err.println("Il metodo \"reductPropositionTwo\" ha restituito un arco che non dovrebbe essere null");
            }

            //Calcolo il costo minimo tra la sorgente della commodity e l'indice
            //del nodo iniziale dell'arco tassato.
            int[] costsArray = Dijkstra.dijkstra(graph, s, true);

            //Se il costo minimo è uguale posso eliminare tutti gli archi 
            //entranti in i meno quello tra s e i.
            if (arcCostBetweenOriginNodeAndTassedArcEndNode == costsArray[i]) {
                //Scorro la colonna i-esima ed elimino tutti gli archi tranne
                //quello in posizione s.
                for (int j = 0; j < mat.length; j++) {
                    if (j != s) {
                        mat[j][i] = new Arc();
                    }
                }
            }
        }

        return graph;
    }

    /**
     * Elimina dall'SPGM per la singola commodity gli archi secondo 
     * la Proposizione 3.
     * @param graph SPGM
     * @param tassedArcsArray Array degli archi tassati.
     * @return il grafo con gli archi eliminati qualora sia possibile.
     */
    private static Graph reductPropositionThree(Graph graph, ArrayList<TassedArc> tassedArcsArray) {

        //Scorro tutte le coppi di archi tassati. Pongo (i1,j1) e (i2,j2) gli 
        //indici dei nodi di partenza e dei nodi di destinazione degli archi 
        //tassati rispettivamente per il primo e per il secondo arco tassato.
        //Pongo inoltre t come indice del nodo sorgente della commodity.
        //Se u(j1,t) <= u(j1,i2) + l(i2,t) possiamo eliminare l'arco (j1,i2).
        //NOTA: calcolo l(i2,t) invece di l(j2,t) perchè per noi l'arco tassato
        //non ha costo 0!

        int t = graph.getCommodities()[0].getDestination();
        Arc[][] mat = graph.getGraph();

        for (int k = 0; k < tassedArcsArray.size(); k++) {
            for (int w = 0; w < tassedArcsArray.size(); w++) {
                //Salto se stesso
                if (k == w) {
                    continue;
                }

                TassedArc firstTassedArc = tassedArcsArray.get(k);
                TassedArc secondTassedArc = tassedArcsArray.get(w);

                int i1 = firstTassedArc.getOriginNode();
                int j1 = firstTassedArc.getDestinationNode();

                int i2 = secondTassedArc.getOriginNode();
                int j2 = secondTassedArc.getDestinationNode();

                //Intanto verifico se l'arco (j1,i2) esiste.
                Arc arcBetweenJ1AndI2 = mat[j1][i2];

                if (!arcBetweenJ1AndI2.exist()) {
                    continue;
                }

                //Verifico se esiste l'arco (j1,t)
                Arc arcBetweenJ1AndT = mat[j1][t];

                if (!arcBetweenJ1AndT.exist()) {
                    continue;
                }

                //Calcolo l(i2,t)
                int costsArray[] = Dijkstra.dijkstra(graph, i2, true);

                //Se il costo tra i2 e t è infinito posso eliminare l'arco, 
                //stessa cosa se u(j1,t) <= u(j1,i2) + l(i2,t)
                try {
                    if (costsArray[t] == Integer.MAX_VALUE
                            || (arcBetweenJ1AndT.getCost() <= arcBetweenJ1AndI2.getCost() + costsArray[t])) {
                        mat[j1][i2] = new Arc();
                    }
                } catch (ArcNotDefinedException e) {
                    System.err.println("Il metodo \"reductPropositionThree\" ha restituito un arco che non dovrebbe essere null");
                }
            }
        }

        return graph;
    }

    /**
     * Elimina dall'SPGM per la singola commodity gli archi secondo 
     * la Proposizione 4.
     * @param graph SPGM
     * @param tassedArcsArray Array degli archi tassati.
     * @return il grafo con gli archi eliminati qualora sia possibile.
     */
    private static Graph reductPropositionFour(Graph graph, ArrayList<TassedArc> tassedArcsArray) {

        //Scorro tutte le coppie di archi tassati. Pongo (i1,j1) e (i2,j2) gli 
        //indici dei nodi di partenza e dei nodi di destinazione degli archi 
        //tassati rispettivamente per il primo e per il secondo arco tassato.
        //Pongo inoltre s l'indice del nodo sorgente della commodity.
        //Se u(s,i1) <= u(j2,i1) + l(s,j2) posso eliminare l'arco di indici (j2,i1)
        //NOTA: uso l(s,j2) al posto di l(s,i2) perchè nel nostro caso gli archi
        //tassati non partono da costo 0!

        int s = graph.getCommodities()[0].getSource();
        Arc[][] mat = graph.getGraph();

        for (int k = 0; k < tassedArcsArray.size(); k++) {
            for (int w = 0; w < tassedArcsArray.size(); w++) {
                //Salto se stesso
                if (k == w) {
                    continue;
                }

                TassedArc firstTassedArc = tassedArcsArray.get(k);
                TassedArc secondTassedArc = tassedArcsArray.get(w);

                int i1 = firstTassedArc.getOriginNode();
                int j1 = firstTassedArc.getDestinationNode();

                int i2 = secondTassedArc.getOriginNode();
                int j2 = secondTassedArc.getDestinationNode();

                //Verifico se l'arco (j2,i1) esiste.
                Arc arcBetWeenJ2AndI1 = mat[j2][i1];

                if (!arcBetWeenJ2AndI1.exist()) {
                    continue;
                }

                //verifico inoltre se l'arco (s,i1) esiste
                Arc arcBetweenSAndI1 = mat[s][i1];

                if (!arcBetweenSAndI1.exist()) {
                    continue;
                }

                //Calcolo l(s,j2)
                int[] costs = Dijkstra.dijkstra(graph, s, true);

                //Il costo tra s e j2 non dovebbe essere mai infinito. 
                //Elimino l'arco se u(s,i1) <= u(j2,i1) + l(s,j2)
                try {
                    if (costs[j2] == Integer.MAX_VALUE
                            || (arcBetweenSAndI1.getCost() <= arcBetWeenJ2AndI1.getCost() + costs[j2])) {
                        mat[j2][i1] = new Arc();
                    }
                } catch (ArcNotDefinedException e) {
                    System.err.println("Il metodo \"reductPropositionFour\" ha restituito un arco che non dovrebbe essere null");
                }
            }
        }

        return graph;
    }

    /**
     * Elimina dall'SPGM per la singola commodity gli archi secondo 
     * la Proposizione 5.
     * @param graph SPGM
     * @param tassedArcsArray Array degli archi tassati.
     * @return il grafo con gli archi eliminati qualora sia possibile.
     */
    private static Graph reductPropositionFive(Graph graph, ArrayList<TassedArc> tassedArcsArray) {

        //Scorro tutti gli archi tassati. Pongo i1 l'indice del nodo iniziale 
        //dell'arco tassato e j1 l'indice finale dell'arco tassato. Pongo poi s
        //in nodo sorgente della commodity e t il nodo destinazione della 
        //commodity. A questo punto verifico se u(s,t) <= l(s,i1) + l(i1,t).
        //Se vale la relazione precedente posso eliminare l'arco tassato (j1,i1).
        //NOTA: uso l(i1,t) e non l(j1,t) perchè i nostri archi tassati hanno
        //un costo minimo diverso da 0!

        int s = graph.getCommodities()[0].getSource();
        int t = graph.getCommodities()[0].getDestination();
        Arc[][] mat = graph.getGraph();

        int i = 0;

        while (i < tassedArcsArray.size()) {

            TassedArc tassedArc = tassedArcsArray.get(i);

            int i1 = tassedArc.getOriginNode();
            int j1 = tassedArc.getDestinationNode();

            //Verifico se l'arco (s,t) esiste (dovrebbe sempre esistere)
            Arc arcBetweenSAndT = mat[s][t];

            if (!arcBetweenSAndT.exist()) {
                i++;
                continue;
            }

            //Calcolo l(s,i1) e l(i1,t)
            int[] costs = Dijkstra.dijkstra(graph, s, true);
            int lsi1 = costs[i1];
            costs = Dijkstra.dijkstra(graph, i1, true);
            int li1t = costs[t];

            //Se uno dei due costi è infinito elimino l'arco tassato.
            //Altrimenti verifico la condizione u(s,t) <= l(s,i1) + l(i1,t).
            try {
                if ((lsi1 == Integer.MAX_VALUE || li1t == Integer.MAX_VALUE)
                        || (arcBetweenSAndT.getCost() <= lsi1 + li1t)) {
                    mat[i1][j1] = new Arc();
                    tassedArcsArray.remove(i);
                    i--;
                }
            } catch (ArcNotDefinedException e) {
                System.err.println("Il metodo \"reductPropositionFive\" ha restituito un arco che non dovrebbe essere null");
            }

            i++;
        }

        return graph;
    }

    /**
     * Elimina dall'SPGM per la singola commodity gli archi secondo 
     * la Proposizione 6.
     * @param graph SPGM
     * @param tassedArcsArray Array degli archi tassati.
     * @return il grafo con gli archi eliminati qualora sia possibile.
     */
    private static Graph reductPropositionSix(Graph graph, ArrayList<TassedArc> tassedArcsArray) {

        //Scorro tutti gli archi tassati a coppie. Pongo (i1,j1) e (i2,j2) gli 
        //indici dei nodi di partenza e dei nodi di destinazione degli archi 
        //tassati rispettivamente per il primo e per il secondo arco tassato.
        //Pongo inoltre s l'indice del nodo sorgente della commodity e t il nodo
        //finale. A questo punto verifico se u(s,t) <= l(s,j1) + u(j1,i2) + l(i2,t).
        //Se vale la relazione allora posso eliminare l'arco (j1,i2).
        //NOTA: uso l(s,j1) al posto di l(s,i1) e l(i2,t) al posto di l(j2,t)
        //perchè i miei archi tassati hanno un costo base!

        int s = graph.getCommodities()[0].getSource();
        int t = graph.getCommodities()[0].getDestination();
        Arc[][] mat = graph.getGraph();

        for (int k = 0; k < tassedArcsArray.size(); k++) {
            for (int w = 0; w < tassedArcsArray.size(); w++) {
                //Salto se stesso
                if (k == w) {
                    continue;
                }

                TassedArc firstTassedArc = tassedArcsArray.get(k);
                TassedArc secondTassedArc = tassedArcsArray.get(w);

                int i1 = firstTassedArc.getOriginNode();
                int j1 = firstTassedArc.getDestinationNode();

                int i2 = secondTassedArc.getOriginNode();
                int j2 = secondTassedArc.getDestinationNode();

                //Verifico se esiste l'arco (s,t) anche se dovrebbe sempre esistere.
                Arc arcBetweenSAndT = mat[s][t];

                if (!arcBetweenSAndT.exist()) {
                    continue;
                }

                //Verifico se esiste l'arco (j1,i2)
                Arc arcBetweenJ1AndI2 = mat[j1][i2];

                if (!arcBetweenJ1AndI2.exist()) {
                    continue;
                }

                //Calcolo l(s,j1) e l(i2,t)
                int[] costs = Dijkstra.dijkstra(graph, s, true);
                int costBetweenSAndJ1 = costs[j1];
                costs = Dijkstra.dijkstra(graph, i2, true);
                int costBetweenI2AndT = costs[t];

                //Se costBetweenSAndJ1 o costBetweenI2AndT è infinito posso 
                //eliminare l'arco. Stessa cosa se vale la relazione
                //u(s,t) <= l(s,j1) + u(j1,i2) + l(i2,t)
                try {
                    if ((costBetweenSAndJ1 == Integer.MAX_VALUE || costBetweenI2AndT == Integer.MAX_VALUE)
                            || (arcBetweenSAndT.getCost() <= costBetweenSAndJ1 + arcBetweenJ1AndI2.getCost() + costBetweenI2AndT)) {
                        mat[j1][i2] = new Arc();
                    }
                } catch (ArcNotDefinedException e) {
                    System.err.println("Il metodo \"reductPropositionSix\" ha restituito un arco che non dovrebbe essere null");
                }
            }
        }

        return graph;
    }

    /**
     * Crea una nuova matrice copiando i riferimenti.
     * @param mat matrice di archi.
     * @return nuova matrice con gli archi riferiti alla vecchia matrice.
     */
    private static Arc[][] copyMat(Arc[][] mat) {
        Arc[][] newMat = new Arc[mat.length][mat.length];

        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat.length; j++) {
                newMat[i][j] = mat[i][j];
            }
        }

        return newMat;
    }

    /**
     * Copia l'array delle etichette.
     * @param labels Array di etichette da copiare.
     * @return array duplicato.
     */
    private static int[] copyLabels(int[] labels) {
        int[] newLabels = new int[labels.length];

        for (int i = 0; i < labels.length; i++) {
            newLabels[i] = labels[i];
        }

        return newLabels;
    }
}
