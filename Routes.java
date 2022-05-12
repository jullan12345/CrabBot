package CrabBot;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

public class Routes {

    private final DataStore ds;
    private List<Vertex> nodes;
    private List<Edge> edges;
    private int[] links;

    public Routes(DataStore ds) {
        this.ds = ds;
    }

   /* public int computeDist(int segStart, int segEnd) {
        // returera kostnad läser inte in commands eller byter färg
        int cost = 0;
        constructNetwork();
        Graph graph = new Graph(nodes, edges);
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);

        // Compute shortest path
        dijkstra.execute(nodes.get(segStart));
        LinkedList<Vertex> path = dijkstra.getPath(nodes.get(segEnd));

        loadNodesPath(path, segStart);
        cost = totalCost(path);

        return cost;
    } */

    public int computeRoute(int segStart, int segEnd) {
        int cost = 0;

        constructNetwork();
        Graph graph = new Graph(nodes, edges);
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);

        // Compute shortest path
        dijkstra.execute(nodes.get(segStart));
        LinkedList<Vertex> path = dijkstra.getPath(nodes.get(segEnd));

        loadNodesPath(path, segStart);

        if (ds.optDone) {
            loadKommandon();
            for (int i = 0; i < ds.noSegments; i++) {
                ds.linkColor[i] = 0;
            }
            for (int i = 1; i < path.size(); i++) {
                for (int k = 0; k < ds.noSegments; k++) {
                    if ((ds.segStart[k] == Integer.parseInt(path.get(i - 1).getId())
                            && ds.segEnd[k] == Integer.parseInt(path.get(i).getId()))
                            || (ds.segEnd[k] == Integer.parseInt(path.get(i - 1).getId())
                            && ds.segStart[k] == Integer.parseInt(path.get(i).getId()))) {
                        ds.linkColor[k] = 1;
                    }
                }
            }
        }
        cost = totalCost(path);
        return cost;
    }

    public void loadKommandon() {
        // skapa och fyll ds.commands listan med commands! 

        ds.commands = new ArrayList<>();
        ds.zones = new ArrayList<>();

        int oldX = 0;
        int oldY = 0;
        int newX = 0;
        int newY = 0;
        int i = 0;
        int count = 0;
//        int k = 0;

        ArrayList<Integer> check = new ArrayList<>(); // spara alla noderna där 

        while (count < ds.nodespath.length) {
            if (count == 0) { // första kommandot som ska skickas
                ds.commands.add(".");
                check.add(ds.nodespath[count]);
            } else {

                // koordinater: 
                // old = gamla - currentNode 
                // new = currentNode - nya 
                if (count + 1 == ds.nodespath.length) { // sista noden 
                    newX = 0;
                    newY = 0;
                } else {
                    newX = ds.nodeX[ds.nodespath[count]] - ds.nodeX[ds.nodespath[count + 1]];
                    newY = ds.nodeY[ds.nodespath[count]] - ds.nodeY[ds.nodespath[count + 1]];
                }
                oldX = ds.nodeX[ds.nodespath[count - 1]] - ds.nodeX[ds.nodespath[count]];
                oldY = ds.nodeY[ds.nodespath[count - 1]] - ds.nodeY[ds.nodespath[count]];

                if (oldX > 0 && oldY == 0) {
                    if (newX > 0 && newY == 0) {
                        ds.commands.add("f");
                        check.add(ds.nodespath[count]);

                    } else if (newX == 0 && newY > 0) {
                        ds.commands.add("h");
                        ds.commands.add("f");
                        check.add(ds.nodespath[count]);
                        check.add(ds.nodespath[count]);
                    } else if (newX == 0 && newY < 0) {
                        ds.commands.add("v");
                        ds.commands.add("f");
                        check.add(ds.nodespath[count]);
                        check.add(ds.nodespath[count]);
                    }
                } else if (oldX < 0 && oldY == 0) {
                    if (newX < 0 && newY == 0) {
                        ds.commands.add("f");
                        check.add(ds.nodespath[count]);
                    } else if (newX == 0 && newY > 0) {
                        ds.commands.add("v");
                        ds.commands.add("f");
                        check.add(ds.nodespath[count]);
                        check.add(ds.nodespath[count]);
                    } else if (newX == 0 && newY < 0) {
                        ds.commands.add("h");
                        ds.commands.add("f");
                        check.add(ds.nodespath[count]);
                        check.add(ds.nodespath[count]);
                    }
                } else if (oldX == 0 && oldY > 0) {
                    if (newX < 0 && newY == 0) {
                        ds.commands.add("h");
                        ds.commands.add("f");
                        check.add(ds.nodespath[count]);
                        check.add(ds.nodespath[count]);
                    } else if (newX > 0 && newY == 0) {
                        ds.commands.add("v");
                        ds.commands.add("f");
                        check.add(ds.nodespath[count]);
                        check.add(ds.nodespath[count]);
                    } else if (newX == 0 && newY > 0) {
                        ds.commands.add("f");
                        check.add(ds.nodespath[count]);
                    }
                } else if (oldX == 0 && oldY < 0) {
                    if (newX < 0 && newY == 0) {
                        ds.commands.add("v");
                        ds.commands.add("f");
                        check.add(ds.nodespath[count]);
                        check.add(ds.nodespath[count]);
                    } else if (newX > 0 && newY == 0) {
                        ds.commands.add("h");
                        ds.commands.add("f");
                        check.add(ds.nodespath[count]);
                        check.add(ds.nodespath[count]);
                    } else if (newX == 0 && newY < 0) {
                        ds.commands.add("f");
                        check.add(ds.nodespath[count]);
                    }
                } else {
                    ds.commands.add("Error");
                    System.out.println("No turn could be calculated " + ds.commands.get(i));
                }
            }
            count++;
        }
        check.add(ds.nodespath[count - 1]); // sista noden i
//        System.out.println("Check " +  check);

        for (int t = 0; t < check.size() - 1; t++) {
            ds.zones.add(ds.getZone(check.get(t + 1), check.get(t)));
        }

        // rensa bort första t samt byt håll på första kommanonda
        // fronten mot hylla när vi är klar med lastning eller avlastning - därav detta byte 
//        ds.commands.remove(0); // remova bara när vi hade att t las till innan 
        if (ds.commands.get(1).equals("h")) {
//            System.out.println("H bytt till V");
            ds.commands.set(1, "v");
        } else if (ds.commands.get(1).equals("v")) {
            ds.commands.set(1, "h");
        }

        ArrayList<String> dummy = new ArrayList<>();
        int j = 0;
        int stop = ds.commands.size();
        boolean nextnext = false;
        List<Integer> remove = new ArrayList<>(); // spara index på de noder ut check som ska tas bort 
        while (j < stop) {
            boolean forward = false;
            if (j + 4 > check.size()) {
                break;
            }
            if (ds.commands.get(j).equals("f")) {
                if (check.get(j) > 71 && check.get(j + 1) <= 71) { // från stor korridor och in 
                    forward = true;
                }
                // ska vi ut ur en koridor använder vi inte x y z - det är bara om vi ska stanna på tejp 
                if (check.get(j) <= 71 && check.get(j + 1) > 71) {
                    dummy.add("f");
                } else if (check.get(j) <= 71 && check.get(j + 2) > 71) {
                    dummy.add("f");
                    remove.add(j + 1);
                    ds.commands.set(j + 1, " ");
                    j++;
                } else if (check.get(j) <= 71 && check.get(j + 3) > 71) {
                    dummy.add("f");
                    remove.add(j + 1);
                    remove.add(j + 1);
                    ds.commands.set(j + 1, " ");
                    ds.commands.set(j + 2, " ");
                    j = j + 2;
                } else if (ds.getZone(check.get(j), check.get(j - 1)) == 2 || ds.getZone(check.get(j), check.get(j - 1)) == 4 || forward) {
//                    System.out.println("Right zone");
                    if (ds.commands.get(j).equals(ds.commands.get(j + 1))) {
//                        System.out.println("One and two are equal");
                        if (ds.commands.get(j).equals(ds.commands.get(j + 2))) {
//                            System.out.println("One, two and three are equal");
                            dummy.add("z");
                            remove.add(j + 1);
                            remove.add(j + 1);
//                            ds.GUInodes.add(j, check.get(j));
                            j = j + 2;
                        } else { // two equal 
                            remove.add(j + 1);
                            dummy.add("y");
//                            ds.GUInodes.add(j, check.get(j));
                            j++;
                        }
                    } else { // only one f in zone 2
                        dummy.add("x");
//                        ds.add(j, check.get(j));
                    }
                } else { // f but in wrong zone
                    dummy.add(ds.commands.get(j));
                }
            } else if (!ds.commands.get(j).equals(" ")) {
                dummy.add(ds.commands.get(j));
            }
            j++;
        }

        dummy.add(ds.commands.get(ds.commands.size() - 2));
        dummy.add(ds.commands.get(ds.commands.size() - 1));
//        System.out.println("Check efter xyz " + check);

        // åka igenom hela korridoren utan att stanna 
        // f z f blir 2 st f
        for (int l = 0; l < dummy.size(); l++) {
            if (dummy.get(l).equals("z")) {
                if (dummy.get(l - 1).equals("f") && dummy.get(l + 1).equals("f")) {
                    dummy.remove(l);
                }
            }
        }

        for (int y = remove.size() - 1; y >= 0; y--) {
            int r = remove.get(y);
            ds.zones.remove(r);
            check.remove(r);
        }

//        System.out.println("Check efter remove " + check);
        ds.GUInodes = new ArrayList<>(check);
        ds.GUInodes.remove(0); // första noden kommer vi inte visualisera 
        // pga AGVn stannar ute i korridoren när den lastat/lossat klart 

        if (ds.nodespath.length == 3) {
            if (ds.nodespath[0] == ds.nodespath[ds.nodespath.length - 1] + 9 || ds.nodespath[0] == ds.nodespath[ds.nodespath.length - 1] - 9) {
                // hyllorna mittemot varandra 
                dummy.clear();
//                System.out.println("kom " + dummy);
                dummy.add("t");
                dummy.add("t");
//                System.out.println("nytt " + dummy);
            }
        }

        int temp = 0;

        // kolla sista noden
        if (check.get(check.size() - 1).equals(ds.startshelf - 1)) {
            dummy.remove(dummy.size() - 1);
            dummy.add("a"); // avlastning

        } else { // plockning av kuber 
            for (int t = 0; t < ds.shelfNumber.length; t++) {
                if (check.get(check.size() - 1).equals(ds.shelfNumber[t] - 1)) { // hitta hyllan som matchar med sista noden på resan = hyllan där artikel ska hämtas 
                    String art = ds.shelfName[t]; // artikeln som ska hämtas på denna plats 
                    String dummy1 = ds.routes.get(ds.routeCounter).get(0); // hämta delruttens artiklar som ska hämtas ex ABC eller EEE

                    String[] dummy2 = new String[3];
                    int place = 0;
                    boolean checked = false;
                    for (int a = 0; a < dummy1.length(); a++) {
                        dummy2[a] = dummy1.split("")[a]; // dela upp varje bokstav till en slot 
                        if (dummy2[a].equals(art) && !checked) {
                            place = a;
                            checked = true;
                        }
                    }
                    int got = 0;
                    for (int x = 0; x < ds.shelfName.length; x++) {
                        if (art.equals(ds.loaded[x][0])) {
                            got = Integer.parseInt(ds.loaded[x][1]); // got = antalet loaded av tänkt artikel
                        } // om artikeln inte finns i listan ds.loaded betyder det att den inte har plockats
                    }
                    int checker = 0;
                    // räkna hur många det är av varje ska hämtas på denna rutt ??? 
                    switch (dummy2.length) { // kolla hur artiklar det är i rutten 
                        case 3: // om rutten är full, dvs 3 artiklar ska hämtas innan avlämning: 
                            switch (place) {
                                case 0: // första plats 
                                    if (dummy2[0].equals(dummy2[1])) { // två första är lika åtminståne
                                        if (dummy2[0].equals(dummy2[2])) { // tre lika - alla tre ska plockas
//                                            System.out.println("Dummy " + dummy.get(dummy.size()-1));
                                            dummy.remove(dummy.size() - 1);
                                            dummy.add("l");
                                            dummy.add("r");
                                            dummy.add("m");
                                            ds.subCounter = 3;
                                            int lastNode = ds.nodespath[ds.nodespath.length - 1];
                                            ds.GUInodes.add(lastNode);
                                            ds.GUInodes.add(lastNode);
                                            ds.GUInodes.add(lastNode);
                                        } else { // två lika - plocka två men vilka ???? beror på hur många som hämtas innan 
                                            ds.subCounter = ds.subCounter + 2;
                                            dummy = addKubKommandoTwo(dummy, got);
                                        }
                                    } else {// en ensam på första plats - plocka en men vilken??? beror på hur många som hämtas innan 
                                        ds.subCounter++;
                                        dummy = addKubKommandoOne(dummy, got);
                                    }
                                    break;
                                case 1: // andra plats 
                                    if (dummy2[1].equals(dummy2[2])) { // mitten och sista är lika - ta båda 
                                        ds.subCounter = ds.subCounter + 2;
                                        dummy = addKubKommandoTwo(dummy, got);
                                    } else { // mitten är ensam
                                        ds.subCounter++;
                                        dummy = addKubKommandoOne(dummy, got);
                                    }
                                    break;
                                case 2: // tredje plats - kan inte jämföra med någon bredvid - hämta själv 
                                    ds.subCounter++;
                                    dummy = addKubKommandoOne(dummy, got);
                                    break;
                            }
                            break;
                        case 2: // bara två stycken i rutten
                            switch (place) {
                                case 0:
                                    if (dummy2[0].equals(dummy2[1])) {
                                        ds.subCounter = ds.subCounter + 2;
                                        dummy = addKubKommandoTwo(dummy, got);
                                    }
                                    break;
                                case 1:
                                    ds.subCounter++;
                                    dummy = addKubKommandoOne(dummy, got);
                                    break;
                            }
                            break;
                        case 1: // endast en artikel i rutten
                            ds.subCounter++;
                            dummy = addKubKommandoOne(dummy, got);
                            break;
                    }
                }
            }
        }

        if (ds.subCounter == 4) {
            ds.subCounter = 0;
        }

        if (!dummy.get(0).equals("t")) {
            dummy.remove(0);
        }
        ds.commands = dummy;
//        System.out.println("GUInodes " + ds.GUInodes);

        // ta bort första commands (AGV ska ej vändas) h är bytt till v och tvärtom 
        // ladda över de nya kommandona 
    }

    public ArrayList<String> addKubKommandoOne(ArrayList<String> dummy, int arg) {
//        System.out.println("Dummy " + dummy.get(dummy.size() - 1));
        // kommando för att hämta en kub
        dummy.remove(dummy.size() - 1);
        int lastNode = ds.nodespath[ds.nodespath.length - 1];
        ds.GUInodes.add(lastNode);
        switch (arg) {
            case 0: // ingen tidigare hämtad = ta vänster 
                dummy.add("l");
                break;
            case 1: // en tidigare hämtad = vänster borta 
                dummy.add("r");
                break;
            case 2: // två hämtade - bara mitten kvar
                dummy.add("m");
                break;
        }
        return dummy;
    }

    public ArrayList<String> addKubKommandoTwo(ArrayList<String> dummy, int arg) {
        // kommando för att hämta två kuber
//        System.out.println("Dummy " + dummy.get(dummy.size() - 1));
        int lastNode = ds.nodespath[ds.nodespath.length - 1];
        ds.GUInodes.add(lastNode);
        ds.GUInodes.add(lastNode);
        dummy.remove(dummy.size() - 1);
        switch (arg) {
            case 0: // ingen tidigare hämtad = ta alla ! 
                dummy.add("l");
                dummy.add("r");
                break;
            case 1: // en tidigare hämtad = vänster borta 
                dummy.add("r");
                dummy.add("m");
                break;
        }
        return dummy;
    }

    public void constructNetwork() {
        nodes = new ArrayList<Vertex>();
        edges = new ArrayList<Edge>();

        // Set up nodes
        for (int i = 0; i < ds.noNodes; i++) {
            Vertex location = new Vertex("" + i, "Nod #" + i);
            nodes.add(location);
        }

        // Set up links
        for (int i = 0; i < ds.noSegments; i++) {

            int xdist;
            int ydist;
            int cost;

            xdist = ds.nodeX[ds.segStart[i]] - ds.nodeX[ds.segEnd[i]];
            ydist = ds.nodeY[ds.segStart[i]] - ds.nodeY[ds.segEnd[i]];

            if (xdist == 0) {
                if (ydist < 0) {
                    cost = -ydist;
                } else {
                    cost = ydist;
                }
            } else {
                if (xdist < 0) {
                    cost = -xdist;
                } else {
                    cost = xdist;
                }
            }

            //links[i] = cost; 
            // System.out.println("Link " + i + " Cost = " + links[i] + " Segstart " + ds.segStart[i] + " Segend " + ds.segEnd[i]);
            Edge arcForward = new Edge("c" + i, nodes.get(ds.segStart[i]), nodes.get(ds.segEnd[i]), cost);
            Edge arcBackward = new Edge("c" + i, nodes.get(ds.segEnd[i]), nodes.get(ds.segStart[i]), cost);

            edges.add(arcForward);
            edges.add(arcBackward);
            //System.out.println("segStart=" + ds.segStart[i] + "\nsegEnd= " + ds.segEnd[i]);
            //System.out.println("Linkcost för " + i + ": " + cost + "\n");

        }
    }

    public void loadNodesPath(LinkedList<Vertex> path, int segStart) {
        // ladda över noderna AGV ska köra på till listan nodespath
        if (path == null) { // segStart == segEnd
            ds.nodespath = new int[1];
            ds.nodespath[0] = segStart;
//            System.out.println(ds.nodespath[0]);
        } else {
            ds.nodespath = new int[path.size()];
            for (int i = 0; i < path.size(); i++) {
                ds.nodespath[i] = Integer.parseInt(path.get(i).getId());
            }
        }
    }

    public int totalCost(LinkedList<Vertex> path) {
        int cost = 0;
        int totalcost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            cost = linkCost(ds.nodespath[i], ds.nodespath[i + 1]);
            totalcost = totalcost + cost;
        }
        return totalcost;
    }

    // För att få exakta länkkostnader och inte behöva trassla med links arrayen
    // segStart = start nod, segEnd = slut nod, endast på en liten sträcka! 
    public int linkCost(int segStart, int segEnd) {
        int xdist;
        int ydist;
        int cost;

        xdist = ds.nodeX[segStart] - ds.nodeX[segEnd];
        ydist = ds.nodeY[segStart] - ds.nodeY[segEnd];

        if (xdist == 0) {
            if (ydist < 0) {
                cost = -ydist;
            } else {
                cost = ydist;
            }
        } else {
            if (xdist < 0) {
                cost = -xdist;
            } else {
                cost = xdist;
            }
        }
        return cost;
    }

    public String[][] numberOfArt(String[][] list) {
        // returns a matrix of article name and how many of that article in order 
        int value = 1;
        int diff = 0;
        int count = 0;
        diff = numberOfDiffArt(list);
        String[][] dummy = new String[diff][2];

        for (int i = 0; i < list.length; i++) {
            if (i == list.length - 1 && list[i][0].equals(list[i - 1][0])) { // sista är lika som den föregående
                dummy[count][1] = String.valueOf(value);
                dummy[count][0] = list[i][0];
                value = 1;
            } else if (i == list.length - 1 && !list[i][0].equals(list[i - 1][0])) { // sista är inte lika som föregående
                dummy[count][1] = String.valueOf(value);
                dummy[count][0] = list[i][0];
            } else if (i >= list.length) {
                break;
            } else if (list[i][0].equals(list[i + 1][0])) { // denna lika med nästa 
                value++;
            } else {
                dummy[count][1] = String.valueOf(value); // antal 
                dummy[count][0] = list[i][0];
                value = 1;
                count++;
            }
        }
        return dummy;
    }

    public int numberOfDiffArt(String[][] list) {
        int value = 1;
        for (int i = 0; i < list.length; i++) {
            if (i + 1 >= list.length) {
                break;
            } else if (!list[i][0].equals(list[i + 1][0])) {
                value++;
            }
        }
        //System.out.println("Number of different art: " + value);
        ds.numOfArt = value;
        return value;
    }

    public String[][] createInitiail() {  // (String[][] list) // list är en lista med artikel och kostnad att hämta o lämna

        String[][] paths = new String[ds.orderList.length][2];

        for (int i = 0; i < ds.orderList.length; i++) {
            paths[i][0] = ds.orderList[i];

//            int nod = op.nodnummer(ds.orderList[i]); // get shelfnumber for art
            int nod = 0;
            for (int p = 0; p < ds.shelfName.length; p++) {

                if (ds.shelfName[p].equals(ds.orderList[i].split("")[0])) {
                    nod = ds.shelfNumber[p] - 1;
                    break;
                }
            }
            paths[i][1] = String.valueOf(2 * computeRoute(nod, ds.startshelf - 1));
        }

        Arrays.sort(paths, new Comparator<String[]>() {

            @Override
            public int compare(final String[] first, final String[] second) {
                return Double.valueOf(first[1]).compareTo(
                        Double.valueOf(second[1])
                );
            }
        });

        String[][] list = Arrays.copyOf(paths, paths.length);
        for (int a = 0; a < list.length; a++) {
        }
//        numberOfArt(list);

        int numberOfRutts = 0; //  number of rutter i initial lösningen
        int klara = 0; // antal artiklar av en sort som är klara och inlagda i rutt
        int doneArt = 0; // antal färdiga artikelsorter
        int counter = 0; // ?? 
        String[][] dummyString = numberOfArt(list); // matrix over how many of each art: Art | #
        String art = "";
        String[][] rutter = new String[list.length][4];

        boolean checked = false;
        int dummy = 0;

        while (doneArt < numberOfDiffArt(list)) { // loopa tills alla olika artiklar är klara
            if (!checked) {
                klara = 0;
                dummy = Integer.parseInt(dummyString[doneArt][1]); // antal av samma sort
                art = dummyString[doneArt][0]; // namn på artikeln
                //System.out.println("\nChecked false! Ny dummy: " + dummy + " " + art);
            }
            //System.out.println("Namn: " + art + " Antal: " + dummy);
            // Hämta och beräkna vikter 
            int weight = 0;
            for (int i = 0; i < ds.volumeList.length; i++) {
                if (ds.volumeList[i].split(":")[0].equals(art)) {
                    weight = ds.volume[i];
                }
            }

            if (dummy >= 3) { // mer än 3 st av en sort
                if (weight * 3 <= ds.capacity) { // får all tre plats ?? 
                    klara = klara + 3; // tre st artiklar är klara och kan köras i en rutt
                    rutter[numberOfRutts][1] = list[counter][1];
                    rutter[numberOfRutts][2] = String.valueOf(weight * 3);
                    rutter[numberOfRutts][0] = art + art + art;
                    rutter[numberOfRutts][3] = String.valueOf(rutter[numberOfRutts][0].length());
                    counter = counter + 3;
                } else if (weight * 2 <= ds.capacity) { // får två plats ?? 
                    klara = klara + 2;
                    rutter[numberOfRutts][1] = list[counter][1];
                    rutter[numberOfRutts][2] = String.valueOf(weight * 2);
                    rutter[numberOfRutts][0] = art + art;
                    rutter[numberOfRutts][3] = String.valueOf(rutter[numberOfRutts][0].length());
                    counter = counter + 2;
                } else if (weight <= ds.capacity) {
                    klara++;
                    rutter[numberOfRutts][1] = list[counter][1];
                    rutter[numberOfRutts][2] = String.valueOf(weight);
                    rutter[numberOfRutts][0] = art;
                    rutter[numberOfRutts][3] = String.valueOf(rutter[numberOfRutts][0].length());
                    counter++;
                }
            } else if (dummy == 2) {
                if (weight * 2 <= ds.capacity) {
                    klara = klara + 2;
                    rutter[numberOfRutts][1] = list[counter][1];
                    rutter[numberOfRutts][2] = String.valueOf(weight * 2);
                    rutter[numberOfRutts][0] = art + art;
                    rutter[numberOfRutts][3] = String.valueOf(rutter[numberOfRutts][0].length());
                    counter = counter + 2;
                } else if (weight <= ds.capacity) {
                    klara++;
                    rutter[numberOfRutts][1] = list[counter][1];
                    rutter[numberOfRutts][2] = String.valueOf(weight);
                    rutter[numberOfRutts][0] = art;
                    rutter[numberOfRutts][3] = String.valueOf(rutter[numberOfRutts][0].length());
                    counter++;
                }
            } else if (dummy == 1) {
                if (weight <= ds.capacity) {

                    rutter[numberOfRutts][0] = art;
                    klara++;
                    rutter[numberOfRutts][1] = list[counter][1];
                    rutter[numberOfRutts][2] = String.valueOf(weight);
                    rutter[numberOfRutts][3] = String.valueOf(rutter[numberOfRutts][0].length());
                    counter++;
                }
            }
            if (dummy > klara) {
                checked = true;
            } else if (dummy == klara) {
                doneArt++;
                checked = false;
            }
            numberOfRutts++;

        }

        int total = 0;
        for (int i = 0; i < numberOfRutts; i++) {
            String out = String.format("Rutt nr %-1s Rutt: %-3s Cost: %-3s Weight: %-2s Number: %-2s", i, rutter[i][0], rutter[i][1], rutter[i][2], rutter[i][3]);
            System.out.println(out);
            total = total + Integer.valueOf(rutter[i][1]);
        }
        System.out.println("Total trip cost: " + total + "\n");
        return rutter;
    }

}
