package application;

public class Coordinate {
	private int x;
	private int y;
	
	public Coordinate(int y, int x){
		this.x = x;
		this.y = y;
	}
	
	public int getCol(){
		return x;
	}
	
	public void setCol(int x){
		this.x = x;
	}

	public int getRow(){
		return y;
	}
	
	public void setRow(int y){
		this.y = y;
	}
	
	@Override
	public boolean equals(Object c){
		if(getCol() == ((Coordinate)c).getCol() && getRow() == ((Coordinate)c).getRow())
			return true;
		else
			return false;
	}
	
	public String toString(){
		return "(" + x + ", " + y + ")";
	}

public static void main(String[] args){
}
}