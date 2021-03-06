package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javafx.scene.text.TextAlignment;

public class Main extends Application /*implements Constants*/ {
	Ship battleship;
	Ship submarine;
	Ship destroyer;
	Ship patrolBoat;
	ArrayList <Ship> ships = new ArrayList<Ship>();
	//topGrid keeps track of client's guesses
	Rectangle[][] topGrid = new Rectangle[11][11];
	//bottonGrid will hold ship positions
	Rectangle[][] bottomGrid = new Rectangle[11][11];
	Label alert = new Label();
	Label playerLbl = new Label("Player");
	Label statusLbl = new Label("Game Status");
	Text opponentsShipsLbl = new Text("Opponent's\nships");
	Text battleshipLbl = new Text("Battleship (4)");
	Text submarineLbl = new Text("Submarine (3)");
	Text destroyerLbl = new Text("Destroyer (3)");
	Text patrolBoatLbl = new Text("Patrol Boat (2)");
	int player;
	BorderPane root = new BorderPane();
	Pane gridPane = new Pane();
	DataOutputStream toServer = null;
	DataInputStream fromServer = null;
	boolean continueToPlay = true;
	boolean p1Turn = false;
	boolean p2Turn = false;
	boolean waiting = true;
	int rowSelected;
	int colSelected;
	int rowReceived;
	int colReceived;
	int player1Count = 0;
	int player2Count = 0;
	Stage primaryStage;
	String hitShip = ""; 
	boolean isDestroyed = false;
	boolean isGameOver = false;
	Alert gameWonAlert = new Alert(AlertType.WARNING);
	Alert gameLostAlert = new Alert(AlertType.WARNING);
	
	@Override
	public void start(Stage primaryStage) throws UnknownHostException, IOException {
			Scene scene = new Scene(root, 390, 625);
			alert.setTextFill(Color.RED);
			drawGrid(topGrid, gridPane, 10, 10);
			drawGrid(bottomGrid, gridPane, 10, 300);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Battleship");
			primaryStage.show();
			shipPlacement();
	
			//highlight squares on mouse hover
			for( int i = 1; i <= 10; i++){
				for( int j = 1; j <= 10; j++){
					final int indexI = i;
					final int indexJ = j;
					topGrid[indexI][indexJ].setOnMouseEntered(e -> {
						topGrid[indexI][indexJ].setFill(Color.CADETBLUE);
						topGrid[indexI][0].setFill(Color.DARKGRAY);
						topGrid[0][indexJ].setFill(Color.DARKGRAY);
					});
					topGrid[indexI][indexJ].setOnMouseExited(e -> {
						topGrid[indexI][indexJ].setFill(Color.LIGHTBLUE);
						topGrid[indexI][0].setFill(Color.LIGHTGRAY);
						topGrid[0][indexJ].setFill(Color.LIGHTGRAY);
					});
				}
			}//end square highlight
			VBox vbox = new VBox(10);
			vbox.getChildren().addAll(opponentsShipsLbl, battleshipLbl, submarineLbl, destroyerLbl, patrolBoatLbl);
			HBox hbox = new HBox(10);
			hbox.getChildren().addAll(playerLbl, statusLbl);
			root.setCenter(gridPane);
			root.setRight(vbox);
			root.setTop(hbox);
			root.setPadding(new Insets(10, 10, 10, 10));
			BorderPane.setAlignment(vbox, Pos.CENTER);
			BorderPane.setAlignment(statusLbl, Pos.CENTER);
			playerLbl.setFont(Font.font(18));
			statusLbl.setFont(Font.font(18));
			opponentsShipsLbl.setFont(Font.font(13));
			battleshipLbl.setFill(Color.GREEN);
			submarineLbl.setFill(Color.GREEN);
			destroyerLbl.setFill(Color.GREEN);
			patrolBoatLbl.setFill(Color.GREEN);
			
			gameWonAlert.setTitle("Game Over");
			gameWonAlert.setHeaderText(null);
			gameWonAlert.setContentText("You Won!");

			gameLostAlert.setTitle("Game Over");
			gameLostAlert.setHeaderText(null);
			gameLostAlert.setContentText("You Lost");

			connectToServer();	
	}//end start()
	
