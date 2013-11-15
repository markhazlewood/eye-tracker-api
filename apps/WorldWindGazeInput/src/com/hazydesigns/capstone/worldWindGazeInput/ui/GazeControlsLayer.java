package com.hazydesigns.capstone.worldWindGazeInput.ui;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenImage;
import gov.nasa.worldwind.render.Size;
import gov.nasa.worldwind.util.WWIO;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * A customization of the WorldWind-standard {@link ViewControlsLayer}.
 * Overrides a minimal amount of the base class to load a different UI suitable
 * for gaze input. Extending this was very useful as the code in
 * ViewControlsSelectListener did not need to be updated or modified, only the
 * UI presentation changes.
 *
 * @author Mark Hazlewood
 *
 * @see gov.nasa.worldwind.layers.ViewControlsLayer
 * @see gov.nasa.worldwind.layers.ViewControlsSelectListener
 */
public class GazeControlsLayer extends RenderableLayer
{

   private boolean initialized = false;

   private boolean mShowEdgePanControls = false;
   private boolean mShowCenterPanControls = false;
   private boolean mShowZoomInControls = false;
   private boolean mShowZoomOutControls = false;

   protected Rectangle referenceViewport;

   private final String mEdgePanImagePath = "images/gaze_pan_alt.png";
   private final String mCenterPanImagePath = "images/gaze_pan.png";
   private final String mZoomInImagePath = "images/gaze_zoomIn_alt.png";
   private final String mZoomOutImagePath = "images/gaze_zoomOut_alt.png";

   private BufferedImage mEdgePanImage = null;
   private BufferedImage mCenterPanImage = null;
   private BufferedImage mZoomInImage = null;
   private BufferedImage mZoomOutImage = null;
   
   private double mZoomScale = 0.8;

   private ScreenImage mEdgePanScreenImage;
   private ScreenImage mCenterPanScreenImage;
   private ScreenImage mZoomInScreenImage;
   private ScreenImage mZoomOutScreenImage;

   private Rectangle mEdgePanScreenBounds;
   private Rectangle mCenterPanScreenBounds;
   private Rectangle mZoomInScreenBounds;
   private Rectangle mZoomOutScreenBounds;

   public GazeControlsLayer()
   {
      super();

      mShowEdgePanControls = true;
      mShowCenterPanControls = false;
      mShowZoomInControls = true;
      mShowZoomOutControls = true;

      try
      {
         mEdgePanImage
                 = ImageIO.read((InputStream) WWIO.getFileOrResourceAsStream(mEdgePanImagePath,
                                 this.getClass()));
         mCenterPanImage
                 = ImageIO.read((InputStream) WWIO.getFileOrResourceAsStream(mCenterPanImagePath,
                                 this.getClass()));
         mZoomInImage
                 = ImageIO.read((InputStream) WWIO.getFileOrResourceAsStream(mZoomInImagePath,
                                 this.getClass()));
         mZoomOutImage
                 = ImageIO.read((InputStream) WWIO.getFileOrResourceAsStream(mZoomOutImagePath,
                                 this.getClass()));
          scaleZoom(mZoomScale);
      }
      catch (IOException ex)
      {
         ex.printStackTrace();
      }
   }
   
   public void reset()
   {
       initialized = false;       
       this.removeAllRenderables();
   }

   @Override
   public void doRender(DrawContext dc)
   {
      if (!this.initialized)
      {
         initialize(dc);
      }

      if (!this.referenceViewport.equals(dc.getView().getViewport()))
      {
         updatePositions(dc);
      }

      super.doRender(dc);
   }

   protected boolean isInitialized()
   {
      return initialized;
   }

   //TODO: Override initialize()?
   protected void initialize(DrawContext dc)
   {
      if (this.initialized)
      {
         return;
      }

      // Edge Pan
      if (this.mShowEdgePanControls)
      {
         mEdgePanScreenImage = new ScreenImage();
         mEdgePanScreenImage.setImageSource(mEdgePanImagePath);
         mEdgePanScreenImage.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
         mEdgePanScreenImage.setOpacity(0.5);

         this.addRenderable(mEdgePanScreenImage);
      }
      
      // Center Pan
      if (this.mShowCenterPanControls)
      {
         mCenterPanScreenImage = new ScreenImage();
         mCenterPanScreenImage.setImageSource(mCenterPanImagePath);
         mCenterPanScreenImage.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
         mCenterPanScreenImage.setOpacity(0.5);
         
         Size s = new Size(Size.EXPLICIT_DIMENSION, 
                           mCenterPanImage.getWidth(), 
                           AVKey.PIXELS, 
                           Size.EXPLICIT_DIMENSION, 
                           mCenterPanImage.getHeight(), 
                           AVKey.PIXELS);
         mCenterPanScreenImage.setSize(s);

         this.addRenderable(mCenterPanScreenImage);
      }

      // Zoom in      
      if (this.mShowZoomInControls)
      {
         mZoomInScreenImage = new ScreenImage();
         mZoomInScreenImage.setImageSource(mZoomInImagePath);
         mZoomInScreenImage.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_ZOOM_IN);
         mZoomInScreenImage.setOpacity(0.5);         
         
         Size s = new Size(Size.EXPLICIT_DIMENSION, 
                           mZoomInImage.getWidth(), 
                           AVKey.PIXELS, 
                           Size.EXPLICIT_DIMENSION, 
                           mZoomInImage.getHeight(), 
                           AVKey.PIXELS);
         mZoomInScreenImage.setSize(s);

         this.addRenderable(mZoomInScreenImage);
      }

