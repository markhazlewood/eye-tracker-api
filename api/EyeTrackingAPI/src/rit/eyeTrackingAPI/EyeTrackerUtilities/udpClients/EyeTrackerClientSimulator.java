/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rit.eyeTrackingAPI.EyeTrackerUtilities.udpClients;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import rit.eyeTrackingAPI.DataConstructs.GazePoint;

/**
 *
 * @author mhazlewood
 */
public class EyeTrackerClientSimulator extends EyeTrackerClient
{
   // <editor-fold defaultstate="expanded" desc="Constructor(s)">
   
   private ArrayList<SimulatedGazePoint> mInterpolatedGazePoints = 
           new ArrayList<SimulatedGazePoint>();
   private boolean mActive = false;
   private short mTimeOnPoints_milliseconds = 0;
   
   private final boolean mInterpolate = false;
   
   /**
    * Constructor
    * @param cursor 
    * @param timeOnPoints 
    */
   public EyeTrackerClientSimulator(GazePoint cursor, short timeOnPoints)
   {
      super(cursor);
      this.setName("Eye Tracker Simulator");
      
      this.mTimeOnPoints_milliseconds = timeOnPoints;
   }
   
   public EyeTrackerClientSimulator(GazePoint cursor, ArrayList<SimulatedGazePoint> gazePath, short timeOnPoints)
   {
      this(cursor, timeOnPoints);
      
      if (this.mInterpolate == true)
      {
         // Build interpolated gaze position list      
         mInterpolatedGazePoints = interpolatePoints(gazePath);
      }
      else
      {
         mInterpolatedGazePoints = gazePath;
      }
   }
   
   public EyeTrackerClientSimulator(GazePoint cursor, String gazePathFilePath, short timeOnPoints)
   {
      this(cursor, timeOnPoints);
      
      // TODO: Load gaze path from file
      
      // TODO: Build interpolated gaze position list
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
      ArrayList<SimulatedGazePoint> interpolatedList = 
              new ArrayList<SimulatedGazePoint>();
      
      // TODO: Fill in interpolatedList with points that follow rawPoints at 
      //          an interval matching mTimeOnPoints.
      
      
      return interpolatedList;
   }

   @Override
   protected void clientOperation()
   {
      SimulatedGazePoint currentPoint;
      int pointIndex = 0;
      
      while (mActive == true)
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