	public void connectToServer() throws UnknownHostException, IOException{
		Socket socket = new Socket("localhost", 8000);
		System.out.println("CONNECTED");
		toServer = new DataOutputStream(socket.getOutputStream());		
		fromServer = new DataInputStream(socket.getInputStream());
		
	    // Control the game on a separate thread
	    new Thread(() -> {
	      try {
	        // Get notification from the server
	        player = fromServer.readInt();
	        System.out.println(player);
	        // Am I player 1 or 2?
	        if (player == 1) {
	          Platform.runLater(() -> {
	            playerLbl.setText("Player 1");
	            statusLbl.setText("Waiting for player 2 to join");
	          });
	          // Receive startup notification from the server
	          int x = fromServer.readInt(); // Whatever read is ignored
	          // The other player has joined
	          Platform.runLater(() -> 
	          statusLbl.setText("Player 2 has joined. I start first"));
	          
	          // It is my turn
	          p1Turn = true;
	          
	        }
	        else if (player == 2) {
	          Platform.runLater(() -> {
	        	playerLbl.setText("Player 2");
	            statusLbl.setText("Waiting for player 1 to move");
	          });
	        }
	  
	        // Continue to play
	        while (continueToPlay) { 
	        	
	          if (player == 1) {
	        	Platform.runLater(() -> {
		            statusLbl.setText("Your move.");
		          });
	        	setAction();
	        	waitForAction(); // Wait for player 1 to move
	            sendCoord(); // Send the move to the server
	            System.out.println("Player 1 sent a cord.");
	            receiveIsHit(); //recieve if its a hit or miss from server
	            isGameOver();
	            p1Turn = false;
	            removeAction();
	            p2Turn = true;
	            Platform.runLater(() -> {
		            statusLbl.setText("Waiting for Player 2 to move.");
		          });
	            receiveCoord();
	           // receiveInfoFromServer();
	          }
	          else if (player == 2) {  
	        	removeAction();
	            receiveCoord();
	            Platform.runLater(() -> {
		            statusLbl.setText("Your move.");
		          });
	            setAction();
	            waitForAction(); 
	            sendCoord(); // Send player 2's move to the server
	            receiveIsHit();
	            isGameOver();
	            p1Turn = true; 
	            p2Turn = false;
	            removeAction();
	            Platform.runLater(() -> { 
		            statusLbl.setText("Waiting for Player 1 to move.");
		          });
	          }
	        }//end while continueToPlay
	      }//end try
	      catch (Exception ex) {
	        ex.printStackTrace();
	      }//end catch
	    }).start();
	}//end connectToServer()
	
	//disables on click when it is not players turn
	public void removeAction(){
		for( int i = 1; i <= 10; i++){
			for( int j = 1; j <= 10; j++){
				if(topGrid[i][j].getFill() == Color.LIGHTBLUE){
					topGrid[i][j].setOnMouseClicked(null);
				}
			}
		}
	}//end remove action
	
	//enables on click when it is players turn
	public void setAction(){
		for( int i = 1; i <= 10; i++){
			for( int j = 1; j <= 10; j++){
				final int indexI = i;
				final int indexJ = j;
				if(topGrid[i][j].getFill() == Color.LIGHTBLUE){
					topGrid[i][j].setOnMouseClicked(e -> {
						rowSelected = indexI;
						colSelected = indexJ;
						System.out.println("Coordinate to send: Row: " + rowSelected + " Column: " + colSelected);
						topGrid[indexI][0].setFill(Color.LIGHTGRAY);
						topGrid[0][indexJ].setFill(Color.LIGHTGRAY);
						topGrid[indexI][indexJ].setOnMouseEntered(null);
						topGrid[indexI][indexJ].setOnMouseExited(null);
						topGrid[indexI][indexJ].setOnMouseClicked(null);
						waiting = false;
					});
				}
			}
		}
	}//end replace action
	
