package CrabBot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class DataStore {

    String fileName;
    boolean problemRead;
    int   startshelf;
    int[] shelfX;
    int[] shelfY;
    int[] nodeX; // x-koordinater för nod
    int[] nodeY; // y-koordinater för nod
    int[] segStart;
    int[] segEnd;
    int   noNodes; // antal noder
    int   noSegments; // antal segment

//    boolean updateUIflag;
    double vehicleX; // x-koordinater för AGV
    double vehicleY; // y-koordinater för AGV

    String[] orderList;
    String[] placementList;
    String[] shelfName;
    String[] volumeList;

    int[] volume;
    int   capacity;
    int[] shelfNumber;
    int[] linkColor;

    // Knapptryck
    boolean aKlick;
    boolean mKlick;
    boolean startKlick;
    boolean optKlick;
    boolean nodstop;
    boolean bluetoothKlick;

    // Bluetooth 
    String address;
    int    kanal;
    boolean connected;

    // Styrning och navigering 
    int   currentNode; // noden som AGV är vid just nu !
    int[] nodespath; // vägen AGV åker genom lagret 
    ArrayList<String>  commands; // lista med kommandon som ska skickas till AGV
    ArrayList<Integer> zones;
    // Beräkning av koordinatskillnad 
    int oldX;
    int oldY;
    int newX;
    int newY;

    // Optimering 
    int[] totalCost;

//    // Meddelande 
    int loadedCounter; // totalt antal loaded artiklar 

    List<List<String>> routes; // här sparas hela resan

    int subCounter; // räknar delrutter
    int routeCounter; // räknar rutter - anger vilken rutt vi kör 

    int numOfArt; // antal olika artiklar som finns i orderlistan

    String[][] loaded; // matris med artikel och hur många av den som plockats 
    int time; // timer tiden 

    String recentMessage; // senaste skickade meddelandet 

    // För uppdatering av GUI - infopanelen 
    String[] arts;
    String   orderListInfo;
    int currentWeight;

    ArrayList<Integer> GUInodes;

    int currentMode; // manuell eller autonom ? 

    boolean pause;
    boolean returnToAv;
    boolean optDone;

    public DataStore() {
        // Initialize the datastore with fixed size arrays for storing some data
        problemRead = false;
        fileName = null;
        shelfX = new int[45];
        shelfY = new int[45];
        nodeX = new int[999];
        nodeY = new int[999];
        segStart = new int[999];
        segEnd = new int[999];
        startshelf = 0;
        noNodes = 0;
        noSegments = 0;

        orderList = new String[100];
        placementList = new String[100];
        shelfName = new String[100];
        volumeList = new String[100];
        volume = new int[100];
        capacity = 0;
        shelfNumber = new int[100];

        linkColor = new int[100];

        aKlick = false;
        mKlick = false;
        startKlick = false;
        optKlick = false;
        nodstop = false;
        bluetoothKlick = false;

        address = null;
        kanal = 0;
        connected = true;

        totalCost = new int[100];

//        sekv = 0;
//        counter = 0;
        loadedCounter = 0;
        routes = new ArrayList<>();

        subCounter = 0;
        routeCounter = 0;

        numOfArt = 0;

        loaded = new String[100][2];
        time = 0;
        recentMessage = new String();
        currentWeight = 0;
        currentMode = 0;

        pause      = false;
        returnToAv = false;
        optDone    = false;
    }

    public void readProblem() {
        String line;

        try {
            // Avlastningsplats
            fileName = "/Users/julialundin/Library/CloudStorage/OneDrive-Linköpingsuniversitet/År 3/TNK111 Kandidaten/Labbar/avlastningsplats.txt";

            File file1 = new File(fileName);
            Scanner scanner1 = new Scanner(file1, "UTF-8");
            line = (scanner1.nextLine());
            startshelf = Integer.parseInt(line.trim());
            currentNode = startshelf - 1; 

            // Orderlista
            fileName = "/Users/julialundin/Library/CloudStorage/OneDrive-Linköpingsuniversitet/År 3/TNK111 Kandidaten/Labbar/orderInd-15s.txt";
            File file2 = new File(fileName);
            Scanner scanner2 = new Scanner(file2, "UTF-8");
            // läs orderlista 
            line = (scanner2.nextLine());
            orderList = line.split(", ");

            // Sortera orderlistan
            Arrays.sort(orderList);

            // Placeringslista
            fileName = "/Users/julialundin/Library/CloudStorage/OneDrive-Linköpingsuniversitet/År 3/TNK111 Kandidaten/Labbar/placeringslistaInd.txt";
            File file3 = new File(fileName);
            Scanner scanner3 = new Scanner(file3, "UTF-8");
            line = (scanner3.nextLine());
            placementList = line.split(", ");
            for (int i = 0; i < placementList.length; i++) {
                shelfName[i] = placementList[i].split(":")[0];
                shelfNumber[i] = Integer.parseInt((placementList[i].split(":")[1]).trim());
            }
            // Volymlista
            fileName = "/Users/julialundin/Library/CloudStorage/OneDrive-Linköpingsuniversitet/År 3/TNK111 Kandidaten/Labbar/viktlista.txt";
            File file4 = new File(fileName);
            Scanner scanner4 = new Scanner(file4, "UTF-8");
            line = (scanner4.nextLine());
            volumeList = line.split(", ");
            for (int i = 0; i < volumeList.length; i++) {
                shelfName[i] = volumeList[i].split(":")[0];
                volume[i] = Integer.parseInt((volumeList[i].split(":")[1]).trim());
            }
            fileName = "/Users/julialundin/Library/CloudStorage/OneDrive-Linköpingsuniversitet/År 3/TNK111 Kandidaten/Labbar/kapacitet.txt";

            File file5 = new File(fileName);
            Scanner scanner5 = new Scanner(file5, "UTF-8");
            line = (scanner5.nextLine());
            capacity = Integer.parseInt(line.trim());

            fileName = "/Users/julialundin/Library/CloudStorage/OneDrive-Linköpingsuniversitet/År 3/TNK111 Kandidaten/Labbar/uppdragstid.txt";

            File file6 = new File(fileName);
            Scanner scanner6 = new Scanner(file6, "UTF-8");
            line = (scanner6.nextLine());
            time = Integer.parseInt(line.trim());
            
            // Inikera att all data är läst
            problemRead = true;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void lineSegments() {
        int segCount = 0;
        // lines/links connecting the shelf nodes to nodes forming the corridors
        for (int i = 0; i < 3; i++) { // For each block of three
            for (int k = 0; k < 3; k++) { // For each pair
                segStart[segCount] = k + i * 3 + 9;
                segEnd[segCount++] = k + 45 + i * 3;
                segStart[segCount] = k + i * 3;
                segEnd[segCount++] = k + 45 + i * 3;
            }
        }
        for (int i = 0; i < 3; i++) { // For each block of three
            for (int k = 0; k < 3; k++) { // For each pair
                segStart[segCount] = k + i * 3 + 18;
                segEnd[segCount++] = k + 54 + i * 3;
                segStart[segCount] = k + i * 3 + 27;
                segEnd[segCount++] = k + 54 + i * 3;
            }
        }
        for (int i = 0; i < 3; i++) { // For each block of three
            for (int k = 0; k < 3; k++) { // For each pair
                segStart[segCount] = k + i * 3 + 36;
                segEnd[segCount++] = k + 63 + i * 3;
            }
        }
        // Horizontal corridors
        for (int i = 45; i < 64; i = i + 9) { // For each block of three
            for (int k = 0; k < 2; k++) { // For each pair
                segStart[segCount] = k + i;
                segEnd[segCount++] = k + i + 1;
                segStart[segCount] = k + i + 3;
                segEnd[segCount++] = k + i + 1 + 3;
                segStart[segCount] = k + i + 6;
                segEnd[segCount++] = k + i + 1 + 6;
            }
        }

        // Horizontal lines between blocks
        segStart[segCount] = 47;
        segEnd[segCount++] = 72;
        segStart[segCount] = 72;
        segEnd[segCount++] = 48;
        segStart[segCount] = 50;
        segEnd[segCount++] = 73;
        segStart[segCount] = 73;
        segEnd[segCount++] = 51;
        segStart[segCount] = 56;
        segEnd[segCount++] = 74;
        segStart[segCount] = 74;
        segEnd[segCount++] = 57;

        segStart[segCount] = 59;
        segEnd[segCount++] = 75;
        segStart[segCount] = 75;
        segEnd[segCount++] = 60;
        segStart[segCount] = 65;
        segEnd[segCount++] = 76;
        segStart[segCount] = 76;
        segEnd[segCount++] = 66;
        segStart[segCount] = 68;
        segEnd[segCount++] = 77;
        segStart[segCount] = 77;
        segEnd[segCount++] = 69;

        // Vertical lines between blocks
        segStart[segCount] = 72;
        segEnd[segCount++] = 74;
        segStart[segCount] = 74;
        segEnd[segCount++] = 76;
        segStart[segCount] = 73;
        segEnd[segCount++] = 75;
        segStart[segCount] = 75;
        segEnd[segCount++] = 77;

        noSegments = segCount;
    }

    public void nodeLocations() {
        // Nodes at every shelf. Nodes 0-44.
        int yoffset = 0;
        for (int i = 0; i < 45; i++) {
            if (i < 9) {
                yoffset = +5;
            }
            if (i >= 9 && i < 18) {
                yoffset = -5 - 20;
            }
            if (i >= 18 && i < 27) {
                yoffset = +5;
            }
            if (i >= 27 && i < 36) {
                yoffset = -5 - 20;
            }
            if (i >= 36) {
                yoffset = +5;
            }
            nodeX[i] = shelfX[i] + 15;
            nodeY[i] = shelfY[i] - yoffset;
        }
        int nodeCount = 45;
        // Nodes for the three horizontal corridors. Nodes 45-71
        yoffset = 20;
        for (int i = 0; i < 9; i++) {
            nodeX[nodeCount] = shelfX[i] + 15;
            nodeY[nodeCount++] = shelfY[i] - yoffset;
        }
        for (int i = 18; i < 27; i++) {
            nodeX[nodeCount] = shelfX[i] + 15;
            nodeY[nodeCount++] = shelfY[i] - yoffset;
        }
        for (int i = 36; i < 45; i++) {
            nodeX[nodeCount] = shelfX[i] + 15;
            nodeY[nodeCount++] = shelfY[i] - yoffset;
        }

        // Nodes for the two vertical corridors. Nodes 72-77
        for (int i = 2; i < 45; i = i + 18) {
            yoffset = 20;
            nodeX[nodeCount] = shelfX[i] + 30 + 22;
            nodeY[nodeCount++] = shelfY[i] - yoffset;
            nodeX[nodeCount] = shelfX[i + 3] + 30 + 22;
            nodeY[nodeCount++] = shelfY[i + 3] - yoffset;
        }
        noNodes = nodeCount;

        // Placerad vid nod 45!
        vehicleX = nodeX[startshelf - 1];
        vehicleY = nodeY[startshelf - 1];
    }

    public void shelfLocations() {
        for (int k = 0; k < 3; k++) {
            // Bottom row (half)
            for (int i = 0; i < 3; i++) {
                shelfX[k * 3 + i] = (90 * k + 45 * k + 30 * i);
                shelfY[k * 3 + i] = 220 - 20;
            }

            // Middle rows
            for (int i = 0; i < 3; i++) {
                shelfX[k * 3 + i + 9] = (90 * k + 45 * k + 30 * i);
                shelfY[k * 3 + i + 9] = 220 - 80;
            }
            for (int i = 0; i < 3; i++) {
                shelfX[k * 3 + i + 18] = (90 * k + 45 * k + 30 * i);
                shelfY[k * 3 + i + 18] = 220 - 100;
            }

            // Top rows
            for (int i = 0; i < 3; i++) {
                shelfX[k * 3 + i + 27] = (90 * k + 45 * k + 30 * i);
                shelfY[k * 3 + i + 27] = 220 - 160;
            }
            for (int i = 0; i < 3; i++) {
                shelfX[k * 3 + i + 36] = (90 * k + 45 * k + 30 * i);
                shelfY[k * 3 + i + 36] = 220 - 180;
            }
        }

    }

    public int getZone(int node, int innan) {
        int zon = 0;
        if (node > 62 && node <= 71) {
            zon = 4;
        } else if (node == 76 || node == 77) {
            if (innan > 62 && innan <= 71) {
                zon = 4;
            } else {
                zon = 1;
            }
        } else if (node > 0 && node <= 44) {
            zon = 3;
        } else if (node > 44 && node <= 62) {
            zon = 2;
        } else if (node > 71 && node <= 75) {
            zon = 1;
        }
        return zon; // returns the zon of the specific node 
    }

    public int[] nextRoute() {
        // kallas på när en ny rutt ska tas fram. 
        // ta fram ny startplats och slutplats 

        if (!returnToAv) {

            String[] art = arts[routeCounter].split(""); // hämta rutten som ska köras // första biten 
            System.out.println("\nNästa rutt: Nr" + routeCounter + "\n-------------------------------------------------");

            // kolla vilken artikel i rutten som vi ska hämta
            int startNode = 0;
            int endNode = 0;
            int first = 0;
            int second = 0;
            int third = 0;

            int artLength = art.length;

            // hämta alla nod nummer för alla artiklar vi ska hämta 
            // undersök hur många som ska hämtas och vart 
            for (int i = 0; i < shelfName.length; i++) {
                if (artLength == 1) {
                    if (art[0].equals(shelfName[i])) {
                        first = shelfNumber[i] - 1;
                    }
                } else if (artLength == 2) {
                    if (art[0].equals(shelfName[i])) {
                        first = shelfNumber[i] - 1;
                    }
                    if (art[1].equals(shelfName[i])) {
                        second = shelfNumber[i] - 1;
                    }
                } else if (artLength == 3) {
                    if (art[0].equals(shelfName[i])) {
                        first = shelfNumber[i] - 1;
                    }
                    if (art[1].equals(shelfName[i])) {
                        second = shelfNumber[i] - 1;
                    }
                    if (art[2].equals(shelfName[i])) {
                        third = shelfNumber[i] - 1;
                    }
                }
            }
            // Hur ska vi åka? Mellan vilka noder? EX: ABC
            // Vilken delrutt är vi på? = subCounter
            switch (subCounter) {
                case 0: // avlastning -> A
                    startNode = startshelf - 1;
                    endNode = first;
                    break;
                case 1: // A -> B
                    startNode = first;
                    if (artLength == 1) {
                        endNode = startshelf - 1;
                        routeCounter++;
                        subCounter = 0;
                    } else {
                        endNode = second;
                    }
                    break;
                case 2: // B -> C
                    startNode = second;
                    if (artLength == 2) {
                        endNode = startshelf - 1;
                        routeCounter++;
                        subCounter = 0;
                    } else {
                        endNode = third;
                    }
                    break;
                case 3: // C -> avlastning
                    startNode = third;
                    endNode = startshelf - 1;
                    routeCounter++;
                    subCounter = 0;
                    break;
            }
            int[] nodes = new int[2];
            nodes[0] = startNode;
            nodes[1] = endNode;
            return nodes;
        } else {
            int[] nodes = new int[2];
            if (currentNode == startshelf - 1) {
            } else {
                System.out.println("Återvänd till startplats - tiden börjar ta slut");
                nodes[0] = currentNode;
                nodes[1] = startshelf - 1;
            }
            return nodes;
        }
    }

    public int getVolume(String art) {
        int volume = 0;
        for (int i = 0; i < volumeList.length; i++) {
            String dummy = volumeList[i].split(":")[0];
            if (art.equals(dummy)) {
                volume = Integer.parseInt(volumeList[i].split(":")[1].trim());
            }
        }
        return volume;
    }

    public String getArticle(int node) {
        String art = "";
        for (int i = 0; i < placementList.length; i++) {
            int dummy = Integer.parseInt((placementList[i].split(":")[1]).trim());
            if (dummy == node + 1) {
                art = placementList[i].split(":")[0];
            }
        }
        return art;
    }

}
