package rit.eyeTrackingAPI.SmoothingFilters;

import java.awt.Point;
import java.util.ArrayList;

import rit.eyeTrackingAPI.DataConstructs.Fixation;

/**
 * A filtering algorithm that uses a combination of of smoothing based on
 * grouping raw data into fixations if the raw data points are within a certain
 * radius and of using linear trend lines to smooth out saccades.
 *
 * @author Corey Engelman
 *
 */
public class FixationAndLeastSquaresFilter extends Filter
{

   /*
    * Fixation threshold is a pixel value representing the radius of the
    * dispersion allowed before switching to the saccade algorithm.
    */
   private static int FIXATION_THRESHOLD = 50;
   private boolean initial = true;

   /*
    * Used to specify regression order
    */
   private static final int LINEAR_FIT = 1;
   private static final int QUADRATIC_FIT = 2;
   private static final int CUBIC_FIT = 3;
   private static final int QUARTIC_FIT = 4;
   /*
    * Specifies how many points of data are drawn on the regression line
    */
   private static final int NUM_REGRESSION_REDRAW_DATA_POINTS = 3;

   private double eyeTrackerRawData[][] = null;
   private double adjustedData[][] = null;
   private Double xMin_Regression = null;
   private Double xMax_Regression = null;
   private double xIncrement_Regression;
   private int regressionIndex = 0;
   private Point current;
   private Point next;

   private ArrayList<Fixation> fixationList = new ArrayList<Fixation>();
   private Fixation currentFixation;
   private boolean beingModified;

   public FixationAndLeastSquaresFilter(int filterIntensity)
   {
      super(filterIntensity);
      eyeTrackerRawData = new double[filterIntensity][2];
      adjustedData = new double[NUM_REGRESSION_REDRAW_DATA_POINTS][2];
   }

