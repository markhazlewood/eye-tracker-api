package rit.eyeTrackingAPI.EyeTrackerUtilities.eyeTrackerClients;

import java.awt.Point;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import rit.eyeTrackingAPI.DataConstructs.GazePoint;

/**
 * This class acts as a simulator for an eye tracker. It accepts either a list of 
 * {@link SimulatedGazePoint} objects or a path to a file containing screen
 * coordinates. Those coordinates are used to simulate eye tracker data reported
 * on a dedicated thread in the same way as any other {@link EyeTrackerClient}.
 *
 * The format expected for a file containing usable gaze points is should match 
 * the following (UTF-8 encoding):
 * 
 *    x_1, y_1, duration_1
 *    x_2, y_2, duration_2
 *    x_3, y_3, duration_3
 *    x_4, y_4, duration_4
 *    ...
 * 
 * X and Y values are self-explanatory. "duration_#" indicates the amount of time
 * to wait "on that point" before sending the next point. This can be useful for
 * simulating eye tracker data at different rates.
 * 
 * This class may additionally interpolate between given screen points if specified.
 * This interpolation extrapolates on given data to match a specified constant
 * duration for all points. If this feature is used, it overrides any "duration_#" 
 * values from the original data set.
 * 
 * Finally, this simulator has the ability to add a semi-random "jitter" to its 
 * output. If a jitter value is set, the coordinate data as-sent will be modified
 * to a random value between 0 and +/- the jitter value.
 * 
 * @author Mark Hazlewood
 * 
 * @see SimulatedGazePoint
 * @see EyeTrackerClient
 */
public class EyeTrackerClientSimulator extends EyeTrackerClient
{
   // <editor-fold defaultstate="expanded" desc="Constructor(s)">
   
   private ArrayList<SimulatedGazePoint> mOriginalGazePoints;
   private ArrayList<SimulatedGazePoint> mInterpolatedGazePoints;
   private boolean mActive = true;
   
   private boolean mInterpolate = false;
   private short mTimeOnPoints_override = 0;
   
   /**
    * 
    * @param cursor 
    * @param timeOnPoints 
    * @param interpolate 
    */
   public EyeTrackerClientSimulator(GazePoint cursor, short timeOnPoints, boolean interpolate)
   {
      super(cursor);
      this.setName("Eye Tracker Simulator");
      
      this.mTimeOnPoints_override = timeOnPoints;
      this.mInterpolate = interpolate;
   }
   
   /**
    * 
    * @param cursor
    * @param gazePath
    * @param timeOnPoints
    * @param interpolate 
    */
   public EyeTrackerClientSimulator(GazePoint cursor, ArrayList<SimulatedGazePoint> gazePath, short timeOnPoints, boolean interpolate)
   {
      this(cursor, timeOnPoints, interpolate);
      
      mOriginalGazePoints = ((ArrayList<SimulatedGazePoint>)gazePath.clone());
      
      if (this.mInterpolate == true)
      {
         // Build interpolated gaze position list
         mInterpolatedGazePoints = interpolatePoints(mOriginalGazePoints);
      }
      else
      {
         mInterpolatedGazePoints = ((ArrayList<SimulatedGazePoint>)mOriginalGazePoints.clone());
      }
   }
   
   /**
    * 
    * @param cursor
    * @param gazePathFilePath
    * @param timeOnPoints
    * @param interpolate 
    */
   public EyeTrackerClientSimulator(GazePoint cursor, String gazePathFilePath, short timeOnPoints, boolean interpolate)
   {
      this(cursor, timeOnPoints, interpolate);
      loadOriginalPointsFromFile(gazePathFilePath);
      
      if (mOriginalGazePoints.size() > 0)
      {
         if (this.mInterpolate == true)
         {
            // Build interpolated gaze position list      
            mInterpolatedGazePoints = interpolatePoints(mOriginalGazePoints);
         }
         else
         {
            mInterpolatedGazePoints = ((ArrayList<SimulatedGazePoint>)mOriginalGazePoints.clone());
         }
      }
   }