	public int isGameOver() {
		int numOfShipsDestroyed = 0;
		for(Ship s: ships) {
			if(s.getCoordinates().isEmpty()) {
				numOfShipsDestroyed++;
			}
		}
		if(numOfShipsDestroyed == 4) {
			//i lost/
			Platform.runLater(() -> { 
	            gameLostAlert.showAndWait();
	          });
			isGameOver = true;
			return player;
		}
		else {
			isGameOver = false;
			return 0;
		}
	}
	
	/*
	 * receive coordinates that opponent chose and determine if it is a hit or miss, send this to server to inform opponent
	 */
	public void receiveCoord() throws IOException{
		rowReceived = fromServer.readInt();
		colReceived = fromServer.readInt();
		
		System.out.println("client rec "+rowReceived +" "+ colReceived);
		boolean isHit = isHit(/*ships,*/ rowReceived, colReceived);
		
		if(isHit){
			if(player == 1)
			bottomGrid[rowReceived][colReceived].setFill(Color.RED);
			if(player == 2)
			bottomGrid[rowReceived][colReceived].setFill(Color.RED);
		}
		else{
			if(player == 1)
				bottomGrid[rowReceived][colReceived].setFill(Color.DARKBLUE);
			if(player == 2)
				bottomGrid[rowReceived][colReceived].setFill(Color.DARKBLUE);
		}
		toServer.writeBoolean(isHit);
		toServer.writeUTF(hitShip);
		toServer.writeBoolean(isDestroyed);
		toServer.writeBoolean(isGameOver);
		System.out.println("sent" + isHit);
	}//end receiveCoord()
	
	public void receiveIsHit() throws IOException{
		boolean isHit = fromServer.readBoolean();
		String hitShip = fromServer.readUTF();
		boolean isDestroyed = fromServer.readBoolean();
		boolean isGameOver = fromServer.readBoolean();
		
		System.out.println("rec " + isHit + " " + hitShip);
		////CHANGE COLOR OF SHIP HIT//////////////////////////////////////////
		if(isHit){
			if(player == 1)
				topGrid[rowSelected][colSelected].setFill(Color.RED);
			if(player == 2)
				topGrid[rowSelected][colSelected].setFill(Color.RED);
			
			switch(hitShip) {
			case("Battleship"):
				battleshipLbl.setFill(Color.ORANGE);
				break;
			case("Submarine"):
				submarineLbl.setFill(Color.ORANGE);
				break;
			case("Destroyer"):
				destroyerLbl.setFill(Color.ORANGE);
				break;
			case("Patrol Boat"):
				patrolBoatLbl.setFill(Color.ORANGE);
				break;
			default:
				break;
			}
		}
		else{
			if(player == 1)
				topGrid[rowSelected][colSelected].setFill(Color.DARKBLUE);	
			if(player == 2)
				topGrid[rowSelected][colSelected].setFill(Color.DARKBLUE);		
		}
		
		//if else for isDestroyed Coloring
		if(isDestroyed){
			switch(hitShip) {
			case("Battleship"):
				battleshipLbl.setFill(Color.RED);
				break;
			case("Submarine"):
				submarineLbl.setFill(Color.RED);
				break;
			case("Destroyer"):
				destroyerLbl.setFill(Color.RED);
				break;
			case("Patrol Boat"):
				patrolBoatLbl.setFill(Color.RED);
				break;
			default:
				break;
			}
		}
		if(isGameOver) { //i won//
			Platform.runLater(() -> { 
				gameWonAlert.showAndWait();
	        });
		}
	}//end receiveIsHit()
	
	public void isDestroyed(Ship s){
		if(s.getCoordinates().isEmpty()) {
			isDestroyed = true;
		}
		else
			isDestroyed = false;
	}//isDestroyed
	
	/*
	 * Send the coordinate choice to the server
	 */
	public void sendCoord() throws IOException{
		toServer.writeInt(rowSelected);
		toServer.writeInt(colSelected);
	}//end sentCoord()
	
	/*
	 * Thread waits for a move
	 */
	private void waitForAction() throws InterruptedException {
	    while (waiting) {
	      Thread.sleep(100);
	    }
	    waiting = true;
	 }//end waitForAction()
	