   @Override
   public synchronized void filter(int x, int y)
   {
      double distance = 0.0;

      if (current == null)
      {
         current = new Point(x, y);
      }
      else
      {
         next = new Point(x, y);
      }

      if (next != null)
      {
         distance = Math.sqrt((Math.pow((current.x - next.x), 2) + Math.pow(
                 (current.y - next.y), 2)));
      }

      ////System.out.println("Distance = " + distance);
      ////System.out.println("Distance as int = " + (int) distance);
      if (mFilterCounter == 0)
      {
         if (initial)
         {
            mLastFilteredCoordinate = new Point(current);
            mNewCoordinateAvailable = true;
            initial = false;
            Fixation fix = new Fixation(mLastFilteredCoordinate);
            fixationList.add(fix);
            currentFixation = fix;
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
         else if (distance < FIXATION_THRESHOLD && !initial)
         {
            ////System.out.println("still a fixation");
            currentFixation.incrementCycles();
         }
         else
         {
            this.filterLeastSquares(x, y);
            ////System.out.println("now in a sacade");
         }
      }
      else
      {
         this.filterLeastSquares(x, y);
      }
   }

   private synchronized void filterLeastSquares(int x, int y)
   {

      //get [points from eye tracker
      if (mFilterCounter < mFilterIntensity)
      {
         eyeTrackerRawData[mFilterCounter][0] = x;
         // adjustedData[filterCounter][0] =
         // eyeTrackerRawData[filterCounter][0];
         eyeTrackerRawData[mFilterCounter][1] = y;
         if (xMin_Regression == null)
         {
            xMin_Regression = eyeTrackerRawData[mFilterCounter][0];
         }
         else if (eyeTrackerRawData[mFilterCounter][0] < xMin_Regression)
         {
            xMin_Regression = eyeTrackerRawData[mFilterCounter][0];
         }
         if (xMax_Regression == null)
         {
            xMax_Regression = eyeTrackerRawData[mFilterCounter][0];
         }
         else if (eyeTrackerRawData[mFilterCounter][0] > xMax_Regression)
         {
            xMax_Regression = eyeTrackerRawData[mFilterCounter][0];
         }

         mFilterCounter++;
      }

      //calculate regression line
      if (mFilterCounter == (mFilterIntensity))
      {
         double coefficients[] = Regression.linear_equation(eyeTrackerRawData, LINEAR_FIT);
         //double coefficients[] = Regression.linear_equation(eyeTrackerRawData, QUADRATIC_FIT);
         // double coefficients[] =
         // Regression.linear_equation(eyeTrackerRawData, CUBIC_FIT);
         // double coefficients[] =
         // Regression.linear_equation(eyeTrackerRawData, QUARTIC_FIT);

         xIncrement_Regression = (xMax_Regression - xMin_Regression)
                 / NUM_REGRESSION_REDRAW_DATA_POINTS;
         double currentXValue = 0.0;
         if (eyeTrackerRawData[0][0] < eyeTrackerRawData[mFilterIntensity - 1][0])
         {
            currentXValue = xMin_Regression;
         }
         else if (eyeTrackerRawData[0][0] > eyeTrackerRawData[mFilterIntensity - 1][0])
         {
            currentXValue = xMax_Regression;
         }

         for (int i = 0; i < NUM_REGRESSION_REDRAW_DATA_POINTS; i++)
         {
            adjustedData[i][0] = currentXValue;
            adjustedData[i][1] = coefficients[0];
            for (int j = 1; j < coefficients.length; j++)
            {
               adjustedData[i][1] += Math.pow(adjustedData[i][0], j)
                       * coefficients[j];
            }
            if (eyeTrackerRawData[0][0] <= eyeTrackerRawData[mFilterIntensity - 1][0])
            {
               currentXValue += xIncrement_Regression;
            }
            else if (eyeTrackerRawData[0][0] > eyeTrackerRawData[mFilterIntensity - 1][0])
            {
               currentXValue -= xIncrement_Regression;
            }

         }
         /*
          * for(double j = xMin_Regression; i < xMax_Regression;
          * i+=xIncrement_Regression){ adjustedData[(int)i][1] =
          * coefficients[0]; for(int k = 1; j < coefficients.length; j++){
          * adjustedData[i][1] +=
          * Math.pow(adjustedData[i][0],j)*coefficients[j]; }
          * 
          * }
          */
         mFilterCounter++;
      }

      if (mFilterCounter == (mFilterIntensity + 1))
      {
         for (int i = 0; i < NUM_REGRESSION_REDRAW_DATA_POINTS; i++)
         {
            //output points
			/*
             && regressionIndex < NUM_REGRESSION_REDRAW_DATA_POINTS) {*/
            if (regressionIndex == NUM_REGRESSION_REDRAW_DATA_POINTS - 1)
            {
               mLastFilteredCoordinate = new Point(
                       (int) eyeTrackerRawData[eyeTrackerRawData.length - 1][0],
                       (int) eyeTrackerRawData[eyeTrackerRawData.length - 1][1]);
               current = mLastFilteredCoordinate;
            }
            else
            {
               mLastFilteredCoordinate = new Point(
                       (int) adjustedData[regressionIndex][0],
                       (int) adjustedData[regressionIndex][1]);
            }

            mNewCoordinateAvailable = true;
            System.out.println("notifying from filterleastesquares");
            notifyAll();
            System.out.println("coordinateRead: " + mLatestCoordinateHasBeenRead);
            //System.out.println("\tfilter waiting with i: " + i);
            do
            {
               try
               {
                  this.wait();
               }
               catch (InterruptedException e)
               {
                  e.printStackTrace();
               }
               System.out.println("woke up with coordinateRead: " + mLatestCoordinateHasBeenRead);
            } while (!mLatestCoordinateHasBeenRead);
            //System.out.println("\tfilter woke up with i: " + i);
            regressionIndex++;

         }
         ////System.out.println("regressionIndex: " + regressionIndex);
         //System.out.println("!(regressionIndex < NUMREGRESSION_REDRAW_POINTS) is " + regressionIndex + "<" + NUM_REGRESSION_REDRAW_DATA_POINTS);
         //System.out.println("= " + !(regressionIndex < NUM_REGRESSION_REDRAW_DATA_POINTS));
         //System.out.println("filterCounter == (filterIntensity+1) is " + filterCounter + "==" + (filterIntensity+1));
         //System.out.println("= " + (filterCounter == (filterIntensity + 1)));
         //System.out.println("the whole thing is: " +(filterCounter == (filterIntensity + 1)
         //		&& !(regressionIndex < NUM_REGRESSION_REDRAW_DATA_POINTS)));
         if (!(regressionIndex < NUM_REGRESSION_REDRAW_DATA_POINTS))
         {

            //System.out.println("a");
            regressionIndex = 0;
            if (mFilterCounter == (mFilterIntensity + 1))
            {
               mFilterCounter = 0;
               xMin_Regression = null;
               xMax_Regression = null;
            }
            //System.out.println("b");

            Fixation fix = new Fixation(mLastFilteredCoordinate);
            boolean repeatFixation = false;
            for (Fixation f : fixationList)
            {
               if (f.getCoordinates().equals(fix.getCoordinates()))
               {
                  repeatFixation = true;
                  currentFixation = f;
                  currentFixation.incrementCycles();
               }
            }
            //System.out.println("c");

            if (!repeatFixation)
            {
               fixationList.add(fix);
               currentFixation = fix;
            }
            //System.out.println("d");

         }
      }
   }

   public void lockFixationList() throws InterruptedException
   {
      while (beingModified)
      {
         try
         {
            wait();
         }
         catch (IllegalMonitorStateException ex)
         {
         }
      }
      beingModified = true;
   }

   public void unlockFixationList()
   {
      beingModified = false;
      try
      {
         notifyAll();
      }
      catch (IllegalMonitorStateException ex)
      {
      }
   }

   public ArrayList<Fixation> getFixationsList()
   {
      return this.fixationList;
   }

   public Fixation getCurrentFixation()
   {
      return currentFixation;
   }

}