      // Zoom out   
      if (this.mShowZoomOutControls)
      {
         mZoomOutScreenImage = new ScreenImage();
         mZoomOutScreenImage.setImageSource(mZoomOutImagePath);
         mZoomOutScreenImage.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_ZOOM_OUT);
         mZoomOutScreenImage.setOpacity(0.5);
         
         Size s = new Size(Size.EXPLICIT_DIMENSION, 
                           mZoomOutImage.getWidth(), 
                           AVKey.PIXELS, 
                           Size.EXPLICIT_DIMENSION, 
                           mZoomOutImage.getHeight(), 
                           AVKey.PIXELS);
         mZoomOutScreenImage.setSize(s);

         this.addRenderable(mZoomOutScreenImage);
      }

      // Place controls according to layout and viewport dimension
      updatePositions(dc);

      this.initialized = true;
   }

   protected Object getImageSource(String control)
   {
       switch (control) 
       {
           case AVKey.VIEW_PAN:
           {
              if (mShowEdgePanControls)
              {
                  return mEdgePanImagePath;
              }
              else
              {
                 return mCenterPanImagePath;
              }
           }
           case AVKey.VIEW_ZOOM_IN:
           {
               return mZoomInImagePath;
           }
           case AVKey.VIEW_ZOOM_OUT:
           {
               return mZoomOutImagePath;
           }
           default:
           {
               return null;
           }
       }
   }

   protected void updatePositions(DrawContext dc)
   {
      updateControlPositions(dc);
   }

   public void updateControlPositions(DrawContext dc)
   {
      Point centerLocation = new Point(dc.getView().getViewport().width / 2, dc.getView().getViewport().height / 2);

      int xOffset = 0;
      int yOffset = 0;

      if (mShowEdgePanControls)
      {
         mEdgePanScreenImage.setScreenLocation(centerLocation);
         mEdgePanScreenBounds = new Rectangle(centerLocation.x - mEdgePanImage.getWidth() / 2,
                 centerLocation.y - mEdgePanImage.getHeight() / 2,
                 mEdgePanImage.getWidth(),
                 mEdgePanImage.getHeight());
      }
      else if (mShowCenterPanControls)
      {
         mCenterPanScreenImage.setScreenLocation(centerLocation);
         mCenterPanScreenBounds = new Rectangle(centerLocation.x - mCenterPanImage.getWidth() / 2,
                 centerLocation.y - mCenterPanImage.getHeight() / 2,
                 mCenterPanImage.getWidth(),
                 mCenterPanImage.getHeight());
      }

      if (mShowZoomInControls)
      {
         mZoomInScreenImage.setScreenLocation(centerLocation);
         mZoomInScreenBounds = new Rectangle(centerLocation.x - mZoomInImage.getWidth() / 2,
                 centerLocation.y - mZoomInImage.getHeight() / 2,
                 mZoomInImage.getWidth(),
                 mZoomInImage.getHeight());
      }

      if (mShowZoomOutControls)
      {
         mZoomOutScreenImage.setScreenLocation(centerLocation);
         mZoomOutScreenBounds = new Rectangle(centerLocation.x - mZoomOutImage.getWidth() / 2,
                 centerLocation.y - mZoomOutImage.getHeight() / 2,
                 mZoomOutImage.getWidth(),
                 mZoomOutImage.getHeight());
      }

      referenceViewport = dc.getView().getViewport();
   }
   
   public void scaleZoom(double newScale)
   {  
      int zoomInWidth = (int)(mZoomInImage.getWidth() * newScale);
      int zoomInHeight = (int)(mZoomInImage.getHeight() * newScale);
      int zoomOutWidth = (int)(mZoomOutImage.getWidth() * newScale);
      int zoomOutHeight = (int)(mZoomOutImage.getHeight() * newScale);
      
      int centerPanWidth = (int)(mCenterPanImage.getWidth() * newScale);
      int centerPanHeight = (int)(mCenterPanImage.getHeight() * newScale);
      
      BufferedImage zoomInImage = 
         new BufferedImage(zoomInWidth, zoomInHeight, BufferedImage.TYPE_INT_ARGB);
      BufferedImage zoomOutImage = 
         new BufferedImage(zoomOutWidth, zoomOutHeight, BufferedImage.TYPE_INT_ARGB);
      BufferedImage centerPanImage = 
         new BufferedImage(centerPanWidth, centerPanHeight, BufferedImage.TYPE_INT_ARGB);
      
      AffineTransform transform = new AffineTransform();
      transform.scale(newScale, newScale);
      AffineTransformOp scaleOp = 
         new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
      
      zoomInImage = scaleOp.filter(mZoomInImage, zoomInImage);
      zoomOutImage = scaleOp.filter(mZoomOutImage, zoomOutImage);
      centerPanImage = scaleOp.filter(mCenterPanImage, centerPanImage);
      
      mZoomInImage = zoomInImage;
      mZoomOutImage = zoomOutImage;
      mCenterPanImage = centerPanImage;
      
      mZoomScale = newScale;
      reset();
   }
   
   public double getZoomScale()
   {
      return mZoomScale;
   }

   public ScreenImage getEdgePanScreenImage()
   {
      return mEdgePanScreenImage;
   }

   public boolean getShowEdgePan()
   {
       return mShowEdgePanControls;
   }
   
   public BufferedImage getEdgePanImage()
   {
      return mEdgePanImage;
   }

   public Rectangle getEdgePanScreenBounds()
   {
      return mEdgePanScreenBounds;
   }
   
   public ScreenImage getCenterPanScreenImage()
   {
      return mCenterPanScreenImage;
   }

   public boolean getShowCenterPan()
   {
       return mShowCenterPanControls;
   }
   
   public BufferedImage getCenterPanImage()
   {
      return mCenterPanImage;
   }

   public Rectangle getCenterPanScreenBounds()
   {
      return mCenterPanScreenBounds;
   }

   public boolean getShowZoomIn()
   {
       return mShowZoomInControls;
   }
   
   public BufferedImage getZoomInImage()
   {
      return mZoomInImage;
   }

   public Rectangle getZoomInScreenBounds()
   {
      return mZoomInScreenBounds;
   }

   public boolean getShowZoomOut()
   {
       return mShowZoomOutControls;
   }
   
   public BufferedImage getZoomOutImage()
   {
      return mZoomOutImage;
   }
   
   public Rectangle getZoomOutScreenBounds()
   {
      return mZoomOutScreenBounds;
   }

   public void unHighlightAll()
   {
      if (mShowEdgePanControls)
      {
         if (mEdgePanScreenImage != null)
         {
            mEdgePanScreenImage.setOpacity(0.5);
         }
      }
      else if (mShowCenterPanControls)
      {
         if (mCenterPanScreenImage != null)
         {
            mCenterPanScreenImage.setOpacity(0.5);
         }
      }
      if (mShowZoomInControls)
      {
         mZoomInScreenImage.setOpacity(0.5);
      }
      if (mShowZoomOutControls)
      {
         mZoomOutScreenImage.setOpacity(0.5);
      }
   }

   public void highlightEdgePan()
   {
      mEdgePanScreenImage.setOpacity(1);
   }
   
   public void highlightCenterPan()
   {
      mCenterPanScreenImage.setOpacity(1);
   }

   public void highlightZoomIn()
   {
      mZoomInScreenImage.setOpacity(1);
   }

   public void highlightZoomOut()
   {
      mZoomOutScreenImage.setOpacity(1);
   }
   
   public void setShowZoomOut(boolean show)
   {
       mShowZoomOutControls = show;
       reset();
   }
   
   public void setShowZoomIn(boolean show)
   {
       mShowZoomInControls = show;
       reset();
   }
   
   public void setShowEdgePan(boolean show)
   {
       mShowEdgePanControls = show;
       reset();
   }
   
   public void setShowCenterPan(boolean show)
   {
      mShowCenterPanControls = show;
      reset();
   }
   
   public void setShowZoomInZoomOutPan(boolean showZoomIn, boolean showZoomOut, boolean showEdgePan, boolean showCenterPan)
   {
       mShowZoomInControls = showZoomIn;
       mShowZoomOutControls = showZoomOut;
       mShowEdgePanControls = showEdgePan;
       mShowCenterPanControls = showCenterPan;
       
       reset();
   }
           
}
