package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;


public class HandleASession implements Runnable, Constants {
	private Socket player1;
	private Socket player2;
	

	private DataInputStream fromPlayer1;
	private DataOutputStream toPlayer1;
	private DataInputStream fromPlayer2;
	private DataOutputStream toPlayer2;
	
	private boolean continueToPlay = true;
	
	public HandleASession(Socket player1, Socket player2) {
		this.player1 = player1;
		this.player2 = player2;
		
	}
	
	@Override
	public void run() {
		try {
			fromPlayer1 = new DataInputStream(player1.getInputStream());
			toPlayer1 = new DataOutputStream(player1.getOutputStream());
			fromPlayer2 = new DataInputStream(player2.getInputStream());
			toPlayer2 = new DataOutputStream(player2.getOutputStream());

			//this is used just to start player 1
			//line 144 of main
			
			//ignored write
			toPlayer1.writeInt(0);
			//toPlayer2.writeInt(0);
			
			//the game is actually ran here
			while(true) {
				//Receive move from player 1******************
				//main line 214
				//toPlayer1.writeInt(4);
				//toPlayer2.writeInt(4);
				int rowSelected = fromPlayer1.readInt();
				int colSelected = fromPlayer1.readInt();
				System.out.println("Server read " + rowSelected + ", " + colSelected);
				
				
				//send to player 2 to determine if hit or miss
					toPlayer2.writeInt(rowSelected);
					toPlayer2.writeInt(colSelected);
					
				//wait for hit or miss answer
					boolean answer = fromPlayer2.readBoolean();
					
				//send hit or miss answer back
					toPlayer1.writeBoolean(answer);
					
				//player 2 moves******************************
					rowSelected = fromPlayer2.readInt();
					colSelected = fromPlayer2.readInt();
					
						
				//send to player 2 to determine if hit or miss
						toPlayer1.writeInt(rowSelected);
						toPlayer1.writeInt(colSelected);
						
					//wait for hit or miss answer
						answer = fromPlayer1.readBoolean();
						
					//send hit or miss answer back
						toPlayer2.writeBoolean(answer);
				
			}
		} catch(Exception e){}

	}

public static void main(String[] args){
}
}
