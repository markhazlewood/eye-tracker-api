package rit.eyeTrackingAPI.SmoothingFilters;

import java.awt.Point;

/**
 *
 * @author Mark Hazlewood
 */
public class PassthroughFilter extends Filter
{
   @Override
   public synchronized void filter(int x, int y)
   {
      mLastFilteredCoordinate = new Point(x, y);
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
   
}
