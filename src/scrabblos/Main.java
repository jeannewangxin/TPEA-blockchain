package scrabblos;

import java.util.concurrent.TimeUnit;

public class Main {

	public static void main(String args[]) {

		System.out.println("++++++++++++++++++++++++++++++++GAME START +++++++++++++++++++++++++++++++++++++++");

		System.out.println("++++++++++++++++++++++++++++++++Round : 0 +++++++++++++++++++++++++++++++++++++++");

        for (int i=0; i<MotorA.CLIENT_QTY; i++) 
        { 
        	try {
				TimeUnit.MILLISECONDS.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
            Thread object = new Thread(new Client());        
            object.start(); 
        }         
    	
        for (int i=0; i<MotorA.POLITTICIAN_QTY; i++) 
        { 

        	try {
        		TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
            Thread object = new Thread(new Politician()); 
            object.start(); 
        } 
        
    }
}
