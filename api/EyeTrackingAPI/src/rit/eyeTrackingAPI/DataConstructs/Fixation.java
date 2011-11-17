package rit.eyeTrackingAPI.DataConstructs;


import java.awt.Point;

/**
 * This class represents a fixation, this can be useful when recording data for analysis later.
 * @author Corey Engelman
 *
 */
public class Fixation {
	/**
	 * how many samples the user was "fixated" at this point
	 */
	private int cycles; 
	
	/**
	 * the coordinates of the fixation
	 */
	private Point coordinates; 

/*	public Fixation(){
		cycles = 1;
	}*/
	
	/**
	 * Constructs a new fixation with the specified coordinates and increments cycles to 1.
	 * @param coordinates
	 */
	public Fixation(Point coordinates){
		this.coordinates = coordinates;
		cycles = 1;
	}
	
/*	public void setCoordinates(int x, int y){
		coordinates = new Point(x,y);
	}*/
	
	/**
	 * Increments cycles by one.
	 */
	public void incrementCycles(){
		cycles++;
	}
	
	/**
	 * The coordinates that this fixation refers to.
	 * @return
	 */
	public Point getCoordinates(){
		return this.coordinates;
	}
	
	/**
	 * Accessor for the field "cycles"
	 * @return the number of samples that the user has been fixated at the specified coordinate
	 */
	public int getCycleCount(){
		return this.cycles;
	}
	
}
