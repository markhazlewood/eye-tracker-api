package rit.eyeTrackingAPI.DataConstructs;
import rit.eyeTrackingAPI.SmoothingFilters.Filter;




/**
 * A class representing a gaze sample point with (x,y) coordinates.
 * 
 * @author Corey Engelman
 * 
 */
public class GazePoint {

	private int x; // x coordinate
	private int y; // y coordinate
	//private TexturedImage texture; // A texture object associated with this
									// cursor (used with openGL)
	private Filter filter;

	/**
	 * The constructor for the cursor class. Creates a cursor with coordinates
	 * (0,0)
	 */
	public GazePoint(Filter filter) {
		this.x = 0;
		this.y = 0;
		this.filter = filter;
		// setTexture(tex);
	}
	
	/**
	 * Accessor for the x coordinate variable
	 * 
	 * @return x
	 */

	public int getX() {
		return x;
	}

	/**
	 * Accessor for the y coordinate variable
	 * 
	 * @return y
	 */

	public int getY() {
		return y;
	}

	/**
	 * Gets the texture associated with this cursor.
	 * 
	 * @return texture
	 *//*
	public TexturedImage getTexture() {
		return texture;
	}*/

	public void setCoordinates(int x, int y) {
		this.x = x;
		this.y = y;
		filter.filter(x, y);
	}

/*	*//*
	 * Returns whether or not the coordinates have been changed.
	 * 
	 * IMPORTANT: After this method is called, positionChanged will be set to
	 * false.
	 * 
	 * @return positionChanged
	 *//*
	public boolean changed() {
		boolean temp = positionChanged; // save a copy of positionChanged to
										// return
		positionChanged = false; // set positionChanged to false
		return temp;
	}

	public void setChanged(boolean change) {
		positionChanged = change;

	}*/
}
