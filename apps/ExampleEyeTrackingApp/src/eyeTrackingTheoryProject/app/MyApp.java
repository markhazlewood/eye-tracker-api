package eyeTrackingTheoryProject.app;
import eyeTrackingTheoryProject.Controller.MyDelegator;
import eyeTrackingTheoryProject.Controller.MyEyeTrackingMediator;
import javax.swing.JFrame;
import rit.eyeTrackingAPI.ApplicationUtilities.*;
import rit.eyeTrackingAPI.DataConstructs.*;
import rit.eyeTrackingAPI.EyeTrackerUtilities.udpClients.*;
import rit.eyeTrackingAPI.SmoothingFilters.*;


public class MyApp extends EyeTrackingApp
{	
	private GazePoint userGazeCoordinates;
	private Filter smoothingFilter;
	
	public MyApp()
   {  
		//use one of my predefined filters to save time, this one in my experience works the best
		smoothingFilter = new FixationAndLeastSquaresFilter(2);
		
		//the gaze point needs to be initialized with a filter
		userGazeCoordinates = new GazePoint(smoothingFilter);      
		
		//comm class takes in a gaze point
      //this.eyeTrackerComm = new IViewXComm(userGazeCoordinates);
		this.eyeTrackerComm = new ITUGazeTrackerComm(userGazeCoordinates);
      this.eyeTrackerComm.connect();
      
		//NOTE: flow of events
		//comm/from eye tracker -> raw gaze coordinate -> filter -> usable,smoothed data
		
		this.mainWindow = new MyEyeTrackingAppUI();
		
		//create the mediator
		//Ignore the last two parameters, these need to be removed when I get the chance, but they
		//are not used, so just input false and 0
		this.eyeMediator = new MyEyeTrackingMediator(smoothingFilter, new MyDelegator(this.mainWindow), false, 0);
		
	}
   
   public static void main(String[] args) 
   {
      MyApp app = new MyApp();
      
      app.mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      app.mainWindow.setVisible(true);
   }
	
}


