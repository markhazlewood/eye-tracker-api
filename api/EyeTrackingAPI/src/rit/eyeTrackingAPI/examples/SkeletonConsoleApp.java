package rit.eyeTrackingAPI.examples;

import java.io.Console;
import rit.eyeTrackingAPI.ApplicationUtilities.EyeTrackingApp;
import rit.eyeTrackingAPI.DataConstructs.GazePoint;
import rit.eyeTrackingAPI.EyeTrackerUtilities.udpClients.ITUGazeTrackerComm;
import rit.eyeTrackingAPI.EyeTrackerUtilities.udpClients.IViewXComm;
import rit.eyeTrackingAPI.SmoothingFilters.Filter;
import rit.eyeTrackingAPI.SmoothingFilters.FixationAndLeastSquaresFilter;

/**
 * A Java console application demonstrating a simple use of the RIT Eye Tracker API
 * 
 * @author MHazlewood
 */
public class SkeletonConsoleApp extends EyeTrackingApp
{  
   // <editor-fold defaultstate="expanded" desc="Private Members">
   
   private Filter mFilter = null;
   private GazePoint mCurrentGazeCoordinates = null;
   
   // </editor-fold>   
   
   // <editor-fold defaultstate="expanded" desc="Constants">

   private static final String USAGE = "Usage:\tSkeletonConsoleApp {none|smi|itu|mirametrix} {none|fixation_least_squares|passthrough}";
   private static final String NONE_TYPESTRING = "none";
   private static final String SMI_TYPESTRING = "smi";
   private static final String ITU_TYPESTRING = "itu";
   private static final String MIRAMETRIX_TYPESTRING = "mirametrix";
   private static final String FIXATION_LEAST_SQUARES_TYPESTRING = "fixation_least_squares";
   private static final String PASSTHROUGH_TYPESTRING = "passthrough";

   // </editor-fold>
   
   // <editor-fold defaultstate="expanded" desc="Helpers">
   
   public enum TrackerType_e
   {
      NONE,
      SMI,
      ITU,
      MIRAMETRIX
   }
   
   public enum FilterType_e
   {
      NONE,
      FIXATION_AND_LEAST_SQUARES,
      PASSTHROUGH
   }
   
   // </editor-fold>
   
   // <editor-fold defaultstate="expanded" desc="Constructor(s)">
   
   public SkeletonConsoleApp(TrackerType_e aTrackerType, FilterType_e aFilterType)
   {
      switch (aFilterType)
      {
         case FIXATION_AND_LEAST_SQUARES:
         {
            mFilter = new FixationAndLeastSquaresFilter(0);
            break;
         }
         case PASSTHROUGH:
            // Nothing for now ...
         case NONE:
         {
            mFilter = null;
            break;
         }
      }
      
      if (mFilter != null)
      {
         mCurrentGazeCoordinates = new GazePoint(mFilter);
         
         switch (aTrackerType)
         {
            case ITU:
            {
               this.eyeTrackerComm = new ITUGazeTrackerComm(mCurrentGazeCoordinates);
               break;
            }
            case SMI:
            {
               this.eyeTrackerComm = new IViewXComm(mCurrentGazeCoordinates);
               break;
            }
            case MIRAMETRIX:
               // Nothing for now ...
            case NONE:
            {
               this.eyeTrackerComm = null;
               break;
            }
         }
      }
   }
   
   // </editor-fold>

   // <editor-fold defaultstate="expanded" desc="Working Functions">

   

   // </editor-fold>
   
   // <editor-fold defaultstate="expanded" desc="Main">
   
   public static void main(String[] args)
   {      
      Console console = System.console();
      if (console != null)
      {
         if (args.length > 2)
         {            
            String trackerType = args[0].trim().toLowerCase();
            
            if (trackerType.equals(ITU_TYPESTRING))
            {
               
            }
         }
         else
         {
            console.printf(USAGE);
         }
      }
   }

   // </editor-fold>
}

