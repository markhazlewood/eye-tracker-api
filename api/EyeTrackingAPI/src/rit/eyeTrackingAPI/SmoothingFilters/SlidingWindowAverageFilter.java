package rit.eyeTrackingAPI.SmoothingFilters;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Implements a simple sliding window average filter for 2D gaze points. The
 * window size is initialized upon construction, but can also be changed at
 * runtime. X and Y values from new points are added to running totals, up to
 * the window size. An average of all current points in the window is then 
 * taken and used as the return point.
 * 
 * The goal of this filter is to help remove jitter. As such, the window size
 * must be tweaked appropriately - if it is too large the returned average will
 * be much further away from the actual point-of-regard. This problem will be 
 * compounded during large, quick changes (saccades). So the window size should
 * be kept relatively small compared with the gaze sampling rate.
 * 
 * @author Mark Hazlewood
 */
public class SlidingWindowAverageFilter extends Filter
{
   // <editor-fold defaultstate="expanded" desc="Private Members">

   private int mWindowSize = 25;
   private Queue<Point> mPointsInWindow = new LinkedList<>();
   private int mCurrentWindowSize = 0;    // used to avoid many .size() calls
   
   private int mCurrentTotalX = 0;
   private int mCurrentTotalY = 0;
   
   private double mCurrentAverageX = 0.0;
   private double mCurrentAverageY = 0.0;

   // </editor-fold>

   // <editor-fold defaultstate="expanded" desc="Constructor(s)">

   public SlidingWindowAverageFilter(int windowSize)
   {
      mWindowSize = windowSize;
   }

   // </editor-fold>

   // <editor-fold defaultstate="expanded" desc="Working Functions">

   @Override
   public synchronized void filter(int x, int y)
   {  
      // Add a new point to the window, for reference during removal
      mPointsInWindow.add(new Point(x, y));
      ++mCurrentWindowSize;
      
      // Maintain a running total value for the window in both X and Y
      mCurrentTotalX += x;
      mCurrentTotalY += y;
      
      // If the window size has been reached, pop off the first point and remove
      // it's values from the running totals
      while (mCurrentWindowSize > mWindowSize)
      {
         Point first = mPointsInWindow.remove();
         mCurrentTotalX -= first.getX();
         mCurrentTotalY -= first.getY();
         --mCurrentWindowSize;
      }
      
      // Determine a new average using the current totals
      mCurrentAverageX = (double)mCurrentTotalX / (double)mCurrentWindowSize;
      mCurrentAverageY = (double)mCurrentTotalY / (double)mCurrentWindowSize;
      
      // Set the point and notify listeners
      mLastFilteredCoordinate = new Point((int)mCurrentAverageX, (int)mCurrentAverageY);
      mNewCoordinateAvailable = true;
      notifyAll();
      
      while (!mLatestCoordinateHasBeenRead)
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
   }

   // </editor-fold>


   // <editor-fold defaultstate="expanded" desc="Properties">

   public void setWindowSize(int size)
   {
      mWindowSize = size;
   }

   // </editor-fold>
}
