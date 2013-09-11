package com.hazydesigns.capstone.worldWindGazeInput;

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import java.awt.Color;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

/**
 *
 * @author mhazlewood
 */
public class TestCube implements Renderable
{
   protected PickSupport mPickSupport;
   protected Position mPosition;
   protected double mSize_meters;

   public TestCube(Position initialPosition, double sizeInMeters)
   {
      mPosition = initialPosition;
      mSize_meters = sizeInMeters;
      
      mPickSupport = new PickSupport();
   }

   /**
    * Set up GL states for drawing our cube
    *
    * @param dc
    */
   protected void beginDrawing(DrawContext dc)
   {
      GL2 gl = dc.getGL().getGL2();

      int attributeMask = GL2.GL_CURRENT_BIT | GL.GL_COLOR_BUFFER_BIT;
      gl.glPushAttrib(attributeMask);

      if (!dc.isPickingMode())
      {
         dc.beginStandardLighting();
      }

      gl.glMatrixMode(GL2.GL_MODELVIEW);
      Matrix surfaceOrientation = dc.getGlobe().computeSurfaceOrientationAtPosition(mPosition);

      double[] matrixArray = new double[16];
      surfaceOrientation.toArray(matrixArray, 0, false);
      gl.glLoadMatrixd(matrixArray, 0);
   }

   @Override
   public void render(DrawContext dc)
   {
      GL2 gl = dc.getGL().getGL2();
      
      beginDrawing(dc);      
      try
      {
         // If in the picking render pass, get a new unique pick color, add this
         // object to the pickable object list, and set up GL to render this frame
         // using ONLY the pick color.
         if (dc.isPickingMode())
         {
            Color pickColor = dc.getUniquePickColor();
            mPickSupport.addPickableObject(pickColor.getRGB(), this, mPosition);
            gl.glColor3ub( (byte)pickColor.getRed(), 
                           (byte)pickColor.getGreen(), 
                           (byte)pickColor.getBlue());
         }
         
         gl.glScaled(mSize_meters, mSize_meters, mSize_meters);
         drawUnitCube(dc);
      }
      finally
      {
         endDrawing(dc);
      }
   }

   protected void endDrawing(DrawContext dc)
   {
      GL2 gl = dc.getGL().getGL2();

      if (!dc.isPickingMode())
      {
         dc.endStandardLighting();
      }

      gl.glPopAttrib();
   }

   protected void drawUnitCube(DrawContext dc)
   {
      // Vertices of a unit cube, centered on the origin.
      float[][] v =
      {
         {
            -0.5f, 0.5f, -0.5f
         }, 
         {
            -0.5f, 0.5f, 0.5f
         }, 
         {
            0.5f, 0.5f, 0.5f
         }, 
         {
            0.5f, 0.5f, -0.5f
         }, 
         {
            -0.5f, -0.5f, 0.5f
         }, 
         {
            0.5f, -0.5f, 0.5f
         }, 
         {
            0.5f, -0.5f, -0.5f
         }, 
         {
            -0.5f, -0.5f, -0.5f
         }
      };

      // Array to group vertices into faces
      int[][] faces =
      {
         {
            0, 1, 2, 3
         }, 
         {
            2, 5, 6, 3
         }, 
         {
            1, 4, 5, 2
         }, 
         {
            0, 7, 4, 1
         }, 
         {
            0, 7, 6, 3
         }, 
         {
            4, 7, 6, 5
         }
      };

      // Normal vectors for each face
      float[][] n =
      {
         {
            0, 1, 0
         }, 
         {
            1, 0, 0
         }, 
         {
            0, 0, 1
         }, 
         {
            -1, 0, 0
         }, 
         {
            0, 0, -1
         }, 
         {
            0, -1, 0
         }
      };

      GL2 gl = dc.getGL().getGL2();

    // Use OpenGL immediate mode for simplicity. Real applications should use
      // vertex arrays or vertex buffer objects for best performance.
      gl.glBegin(GL2.GL_QUADS);
      try
      {
         for (int i = 0; i < faces.length; i++)
         {
            gl.glNormal3f(n[i][0], n[i][1], n[i][2]);

            for (int j = 0; j < faces[0].length; j++)
            {
               gl.glVertex3f(v[faces[i][j]][0], v[faces[i][j]][1], v[faces[i][j]][2]);
            }
         }
      }
      finally
      {
         gl.glEnd();
      }
   }

}
