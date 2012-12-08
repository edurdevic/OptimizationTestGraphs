/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tariffnetwork;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import tariffnetwork.datastructures.Arc;
import tariffnetwork.datastructures.Commodity;
import tariffnetwork.datastructures.Graph;
import tariffnetwork.datastructures.PathInfo;
import tariffnetwork.exceptions.ArcNotDefinedException;
import tariffnetwork.exceptions.ArcNotTariffedException;

/**
 * Riduce il numero di path, con la preposizione numero 7 dell'articolo di
 * Bouhtou modificato per adattarsi alla condizion meno restrittiva della tariffazione
 * a chilometro (ovvero lunghezza).
 * @author Erni
 */
public class PathReducer {

    public static  ArrayList<PathInfo> getReducedPathList(Graph g){
        Arc[][] graph = g.getGraph();
        //Commodity[] commodities = g.getCommodities();

        //Qui ci saranno la lista di tutti i Path che portano dalle Sorg alle Dest
        ArrayList<PathInfo> pathList = new ArrayList<PathInfo>();

        int j = 0;
        int riga = 0;
        boolean finito = false;
        boolean innerLoop = true;

        int maxRowIndex = graph.length - 1;
        int numeroPathPerSorgente = 0;
        int lastPassedIndexOnNode[] = new int[graph.length];

        boolean sources[] = getSources(g);
        boolean searchedDestinations[] = null;

        ArrayList<Arc> tariffedArcList = getTariffArcList(g);

        try {
            //Per ogni Commodity devo trovare tutte le strade che portano alle destinazioni
            //utili (corrispondenti a commodity)
            for (int sorgente = 0; sorgente < graph.length; sorgente++) {
                //Solo se è una sorgente conviene cercare i percorsi
                if (sources[sorgente]) {
                    //System.out.println("Percorsi trovati:  " + numeroPathPerSorgente);
                    //System.out.println("Source: " + sorgente);
                    //System.out.println();

                    //Azzero l'indice degli archi passati quando cambio sorgente
                    for (int i = 0; i < lastPassedIndexOnNode.length; i++) {
                        lastPassedIndexOnNode[i] = -1;
                    }

                    //Tengo conto di tutte le destinazioni che devo raggiungere:
                    searchedDestinations = getDestinationsForSource(sorgente, g);

                    j = 0;
                    riga = sorgente;   //Parto dalla riga k (è una sorgente)
                    finito = false;

                    while (!finito) { //Finchè non arrivo alla fine della riga della sorgente

                        // <editor-fold defaultstate="collapsed" desc="Condizioni di percorrenza del grafo">
                        if (graph[riga][j].exist()) {
                            //L'arco esiste, devo percorrerlo (a meno che non lo abbia già percorso per arrivare qui)
                            if (lastPassedIndexOnNode[j] < 0) {
                                //Se non lo ho percorso in questo path, posso percorrerlo
                                //System.out.println(riga + " -->" + j + getArrayRappresentation(lastPassedIndexOnNode));
                                if (searchedDestinations[j]) {
                                    //Se sto per saltare a un nodo destinazione, non serve andarci.
                                    //Salvo il path, poi faccio come se l'arco non esistesse

                                    numeroPathPerSorgente++;
                                    int path[] = lastPassedIndexOnNode.clone();
                                    path[riga] = j;
                                    addPathToPathList(pathList, new PathInfo(path, sorgente, j), tariffedArcList, g);
                                    //System.out.println("Path found: " + getArrayRappresentation(lastPassedIndexOnNode));

                                    // <editor-fold defaultstate="collapsed" desc="Ciclo per arco non percorribile-Risalita">
                                    do {
                                        //L'arco non esiste, devo continuare sulla stessa riga
                                        if (j < maxRowIndex) {
                                            j++;    //se la riga non è finita, continuo sulla stessa riga
                                            innerLoop = false;
                                        } else {
                                            //L'arco non esiste, se la riga è finita:
                                            if (sorgente == riga) {
                                                //Se la riga della sorgente è finita, basta così per questa sorgente
                                                finito = true;
                                                innerLoop = false;
                                            } else {
                                                //L'arco non esiste, la riga è finita, la riga non è sorgente
                                                //Se la riga è finita devo tornare un nodo sopra, dove ero rimasto l'ultima volta
                                                lastPassedIndexOnNode[riga] = -1;    //Ci potrò ripassare, quindi lo metto negativo
                                                //System.out.println(trovaRigaPrecedente(lastPassedIndexOnNode, riga) + "<-- " + riga + getArrayRappresentation(lastPassedIndexOnNode));

                                                riga = trovaRigaPrecedente(lastPassedIndexOnNode, riga);
                                                j = lastPassedIndexOnNode[riga];    //Devo continuare da dove ho lasciato, non dall'inizio
                                                innerLoop = true;
                                            }
                                        }
                                    } while (innerLoop);
                                    //</editor-fold>

                                } else {
                                    lastPassedIndexOnNode[riga] = j;    //Percorro l'arco, mi sposto sulla riga definita dalla colonna j
                                    riga = j;
                                    j = 0;
                                }

                            } else {
                                // <editor-fold defaultstate="collapsed" desc="Ciclo per arco non percorribile-Risalita">
                                do {
                                    //L'arco non esiste, devo continuare sulla stessa riga
                                    if (j < maxRowIndex) {
                                        j++;    //se la riga non è finita, continuo sulla stessa riga
                                        innerLoop = false;
                                    } else {
                                        //L'arco non esiste, se la riga è finita:
                                        if (sorgente == riga) {
                                            //Se la riga della sorgente è finita, basta così per questa sorgente
                                            finito = true;
                                            innerLoop = false;
                                        } else {
                                            //L'arco non esiste, la riga è finita, la riga non è sorgente
                                            //Se la riga è finita devo tornare un nodo sopra, dove ero rimasto l'ultima volta
                                            lastPassedIndexOnNode[riga] = -1;    //Ci potrò ripassare, quindi lo azzero
                                            //System.out.println(trovaRigaPrecedente(lastPassedIndexOnNode, riga) + "<-- " + riga + getArrayRappresentation(lastPassedIndexOnNode));

                                            riga = trovaRigaPrecedente(lastPassedIndexOnNode, riga);
                                            j = lastPassedIndexOnNode[riga];    //Devo continuare da dove ho lasciato, non dall'inizio
                                            innerLoop = true;
                                        }
                                    }
                                } while (innerLoop);
                                // </editor-fold>
                            }

                        } else {
                            // <editor-fold defaultstate="collapsed" desc="Ciclo per arco non percorribile-Risalita">
                            do {
                                //L'arco non esiste, devo continuare sulla stessa riga
                                if (j < maxRowIndex) {
                                    j++;    //se la riga non è finita, continuo sulla stessa riga
                                    innerLoop = false;
                                } else {
                                    //L'arco non esiste, se la riga è finita:
                                    if (sorgente == riga) {
                                        //Se la riga della sorgente è finita, basta così per questa sorgente
                                        finito = true;
                                        innerLoop = false;
                                    } else {
                                        //L'arco non esiste, la riga è finita, la riga non è sorgente
                                        //Se la riga è finita devo tornare un nodo sopra, dove ero rimasto l'ultima volta
                                        lastPassedIndexOnNode[riga] = -1;    //Ci potrò ripassare, quindi lo azzero
                                        //System.out.println(trovaRigaPrecedente(lastPassedIndexOnNode, riga) + "<-- " + riga + getArrayRappresentation(lastPassedIndexOnNode));

                                        riga = trovaRigaPrecedente(lastPassedIndexOnNode, riga);
                                        j = lastPassedIndexOnNode[riga];    //Devo continuare da dove ho lasciato, non dall'inizio
                                        innerLoop = true;
                                    }
                                }
                            } while (innerLoop);
                            // </editor-fold>
                        }
                        //</editor-fold>

                    }
                }
            }
            //System.out.println("Percorsi trovati:  " + numeroPathPerSorgente);
        } catch (Exception e) {
            System.out.print("Errore nella creazione dei path del grafo, vedi la classe PathReducer.");
        }

        return pathList;
    }

