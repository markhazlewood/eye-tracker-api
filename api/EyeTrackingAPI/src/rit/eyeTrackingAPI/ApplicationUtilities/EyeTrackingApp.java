package rit.eyeTrackingAPI.ApplicationUtilities;
import javax.swing.JFrame;

import rit.eyeTrackingAPI.EyeTrackerUtilities.udpClients.EyeTrackerClient;


/**
 * This class represents an application using eye tracking. The class has a JFrame for displaying a user interface, 
 * an EyeTrackerClient for communicating with the eye tracker via UDP, and an EyeTrackingMediator to perform operations
 * based on data received from the tracker.
 * 
 * @author Corey Engelman
 *
 */
public abstract class EyeTrackingApp{

	/**
	 * A window for displaying a user interface, not that this does not need to be used if the application does not
	 * have a user interface. For example an eye tracking application which strictly needs to record data may not need this.
	 */
	protected JFrame mainWindow;
	
	/**
	 * An EyeTrackingClient object used for communicating with the tracker. Note that EyeTrackerClient is abstract and that
	 * one of the included implementations will need to be used. The implementation used is dependent on the eye tracker being used.
	 * Furthermore, if no implementation exists for the type of eye tracker being used, this class can be extended to make a new client
	 * program for the eye tracker being used.
	 */
	protected EyeTrackerClient eyeTrackerComm;
	
	/**
	 * An EyeTrackingMediator for performing operations based on the input from the tracker. This is an abstract class, so a custom implementation
	 * of this will need to be created based on what is to be done with the data coming from the tracker. For example, if you wish to display a cursor 
	 * where the user is looking, the custom EyeTrackingMediator should draw a cursor at the coordinates of the users gaze everytime it refreshes.
	 */
	protected EyeTrackingMediator eyeMediator;
	
}
