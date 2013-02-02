package rit.eyeTrackingAPI.EyeTrackerUtilities.udpClients;

import rit.eyeTrackingAPI.DataConstructs.GazePoint;

/**
 * An abstract class for communicating and receiving gaze sample points from an eye tracker via UDP.
 * @author Corey Engelman
 *
 */
public abstract class EyeTrackerClient extends Thread
{	
	/**
	 * Represents the current sample point from the eye tracker.
	 */
	protected GazePoint cursor;
   
	/**
	 * A flag for whether or not this client object is connected to the port the eye tracker will be sending points to.
	 * Does not guarantee connection with the eye tracker. 
	 */
	protected boolean connected = false;
	
	/**
	 * Creates a new eye Tracker client with a reference to a GazePoint object
	 * @param cursor
	 */
	public EyeTrackerClient( GazePoint cursor)
   {
		this.cursor = cursor;
		this.setName("Comm Thread");
	}
	
   /**
    * The primary method of execution for this client thread.
    */
	@Override
	public void run() 
   {
      clientOperation();
	}
	
	/**
	 * A method that connects the client object to the port that it will be receiving coordinates from the eye tracker on.
	 */
	public abstract void connect();
	
	/**
	 * The operation specific to receiving coordinates from the eye tracker and passing them on to the filter should be implemented
	 * in this method.
	 */
	protected abstract void clientOperation();
	
	/**
	 * A method that disconnects the client object from the socket it is receiving input from the eye tracker on.
	 */
	public abstract void disconnect();
   
	/**
	 * An accessor for the flag "connected"
	 * @return - the value of the boolean connected
	 */
	public abstract boolean isConnected();
   
	/**
	 * Gracefully stops the loop in the threads run method by changing the stop flag.
	 */
	public abstract void requestStop();
   
	/**
	 * Toggle the eye tracker on/off without disconnecting.
	 */
	public abstract void toggle();
	
}
