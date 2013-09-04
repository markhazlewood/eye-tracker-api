package rit.eyeTrackingAPI.ApplicationUtilities;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionListener;

import rit.eyeTrackingAPI.SmoothingFilters.Filter;


/*import rit.hbir.multitouch.*;
 import rit.hbir.multitouch.scenes.ImageScene;

 import org.mt4j.MTApplication;
 import org.mt4j.components.MTCanvas;
 import org.mt4j.components.MTComponent;
 import org.mt4j.components.TransformSpace;
 import org.mt4j.sceneManagement.IPreDrawAction;
 import org.mt4j.util.math.Vector3D;

 import rit.hbir.control.eventHandling.CBIRActionListener;
 import rit.hbir.graphics.filter.Filter;*/
/**
 * A graphics rendering thread class. Handles performing operations based on
 * data coming from the eye tracker.
 *
 * @author cde7825
 *
 */
public abstract class EyeTrackingMediator
{

   //protected volatile ArrayList drawables = new ArrayList();
   protected Runnable runnable;
   protected Thread thread;
   protected boolean cursorVisible = true;
   protected int screenWidth;
   protected GraphicsDevice[] displays;
   protected int stimulusDisplay = 1;
   protected Point canvasPoint;
   protected int canvasWidth;
   protected int canvasHeight;
   protected final Filter filter;
   protected Point pointOnCanvas;
   protected boolean shouldStop;
   protected boolean paintingFixations = false;
   protected boolean printExceptions = true;
   protected boolean ignoreExceptions = true;
   protected ActionListener listener;
   protected Boolean eyeTracking = false;
   protected boolean testMode = false;

   /**
    * Takes in the MTApplication, the filter, the ActionListener that will be
    * handling its events, and a boolean that says whether or not to paint
    * fixations.
    *
    * @param mtApp
    * @param filter
    * @param actionListener
    * @param paintingFixations
    * @param display - the display being used
    */
   public EyeTrackingMediator(Filter filter, ActionListener actionListener, boolean paintingFixations, int display)
   {
      this.listener = actionListener;
      this.filter = filter;
      this.paintingFixations = paintingFixations;
      this.stimulusDisplay = display;

      GraphicsEnvironment ge = GraphicsEnvironment
              .getLocalGraphicsEnvironment();
      GraphicsDevice[] displays = ge.getScreenDevices();
      GraphicsConfiguration displayConf = displays[stimulusDisplay]
              .getDefaultConfiguration();

      screenWidth = displayConf.getBounds().width;

   }

   /*
    * A function for updating cursor coordinates and translating them from
    * screen coordinates to open gl coordinates.
    */
   protected abstract void updateCursorCoordinates();

   /*
    * A function for determining if the cursor position is actually on the
    * canvas.
    * 
    * @param horizontalClipping - horizontal maximum
    * 
    * @param verticalClipping - vertical maximum
    * 
    * @return true/false
    */
   protected boolean onCanvas(int x, int y)
   {
      // set horizontal and vertical clipping
      int horizontalClipping = canvasPoint.x + canvasWidth;
      int verticalClipping = canvasPoint.y + canvasHeight;

      if (horizontalClipping >= x && verticalClipping >= y)
      {
         return true;
      }
      return false;
   }

   /**
    * Accessor for cursorVisible.
    *
    * @return true if the cursor should be visible false otherwise
    */
   public boolean getCursorVisibility()
   {
      return cursorVisible;
   }

   /**
    * Mutator for the cursorVisible field.
    *
    * @param visibility - the new value of cursorVisible
    */
   public void setCursorVisibility(boolean visibility)
   {
      cursorVisible = visibility;
   }

   /**
    * This method is repeatedly performed as new coordinates are received from
    * the eye tracker.
    *
    * @param newGazePoint - the new point from the eye tracker
    */
   protected abstract void display(Point newGazePoint);

   /**
    * A Runnable object that will loop until told to stop, calling its display
    * method when a new coordinate is available and paint fixations if the
    * "paintingFixations" field is set to true.
    *
    * @author Corey Engelman
    *
    */
   public class RenderingLoop implements Runnable
   {

      @Override
      public void run()
      {

         if (!testMode)
         {
            filter.waitForNewCoordinate();
         }

         while (!shouldStop)
         {
            if (!testMode)
            {
               synchronized (filter)
               {
                  Point newGazePoint = filter.getNewCoordinate();
                  display(newGazePoint);

                  filter.notifyCoordinateRead();
                  filter.waitForNewCoordinate();
               }

            }
            else
            {
               display(null);
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
      return (thread != null);
   }

   /**
    * Creates a new thread and renderingLoop object and then starts the thread.
    */
   public synchronized void start()
   {
      if (runnable == null)
      {
         runnable = new RenderingLoop();

         thread = new Thread(runnable);
         thread.setName("Rendering Thread");

         thread.start();

      }

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

      while (shouldStop && thread != null)
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
      return filter;
   }

   public boolean isPaintingFixations()
   {
      return paintingFixations;
   }

   public void setTestMode(boolean testMode)
   {
      this.testMode = testMode;
   }

}
