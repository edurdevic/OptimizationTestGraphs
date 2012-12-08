/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tariffnetwork;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import tariffnetwork.datastructures.*;
import tariffnetwork.exceptions.ArcNotDefinedException;
import tariffnetwork.exceptions.ArcNotTariffedException;

/**
 *
 * @author Erni
 */
public class DataFileWriter {

    /**
     * Scrive il file .dat necessario per risolvere il problema con il risolutore di AguiariStancich
     * @param filename Nome del file output
     * @param g Grafo da scrivere
     * @throws FileNotFoundException
     */
    public void writePathDataFile(String filename, Graph g) throws FileNotFoundException {
        FileOutputStream file = new FileOutputStream(filename);
        PrintStream out = new PrintStream(file);


        int tariffArcNumber = getTariffArcsNumber(g);
        out.println(getLinearCountArray("ArchiConTariffa", tariffArcNumber));
        out.println(getLinearCountArray("Commodity", g.getCommodities().length));

        ArrayList<PathInfo> pathList = getPathList(g);
        System.out.println("Path number: " + pathList.size());



        /*
        //DEBUG---------------
        System.out.println();
        System.out.println("Paths: ");
        for(int i=0; i<pathList.size(); i++){
            System.out.println((i+1) + ": " + getArrayRappresentation(pathList.get(i).getPath()));
        }
        System.out.println();
        //DEBUG---------------
         */


        out.println(getLinearCountArray("Percorsi", pathList.size()));

        //Scrivo la Matrice ListaCardinalitaCommodity
        out.print("ListaCardinalitaCommodity : [");
        for (int i = 0; i < g.getCommodities().length; i++) {
            out.print(g.getCommodities()[i].getCardinality() + " ");
        }
        out.print("]");
        out.println();

        //Metto tutti gli archi tassati in un array list in modo da averli ordinati e tutti assieme
        ArrayList<Arc> tariffArcList = getTariffArcList(g);

        //Trovo i dati degli archi con tariffa e dei costi, salvandoli nella lista di oggetti PathInfo
        for (int i = 0; i < pathList.size(); i++) {
            getPathCostAndTariffArcData(pathList.get(i), g, tariffArcList);
        }

        //Scrivo la Matrice ListaPercorsiCommodity
        out.print("ListaPercorsiCommodity : [");
        for (int i = 0; i < pathList.size(); i++) {
            for (int commodityIndex = 0; commodityIndex < g.getCommodities().length; commodityIndex++) {
                //Se sorgente e destinazione della commodity e del path corrispondono,
                //scrivi il valore dell'indice della commodity corrispondente
                if (g.getCommodities()[commodityIndex].getSource() == pathList.get(i).getSource() && g.getCommodities()[commodityIndex].getDestination() == pathList.get(i).getDestination()) {
                    out.print(commodityIndex + 1 + " ");
                }
            }
        }
        out.print("]");
        out.println();

        //Scrivo la Matrice ListaPercorsiCostiFissi
        out.print("ListaPercorsiCostiFissi : [");
        for (int i = 0; i < pathList.size(); i++) {
            out.print(pathList.get(i).getCost() + " ");
        }
        out.print("]");
        out.println();


        //Scrivo la Matrice Lunghezze = Lunghezze degli archi tassati
        out.print("Lunghezze : [");
        for (int i = 0; i < tariffArcList.size(); i++) {
            try {
                out.print(tariffArcList.get(i).getTariffedLength() + " ");
            } catch (Exception ex) {
                Logger.getLogger(DataFileWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        out.print("]");
        out.println();


        //Scrivo la Matrice PercorsiArchiTariffa
        out.print("MatricePercorsiArchiTariffa : [");
        for (int i = 0; i < pathList.size(); i++) {
            out.print(getArrayRappresentation(pathList.get(i).getTariffArcs()));
            out.println();
        }
        out.println("]");


    }

    public void writeReducedPathDataFile(String filename, Graph g) throws FileNotFoundException {
        FileOutputStream file = new FileOutputStream(filename);
        PrintStream out = new PrintStream(file);


        int tariffArcNumber = getTariffArcsNumber(g);
        out.println(getLinearCountArray("ArchiConTariffa", tariffArcNumber));
        out.println(getLinearCountArray("Commodity", g.getCommodities().length));

        ArrayList<PathInfo> pathList = PathReducer.getReducedPathList(g);
        System.out.println("Reduced path number: " + pathList.size());



        /*
        //DEBUG---------------
        System.out.println();
        System.out.println("Paths: ");
        for(int i=0; i<pathList.size(); i++){
            System.out.println((i+1) + ": " + getArrayRappresentation(pathList.get(i).getPath()));
        }
        System.out.println();
        //DEBUG---------------
         */


        out.println(getLinearCountArray("Percorsi", pathList.size()));

        //Scrivo la Matrice ListaCardinalitaCommodity
        out.print("ListaCardinalitaCommodity : [");
        for (int i = 0; i < g.getCommodities().length; i++) {
            out.print(g.getCommodities()[i].getCardinality() + " ");
        }
        out.print("]");
        out.println();

        //Metto tutti gli archi tassati in un array list in modo da averli ordinati e tutti assieme
        ArrayList<Arc> tariffArcList = getTariffArcList(g);

        //Trovo i dati degli archi con tariffa e dei costi, salvandoli nella lista di oggetti PathInfo
        for (int i = 0; i < pathList.size(); i++) {
            getPathCostAndTariffArcData(pathList.get(i), g, tariffArcList);
        }

        //Scrivo la Matrice ListaPercorsiCommodity
        out.print("ListaPercorsiCommodity : [");
        for (int i = 0; i < pathList.size(); i++) {
            for (int commodityIndex = 0; commodityIndex < g.getCommodities().length; commodityIndex++) {
                //Se sorgente e destinazione della commodity e del path corrispondono,
                //scrivi il valore dell'indice della commodity corrispondente
                if (g.getCommodities()[commodityIndex].getSource() == pathList.get(i).getSource() && g.getCommodities()[commodityIndex].getDestination() == pathList.get(i).getDestination()) {
                    out.print(commodityIndex + 1 + " ");
                }
            }
        }
        out.print("]");
        out.println();

        //Scrivo la Matrice ListaPercorsiCostiFissi
        out.print("ListaPercorsiCostiFissi : [");
        for (int i = 0; i < pathList.size(); i++) {
            out.print(pathList.get(i).getCost() + " ");
        }
        out.print("]");
        out.println();


        //Scrivo la Matrice Lunghezze = Lunghezze degli archi tassati
        out.print("Lunghezze : [");
        for (int i = 0; i < tariffArcList.size(); i++) {
            try {
                out.print(tariffArcList.get(i).getTariffedLength() + " ");
            } catch (Exception ex) {
                Logger.getLogger(DataFileWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        out.print("]");
        out.println();


        //Scrivo la Matrice PercorsiArchiTariffa
        out.print("MatricePercorsiArchiTariffa : [");
        for (int i = 0; i < pathList.size(); i++) {
            out.print(getArrayRappresentation(pathList.get(i).getTariffArcs()));
            out.println();
        }
        out.println("]");


    }

    public void writeArcDataFile(String filename, Graph g) throws FileNotFoundException {
        FileOutputStream file = new FileOutputStream(filename);
        PrintStream out = new PrintStream(file);
        //PrintStream out = System.out;


        //Arrat lineari per i Set:
        out.println(getLinearCountArray("NODI", g.getGraph().length));
        out.println(getLinearCountArray("COMMODITY", g.getCommodities().length));


        //Il primo array dovrebbe rappresentare tutti gli archi con il valore del loro
        //costo e -1 dove l'arco non è definito
        out.print("archic : ");
        out.print(getArrayRappresentation(g.getGraph()));
        out.println();


        //Suppongo che lunghezzaarchi debba contenere le lunghezze degli archi tassati dove 
        //un valore è presente, mentre zero se l'arco non è tassato o è inesistente.
        //Manca la documentazione per questo file .dat
        out.print("lunghezzarchi : ");
        out.print(getTariffArcLength(g.getGraph()));
        out.println();


        //Array[Commodity], rappresenta le cardinalità delle commodity
        out.print("occorrenza : ");
        out.print(getCommodityCardinalityRappresentation(g.getCommodities()));
        out.println();


        //percorsoc: Array[Commodity][Nodi]
        //Ha una riga per ogni commoditi, 
        //Sulla colonna con indice corrispondente alla sorgente contiene un -1
        //Sulla colonna con indice corrispondente alla destinazione contiene un 1
        //Il resto è tutto zero
        out.print("percorsoc : ");
        out.print(getCommodityRappresentationForSegattoPlangarica(g));
        out.println();


    }

    /**
     * Genera una lista degli archi tariffati copiando i riferimenti degli archi di g
     * nell'ArrayList ritornato
     * @param g Grafo da cui prendere gli archi tassati
     * @return ArrayList<Arc> di archi tassati
     */
    private ArrayList<Arc> getTariffArcList(Graph g) {
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

    /**
     * Aggiorna l'oggetto PathInfo aggiungendoci il costo e l'array di boolean che rappresentano se un certo
     * arco tassato è presente o meno. L'indice dell'arco tassato preso in considerazione è quello
     * del tariffArcList passato come parametro
     * @param path Percorso per il quale calcolare il costo e archi tassati. L'oggetto viene modificato dal metodo
     * @param g Grafo dal quale estrarre il costo dagli archi
     * @param tariffArcList Lista degli archi tassati
     */
    private void getPathCostAndTariffArcData(PathInfo path, Graph g, ArrayList<Arc> tariffArcList) {
        int cost = 0;
        
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

                    }
                }
            } catch (ArcNotDefinedException ex) {
                Logger.getLogger(DataFileWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        path.setTariffArcs(tariffArcs);
        path.setCost(cost);
        

    }

    /**
     * Ritorna un ArrayList di percorsi PathInfo percorrendo tutti i path possibili
     * nel grafo g
     * @param g Grafo da cui estrarre i percorsi
     * @return Lista di percorsi PathInfo
     */
    private ArrayList<PathInfo> getPathList(Graph g) {

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
                                    pathList.add(new PathInfo(path, sorgente, j));
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
            System.out.print("ooooooooooooopppSSSSSSSSSSSSSSSSS");
        }

        return pathList;
    }

    /**
     * Ritorna un array di boolean che per ogni nodo del grafo g definisce
     * se il nodo è una sorgente o meno
     * @param g Grafo
     * @return Array di boolean getsources()[i] è true se il nodo i è sorgente
     */
    private boolean[] getSources(Graph g) {
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
    private boolean[] getDestinationsForSource(int source, Graph g) {
        boolean destinations[] = new boolean[g.getGraph().length];
        for (Commodity c : g.getCommodities()) {
            if (c.getSource() == source) {
                destinations[c.getDestination()] = true;
            }
        }
        return destinations;
    }

    /**
     * Ritorna una rappresentazione testuale di un array in forma [1 2 3 ]
     * @param a Array da rappresentare
     * @return Stringa di rappresentazione
     */
    private String getArrayRappresentation(int[] a) {
        String s = "[";
        for (int i : a) {
            s += i + " ";
        }
        s += "]";
        return s;
    }

    /**
     * Ritorna una stringa nel formato usato di Xpress per le matrici
     * Gli archi esistenti sono rappresentati dal valore del costo,
     * mentre quelli inesistenti vengono rappresentati con -1.
     * @param a Grafo sotto forma di array (Quadrato) di archi
     * @return Stringa che rappresenta l'array
     */
    private String getArrayRappresentation(Arc[][] a) {
        StringBuffer s = new StringBuffer("[");
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length; j++) {
                if (a[i][j].exist()) {
                    try {
                        s.append(a[i][j].getCost() + " ");
                    } catch (ArcNotDefinedException ex) {
                        Logger.getLogger(DataFileWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    s.append("-1 ");
                }
            }
            s.append("\n");
        }
        s.append("]");
        return s.toString();
    }

    private String getCommodityCardinalityRappresentation(Commodity[] commodity) {
        StringBuffer s = new StringBuffer("[");
        for (int i = 0; i < commodity.length; i++) {

            s.append(commodity[i].getCardinality() + " ");

        }
        s.append("]");
        return s.toString();
    }
    
    private String getCommodityRappresentationForSegattoPlangarica(Graph g){
        StringBuffer s = new StringBuffer("[");
        for (int i = 0; i < g.getCommodities().length; i++) {
            for (int j = 0; j < g.getGraph().length; j++) {

                if(g.getCommodities()[i].getSource() == j){
                    s.append("-1 ");
                }
                else if(g.getCommodities()[i].getDestination() == j){
                    s.append("1 ");
                }
                else {
                    s.append("0 ");
                }
                
            }
            s.append("\n");
        }
        s.append("]");
        return s.toString();
    }

    /**
     * Ritorna una stringa nel formato usato di Xpress per le matrici
     * Gli archi esistenti e tassati sono rappresentati dal valore del costo,
     * mentre quelli inesistenti o non tassati vengono rappresentati con 0.
     * @param a Grafo sotto forma di array (Quadrato) di archi
     * @return Stringa che rappresenta l'array
     */
    private String getTariffArcLength(Arc[][] a) {

        StringBuffer s = new StringBuffer("[");
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length; j++) {
                try {
                    //Scrivo il valore solo se è tassato
                    if (a[i][j].exist() && a[i][j].getTariffed()) {
                        try {
                            //Giusto:
                            s.append(a[i][j].getTariffedLength() + " ");

                        } catch (ArcNotTariffedException ex) {
                            Logger.getLogger(DataFileWriter.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } 
                    else {
                        s.append("0 ");
                    }

                } catch (ArcNotDefinedException ex) {
                    Logger.getLogger(DataFileWriter.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            s.append("\n");
        }

        s.append("]");
        return s.toString();
    }

    /**
     * Ritorna una rappresentazione testuale di un array in forma 1 0 0
     * 1 = true, 0 = false
     * @param a Array da rappresentare
     * @return Stringa di rappresentazione
     */
    private String getArrayRappresentation(boolean[] a) {
        String s = "";
        for (boolean i : a) {
            if (i) {
                s += "1 ";
            } else {
                s += "0 ";
            }
        }

        return s;
    }

    /**
     * Ritorna una rappresentazione testuale di un array composto dai numeri da
     * 1 a length, in forma  Name = [1 2 3 ... length]
     * @param name Nome dell'array da rappresentare
     * @param length lunghezza dell'array
     */
    private String getLinearCountArray(String name, int length) {
        StringBuffer o = new StringBuffer();
        o.append(name + " : [");
        for (int i = 1; i <=
                length; i++) {
            o.append(i + " ");
        }

        o.append("]");
        return o.toString();
    }

    /**
     * Conta quanti archi tariffati ci sono nel grafo g
     * @param g Grafo
     * @return numero di archi tassati presenti in g
     */
    private int getTariffArcsNumber(Graph g) {
        int tariffArcNumber = 0;
        for (Arc[] row : g.getGraph()) {
            for (Arc a : row) {
                if (a.exist()) {
                    try {
                        if (a.getTariffed()) {
                            tariffArcNumber++;
                        }

                    } catch (Exception e) {
                    }
                }
            }
        }
        return tariffArcNumber;
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
    private int trovaRigaPrecedente(int[] lastPassedIndexOnNode, int riga) {
        for (int i = 0; i <
                lastPassedIndexOnNode.length; i++) {
            if (lastPassedIndexOnNode[i] == riga) {
                return i;
            }

        }
        return -1;
    }
}
