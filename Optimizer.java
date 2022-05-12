package CrabBot;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class Optimizer {

    private final DataStore ds;
    private List<Vertex> nodes;
    private List<Edge> edges;
    private int[] links;
    private final Routes rot;

    public Optimizer(DataStore ds, Routes rot) {
        this.ds = ds;
        this.rot = rot;
    }

    public List<List<String>> optOne(String[][] init) {
        //Ändrar matris till lista
        String[][] done = new String[init.length][4];
        String[][] donetwo = new String[init.length][4];
        int counter = 0;
        int counter2 = 0;
        List<List<String>> temp = Arrays.stream(init).map(Arrays::asList).collect(Collectors.toList());
        for (int l = 0; l < temp.size(); l++) {
            if (temp.get(l).get(0) == null) {
                temp.remove(l);
                l--;
            }
        }

        //Kolla efter en redan färdig rutt med 3 i, om så, lägg i "färdig rutt"= done.
        for (int i = 0; i < temp.size(); i++) {

            if (Integer.parseInt(temp.get(i).get(3)) == 3) {
                done[counter][0] = temp.get(i).get(0);
                done[counter][1] = temp.get(i).get(1);
                done[counter][2] = temp.get(i).get(2);
                done[counter][3] = temp.get(i).get(3);
                temp.remove(i);
                counter++;
                i--;
            } else if (Integer.parseInt(temp.get(i).get(3)) == 2) {
                donetwo[counter2][0] = temp.get(i).get(0);
                donetwo[counter2][1] = temp.get(i).get(1);
                donetwo[counter2][2] = temp.get(i).get(2);
                donetwo[counter2][3] = temp.get(i).get(3);
                temp.remove(i);
                counter2++;
                i--;
            }
        }

        List<List<String>> twos = Arrays.stream(donetwo).map(Arrays::asList).collect(Collectors.toList());

        for (int l = 0; l < twos.size(); l++) {
            if (twos.get(l).get(0) == null) {
                twos.remove(l);
                l--;
            }
        }
        //temp innehåller nu bara delruttar med en art (ett stopp) i

        //Para ihop en delrutt med 2 ordrar i med en som bara har 1 order
        for (int i = 0; i < twos.size(); i++) {// ska vi verkligen ha temp size här?
            pairTwoOne(temp, done, twos, counter, ds.capacity);
            i--;
        }
        counter2 = done.length - temp.size();// ny counter för done
        //para ihop ensamma ettor med varandra
        for (int i = 0; i < temp.size(); i++) {
            pairOneOne(temp, done, counter2, ds.capacity);
            counter2++;
            i--;
        }

        List<List<String>> allDone = Arrays.stream(done).map(Arrays::asList).collect(Collectors.toList());
        //ta bort tomma rader ur allDone
        for (int l = 0; l < allDone.size(); l++) {
            if (allDone.get(l).get(0) == null) {
                allDone.remove(l);
                l--;
            }
        }

        //Skriver ut hur en optimerad delrutt ser ut när den är skapad
        int totalcost = 0;
        for (int k = 0; k < allDone.size(); k++) {
//            System.out.println("Artikel: " + allDone.get(k).get(0) + " Kostnad: " + allDone.get(k).get(1) + " Vikt: " + allDone.get(k).get(2) + " Antal: " + allDone.get(k).get(3));  
            totalcost = totalcost + Integer.valueOf(allDone.get(k).get(1));
        }
//        System.out.println("Total cost for the hole route: " + totalcost);

        //att göra: sortera delrutternas ordning
        //Sortera delrutternas ordning
        class CustomComparator implements Comparator<List<String>> {

            @Override
            public int compare(List<String> arg1, List<String> arg2) {
                String firstString_arg1 = arg1.get(3);
                String firstString_arg2 = arg2.get(3);
                return Integer.valueOf(firstString_arg1).compareTo(Integer.valueOf(firstString_arg2));
            }
        }

        Collections.sort(allDone, new CustomComparator());
        Collections.reverse(allDone);

        for (int i = 0; i < allDone.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (i != j) {
                    List<String> swich = new ArrayList<String>();
                    if (Integer.valueOf(allDone.get(j).get(3)) == Integer.valueOf(allDone.get(i).get(3)) && Integer.valueOf(allDone.get(j).get(1)) > Integer.valueOf(allDone.get(i).get(1))) {

                        swich = allDone.get(i);
                        allDone.set(i, allDone.get(j));
                        allDone.set(j, swich);
                    }

                }
            }
        }
        int total = 0;
        for (int i = 0; i < allDone.size(); i++) {
            String out = String.format("Rutt nr %-1s Rutt: %-3s Cost: %-3s Weight: %-2s Number: %-2s", i, allDone.get(i).get(0), allDone.get(i).get(1), allDone.get(i).get(2), allDone.get(i).get(3));
            System.out.println(out);
            total = total + Integer.valueOf(allDone.get(i).get(1));
        }
        System.out.println("Total trip cost: " + total + "\n");
        return allDone;
    }

    public int nodeNumber(String arg) {//Hitta nodeNumber i nodnätverket för en hyllplats
        int nodeNumber = 0;
        for (int p = 0; p < ds.shelfName.length; p++) {

            if (ds.shelfName[p].equals(arg.split("")[0])) {
                nodeNumber = ds.shelfNumber[p] - 1;
                break;
            }
        }
        return nodeNumber;
    }

    //Denna metod ska para ihop 2 med 1
    public void pairTwoOne(List<List<String>> ones, String[][] klar, List<List<String>> twos, int arg3, int capacity) {
        // arg3 = slotnummer i den färdiga listan, i starten är arg3 hur många färdiga rutter som finns
        // Hitta rutt med bara 2 i, para isåfal ihop dem med en rutt med bara ett stopp. Så att en rutt med 3 skapas.
        // Om ingen rutt med ett stopp finns ska rutten sparas som den med 2 i klar rutten.
        for (int i = 0; i < twos.size(); i++) {
            int counter2 = 0;
            // Hitt rutt med bara 2 artiklar  
            // Om hittad spara dess parametrar i en variabel för att senare bygga på
            String art = twos.get(i).get(0);
            String cost = null;
            String volume = twos.get(i).get(2);
            String amount = twos.get(i).get(3);

            int start = nodeNumber(twos.get(i).get(0)); //hyllplats för plats med 2 artiklar
            int bestdist = 100000000;  //stort tal pga att den ska alltid ändras vid första jämförelsen
            int platsarg = 0;

            //leta efter en delrutt med 1 art i
            for (int k = 0; k < ones.size(); k++) {
                // om ingen ensam finns, behåll 2 i en rutt och gå vidare
                String tempvikt = String.valueOf(Integer.parseInt(volume) + Integer.parseInt(ones.get(k).get(2)));
                int end = nodeNumber(ones.get(k).get(0)); // nodnummret för ensam art

                if (start != end) {
                    int temp2 = rot.computeRoute(start, end);//beräkna cost för snabbast väg mellan hyllorna
                    if (temp2 < bestdist && Integer.parseInt(tempvikt) <= capacity) { //kolla om distansen är bäst jämfört med tidigare funna ensamma, isåfall byta bästa distans
                        bestdist = temp2;
                        platsarg = k;
                        counter2++;
                    }
                }
            }

            if (bestdist != 100000000) { // om en ensam finns, para ihop med delrutt med 2 artiklar
                art = art + ones.get(platsarg).get(0); // namn på hämtade artiklar
                int temp5 = Integer.parseInt(twos.get(i).get(1)); // kostnaden att åka till 2 rutt och tillbaka till avlast
                int start2 = nodeNumber(ones.get(platsarg).get(0)); // nodplatsen för ensam art att hämta
                int end2 = ds.startshelf - 1; // end = avlastningsplatsen

                int dist = rot.computeRoute(start2, end2); // cost från ensam nodplats till avlast  
                cost = String.valueOf(temp5 / 2 + bestdist + dist); // åka från avlastning till nästa, sen till ensam, sen till avlast

                volume = String.valueOf(Integer.parseInt(volume) + Integer.parseInt(ones.get(platsarg).get(2)));
                amount = String.valueOf(Integer.parseInt(amount) + Integer.parseInt(ones.get(platsarg).get(3)));
                klar[arg3][0] = art;
                klar[arg3][1] = cost;
                klar[arg3][2] = volume;
                klar[arg3][3] = amount;

                //System.out.println("Ny klar rutt: " + art + " " + cost + " " + volume + " " + amount);
                arg3++; // en till ny rutt
                twos.remove(i);
                //Tar bort 1or
                if (counter2 != 0) { // en 2 och 1 är komb
                    if (platsarg != 0) {
                        ones.remove(platsarg);//-1
                    } else {
                        ones.remove(platsarg);
                    }
                }
            } else {
                klar[arg3][0] = twos.get(i).get(0);
                klar[arg3][1] = twos.get(i).get(1);
                klar[arg3][2] = twos.get(i).get(2);
                klar[arg3][3] = twos.get(i).get(3);
                twos.remove(i);
                arg3++;
            }
            i--; // Eftersom vi tar bort en i plats, måste index uppdateras, dvs minskas
        }
    }

    //Denna metod ska skapa en distansmatris för flera olika hyllplatser
    public int[][] oneDistMatrix(List<List<String>> ones) {
        //Skapa en distansmatris
        int counter2 = 0; //counter/indexiering för kolumner i matris
        int[][] distanceMatrix = new int[ones.size()][ones.size()];//skapa en tom matris
        //fyll distansmatrisen
        for (int i = 0; i < ones.size(); i++) {
            int counter1 = 0; //counter/indexering för rader i matris
            for (int j = 0; j < ones.size(); j++) {
                int start = nodeNumber(ones.get(i).get(0)); //nodnummret vi är på i I
                int end = nodeNumber(ones.get(j).get(0)); // nodnummret för den vi jämför med i J
                if (j != i && start != end) {
                    int cost = rot.computeRoute(start, end);//beräkna cost för snabbast väg mellan hyllorna
                    distanceMatrix[counter1][counter2] = cost; //lägg in kostand på den plats vi är på i matrisen
                    //System.out.println(counter1 + "," + counter2 + " cost:" + cost);
                    counter1++;
                } else {//om vi hittar samma plats vi är på så ska kostnaden sparas som jätte stor
                    distanceMatrix[counter1][counter2] = 1000000000;
                    counter1++;
                }
            }
            counter2++;
        }
        return distanceMatrix;
    }

    public void pairOneOne(List<List<String>> ones, String[][] klar, int arg3, int capacity) {
        //Skapa först distans matris för alla hyllplatser som är kvar
        int distance[][] = oneDistMatrix(ones);
        //Hitta närmsta till den som är närmst start
        int bestdist = 100000000;
        int index = 0;
        int startstopp = Integer.parseInt(ones.get(0).get(1));
        //initierng av rutt egenskaper för ny rutt
        String art = ones.get(0).get(0);
        String cost = String.valueOf(startstopp / 2);
        String volume = ones.get(0).get(2);
        String amount = ones.get(0).get(3);

        //loop för att finna en etta att kombinera en annan ettta med, med avseende på kortast distans
        for (int j = 0; j < ones.size(); j++) {
            int temp = distance[j][0];
            int tempvikt = Integer.parseInt(volume) + Integer.parseInt(ones.get(j).get(2));
            if (temp < bestdist && temp != 0 && tempvikt <= capacity) {
                bestdist = temp;
                index = j;
            }
        }

        if (bestdist != 100000000) { // om en ensam finns, para ihop med delrutt med 2 artiklar
            art = art + ones.get(index).get(0); // namn/beteckning på hämtade artiklar
            cost = String.valueOf(Integer.parseInt(cost) + bestdist); // plusa ihop total distans för ny rutt
            volume = String.valueOf(Integer.parseInt(volume) + Integer.parseInt(ones.get(index).get(2)));
            amount = String.valueOf(Integer.parseInt(amount) + Integer.parseInt(ones.get(index).get(3)));
            //initierar högt värde på best dist igen för att kunna söka efter ett till stopp att para ihop rutten med
            bestdist = 100000000;
            int index2 = 0;
            // hitta närmsta från den i mitten av rutten till den som ska vara sist
            for (int i = 0; i < ones.size(); i++) {
                int temp = distance[i][index];
                int tempvikt = Integer.parseInt(volume) + Integer.parseInt(ones.get(i).get(2));
                if (temp < bestdist && temp != 0 && tempvikt <= capacity && i != index && i != 0) {
                    bestdist = temp;
                    index2 = i;
                }
            }

            if (bestdist != 100000000) { // om en ensam finns, para ihop med delrutt med 2 artiklar
                art = art + ones.get(index2).get(0); // namn på hämtade artiklar
                //beräkna kostnaden för att köra till avlast
                int start2 = nodeNumber(ones.get(index).get(0)); // nodplatsen för ensam art att hämta
                int end2 = ds.startshelf - 1; // end = avlastningsplatsen
                int dist = rot.computeRoute(start2, end2); // cost från ensam nodplats till avlast  
                cost = String.valueOf(Integer.parseInt(cost) + bestdist + dist); //summering av rutt cost
                volume = String.valueOf(Integer.parseInt(volume) + Integer.parseInt(ones.get(index2).get(2)));
                amount = String.valueOf(Integer.parseInt(amount) + Integer.parseInt(ones.get(index2).get(3)));
                //Spara in ny rutt och dess info i den arrayen med färdiga rutter
                klar[arg3][0] = art;
                klar[arg3][1] = cost;
                klar[arg3][2] = volume;
                klar[arg3][3] = amount;
                /* System.out.println("Ny klar rutt: " + art + " " + cost + " " + volume + " " + amount);
                System.out.println("-------" + arg3);
                System.out.println("Listan på rutter som ej är klara: " + ones);*/
                if (index == ones.size() - 1) {
                    index--;
                }
                ones.remove(index2);//tar bort rutt med en art från den arrayen med ettor

                ones.remove(index);//tar bort rutt med en art från den arrayen med ettor
                ones.remove(0);//tar bort rutt med en art från den arrayen med ettor

                //System.out.println("Ny uppdaterad lista: " + ones);
            } else { // om en rutt med 2 inte går att kombinera med en till, spara rutt som rutt med 2 stopp.
                //Spara in ny rutt och dess info i den arrayen med färdiga rutter
                klar[arg3][0] = art;
                klar[arg3][1] = cost;
                klar[arg3][2] = volume;
                klar[arg3][3] = amount;
                /*System.out.println("Ny klar rutt: " + art + " " + cost + " " + volume + " " + amount);
                System.out.println("Listan på rutter som ej är klara: " + ones);*/
                ones.remove(index);//tar bort rutt med en art från den arrayen med ettor
                ones.remove(0);//tar bort rutt med en art från den arrayen med ettor
                // System.out.println("Ny uppdaterad lista: " + ones);
            }
        } else { //om ingen finns att para ihop med från hela början, spara som ensam
            //Spara in ny rutt och dess info i den arrayen med färdiga rutter
            klar[arg3][0] = art;
            klar[arg3][1] = String.valueOf(startstopp);
            klar[arg3][2] = volume;
            klar[arg3][3] = amount;
            /*System.out.println("Ny klar rutt: " + art + " " + cost + " " + volume + " " + amount);
            System.out.println("-------" + arg3);
            System.out.println("Listan på rutter som ej är klara: " + ones);*/
            ones.remove(0); //tar bort rutt med en art från den arrayen med ettor
            //System.out.println("Ny uppdaterad lista: " + ones);
        }
        
    }
}