    private static void addPathToPathList(ArrayList<PathInfo> pathList, PathInfo path, ArrayList<Arc> tariffedArcList, Graph g){
        //se per 2 percorsi (i e j) con stessa sorgente e destinazione vale che
        // Ci >= Cj  &&  Li >= Lj
        // allora il percorso i si può eliminare

        getPathCostAndTariffArcData(path, g, tariffedArcList);


        for (PathInfo p:pathList){
            if (p.getSource() == path.getSource() && p.getDestination() == path.getDestination()){
                if (p.getCost() <= path.getCost() && p.getTariffedLength() <= path.getTariffedLength()){
                    //non serve inserire il path nella lista perchè costerà semppre di più
                    return;
                }
                if (p.getCost() >= path.getCost() && p.getTariffedLength() >= path.getTariffedLength()){
                    //Qello che voglio inserire è più corto di p (che è stato inserito prima),
                    //quindi devo eliminare p e aggiungere path
                    pathList.remove(p);
                    pathList.add(path);
                    return;
                }
            }
        }

         pathList.add(path);
    }

    /**
     * Aggiorna l'oggetto PathInfo aggiungendoci il costo e l'array di boolean che rappresentano se un certo
     * arco tassato è presente o meno. L'indice dell'arco tassato preso in considerazione è quello
     * del tariffArcList passato come parametro
     * @param path Percorso per il quale calcolare il costo e archi tassati. L'oggetto viene modificato dal metodo
     * @param g Grafo dal quale estrarre il costo dagli archi
     * @param tariffArcList Lista degli archi tassati
     */
    private static void getPathCostAndTariffArcData(PathInfo path, Graph g, ArrayList<Arc> tariffArcList) {
        int cost = 0;
        int length = 0;
        boolean[] tariffArcs = new boolean[tariffArcList.size()];
        //ciclo per tutti i nodi
        for (int i = 0; i < path.getPath().length; i++) {
            try {
                if (path.getPath()[i] >= 0) {
                    //Solo se il link esiste, conta il costo
                    //Del grafo prendo l'arco in posizione [i][path.getPath()[i]]
                    cost += g.getGraph()[i][path.getPath()[i]].getCost();
                    //Se esiste devo anche vedere se è tariffato
                    int indexOfTariffArc = tariffArcList.indexOf(g.getGraph()[i][path.getPath()[i]]);
                    if (indexOfTariffArc >= 0) {
                        //L'arco selezionato è tassato (indice > 0) devo inserirlo nel boolean[] tariffArcs
                        //che verra stampato nel file .dat per AguiariStancich
                        tariffArcs[indexOfTariffArc] = true;

                        //Poi devo contare la lunghezza tariffata
                        length += g.getGraph()[i][path.getPath()[i]].getTariffedLength();
                    }
                }
            } catch (ArcNotDefinedException ex) {
                Logger.getLogger(DataFileWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ArcNotTariffedException ex) {
                Logger.getLogger(DataFileWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        path.setTariffArcs(tariffArcs);
        path.setCost(cost);
        path.setTariffedLength(length);
    }

     /**
     * Ritorna un array di boolean che per ogni nodo del grafo g definisce
     * se il nodo è una sorgente o meno
     * @param g Grafo
     * @return Array di boolean getsources()[i] è true se il nodo i è sorgente
     */
    private static boolean[] getSources(Graph g) {
        boolean sources[] = new boolean[g.getGraph().length];
        for (int i = 0; i < g.getGraph().length; i++) {
            sources[i] = false;
        }
        for (Commodity c : g.getCommodities()) {
            sources[c.getSource()] = true;
        }
        return sources;
    }


     /**
     * Dato l'indice di una sorgente, ritorna un array che definisce per ogni
     * nodo se è una destinazione per qualsiasi commodity che abbia come sorgente
     * l'indice passato
     * @param source Indice della sorgente per cui trovare le destinazioni
     * @param g Grafo
     * @return getDestinationsForSource(i, g)[j] vale true se il nodo j è destinazione
     * di una commodity che ha i come sorgente
     */
    private static boolean[] getDestinationsForSource(int source, Graph g) {
        boolean destinations[] = new boolean[g.getGraph().length];
        for (Commodity c : g.getCommodities()) {
            if (c.getSource() == source) {
                destinations[c.getDestination()] = true;
            }
        }
        return destinations;
    }

    /**
     * Data la riga attuale (indice del nodo per il quale stamo cercando gli archi)
     * e la matrice delle transizioni lastPassedIndexOnNode ritorna l'indice del
     * nodo dal quale siamo arrivati al nodo attuale percorrendo un arco
     * @param lastPassedIndexOnNode matrice delle transizioni nella ricerca dei percorsi
     * @param riga Indice del nodo attuale
     * @return indice del nodo dal quale siamo arrivati al nodo attuale in base alla
     * matrice di transizione
     */
    private static int trovaRigaPrecedente(int[] lastPassedIndexOnNode, int riga) {
        for (int i = 0; i <
                lastPassedIndexOnNode.length; i++) {
            if (lastPassedIndexOnNode[i] == riga) {
                return i;
            }

        }
        return -1;
    }

    /**
     * Genera una lista degli archi tariffati copiando i riferimenti degli archi di g
     * nell'ArrayList ritornato
     * @param g Grafo da cui prendere gli archi tassati
     * @return ArrayList<Arc> di archi tassati
     */
    private static ArrayList<Arc> getTariffArcList(Graph g) {
        ArrayList<Arc> tariffArcList = new ArrayList<Arc>();
        int nodeNumber = g.getGraph().length;
        try {

            //Scorro tutto il grafo e se l'arco è tariffato, copio il riferimento nell'
            //ArrayList
            for (int i = 0; i < nodeNumber; i++) {
                for (int j = 0; j < nodeNumber; j++) {
                    if (g.getGraph()[i][j].exist() && g.getGraph()[i][j].getTariffed()) {
                        //Se il nodo esiste ed è con tariffa, aggiungilo alla lista degli archi tariffati
                        tariffArcList.add(g.getGraph()[i][j]);
                    }
                }
            }
        } catch (ArcNotDefinedException ex) {
            Logger.getLogger(DataFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tariffArcList;
    }
}
