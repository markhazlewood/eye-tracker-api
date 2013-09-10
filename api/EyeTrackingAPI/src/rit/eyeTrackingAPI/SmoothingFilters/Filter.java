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
   protected int mFilterIntensity = 0;
   protected int mFilterCounter = 0;
   protected boolean mNewCoordinateAvailable = false;
   protected Point mLastFilteredCoordinate;
   protected boolean mLatestCoordinateHasBeenRead = false;

   /**
    * Constructs a filter with the cursor to be updated, the filter intensity,
    * and type.
    *
    */
   public Filter()
   {
   }

   /**
    * Constructs a filter with the cursor to be updated, the filter intensity,
    * and type.
    *
    * @param filterIntensity The number of points taken in per filter calculation
    */
   public Filter(int filterIntensity)
   {
      mFilterIntensity = filterIntensity;
   }

   /**
    * Call filter with a string of tokens with new eye position data
    *
    * @param x
    * @param y
    */
   public abstract void filter(int x, int y);

   /**
    * Called to check if a new coordinate is available.
    * 
    * @return True if a new coordinate was available since the last time this
    * method was called, otherwise false.
    */
   public boolean newCoordinateAvailable()
   {
      boolean available = mNewCoordinateAvailable;
      mNewCoordinateAvailable = false;
      
      return available;
   }

   /**
    * Suspends the current thread until the next coordinate is received to allow 
    * other threads to run
    */
   public synchronized void waitForNewCoordinate()
   {
      while (!mNewCoordinateAvailable)
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
      
      // New coordinate is available, set read state to false
      mLatestCoordinateHasBeenRead = false;
   }

   /**
    * Changes the coordinateRead flag to true, then notifies all.
    */
   public synchronized void notifyCoordinateRead()
   {
      mLatestCoordinateHasBeenRead = true;
      mNewCoordinateAvailable = false;
      notifyAll();      
   }

   /**
    * Access the last filtered gaze coordinate.
    *
    * @return the filtered gaze point in screen space
    */
   public Point getLastFilteredCoordinate()
   {
      return mLastFilteredCoordinate;
   }
}
