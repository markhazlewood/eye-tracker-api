//*****************************************************************************
// Classification:   UNCLASSIFIED//FOUO
//
// NAME:  MyScreenAnnotation.java
//
// AUTHOR/DATE:  Mark  1/19/14
//
// Copyright 2014, SRC; originally developed for ...
//
//*****************************************************************************

/*Expression selection is undefined on line 12, column 5 in Templates/Classes/Class.java.*/

package com.hazydesigns.capstone.worldWindGazeInput.ui;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenAnnotation;
import java.awt.Point;

/**
 *
 * @author Mark
 */
public class MyScreenAnnotation extends ScreenAnnotation
{
   // <editor-fold defaultstate="expanded" desc="Private Members">

   

   // </editor-fold>

   // <editor-fold defaultstate="expanded" desc="Constructor(s)">

   public MyScreenAnnotation(String content, Point location, AnnotationAttributes attr)
   {
      super(content, location, attr);
   }

   // </editor-fold>

   // <editor-fold defaultstate="expanded" desc="Working Functions">

   @Override
   protected void drawBackgroundImage(DrawContext dc, int width, int height, double opacity, Position pickPosition)
   {
      super.drawBackgroundImage(dc, width, height, opacity, pickPosition);
   }

   // </editor-fold>


   // <editor-fold defaultstate="expanded" desc="Properties">

   

   // </editor-fold>
}
