package CrabBot;

public class GuiUpdate implements Runnable {

    private AutonomGUI aui;
    private DataStore ds;
    private myBluetoothTransceiver mBT;

    public GuiUpdate(DataStore ds, AutonomGUI aui, myBluetoothTransceiver mBT) {
        this.aui = aui;
        this.ds = ds;
        this.mBT = mBT;
    }

    @Override
    public void run() {
        try {
            while (!ds.nodstop) {
                Thread.sleep(10);
                uppdateLocation();
                aui.updateInfo();
                aui.repaint();
                aui.enterNewRoute();
            }
        } catch (InterruptedException exception) {
        }
    }

    public void uppdateLocation() {
        if (mBT.uppdate > 0) {
            ds.currentNode = ds.GUInodes.get(mBT.uppdate);
            if (mBT.uppdate == ds.GUInodes.size()) mBT.uppdate = -1;
            int x = ds.nodeX[ds.currentNode];
            int y = ds.nodeY[ds.currentNode];
            ds.vehicleX = x;
            ds.vehicleY = y;
        }

    }

}