	/*
	 * determines if coordinate received from opponent is a hit or miss
	 */
	private boolean isHit(/*ArrayList<Ship> list, */int row, int col) throws IOException{
		Coordinate c = new Coordinate(row, col);
		boolean isHit = false;
		for(Ship s: ships){
			if(s.getCoordinates().contains(c)){
				isHit = true;				
				//remove coord from ship list
				s.removeCoordinate(c);
				System.out.print("REMOVED COORDINATE: " + c.toString() + s.toString());
				hitShip = s.getName();
				isDestroyed(s);
			}//end if
		}//end for
		return isHit;
		
	}//end isHit()
	
	/*
	 * drawGrid() will draw a 2D grid on a specified pane with specified starting x, y coordinates
	 */
	public void drawGrid(Rectangle[][] a, Pane p, int x, int y){
		//add Rectangles to topGrid and display them on pane
		for(int i = 0; i < a.length; i++ ){
			for(int j = 0; j < a[0].length; j++){
				a[i][j] = new Rectangle(x, y, 25, 25);
				a[i][j].setFill(Color.LIGHTBLUE);
				a[i][j].setStroke(Color.BLACK);
				p.getChildren().add(a[i][j]);
				//place numbers on top row of squares
				if(i == 0 && j > 0 && j < 11){
					a[i][j].setFill(Color.LIGHTGRAY);
					Text t = new Text(j + "");
					t.setWrappingWidth(13);
					t.setTextAlignment(TextAlignment.CENTER);
					//position text in center of square
					t.setX(a[i][j].getX() + a[i][j].getWidth()/2 - t.getWrappingWidth() / 2);
					t.setY(a[i][j].getY() + a[i][j].getHeight()/2 + t.getWrappingWidth()/2 - 2);
					p.getChildren().add(t);
				}//end if 
				if(j == 0 && i > 0 && i < 11){
					a[i][j].setFill(Color.LIGHTGRAY);
					Text t = new Text(i + "");
					t.setWrappingWidth(13);
					t.setTextAlignment(TextAlignment.CENTER);
					//position text in center of square
					t.setX(a[i][j].getX() + a[i][j].getWidth()/2 - t.getWrappingWidth() / 2);
					t.setY(a[i][j].getY() + a[i][j].getHeight()/2 + t.getWrappingWidth()/2 - 2);
					p.getChildren().add(t);
				}//end if
				//add width of rectangles to x to move next rectangle to be placed over
				x += a[i][j].getWidth();
			}//end inner for
			//set starting point x back to 10
			x = 10;
			//add height of rectangles to starting point y to move down one row
			y += a[i][0].getHeight();
		}//end outer for
		a[0][0].setFill(Color.LIGHTGRAY);
	}//end drawGrid()
	
	public void shipPlacement(){
		VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);
		Label info = new Label("Welcome to Battleship");
		info.setTextAlignment(TextAlignment.CENTER);
		Button randomShips = new Button("Place Ships Randomly");
		Button placeShips = new Button("Choose Ship Positions");
		Button done = new Button("Done");
		vbox.getChildren().addAll(info, randomShips, placeShips, done);
				
		Stage shipPlacementStage = new Stage();
		Scene scene = new Scene(vbox, 300, 170);
		shipPlacementStage.setScene(scene);
		shipPlacementStage.show();	
		
		placeShips.setOnAction(e -> {
			shipPlacementStage.close();
			placeBattleship();
		});
		
		randomShips.setOnAction(e -> {
			vbox.getChildren().remove(placeShips);
			
			for (int row = 0; row < bottomGrid.length; row++){
				for(int col = 0; col < bottomGrid[0].length; col++){
					if((row > 0) && (col > 0))
						bottomGrid[row][col].setFill(Color.LIGHTBLUE);
				}
			}
			ships.clear();
			battleship = new Ship(createRandomShip(4, shipPlacementStage), "Battleship");
			placeShip(battleship);
			ships.add(battleship);
			
			submarine = new Ship(createRandomShip(3, shipPlacementStage), "Submarine");
			placeShip(submarine);
			ships.add(submarine);
			
			destroyer = new Ship(createRandomShip(3, shipPlacementStage), "Destroyer");
			placeShip(destroyer);
			ships.add(destroyer);
			
			patrolBoat = new Ship(createRandomShip(2, shipPlacementStage), "Patrol Boat");
			placeShip(patrolBoat);	
			ships.add(patrolBoat);
		});
		
