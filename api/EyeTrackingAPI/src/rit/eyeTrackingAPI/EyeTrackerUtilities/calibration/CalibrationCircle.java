package rit.eyeTrackingAPI.EyeTrackerUtilities.calibration;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JComponent;

/**
 * This class represents a calibration circle. The circle will be white, with a red center. This circle can then
 * be moved to different places on the screen to perform a calibration using eye tracker.
 */
public class CalibrationCircle extends JComponent{

	private static final int INNER_CIRCLE_DIAMETER = 5;
	private static final int OUTER_CIRCLE_DIAMETER = 40;
	
	private int x = 0;
	private int y = 0;

	//override the paint method
	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2f));
        
        //paint outer white circle
        g2d.setPaint(new Color(250,250,250));
        g2d.fillOval(x -(OUTER_CIRCLE_DIAMETER/2), y -(OUTER_CIRCLE_DIAMETER/2), OUTER_CIRCLE_DIAMETER, OUTER_CIRCLE_DIAMETER);
        
        //paint inner red circle
        g2d.setPaint(new Color(250,0,0));
        g2d.fillOval(x - (INNER_CIRCLE_DIAMETER/2), y -(INNER_CIRCLE_DIAMETER/2), INNER_CIRCLE_DIAMETER, INNER_CIRCLE_DIAMETER);
        
	}
	
	/**
	 * Used to change the coordinates of the calibration circle
	 * @param x - the x coordinate
	 * @param y - the y coordinate
	 */
	public void setCoordinates(int x, int y){
		this.x = x;
		this.y = y;
	}

	/**
	 * Used to change the coordinates of the calibration circle
	 * @param point - the point to move the circle to
	 */
	public void setCoordinates(Point point) {
		this.x = point.x;
		this.y = point.y;
	}
		
}

