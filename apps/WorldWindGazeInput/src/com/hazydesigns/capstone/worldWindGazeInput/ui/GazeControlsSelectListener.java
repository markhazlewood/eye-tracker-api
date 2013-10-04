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
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.render.ScreenImage;
import gov.nasa.worldwind.view.orbit.OrbitView;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.Timer;

/**
 *
 * @author Mark Hazlewood
 */
public class GazeControlsSelectListener implements SelectListener
{

   protected static final int DEFAULT_TIMER_DELAY = 50;
   protected static final int GAZE_ACTIVATION_DELAY = 1000;

   protected WorldWindow wwd;
   protected GazeControlsLayer controlsLayer;

   protected Timer repeatTimer;
   protected String pressedControlType = null;
   protected Point lastPickPoint = null;

   protected double panStep = .6;
   protected double zoomStep = .8;
   protected double headingStep = 1;
   protected double pitchStep = 1;
   protected double fovStep = 1.05;
   protected double veStep = 0.1;

   private Timer mGazeDelayTimer;
   private boolean mShouldActivate = false;

   private ScreenAnnotation mCursorImage = null;

   public GazeControlsSelectListener(WorldWindow wwd, GazeControlsLayer layer)
   {
      this.wwd = wwd;
      this.controlsLayer = layer;

      // Setup repeat timer
      this.repeatTimer = new Timer(DEFAULT_TIMER_DELAY, (ActionEvent event) ->
      {
         if (mShouldActivate == true)
         {
            updateView(pressedControlType);
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

      pressedControlType = ((AVList) event.getTopObject()).getStringValue(AVKey.VIEW_OPERATION);
      if (pressedControlType == null)
      {
         return;
      }

      if (event.getEventAction().equals(SelectEvent.ROLLOVER)
              || event.getEventAction().equals(SelectEvent.HOVER))
      {
         // Since the UI images are all centered in the view, and overlap, the 
         // "top" one here will always be the zoom out image. We want to actually
         // get the correct one. The order of nestings goes, from the center: 
         // zoom in > pan > zoom out
         // So use that order for "pressed" precedence

         controlsLayer.unHighlightAll();

         boolean panPicked = false;
         boolean zoomInPicked = false;
         boolean zoomOutPicked = false;

         Rectangle zoomInScreenBounds = controlsLayer.getZoomInScreenBounds();
         if (zoomInScreenBounds.contains(event.getPickPoint()))
         {
            int pixelX = event.getPickPoint().x - zoomInScreenBounds.x;
            int pixelY = event.getPickPoint().y - zoomInScreenBounds.y;

            try
            {
               int pixelColor = controlsLayer.getZoomInImage().getRGB(pixelX, pixelY);
               boolean transparent = ((pixelColor >> 24) == 0x00);

               if (!transparent)
               {
                  controlsLayer.highlightZoomIn();
                  zoomInPicked = true;
                  pressedControlType = AVKey.VIEW_ZOOM_IN;
               }
            }
            catch (Exception ex)
            {
               System.out.println(ex.getMessage());
            }
         }

         if (zoomInPicked == false)
         {
            Rectangle panScreenBounds = controlsLayer.getPanScreenBounds();
            if (panScreenBounds.contains(event.getPickPoint()))
            {
               int pixelX = event.getPickPoint().x - panScreenBounds.x;
               int pixelY = event.getPickPoint().y - panScreenBounds.y;

               try
               {
                  int pixelColor = controlsLayer.getPanImage().getRGB(pixelX, pixelY);
                  boolean transparent = ((pixelColor >> 24) == 0x00);

                  if (!transparent)
                  {
                     controlsLayer.highlightPan();
                     panPicked = true;
                     pressedControlType = AVKey.VIEW_PAN;
                  }
               }
               catch (Exception ex)
               {
                  System.out.println(ex.getMessage());
               }
            }
         }
         
         if (zoomInPicked == false && panPicked == false)
         {
            Rectangle zoomOutScreenBounds = controlsLayer.getZoomOutScreenBounds();
            if (zoomOutScreenBounds.contains(event.getPickPoint()))
            {               
               int pixelX = event.getPickPoint().x - zoomOutScreenBounds.x;
               int pixelY = event.getPickPoint().y - zoomOutScreenBounds.y;
               
               try
               {
                  int pixelColor = controlsLayer.getZoomOutImage().getRGB(pixelX, pixelY);
                  boolean transparent = ((pixelColor >> 24) == 0x00);

                  if (!transparent)
                  {
                     controlsLayer.highlightZoomOut();
                     zoomOutPicked = true;
                     pressedControlType = AVKey.VIEW_ZOOM_OUT;
                  }
               }
               catch (Exception ex)
               {
                  System.out.println(ex.getMessage());
               }
            }
         }
         
         if (panPicked || zoomInPicked || zoomOutPicked)
         {
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
               mGazeDelayTimer.restart();
            }
         }
         else
         {
            mShouldActivate = false;
         }
      }
   }

   protected void updateView(String controlType)
   {
      if (this.wwd == null)
      {
         return;
      }
      if (!(this.wwd.getView() instanceof OrbitView))
      {
         return;
      }

      OrbitView view = (OrbitView) this.wwd.getView();
      view.stopAnimations();
      view.stopMovement();
      
      switch (controlType)
      {
         case AVKey.VIEW_PAN:
         {
            resetOrbitView(view);
            // Go some distance in the control mouse direction
            Angle heading = computePanHeading(view, controlsLayer.getPanScreenImage());
            Angle distance = computePanAmount(this.wwd.getModel().getGlobe(), view, controlsLayer.getPanScreenImage(), panStep);
            LatLon newViewCenter = LatLon.greatCircleEndPosition(view.getCenterPosition(),
                    heading, distance);
            // Turn around if passing by a pole - TODO: better handling of the pole crossing situation
            if (this.isPathCrossingAPole(newViewCenter, view.getCenterPosition()))
            {
               view.setHeading(Angle.POS180.subtract(view.getHeading()));
            }  // Set new center pos
            view.setCenterPosition(new Position(newViewCenter, view.getCenterPosition().getElevation()));
            break;
         }
         
         case AVKey.VIEW_ZOOM_IN:
         {
            resetOrbitView(view);
            view.setZoom(computeNewZoom(view, -zoomStep));
            break;
         }
         
         case AVKey.VIEW_ZOOM_OUT:
         {
            resetOrbitView(view);
            view.setZoom(computeNewZoom(view, zoomStep));
            break;
         }
         
         default:
         {
            break;
         }         
      }
      
      view.firePropertyChange(AVKey.VIEW, null, view);
   }

   protected Layer getParentLayer()
   {
      return controlsLayer;
   }

   protected void resetOrbitView(OrbitView view)
   {
      if (view.getZoom() > 0)   // already in orbit view mode
      {
         return;
      }

        // Find out where on the terrain the eye is looking at in the viewport center
      // TODO: if no terrain is found in the viewport center, iterate toward viewport bottom until it is found
      Vec4 centerPoint = computeSurfacePoint(view, view.getHeading(), view.getPitch());
      // Reset the orbit view center point heading, pitch and zoom
      if (centerPoint != null)
      {
         Vec4 eyePoint = view.getEyePoint();
         // Center pos on terrain surface
         Position centerPosition = wwd.getModel().getGlobe().computePositionFromPoint(centerPoint);
         // Compute pitch and heading relative to center position
         Vec4 normal = wwd.getModel().getGlobe().computeSurfaceNormalAtLocation(centerPosition.getLatitude(),
                 centerPosition.getLongitude());
         Vec4 north = wwd.getModel().getGlobe().computeNorthPointingTangentAtLocation(centerPosition.getLatitude(),
                 centerPosition.getLongitude());
         // Pitch
         view.setPitch(Angle.POS180.subtract(view.getForwardVector().angleBetween3(normal)));
         // Heading
         Vec4 perpendicular = view.getForwardVector().perpendicularTo3(normal);
         Angle heading = perpendicular.angleBetween3(north);
         double direction = Math.signum(-normal.cross3(north).dot3(perpendicular));
         view.setHeading(heading.multiply(direction));
         // Zoom
         view.setZoom(eyePoint.distanceTo3(centerPoint));
         // Center pos
         view.setCenterPosition(centerPosition);
      }
   }

   protected double computeNewZoom(OrbitView view, double amount)
   {
      double coeff = 0.05;
      double change = coeff * amount;
      double logZoom = view.getZoom() != 0 ? Math.log(view.getZoom()) : 0;
        // Zoom changes are treated as logarithmic values. This accomplishes two things:
      // 1) Zooming is slow near the globe, and fast at great distances.
      // 2) Zooming in then immediately zooming out returns the viewer to the same zoom value.
      return Math.exp(logZoom + change);
   }

   protected Angle computePanHeading(OrbitView view, ScreenImage control)
   {
      // Compute last pick point 'heading' relative to pan control center
      Vec4 center = new Vec4(controlsLayer.getPanScreenImage().getScreenLocation().x,
              controlsLayer.getPanScreenImage().getScreenLocation().y,
              0);
      double px = lastPickPoint.x - center.x;
      double py = view.getViewport().getHeight() - lastPickPoint.y - center.y;
      Angle heading = view.getHeading().add(Angle.fromRadians(Math.atan2(px, py)));
      heading = heading.degrees >= 0 ? heading : heading.addDegrees(360);
      return heading;
   }

   protected Angle computePanAmount(Globe globe, OrbitView view, ScreenImage control, double panStep)
   {
      // Compute last pick point distance relative to pan control center
      Vec4 center = new Vec4(control.getScreenLocation().x, control.getScreenLocation().y, 0);
      double px = lastPickPoint.x - center.x;
      double py = view.getViewport().getHeight() - lastPickPoint.y - center.y;
      double pickDistance = Math.sqrt(px * px + py * py);
      double pickDistanceFactor = Math.min(pickDistance / 10, 5);

      // Compute globe angular distance depending on eye altitude
      Position eyePos = view.getEyePosition();
      double radius = globe.getRadiusAt(eyePos);
      double minValue = 0.5 * (180.0 / (Math.PI * radius)); // Minimum change ~0.5 meters
      double maxValue = 1.0; // Maximum change ~1 degree

        // Compute an interpolated value between minValue and maxValue, using (eye altitude)/(globe radius) as
      // the interpolant. Interpolation is performed on an exponential curve, to keep the value from
      // increasing too quickly as eye altitude increases.
      double a = eyePos.getElevation() / radius;
      a = (a < 0 ? 0 : (a > 1 ? 1 : a));
      double expBase = 2.0; // Exponential curve parameter.
      double value = minValue + (maxValue - minValue) * ((Math.pow(expBase, a) - 1.0) / (expBase - 1.0));

      return Angle.fromDegrees(value * pickDistanceFactor * panStep);
   }

   protected Angle computeLookHeading(OrbitView view, ScreenImage control, double headingStep)
   {
      // Compute last pick point 'heading' relative to look control center on x
      Vec4 center = new Vec4(control.getScreenLocation().x, control.getScreenLocation().y, 0);
      double px = lastPickPoint.x - center.x;
      double pickDistanceFactor = Math.min(Math.abs(px) / 3000, 5) * Math.signum(px);
      // New heading
      Angle heading = view.getHeading().add(Angle.fromRadians(headingStep * pickDistanceFactor));
      heading = heading.degrees >= 0 ? heading : heading.addDegrees(360);
      return heading;
   }

   protected Angle computeLookPitch(OrbitView view, ScreenImage control, double pitchStep)
   {
      // Compute last pick point 'pitch' relative to look control center on y
      Vec4 center = new Vec4(control.getScreenLocation().x, control.getScreenLocation().y, 0);
      double py = lastPickPoint.y - center.y;
      double pickDistanceFactor = Math.min(Math.abs(py) / 3000, 5) * Math.signum(py);
      // New pitch
      Angle pitch = view.getPitch().add(Angle.fromRadians(pitchStep * pickDistanceFactor));
      pitch = pitch.degrees >= 0 ? (pitch.degrees <= 90 ? pitch : Angle.fromDegrees(90)) : Angle.ZERO;
      return pitch;
   }

   protected Vec4 computeSurfacePoint(OrbitView view, Angle heading, Angle pitch)
   {
      Globe globe = wwd.getModel().getGlobe();
        // Compute transform to be applied to north pointing Y so that it would point in the view direction
      // Move coordinate system to view center point
      Matrix transform = globe.computeSurfaceOrientationAtPosition(view.getCenterPosition());
      // Rotate so that the north pointing axes Y will point in the look at direction
      transform = transform.multiply(Matrix.fromRotationZ(heading.multiply(-1)));
      transform = transform.multiply(Matrix.fromRotationX(Angle.NEG90.add(pitch)));
      // Compute forward vector
      Vec4 forward = Vec4.UNIT_Y.transformBy4(transform);
      // Return intersection with terrain
      Intersection[] intersections = wwd.getSceneController().getTerrain().intersect(
              new Line(view.getEyePoint(), forward));
      return (intersections != null && intersections.length != 0) ? intersections[0].getIntersectionPoint() : null;
   }

   protected void setupFirstPersonView(OrbitView view)
   {
      if (view.getZoom() == 0)  // already in first person mode
      {
         return;
      }

      Vec4 eyePoint = view.getEyePoint();
      // Center pos at eye pos
      Position centerPosition = wwd.getModel().getGlobe().computePositionFromPoint(eyePoint);
      // Compute pitch and heading relative to center position
      Vec4 normal = wwd.getModel().getGlobe().computeSurfaceNormalAtLocation(centerPosition.getLatitude(),
              centerPosition.getLongitude());
      Vec4 north = wwd.getModel().getGlobe().computeNorthPointingTangentAtLocation(centerPosition.getLatitude(),
              centerPosition.getLongitude());
      // Pitch
      view.setPitch(Angle.POS180.subtract(view.getForwardVector().angleBetween3(normal)));
      // Heading
      Vec4 perpendicular = view.getForwardVector().perpendicularTo3(normal);
      Angle heading = perpendicular.angleBetween3(north);
      double direction = Math.signum(-normal.cross3(north).dot3(perpendicular));
      view.setHeading(heading.multiply(direction));
      // Zoom
      view.setZoom(0);
      // Center pos
      view.setCenterPosition(centerPosition);
   }

   protected boolean isPathCrossingAPole(LatLon p1, LatLon p2)
   {
      return Math.abs(p1.getLongitude().degrees - p2.getLongitude().degrees) > 20
              && Math.abs(p1.getLatitude().degrees - 90 * Math.signum(p1.getLatitude().degrees)) < 10;
   }
}