		done.setOnAction(e -> {
			shipPlacementStage.close();
		});
	}//end placeShipsRandomly()
	
	public void placeBattleship(){
		GridPane pane = new GridPane();
		pane.setHgap(10);
		pane.setVgap(10);
		pane.setAlignment(Pos.CENTER);
		
		Label info = new Label("Enter coordinates to place the front of the ship, \nor place all ships randomly");
		info.setTextAlignment(TextAlignment.CENTER);
		Label x = new Label("X");
		Label y = new Label("Y");
		Label battleshipLbl = new Label("Battleship (Size: 4)");
		TextField bX = new TextField();
		bX.setPrefColumnCount(2);
		TextField bY = new TextField();
		bY.setPrefColumnCount(2);
		CheckBox bcb = new CheckBox("Vertical");
		Button placeBattleship = new Button("Place Battleship");
		//Button randomShips = new Button("Place Ships Randomly");
		pane.add(x, 1, 0);
		pane.add(y, 2, 0);
		pane.add(battleshipLbl, 0, 1);
		pane.add(bX, 1, 1);
		pane.add(bY, 2, 1);
		pane.add(bcb, 3, 1);

		BorderPane bPane = new BorderPane();
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(alert, placeBattleship);
		hbox.setAlignment(Pos.CENTER);
		bPane.setPadding(new Insets(10, 10, 10, 10));
		BorderPane.setAlignment(info, Pos.CENTER);
		bPane.setCenter(pane);
		bPane.setTop(info);
		bPane.setBottom(hbox);
		Stage battleshipStage = new Stage();
		Scene scene = new Scene(bPane, 300, 170);
		battleshipStage.setScene(scene);
		battleshipStage.show();	
		
		
		placeBattleship.setOnAction(e -> {
			try{
				battleship = new Ship(createShip(Integer.parseInt(bX.getText()), Integer.parseInt(bY.getText()), 4, bcb.isSelected(), battleshipStage), "Battleship");
				placeShip(battleship);
				ships.add(battleship);
				placeSubmarine();
			}
			catch(NumberFormatException ex){
				alert.setText("Not a valid \ncoordinate");
				bX.setText("");
				bY.setText("");
			}
		});		
	}//end placeBattleship()
	
	public void placeSubmarine(){
		alert.setText("");
		GridPane pane = new GridPane();
		pane.setHgap(10);
		pane.setVgap(10);
		pane.setAlignment(Pos.CENTER);
		
		Label info = new Label("Enter coordinates to place the front of the ship");
		info.setTextAlignment(TextAlignment.CENTER);
		Label x = new Label("X");
		Label y = new Label("Y");
		Label submarineLbl = new Label("Submarine (Size: 3)");
		TextField sX = new TextField();
		sX.setPrefColumnCount(2);
		TextField sY = new TextField();
		sY.setPrefColumnCount(2);
		CheckBox scb = new CheckBox("Vertical");
		Button placeSubmarine = new Button("Place Submarine");
		pane.add(x, 1, 0);
		pane.add(y, 2, 0);
		pane.add(submarineLbl, 0, 1);
		pane.add(sX, 1, 1);
		pane.add(sY, 2, 1);
		pane.add(scb, 3, 1);
		
		BorderPane bPane = new BorderPane();
		bPane.setPadding(new Insets(10, 10, 10, 10));
		BorderPane.setAlignment(info, Pos.CENTER);
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(alert, placeSubmarine);
		hbox.setAlignment(Pos.CENTER);
		bPane.setCenter(pane);
		bPane.setTop(info);
		bPane.setBottom(hbox);
		BorderPane.setAlignment(placeSubmarine, Pos.CENTER_RIGHT);
		Stage submarineStage = new Stage();
		Scene scene = new Scene(bPane, 300, 160);
		submarineStage.setScene(scene);
		submarineStage.show();	
		
		placeSubmarine.setOnAction(e -> {
			try{
				submarine = new Ship(createShip(Integer.parseInt(sX.getText()), Integer.parseInt(sY.getText()), 3, scb.isSelected(), submarineStage), "Submarine");
				placeShip(submarine);
				ships.add(submarine);
				placeDestroyer();
			}
			catch(NumberFormatException ex){
				alert.setText("Not a valid \ncoordinate");
				sX.setText("");
				sY.setText("");
			}
		});
	}//end placeSubmarine()
	
	public void placeDestroyer(){
		alert.setText("");
		GridPane pane = new GridPane();
		pane.setHgap(10);
		pane.setVgap(10);
		pane.setAlignment(Pos.CENTER);
		
		Label info = new Label("Enter coordinates to place the front of the ship");
		info.setTextAlignment(TextAlignment.CENTER);
		Label x = new Label("X");
		Label y = new Label("Y");
		Label submarineLbl = new Label("Destroyer (Size: 3)");
		TextField dX = new TextField();
		dX.setPrefColumnCount(2);
		TextField dY = new TextField();
		dY.setPrefColumnCount(2);
		CheckBox dcb = new CheckBox("Vertical");
		Button placeDestroyer = new Button("Place Destroyer");
		pane.add(x, 1, 0);
		pane.add(y, 2, 0);
		pane.add(submarineLbl, 0, 1);
		pane.add(dX, 1, 1);
		pane.add(dY, 2, 1);
		pane.add(dcb, 3, 1);
		
		BorderPane bPane = new BorderPane();
		bPane.setPadding(new Insets(10, 10, 10, 10));
		BorderPane.setAlignment(info, Pos.CENTER);
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(alert, placeDestroyer);
		hbox.setAlignment(Pos.CENTER);
		bPane.setCenter(pane);
		bPane.setTop(info);
		bPane.setBottom(hbox);
		BorderPane.setAlignment(placeDestroyer, Pos.CENTER_RIGHT);
		Stage destroyerStage = new Stage();
		Scene scene = new Scene(bPane, 300, 140);
		destroyerStage.setScene(scene);
		destroyerStage.show();
		
		placeDestroyer.setOnAction(e -> {
			try{
				destroyer = new Ship(createShip(Integer.parseInt(dX.getText()), Integer.parseInt(dY.getText()), 3, dcb.isSelected(), destroyerStage), "Destroyer");
				placeShip(destroyer);
				ships.add(destroyer);
				placePatrolBoat();
			}
			catch(NumberFormatException ex){
				alert.setText("Not a valid \ncoordinate");
				dX.setText("");
				dY.setText("");
			}
		});
	}//end placeDestroyer()
	
	public void placePatrolBoat(){
		alert.setText("");
		GridPane pane = new GridPane();
		pane.setHgap(10);
		pane.setVgap(10);
		pane.setAlignment(Pos.CENTER);
		
		Label info = new Label("Enter coordinates to place the front of the ship");
		info.setTextAlignment(TextAlignment.CENTER);
		Label x = new Label("X");
		Label y = new Label("Y");
		Label submarineLbl = new Label("PatrolBoat (Size: 2)");
		TextField pX = new TextField();
		pX.setPrefColumnCount(2);
		TextField pY = new TextField();
		pY.setPrefColumnCount(2);
		CheckBox pcb = new CheckBox("Vertical");
		Button placePatrolBoat = new Button("Place Patrol Boat");
		pane.add(x, 1, 0);
		pane.add(y, 2, 0);
		pane.add(submarineLbl, 0, 1);
		pane.add(pX, 1, 1);
		pane.add(pY, 2, 1);
		pane.add(pcb, 3, 1);
		
		BorderPane bPane = new BorderPane();
		bPane.setPadding(new Insets(10, 10, 10, 10));
		BorderPane.setAlignment(info, Pos.CENTER);
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(alert, placePatrolBoat);
		hbox.setAlignment(Pos.CENTER);
		bPane.setCenter(pane);
		bPane.setTop(info);
		bPane.setBottom(hbox);
		BorderPane.setAlignment(placePatrolBoat, Pos.CENTER_RIGHT);
		Stage patrolBoatStage = new Stage();
		Scene scene = new Scene(bPane, 300, 140);
		patrolBoatStage.setScene(scene);
		patrolBoatStage.show();
		
		placePatrolBoat.setOnAction(e -> {
			try{
				patrolBoat = new Ship(createShip(Integer.parseInt(pX.getText()), Integer.parseInt(pY.getText()), 2, pcb.isSelected(), patrolBoatStage), "Patrol Boat");
				placeShip(patrolBoat);
				ships.add(patrolBoat);
			}
			catch(NumberFormatException ex){
				alert.setText("Not a valid \ncoordinate");
				pX.setText("");
				pY.setText("");
			}
		});
	}//end placePatrolBoat()
	
	/*
	 * createShip() will create an ArrayList of Coordinates with the x, y coordinates the user 
	 * entered, and the size of the ship. If vertical is false the ship will be placed horizontally. 
	 * Method returns an ArrayList of coordinates to create the ship object
	 */
	public ArrayList<Coordinate> createShip(int x, int y, int size, boolean vertical, Stage stage){
		ArrayList<Coordinate> ship = new ArrayList<Coordinate>();
		//calculate remaining coordinates for ship using coordinate user entered
		Coordinate c1 = new Coordinate(x, y);
		boolean shipCreated = false;
		while(!shipCreated){
			if(vertical){
				for(int i = 0; i < size; i++){
					Coordinate c = new Coordinate(c1.getCol() + i, c1.getRow());
					if(!overlaps(ships, c) && !(offGrid(c))){
						ship.add(c);
						System.out.println("v added " + c.toString());
						alert.setText("");
						if(i == size - 1)
						shipCreated = true;
					}
					else{
						alert.setText("Ship overlaps another \nship or is off grid");
						ship.clear();
						break;
					}
				}
			}//end if vertical
			if(!vertical){ 
				for(int i = 0; i < size; i++){
					Coordinate c = new Coordinate(c1.getCol(), c1.getRow() + i);
					if(!overlaps(ships, c) && !offGrid(c)){
						ship.add(c);
						System.out.println("h added " + c.toString());
						if(i == size - 1)
						shipCreated = true;
					}
					else{
						alert.setText("Ship overlaps another \nship or is off grid");
						ship.clear();
						break;
					}
				}
			}//end if horizontal
			if(!shipCreated){
				stage.close();
				ship.clear();
				c1 = getNewCoordinate();
			}
		}//end while
		stage.close();
		System.out.println(ship.toString() + vertical);
		return ship;
	}//end createShip()
	
	/*
	 * getNewCoordinate() allows the user to enter a new coordinate if they entered one that overlaps with a ship
	 * or is off the grid the first time
	 */
	public Coordinate getNewCoordinate(){
		GridPane pane = new GridPane();
		pane.setHgap(10);
		pane.setVgap(10);
		pane.setAlignment(Pos.CENTER);
		Stage newCoordStage = new Stage();
		Label info = new Label("Enter a new coordinate");
		info.setTextAlignment(TextAlignment.CENTER);
		Label x = new Label("X");
		Label y = new Label("Y");
		Label newCoordLbl = new Label("New Coordinate");
		TextField X = new TextField();
		X.setPrefColumnCount(2);
		TextField Y = new TextField();
		Y.setPrefColumnCount(2);
		Button save = new Button("Save");
		save.setOnAction(e -> {
			Stage stage = (Stage) save.getScene().getWindow();
		    stage.close();
		});	
		
		pane.add(x, 1, 0);
		pane.add(y, 2, 0);
		pane.add(newCoordLbl, 0, 1);
		pane.add(X, 1, 1);
		pane.add(Y, 2, 1);
		
		BorderPane bPane = new BorderPane();
		bPane.setPadding(new Insets(10, 10, 10, 10));
		BorderPane.setAlignment(info, Pos.CENTER);
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(alert, save);
		hbox.setAlignment(Pos.CENTER);
		bPane.setCenter(pane);
		bPane.setTop(info);
		bPane.setBottom(hbox);
		BorderPane.setAlignment(save, Pos.CENTER_RIGHT);
		//Stage newCoordStage = new Stage();
		newCoordStage.setTitle("New Coordinate");
		Scene scene = new Scene(bPane, 300, 160);
		newCoordStage.setScene(scene);
		newCoordStage.showAndWait();
		Coordinate c = new Coordinate(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()));
		return c;
	}//end getNewPoint
	
	/*
	 * createRandomShip() will create a ship with a specified size with a random position
	 */
	public ArrayList<Coordinate> createRandomShip(int size, Stage stage){
		ArrayList<Coordinate> randomShip = new ArrayList<Coordinate>();
		Random rand = new Random();
		Boolean shipCreated = false;
		Coordinate c1 = new Coordinate(rand.nextInt(9) + 1,  rand.nextInt(9) + 1);
		int vertical = rand.nextInt(2);
		while(!shipCreated){
			if(vertical == 1){
				for(int i = 0; i < size; i++){
					Coordinate c = new Coordinate(c1.getCol() + i, c1.getRow());
					if(!overlaps(ships, c) && !(offGrid(c))){
						randomShip.add(c);
						System.out.println("v added " + c.toString());
						if(i == size - 1)
						shipCreated = true;
					}
					else{
						randomShip.clear();
						break;
					}
				}
			}//end if vertical
			if(vertical == 0){ 
				for(int i = 0; i < size; i++){
					Coordinate c = new Coordinate(c1.getCol(), c1.getRow() + i);
					if(!overlaps(ships, c) && !offGrid(c)){
						randomShip.add(c);
						System.out.println("h added " + c.toString());
						if(i == size - 1)
						shipCreated = true;
					}
					else{
						randomShip.clear();
						break;
					}
				}
			}//end if horizontal
			if(!shipCreated){
				//stage.close();
				randomShip.clear();
				c1 = new Coordinate(rand.nextInt(9) + 1,  rand.nextInt(9) + 1);
			}
		}//end while
		//stage.close();
		System.out.println(randomShip.toString() + vertical);
		return randomShip;
	}//end createRandomShip()
	
	/*
	 * overlaps() returns true if a coordinate in a ship overlaps with another ship
	 */
	public boolean overlaps(ArrayList<Ship> list, Coordinate c){
		boolean overlaps = false;
		if(list.isEmpty())
			return overlaps;
		
		for(Ship s: list){
			if(s.getCoordinates().contains(c)){
				overlaps = true;
			}
		}
		return overlaps;
	}//end overlaps()
	
	/*
	 * offGrid() returns true if a coordinate is placed off the grid
	 */
	public boolean offGrid(Coordinate c){
		boolean offGrid = false;
		if(c.getRow() > 10 || c.getRow() < 1)
			offGrid = true;
		if(c.getCol() > 10 || c.getCol() < 1)
			offGrid = true;
			
		return offGrid;
	}//end offGrid
	
	/*
	 * placeShip takes in a Ship object as a parameter and loops through the bottomGrid. When it comes to 
	 * a square that has matching coordinates with a part of the ship, it will place the ship part by
	 * coloring the square gray
	 */
	public void placeShip(Ship s){
		try {
			for(int i = 0; i < bottomGrid.length ; i++){
				for(int j = 0; j < bottomGrid[0].length; j++){
					for (int k = 0; k < s.getCoordinates().size(); k++){
						if(i == s.getCoordinates().get(k).getCol() && 
						   j == s.getCoordinates().get(k).getRow()){
							bottomGrid[j][i].setFill(Color.DIMGRAY);
						}
					}
				}
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}//end placeShip()
	
	public static void main(String[] args) {
		launch(args);
	}//end main()
}//end class BattleshipClient