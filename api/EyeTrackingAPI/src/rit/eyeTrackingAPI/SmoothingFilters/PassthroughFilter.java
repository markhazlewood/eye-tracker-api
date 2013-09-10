/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rit.eyeTrackingAPI.SmoothingFilters;

import java.awt.Point;

/**
 *
 * @author mhazlewood
 */
public class PassthroughFilter extends Filter
{
   @Override
   public synchronized void filter(int x, int y)
   {
      newCoordinate = new Point(x, y);
      newCoordinateAvailable = true;
      notifyAll();
      while (!coordinateRead)
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
