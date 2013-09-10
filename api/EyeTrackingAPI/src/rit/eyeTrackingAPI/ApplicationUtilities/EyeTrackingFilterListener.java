package rit.eyeTrackingAPI.ApplicationUtilities;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionListener;

import rit.eyeTrackingAPI.SmoothingFilters.Filter;

/**
 * A thread-based class that continuously polls a {@link Filter} for its latest
 * filtered gaze point on a dedicated thread. When a new filtered point is 
 * discovered the {@link #newPoint(Point)} method is called.
 *
 * @author Corey Engelman
 * 
 * @see Filter
 * @see #newPoint(Point)
 *
 */
public abstract class EyeTrackingFilterListener
{
   protected Thread mPollingThread;
   
   protected boolean mCursorVisible = true;
   protected int mScreenWidth;
   protected GraphicsDevice[] mDisplayDevices;
   protected int mActiveScreenIndex = 1;
   protected Point canvasPoint;
   protected int canvasWidth;
   protected int canvasHeight;
   protected final Filter mFilter;
   protected Point pointOnCanvas;
   protected boolean shouldStop;
   protected boolean mDrawGazePoints = false;
   protected boolean printExceptions = true;
   protected boolean ignoreExceptions = true;
   protected ActionListener mActionListener;
   protected Boolean eyeTracking = false;
   protected boolean testMode = false;

   /**
    * Takes in the MTApplication, the filter, the ActionListener that will be
    * handling its events, and a boolean that says whether or not to paint
    * fixations.
    *
    * @param filter The filter of interest, where new points will continuously 
    * be polled.
    * @param actionListener A listener to notify when a new point is available. 
    * Can be null if desired. Implementing classes should check to see if this
    * member is set and call actions on it appropriately from the {@link #newPoint(Point)}
    * method.
    * @param drawGazePoints If true, gaze points identified by this listener
    * will be drawn to the screen.
    * @param screenIndex Zero-based index of the screen being used. Default is 0.
    */
   public EyeTrackingFilterListener(Filter filter, 
                                    ActionListener actionListener,
                                    boolean drawGazePoints,
                                    int screenIndex)
   {
      mActionListener = actionListener;
      mFilter = filter;
      mDrawGazePoints = drawGazePoints;
      mActiveScreenIndex = screenIndex;

      GraphicsDevice[] screenDevices = 
              GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();      
      GraphicsConfiguration displayConf = 
              screenDevices[mActiveScreenIndex].getDefaultConfiguration();

      mScreenWidth = displayConf.getBounds().width;

   }

   /*
    * A function for updating cursor coordinates and translating them from
    * screen coordinates to open gl coordinates.
    */
   protected abstract void updateCursorCoordinates();

   /**
    * A function for determining if a given x, y position is actually on the
    * canvas.
    * 
    * @param x The X position to test.
    * @param y The Y position to test.
    * 
    * @return True if the x, y position is on the canvas, otherwise false.
    */
   protected boolean onCanvas(int x, int y)
   {
      // set horizontal and vertical clipping
      int horizontalClipping = canvasPoint.x + canvasWidth;
      int verticalClipping = canvasPoint.y + canvasHeight;
      
      return (horizontalClipping >= x && verticalClipping >= y);
   }

   /**
    * Accessor for cursorVisible.
    *
    * @return true if the cursor should be visible false otherwise
    */
   public boolean getCursorVisibility()
   {
      return mCursorVisible;
   }

   /**
    * Mutator for the cursorVisible field.
    *
    * @param visibility - the new value of cursorVisible
    */
   public void setCursorVisibility(boolean visibility)
   {
      mCursorVisible = visibility;
   }

   /**
    * This method is repeatedly performed as new coordinates are received from
    * the eye tracker.
    *
    * @param newGazePoint - the new point from the eye tracker
    */
   protected abstract void newPoint(Point newGazePoint);

   /**
    * A Runnable object that will loop until told to stop, calling its display
    * method when a new coordinate is available and paint fixations if the
    * "paintingFixations" field is set to true.
    *
    * @author Corey Engelman
    *
    */
   public class FilterPoller implements Runnable
   {
      @Override
      public void run()
      {
         if (!testMode)
         {
            mFilter.waitForNewCoordinate();
         }

         while (!shouldStop)
         {
            if (!testMode)
            {
               synchronized (mFilter)
               {
                  Point lastGazePoint = mFilter.getLastFilteredCoordinate();
                  newPoint(lastGazePoint);

                  mFilter.notifyCoordinateRead();
                  mFilter.waitForNewCoordinate();
               }

            }
            else
            {
               newPoint(null);
            }

         }

      }

   }

   /**
    * Accessor for determining if the animation thread is running
    *
    * @return true if it is running false otherwise
    */
   public synchronized boolean isAnimating()
   {
      return (mPollingThread != null);
   }

   /**
    * Creates a new thread and renderingLoop object and then starts the thread.
    */
   public synchronized void start()
   {
      mPollingThread = new Thread(new FilterPoller());
      mPollingThread.setName("Filter Polling Thread");

      mPollingThread.start();

   }

   public synchronized boolean isEyeTracking()
   {
      return eyeTracking;
   }

   public synchronized void setEyeTracking(boolean eyeTracking)
   {
      this.eyeTracking = eyeTracking;
   }

   /**
    * Request a stop for the animation thread. Essentially sets a flag so that
    * the next time through its animation loop, it exits the loop and stops on
    * its own.
    */
   public synchronized void stop()
   {
      shouldStop = true;
      notifyAll();

      while (shouldStop && mPollingThread != null)
      {
         try
         {
            wait();
         }
         catch (InterruptedException ex)
         {
            //do nothing
         }
      }

   }

   /**
    * Returns the current location of the users eye fixation with respect to the
    * canvas.
    *
    * @return Point - pointOnCanvas
    */
   public Point getPointOnCanvas()
   {
      return this.pointOnCanvas;
   }

   public Filter getFilter()
   {
      return mFilter;
   }

   public boolean isPaintingFixations()
   {
      return mDrawGazePoints;
   }

   public void setTestMode(boolean testMode)
   {
      this.testMode = testMode;
   }

}
