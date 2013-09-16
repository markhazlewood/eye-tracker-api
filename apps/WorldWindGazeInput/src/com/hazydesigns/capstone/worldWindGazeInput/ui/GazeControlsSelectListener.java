/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hazydesigns.capstone.worldWindGazeInput.ui;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.view.orbit.OrbitView;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.Timer;

/**
 *
 * @author Mark Hazlewood
 */
public class GazeControlsSelectListener extends ViewControlsSelectListener
{
   protected static final int GAZE_ACTIVATION_DELAY = 1000;   
   
   private Timer mGazeDelayTimer;
   private boolean mShouldActivate = false;
   
   private ScreenAnnotation mCursorImage = null;

   public GazeControlsSelectListener(WorldWindow wwd, GazeControlsLayer layer)
   {
      super(wwd, layer);
      
      // Setup repeat timer
      if (this.repeatTimer != null)
      {
         this.repeatTimer.stop();
         this.repeatTimer = null;
      }
      this.repeatTimer = new Timer(DEFAULT_TIMER_DELAY, (ActionEvent event) ->
      {
         if (mShouldActivate == true)
         {
            updateView(pressedControl, pressedControlType);
         }
      });
      this.repeatTimer.start();
   }
   
   public GazeControlsSelectListener(WorldWindow wwd, GazeControlsLayer layer, ScreenAnnotation cursorImage)
   {
      this(wwd, layer);      
      mCursorImage = cursorImage;
   }

   @Override
   public void selected(SelectEvent event)
   {      
      if (this.wwd == null)
      {
         return;
      }
      
      if (mCursorImage != null)
      {
         if (event != null && event.getPickPoint() != null)
         {
            int x = event.getPickPoint().x;
            int y = Math.abs(event.getPickPoint().y - this.wwd.getView().getViewport().height);

            mCursorImage.setScreenPoint(new Point(x, y));
            this.wwd.redraw();
         }
      }

      if (!(this.wwd.getView() instanceof OrbitView))
      {
         return;
      }

      OrbitView view = (OrbitView) this.wwd.getView();

      if (this.viewControlsLayer.getHighlightedObject() != null)
      {
         this.viewControlsLayer.highlight(null);
         this.wwd.redraw(); // must redraw so the de-highlight can take effect
      }

      if (event.getMouseEvent() != null && event.getMouseEvent().isConsumed())
      {
         return;
      }

      if (event.getTopObject() == null || event.getTopPickedObject().getParentLayer() != this.getParentLayer()
              || !(event.getTopObject() instanceof AVList))
      {
         // This case triggers when a control is activated and then the cursor
         // leaves the control, so we can treat it as a "mouse out" event

         // Reset activation state
         mShouldActivate = false;
         
         // Kill gaze timer, just in case it's running
         if (mGazeDelayTimer != null)
         {
            mGazeDelayTimer.stop();
            mGazeDelayTimer = null;
         }

         return;
      }
      
      // This is needed every update for certain view actions
      this.lastPickPoint = event.getPickPoint();
      
      String controlType = ((AVList) event.getTopObject()).getStringValue(AVKey.VIEW_OPERATION);
      if (controlType == null)
      {
         return;
      }

      if (event.getEventAction().equals(SelectEvent.ROLLOVER)
              || event.getEventAction().equals(SelectEvent.HOVER))
      {         
         this.pressedControl = (ScreenAnnotation) event.getTopObject();
         this.pressedControlType = controlType;
         
         // Make sure the control is highlighted
         if (this.viewControlsLayer.getHighlightedObject() != this.pressedControl)
         {
            this.viewControlsLayer.highlight(this.pressedControl);
            this.wwd.redraw();
         }
         
         // If the timer hasn't been started yet, start it
         if (mGazeDelayTimer == null)
         {
            mGazeDelayTimer = new Timer(GAZE_ACTIVATION_DELAY, (ActionEvent ae) ->
            {
               // For gaze input, treat a hover or rollover as a selection after
               // a delay.
               mShouldActivate = true;

               mGazeDelayTimer.stop();
            });
            mGazeDelayTimer.start();
         }
         
         // If the timer has started, then elapsed, continue button activation
         else if (mGazeDelayTimer.isRunning() == false)
         {
            mShouldActivate = true;
         }
      }
   }

}
