//By Jessica B and Robert I

package application;

import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Constants {
	public static void main(String[] args) {
		
	new Thread (() -> {
		try {
			ServerSocket serverSocket = new ServerSocket(8000);
			System.out.println("Server started");
			
				while(true) {
					
					//connects to player 1
					Socket player1 = serverSocket.accept();
					System.out.println("Player 1 connected.");
					new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);
					

					//connects to player 2
					Socket player2 = serverSocket.accept();
					System.out.println("Player 2 connected.");
					new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);

					//starts the game with 2 players
					new Thread(new HandleASession(player1, player2)).start();
				}
		}catch(Exception e) {}
	}).start();
	
	}
}
    
