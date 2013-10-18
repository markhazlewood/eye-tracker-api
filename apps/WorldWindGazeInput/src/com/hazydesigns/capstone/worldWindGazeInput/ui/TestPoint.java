//*****************************************************************************
// Classification:   UNCLASSIFIED//FOUO
//
// NAME:  TestPoint.java
//
// AUTHOR/DATE:  Mark  10/18/13
//
// Copyright 2013, SRC; originally developed for ...
//
//*****************************************************************************

/*Expression selection is undefined on line 12, column 5 in Templates/Classes/Class.java.*/
package com.hazydesigns.capstone.worldWindGazeInput.ui;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

/**
 *
 * @author Mark
 */
public class TestPoint
{
   // <editor-fold defaultstate="expanded" desc="Private Members">
   
   public final static double SHOW_SUBPOINT_ELEVATION = 10000.0;
   public final static double SHOW_SUBPOINT_DISTANCE = 20000.0;
   public final static double ACTIVATE_SUBPOINT_DISTANCE = 20000.0;

   private final GlobeAnnotation mMainPoint;
   private final ArrayList<GlobeAnnotation> mSubPoints;
   private boolean mShowingSubPoints = false;
   
   private final AnnotationAttributes mMainPointAttributes;
   private final AnnotationAttributes mSubPointAttributes;
   
   private final Color mSubPointInactiveColor = Color.YELLOW.brighter().brighter();
   private final Color mSubPointActiveColor = Color.GREEN.brighter().brighter();
   
   private final double mGlobeRadius;   
   private final RenderableLayer mPointLayer;

   // </editor-fold>
   
   // <editor-fold defaultstate="expanded" desc="Constructor(s)">
   
   public TestPoint(String pointNumber, 
                    Position mainPointPosition, 
                    Position[] subPointPositions,
                    RenderableLayer pointLayer,
                    double globeRadius)
   {
      mGlobeRadius = globeRadius;
      mPointLayer = pointLayer;
      
      mMainPointAttributes = new AnnotationAttributes();
      mMainPointAttributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
      mMainPointAttributes.setBackgroundColor(Color.RED.brighter().brighter());
      mMainPointAttributes.setFont(Font.decode("Arial-BOLD-26"));
      mMainPointAttributes.setBorderWidth(2);
      mMainPointAttributes.setBorderColor(Color.BLACK);
      mMainPointAttributes.setTextColor(Color.WHITE);
      mMainPointAttributes.setFrameShape(AVKey.SHAPE_ELLIPSE);
      mMainPointAttributes.setScale(0.5);
      mMainPointAttributes.setLeader(AVKey.SHAPE_NONE);
      mMainPointAttributes.setTextAlign(AVKey.CENTER);
      
      mSubPointAttributes = new AnnotationAttributes();
      mSubPointAttributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
      mSubPointAttributes.setBackgroundColor(mSubPointInactiveColor);
      mSubPointAttributes.setFont(Font.decode("Arial-BOLD-26"));
      mSubPointAttributes.setBorderWidth(2);
      mSubPointAttributes.setBorderColor(Color.BLACK);
      mSubPointAttributes.setTextColor(Color.WHITE);
      mSubPointAttributes.setFrameShape(AVKey.SHAPE_ELLIPSE);
      mSubPointAttributes.setScale(0.5);
      mSubPointAttributes.setLeader(AVKey.SHAPE_NONE);
      mSubPointAttributes.setTextAlign(AVKey.CENTER);
      mSubPointAttributes.setVisible(false);
      
      mMainPoint = new GlobeAnnotation(pointNumber, mainPointPosition, mMainPointAttributes);     
      mPointLayer.addRenderable(mMainPoint);
      
      mSubPoints = new ArrayList<>();
      for (int i = 0; i < subPointPositions.length; i++)
      {
         GlobeAnnotation annotation = new GlobeAnnotation("" + pointNumber + "-" + i, subPointPositions[i], mSubPointAttributes);
         mSubPoints.add(annotation);         
      }
   }

   // </editor-fold>
   
   // <editor-fold defaultstate="expanded" desc="Working Functions">
   
   public void handleCameraPosition(LatLon groundPoint, double elevation)
   {
      // First check camera elevation. If elevation is low enough, show sub
      // points. If it is high, and sub points were being shown, hide them.
      if (elevation <= SHOW_SUBPOINT_ELEVATION)
      {
         // Check distance to main point. If it is close enough, show sub points
         double distToMain = distanceToMainPoint(groundPoint);
         if (!mShowingSubPoints && distToMain <= SHOW_SUBPOINT_DISTANCE)
         {
            toggleSubPoints(true);
         }
         else if (mShowingSubPoints)
         {
            mSubPoints.stream().filter((globeAnnotation) -> (distanceBetweenPoints(globeAnnotation.getPosition(), groundPoint) <= ACTIVATE_SUBPOINT_DISTANCE)).forEach((GlobeAnnotation globeAnnotation) ->
            {
               globeAnnotation.getAttributes().setBackgroundColor(mSubPointActiveColor);
            });
         }
      }
      else if (mShowingSubPoints)
      {
         toggleSubPoints(false);
      }
   }
   
   private void toggleSubPoints(boolean show)
   {
      mMainPoint.getAttributes().setVisible(!show);
      
      mSubPoints.stream().forEach((globeAnnotation) ->
      {
         if (show)
         {
            mPointLayer.addRenderable(globeAnnotation);
         }
         else
         {
            mPointLayer.removeRenderable(globeAnnotation);
         }
      });
      
      mShowingSubPoints = show;
   }
   
   private double distanceToMainPoint(LatLon pos)
   {
      return distanceBetweenPoints(mMainPoint.getPosition(), pos);
   }
   
   private double distanceBetweenPoints(LatLon pos1, LatLon pos2)
   {
      return (LatLon.rhumbDistance(pos1, pos2).degrees * mGlobeRadius);
   }
   
   // </editor-fold>
   
   // <editor-fold defaultstate="expanded" desc="Properties">
   
   

   // </editor-fold>
}
