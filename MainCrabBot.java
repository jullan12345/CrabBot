package CrabBot;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainCrabBot {

    DataStore ds;
    MainMenu mm;
    AutonomGUI aui;
    myBluetoothTransceiver mBT;
    ManuelGUI mg;
    Routes rot;
    Optimizer op;

    MainCrabBot() {

        ds = new DataStore();

        ds.readProblem();
        ds.shelfLocations();
        ds.nodeLocations();
        ds.lineSegments();
        mm = new MainMenu(ds);
        mm.setVisible(true);

        // Bluetoothknapp icke tryckt - pausar koden i väntan på att den trycks
        while (!ds.bluetoothKlick) { 
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
        
        aui = mm.getCUI();
        aui.repaint();
        rot = new Routes(ds);
        mBT = new myBluetoothTransceiver(ds, aui, rot);
        mBT.connectBluetooth();
        mm.uppdateB(mBT.ok);

        // väntar på nästa knapp tryck
        while (!ds.aKlick) {
            try {
                Thread.sleep(20);
            if (ds.mKlick) {
                mg = new ManuelGUI(ds, mBT);
                mg.setVisible(true);
                break;
            }
            } catch (InterruptedException ex) {
            }
        }

        op = new Optimizer(ds, rot);

        // "Planera rutt"-knapp icke tryckt - pausar koden i väntan på att vi ska starta programmet
        while (!ds.optKlick) { 
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }

        // Skapa initial lösningen
        System.out.println("Initial:");
        String[][] initial = rot.createInitiail();

        // Omvandla String[][] till List<List<String>> och rensa tomma slots
        List<List<String>> temp = Arrays.stream(initial).map(Arrays::asList).collect(Collectors.toList());
        for (int l = 0; l < temp.size(); l++) {
            if (temp.get(l).get(0) == null) {
                temp.remove(l);
                l--;
            }
        }

        // Optimera initiallösningen
        // temp1 innehåller alla delrutterna ex ABC
        System.out.println("Optimerad rutt:");
        List<List<String>> temp1 = op.optOne(initial);
        ds.optDone = true;

        ds.routes = temp1; // fyll routes med alla delrutterna 

        ds.arts = new String[ds.routes.size()]; // spara alla "ruttbeskrivningar" / "artikelorningar" ex ABC
        ds.orderListInfo = new String(); // skapa en lista med ex ABC BBD för visualisering i GUI
        for (int i = 0; i < ds.routes.size(); i++) {
            ds.arts[i] = ds.routes.get(i).get(0);
            ds.orderListInfo = ds.orderListInfo + " " + ds.arts[i];
        }
        // Starta thread för uppdatering av GUI
        GuiUpdate gui = new GuiUpdate(ds, aui, mBT);
        Thread t2 = new Thread(gui);
        t2.start();

        int[] nodes = new int[2];
        nodes = ds.nextRoute(); // hämta start och slutnod till nästa rutt 
        System.out.println("New route:\nNodes: Start " + nodes[0] + " End " + nodes[1]);
        rot.computeRoute(nodes[0], nodes[1]); // skapa ny rutt med nya start och slut

        aui.updateInfo();
        aui.repaint();

        // Startknappen icke tryckt - pausar koden i väntan på att vi ska starta programmet
        while (!ds.startKlick) { 
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
        
        // starta tråd med timer
        int time = ds.time;
        myTimer timer = new myTimer(time, aui, ds);
        Thread t3 = new Thread(timer);
        t3.start();

        // skapa de första meddelandet och skicka det till AGV
        String s = mBT.newInstructMessage(null);
        mBT.send(s);

        while (ds.startKlick) {
            if (ds.returnToAv && ds.recentMessage.split("")[3].equals("a")) {
                // avslutande meddelande 
                mBT.send("111w");
                break;
            }
            if (ds.nodstop) {
                // NÖDSTOPP
                String dummy = null;
                dummy = mBT.sekv + ds.currentMode + 0 + "s";
                mBT.send(dummy);
            }
        }
    }

    public static void main(String[] args) {

        MainCrabBot x = new MainCrabBot();
    }
}
