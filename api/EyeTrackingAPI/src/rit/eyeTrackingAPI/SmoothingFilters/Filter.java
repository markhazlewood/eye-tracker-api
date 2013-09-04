package rit.eyeTrackingAPI.SmoothingFilters;

import java.awt.Point;

/**
 * A class representing a filtering algorithm for smoothing jittery raw data
 * from the eye tracker.
 *
 * @author Corey Engelman
 *
 */
public abstract class Filter
{

   protected int filterIntensity = 0;
   protected int filterCounter = 0;
   protected boolean newCoordinateAvailable = false;
   protected Point newCoordinate;
   protected boolean coordinateRead = false;

   /**
    * Constructs a filter with the cursor to be updated, the filter intensity,
    * and type.
    *
    * @param cursor - the cursor that should be updated by this filter.
    */
   public Filter()
   {
   }

   /**
    * Constructs a filter with the cursor to be updated, the filter intensity,
    * and type.
    *
    * @param filterIntensity - the number of points taken in per filter
    * calculation
    * @param cursor - the cursor that should be updated by this filter.
    */
   public Filter(int filterIntensity)
   {
      this.filterIntensity = filterIntensity;
   }

   /**
    * Call filter with a string of tokens with new eye position data
    *
    * @param tokens - {iViewX Command string, time stamp in milli seconds, eye
    * type: l - left|r - right|b - both, left eye x position, right eye x
    * position, left eye y position, right eye y position}
    */
   public abstract void filter(int x, int y);

   public boolean newCoordinateAvailable()
   {
      boolean copy = this.newCoordinateAvailable;
      newCoordinateAvailable = false;
      return copy;
   }

   /**
    * Wait until the next coordinate is received to allow other threads to run
    */
   public synchronized void waitForNewCoordinate()
   {
      while (!newCoordinateAvailable)
      {
         try
         {
            wait();
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
      }
      coordinateRead = false;
   }

   /**
    * Changes the coordinateRead flag to true, then notifies all.
    */
   public synchronized void notifyCoordinateRead()
   {
      coordinateRead = true;
      newCoordinateAvailable = false;
      notifyAll();
      //System.out.println("notified filter");
   }

   /**
    * Access the current filtered coordinate.
    *
    * @return - the filtered gaze point
    */
   public Point getNewCoordinate()
   {
      return newCoordinate;
   }

}
