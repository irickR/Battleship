//By Jessica B and Robert I

package application;

import java.util.ArrayList;

public class Ship {
	String name;
	int size;
	ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
	
	Ship(ArrayList<Coordinate> c, String name){
		for(int i = 0; i < c.size(); i++){
			coordinates.add(c.get(i));
		}
		this.name = name;
	}
	
	public boolean isHit(){
		return true;
	}
	
	public boolean isSunk(){
		return true;
	}
	
	public int getSize(){
		return size;
	}
	
	public ArrayList<Coordinate> getCoordinates(){
		return coordinates;
	}
	
	public void removeCoordinate(Coordinate c){
		coordinates.remove(c);
	}
	
	public String getName(){
		return name;
	}
	public String toString(){
		String s = getName() + " ";
		for(int i = 0; i<coordinates.size(); i++){
			s += coordinates.get(i).toString();
		}
		return s;
	}
}