   /**
    * Populates the *original* gaze point list with content from the specified
    * gaze point file.
    * 
    * @param gazePathFilePath Path to a file containing gaze data.
    */
   private void loadOriginalPointsFromFile(String gazePathFilePath)
   {
      mOriginalGazePoints = new ArrayList<>();
      Path filePath = Paths.get(gazePathFilePath);
      
      try(BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8))
      {
         String line;
         String[] values;
         int x, y;
         short timeOnPoint;
         
         while ((line = reader.readLine()) != null)
         {
            values = line.split(",");
            x = Integer.parseInt(values[0].trim());
            y = Integer.parseInt(values[1].trim());
            timeOnPoint = Short.parseShort(values[2].trim());
            
            mOriginalGazePoints.add(new SimulatedGazePoint(new Point(x, y), timeOnPoint));
         }         
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   // </editor-fold>

   @Override
   public void connect()
   {
      // Do nothing, there is nothing to connect to
   }
   
   @Override
   public void disconnect()
   {
      // Do nothing, there is nothing to disconnect from
   }
   
   @Override
   public void toggle()
   {
      // Do nothing, nothing to connect to or disconnect from
   }
   
   @Override
   public boolean isConnected()
   {
      return true;
   }
   
   /**
    * 
    * @param rawPoints
    * @return 
    */
   private ArrayList<SimulatedGazePoint> interpolatePoints(List<SimulatedGazePoint> rawPoints)
   {
      ArrayList<SimulatedGazePoint> interpolatedList = new ArrayList<>();
      
      // TODO: Fill in interpolatedList with points that follow rawPoints at 
      //          an interval matching mTimeOnPoints.
      
      
      return interpolatedList;
   }
   
   public void setJitter(int jitterValue)
   {
      try
      {
         mInterpolatedGazePoints.clear();
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
      
      for (SimulatedGazePoint point : mOriginalGazePoints)
      {
         int x = point.getPoint().x + (int)((Math.random() * (jitterValue*2)) - jitterValue);
         int y = point.getPoint().y + (int)((Math.random() * (jitterValue*2)) - jitterValue);
         
         SimulatedGazePoint newPoint = new SimulatedGazePoint(new Point(x, y), point.getTimeOnPoint_milliseconds());
         mInterpolatedGazePoints.add(newPoint);
      }
   }

   /**
    * Primary operational method for the client. Will read in points from the
    * interpolated list then exit the thread.
    */
   @Override
   protected void clientOperation()
   {
      SimulatedGazePoint currentPoint;
      int pointIndex = 0;
      
      while (mActive == true && pointIndex < mInterpolatedGazePoints.size())
      {     
         if (mInterpolatedGazePoints.size() > 0)
         {         
            // Pull out next interpolated point
            currentPoint = mInterpolatedGazePoints.get(pointIndex);
            pointIndex = pointIndex+1;

            // Send it out
            mGazePointContainer.setCoordinates( currentPoint.getPoint().x, 
                                                currentPoint.getPoint().y);

            // Wait to send out the next point
            try
            {
               Thread.sleep(currentPoint.getTimeOnPoint_milliseconds());
            }
            catch (InterruptedException ex)
            {
               ex.printStackTrace();
            }
         }
      }
   }

   @Override
   public void requestStop()
   {
      mActive = false;
   }
   
   /**
    * Container class for a point and a duration.
    */
   public class SimulatedGazePoint
   {
      private Point mPoint = new Point();
      private short mTimeOnPoint_milliseconds = 0;
      
      public SimulatedGazePoint(Point point, short timeOnPoint)
      {
         this.mPoint = point;
         this.mTimeOnPoint_milliseconds = timeOnPoint;
      }
      
      public Point getPoint()
      {
         return this.mPoint;
      }
      
      public short getTimeOnPoint_milliseconds()
      {
         return this.mTimeOnPoint_milliseconds;
      }
   }
   
}
