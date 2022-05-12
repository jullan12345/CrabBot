/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CrabBot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author clary35
 */

public class MapPanel extends JPanel {

    DataStore ds;

    MapPanel(DataStore ds) {
        this.ds = ds;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Color LIGHT_COLOR = new Color(150, 150, 150);
        final Color DARK_COLOR = new Color(0, 0, 0);
        final Color RED_COLOR = new Color(220, 0, 0);
        final Color BLUE_COLOR = new Color(0, 0, 220);

        int panelHeight = getHeight();
        int panelWidth = getWidth();

        double xscale = 1.0*panelWidth/360.0;
        double yscale = 1.0*panelHeight/220.0;

        if (ds.problemRead == true) { 
            
            // Mark the unloading shelf with a red rectangle
            g.setColor(RED_COLOR);
            g.fillRect( (int)(ds.shelfX[ds.startshelf-1]*xscale), (int)(ds.shelfY[ds.startshelf-1]*yscale), 
                    (int)(xscale*30), (int)(yscale*20));
        
            // Draw shelves as rectangles with numbers inside
            g.setColor(DARK_COLOR);
            g.setFont(new Font("Dialog", Font.BOLD, 16)); 
            String shelf;
            int shelftw;
            FontMetrics fm = g.getFontMetrics();
            
            for(int i=0; i < 45; i++){
                shelf = ""+(i+1);
                shelftw = (int)(fm.stringWidth(shelf) / 4);
                g.drawRect((int)(xscale*(ds.shelfX[i])), (int)(yscale*ds.shelfY[i]),
                        (int)(xscale*30), (int)(yscale*20));
                g.drawString( shelf, (int)((ds.shelfX[i]+15-shelftw)*xscale), 
                        (int)((ds.shelfY[i]+13)*yscale) );
            }

            // Draw nodes at every shelf and corridor
            for(int i=0; i < ds.noNodes; i++){
                g.setColor(BLUE_COLOR);
                g.fillOval((int)((ds.nodeX[i]-2)*xscale), (int)((ds.nodeY[i]-2)*yscale),(int)(4*xscale),(int)(4*yscale));
                g.setColor(RED_COLOR);
                g.drawString( ""+i, (int)((ds.nodeX[i]-2)*xscale), (int)((ds.nodeY[i]-2)*yscale));
            }

            // Draw all line segments
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3));
            g2.setColor(BLUE_COLOR);
            
            for(int i=0; i < ds.noSegments; i++){ 
                g2.drawLine((int)((ds.nodeX[ds.segStart[i]])*xscale), (int)((ds.nodeY[ds.segStart[i]])*yscale), (int)((ds.nodeX[ds.segEnd[i]])*xscale), (int)((ds.nodeY[ds.segEnd[i]])*yscale));
            }            
            
            
            // Rita en prick som representerar AGVn
            g.setColor(RED_COLOR);
            int x = (int) (ds.vehicleX * xscale);
            int y = (int) (ds.vehicleY * yscale);
            g.drawOval(x - 20/2, y - 20/2 , 20, 20);
            
            
            // Kortast väg
            for (int i = 0; i < ds.noSegments; i++) {
                if (ds.linkColor[i] == 1) { g2.setColor(RED_COLOR);} 
                else {g2.setColor(BLUE_COLOR);}
                
                g2.drawLine((int) ((ds.nodeX[ds.segStart[i]]) * xscale),
                            (int) ((ds.nodeY[ds.segStart[i]]) * yscale), 
                            (int) ((ds.nodeX[ds.segEnd[i]])   * xscale),
                            (int) ((ds.nodeY[ds.segEnd[i]])   * yscale));
            }
            

        }
    } // end paintComponent
}
