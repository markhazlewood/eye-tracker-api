/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rit.eyeTrackingAPI.EyeTrackerUtilities.udpClients;

import java.awt.Point;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rit.eyeTrackingAPI.DataConstructs.GazePoint;

/**
 *
 * @author mhazlewood
 */
public class EyeTrackerClientSimulator extends EyeTrackerClient
{
   // <editor-fold defaultstate="expanded" desc="Constructor(s)">
   
   private ArrayList<SimulatedGazePoint> mOriginalGazePoints;
   private ArrayList<SimulatedGazePoint> mInterpolatedGazePoints;
   private boolean mActive = true;
   private short mTimeOnPoints_milliseconds = 0;
   
   private boolean mInterpolate = false;
   
   /**
    * Constructor
    * @param cursor 
    * @param timeOnPoints 
    * @param interpolate 
    */
   public EyeTrackerClientSimulator(GazePoint cursor, short timeOnPoints, boolean interpolate)
   {
      super(cursor);
      this.setName("Eye Tracker Simulator");
      
      this.mTimeOnPoints_milliseconds = timeOnPoints;
      this.mInterpolate = interpolate;
   }
   
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
   
   public EyeTrackerClientSimulator(GazePoint cursor, String gazePathFilePath, short timeOnPoints, boolean interpolate)
   {
      this(cursor, timeOnPoints, interpolate);
      
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
         int x = point.getPoint().x + (int)((Math.random() * jitterValue) - (jitterValue/2.0));
         int y = point.getPoint().y + (int)((Math.random() * jitterValue) - (jitterValue/2.0));
         
         SimulatedGazePoint newPoint = new SimulatedGazePoint(new Point(x, y), point.getTimeOnPoint_milliseconds());
         mInterpolatedGazePoints.add(newPoint);
      }
   }

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
            pointIndex = (pointIndex == mInterpolatedGazePoints.size()-1) ? 0 : pointIndex+1;

            // Send it out
            this.cursor.setCoordinates(currentPoint.getPoint().x, currentPoint.getPoint().y);

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
