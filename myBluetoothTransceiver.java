package CrabBot;

import java.io.*;
import javax.microedition.io.*;
import javax.bluetooth.*;

public class myBluetoothTransceiver {

    DataStore ds;
    AutonomGUI aui;
    Routes rot;

    StreamConnection connection;
    PrintStream bluetooth_out;
    BufferedReader bluetooth_in;
    BufferedReader tangentbord;
    int r;

    int uppdate;

    // Meddelande 
    int sekv; // sekvensummer
    int counter; // håller koll på vilket meddelande ur commands som ska skickas
    boolean ok;

    public myBluetoothTransceiver(DataStore ds, AutonomGUI aui, Routes rot) {
        this.ds = ds;
        this.aui = aui;
        this.rot = rot;
        r = 0;
        uppdate = -1;
        counter = 0;
        ok = false;
    }

    public boolean connectBluetooth() {

        try {
            connection = (StreamConnection) Connector.open("btspp://" + ds.address + ":" + ds.kanal);

            // Skickare 
            bluetooth_out = new PrintStream(connection.openOutputStream());
            //Mottagare
            bluetooth_in = new BufferedReader(new InputStreamReader(connection.openInputStream()));

            aui.appendStatus("Kopplad till AGV", 'b');
            ok = true;
            connection.close();
        } catch (Exception e) {
            System.out.println(e.toString());
            aui.appendStatus("Ingen enhet kopplad", 'b');
            ok = false;
        }
        return ok;
    }

    public void send(String s) {
        try {
            bluetooth_out.println(s);
            System.out.println("Skickat meddelande: " + s);
            String dummy = "\nSkickat meddelande " + s + " nod " + ds.currentNode;
            aui.appendStatus(dummy, 'h');
            uppdate++;
            System.out.println(ds.currentNode);
            receive();
        } catch (Exception e) {
        }
    }

    public void receive() {
        try {
            System.out.println("Receivce meddelande nr: " + r);
            r++; // För att räkna antal mottagna meddelanden 
            String messageIn = bluetooth_in.readLine();
            System.out.println("message in " + messageIn);
            if (messageIn != null || !messageIn.isBlank()) {
                System.out.println("Mottaget meddelande : " + messageIn);
                String dummy = "\nMottaget meddelande : " + messageIn;
                aui.appendStatus(dummy, 'h');
                checkMessage(messageIn);
            }
        } catch (Exception e) {}
    }

    public String newInstructMessage(String s) {
        String dummy = null;
        // mode == manuellt eller autonomt
        int zon = 0;
        String kom = null;
        if (ds.currentMode == 0) { // manuell
            kom = s;
            send(s);
        } else {
            kom = ds.commands.get(counter);
            if (kom.equals("l") || kom.equals("r") || kom.equals("m") || kom.equals("a") || counter + 1 == ds.GUInodes.size()) {
                zon = 3;
            } else {
                // vi vill ha kommande zon och därav måste vi ha kommande nod. 
                zon = ds.zones.get(counter + 1); //ds.nodespath[ds.counter+1]);
            }
            dummy = String.valueOf(sekv) + ds.currentMode + String.valueOf(zon) + kom;
            ds.recentMessage = dummy;
        }
        return dummy;
    }

    public String checkMessage(String arg) {
        String[] dummy = arg.split("");
        String out = new String();
        
        if (dummy[2].equals("p")) { // AGV har kört öven en tejp - använding till uppdatering av GUI
            out = "Kört över tejp";
            aui.appendStatus(out, 'h');
            if (dummy[1].equals("0")) { // ingen request från AGV - skicka inget 
                receive();
            } else if (dummy[1].equals("1")) { // request - skicka nytt meddelande
                counter++;
                sekv++;
                if (sekv == 10) {
                    sekv = 0;
                }
                String temp = newInstructMessage(null);
                send(temp);
            }
        } else {
            if (!dummy[0].equals(String.valueOf(sekv))) { // fel ACK => retransmit
                System.out.println("\nFel ACK - retransmit");
                out = "Fel ACK\n" + " Skickat igen: " + ds.recentMessage + "\n";
                aui.appendStatus(out, 'h');
                System.out.println("Skickat igen " + ds.recentMessage);
                counter--;
                send(ds.recentMessage);
            } else if (dummy[0].equals(String.valueOf(sekv))) {
                if (dummy[2].equals("f")) { // allmänt fel
                    System.out.println("Allmänt fel har intäffat hos AGV");
                    out = "Allmänt fel\n";
                } else if (dummy[2].equals("u")) { // AGV har utfört en plocking - uppdatera antal loaded
                    out = "Artikel plockad\n";
                    ds.loadedCounter++;
                    // uppdatera currentNode vikt 
                    String art = ds.getArticle(ds.GUInodes.get(uppdate+1));
                    System.out.println("Uppdatera vikt:\nHämtad artikel = " + art + " väger : " + ds.getVolume(art));
                    ds.currentWeight = ds.currentWeight + ds.getVolume(art); // används i updateInfo
                } else if (dummy[2].equals("k")) { // AGV har plockat av alla artiklar vid avlastningsplats - starta ny rutt 
                    out = "Avlastning klar\n";
                    // reseta vikt till 0 
                    ds.currentWeight = 0;
                }
                aui.appendStatus(out, 'h');
                if (dummy[1].equals("1")) { // request 
                    // send new message with instuctions
                    sekv++;
                    counter++;
                    if (sekv == 10) {
                        sekv = 0;
                    }
                    if (counter == ds.commands.size()) { // alla commands är genomlästa - skapa ny rutt! 
                        int[] nodes = new int[2];
                        counter = 0;
                        nodes = ds.nextRoute();
                        System.out.println("New route\nNodes: Start " + nodes[0] + " End " + nodes[1]);
                        rot.computeRoute(nodes[0], nodes[1]);
                        uppdate = -1;
                    }
                    String s = newInstructMessage(null);
                    send(s);
                }
            }
        }
        return out;
    }

}
