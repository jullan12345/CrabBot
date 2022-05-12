/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CrabBot;

/**
 *
 * @author lundinjuliia
 */
public class myTimer implements Runnable {
    private int seconds;
    private int minutes;
    private int amountOfTime; // minuter
    private int counter; 
    
    DataStore ds; 
    AutonomGUI cui;
    
    public myTimer(int time, AutonomGUI cui, DataStore ds){
        seconds = 0;
        amountOfTime = time;
        counter = time*60; // counter = totala antalet sekunder
        minutes = amountOfTime;
       
        this.ds = ds; 
        this.cui = cui;
        
    }
    
    @Override
    public void run(){
        try{
            while (!ds.startKlick){
                Thread.sleep(10);
            }
            while (counter > 0){
                Thread.sleep(1000);
                
                if (seconds == 0){
                    minutes = minutes - 1;
                    seconds = 60;
                }
                seconds = seconds - 1; 

                cui.changeTime(seconds, minutes);
                counter = counter - 1; 
                
                if (minutes == 0 && seconds == 30){
                    ds.returnToAv = true;
                }
            }
            
        } catch (InterruptedException exception){ }
    }
    
    
}
