//By Jessica B and Robert I

package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;


public class HandleASession implements Runnable {
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

			//ignored write
			toPlayer1.writeInt(0);
			//toPlayer2.writeInt(0);

			//the game is actually ran here
			while(true) {
				//Receive move from player 1******************
				int rowSelected = fromPlayer1.readInt();
				int colSelected = fromPlayer1.readInt();
				System.out.println("Server read " + rowSelected + ", " + colSelected);


				//send to player 2 to determine if hit or miss
					toPlayer2.writeInt(rowSelected);
					toPlayer2.writeInt(colSelected);

				//wait for hit or miss answer
					boolean isHit = fromPlayer2.readBoolean();
					String hitShip = fromPlayer2.readUTF();
					System.out.println("Server got: " + hitShip);

					boolean isDestroyed = fromPlayer2.readBoolean();
					System.out.println("Server got that is was destroyed: " + hitShip);
					boolean isGameOver = fromPlayer2.readBoolean();
					System.out.println("Server got that the game was over: " + isGameOver);


				//send hit or miss answer back
					toPlayer1.writeBoolean(isHit);
					toPlayer1.writeUTF(hitShip);
					toPlayer1.writeBoolean(isDestroyed);
					toPlayer1.writeBoolean(isGameOver);


				//player 2 moves******************************
					rowSelected = fromPlayer2.readInt();
					colSelected = fromPlayer2.readInt();


				//send to player 2 to determine if hit or miss
						toPlayer1.writeInt(rowSelected);
						toPlayer1.writeInt(colSelected);

					//wait for hit or miss answer
						isHit = fromPlayer1.readBoolean();
						hitShip = fromPlayer1.readUTF();
						System.out.println("Server got: " + hitShip);
						isDestroyed = fromPlayer1.readBoolean();
						System.out.println("Server got that is was destroyed: " + hitShip);
						isGameOver = fromPlayer1.readBoolean();

					//send hit or miss answer back
						toPlayer2.writeBoolean(isHit);
						toPlayer2.writeUTF(hitShip);

						toPlayer2.writeBoolean(isDestroyed);
						toPlayer2.writeBoolean(isGameOver);

			}
		} catch(Exception e){}

	}

public static void main(String[] args){
}
}