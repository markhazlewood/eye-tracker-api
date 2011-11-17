package eyeTrackingTheoryProject.Controller;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import rit.eyeTrackingAPI.ApplicationUtilities.EyeTrackingMediator;
import rit.eyeTrackingAPI.SmoothingFilters.Filter;


public class MyEyeTrackingMediator extends EyeTrackingMediator 
{	
	public MyEyeTrackingMediator( Filter filter, ActionListener actionListener,
                                 boolean paintingFixations, int display) 
   {
		super(filter, actionListener, paintingFixations, display);		
	}

	/**
    * This function will be called whenever the filter owned by this class has
    * a new gaze point to report.
    * @param newUserGazePoint, the new gaze point
    */
   @Override
	protected void display(Point newUserGazePoint) 
   {
		// TODO Auto-generated method stub
		//this is repeatedly called on getting a new point out of the filter
		//the parameter is the new point
		//depending on what you wish to do it you can detect complex gaze gestures
		//detect what component they are looking at
		//then fire events to your delegator
		
		//if(lookingAtComponentX){
			this.listener.actionPerformed(new ActionEvent(this, 0, "nameOfAction"));
		//}else if(lookingAtComponentY){
			this.listener.actionPerformed(new ActionEvent(this, 0, "nameOfAnotherAction"));
		//}
		
	}

	@Override
	protected void updateCursorCoordinates() 
   {
		// TODO Auto-generated method stub

	}

}
