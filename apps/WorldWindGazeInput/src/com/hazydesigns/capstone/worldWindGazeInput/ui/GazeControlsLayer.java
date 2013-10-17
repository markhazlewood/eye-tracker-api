package com.hazydesigns.capstone.worldWindGazeInput.ui;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenImage;
import gov.nasa.worldwind.util.WWIO;
import java.awt.Point;
import java.awt.Rectangle;
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

   private boolean mShowPanControls = false;
   private boolean mShowZoomInControls = false;
   private boolean mShowZoomOutControls = false;

   protected Rectangle referenceViewport;

   private final String mPanImagePath = "images/gaze_pan_alt.png";
   private final String mZoomInImagePath = "images/gaze_zoomIn_alt.png";
   private final String mZoomOutImagePath = "images/gaze_zoomOut_alt.png";

   private BufferedImage mPanImage = null;
   private BufferedImage mZoomInImage = null;
   private BufferedImage mZoomOutImage = null;

   private ScreenImage mPanScreenImage;
   private ScreenImage mZoomInScreenImage;
   private ScreenImage mZoomOutScreenImage;

   private Rectangle mPanScreenBounds;
   private Rectangle mZoomInScreenBounds;
   private Rectangle mZoomOutScreenBounds;

   public GazeControlsLayer()
   {
      super();

      mShowPanControls = true;
      mShowZoomInControls = true;
      mShowZoomOutControls = true;

      try
      {
         mPanImage
                 = ImageIO.read((InputStream) WWIO.getFileOrResourceAsStream(mPanImagePath,
                                 this.getClass()));
         mZoomInImage
                 = ImageIO.read((InputStream) WWIO.getFileOrResourceAsStream(mZoomInImagePath,
                                 this.getClass()));
         mZoomOutImage
                 = ImageIO.read((InputStream) WWIO.getFileOrResourceAsStream(mZoomOutImagePath,
                                 this.getClass()));
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

      // Pan
      if (this.mShowPanControls)
      {
         mPanScreenImage = new ScreenImage();
         mPanScreenImage.setImageSource(mPanImagePath);
         mPanScreenImage.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
         mPanScreenImage.setOpacity(0.5);

         this.addRenderable(mPanScreenImage);
      }

      // Zoom in      
      if (this.mShowZoomInControls)
      {
         mZoomInScreenImage = new ScreenImage();
         mZoomInScreenImage.setImageSource(mZoomInImagePath);
         mZoomInScreenImage.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_ZOOM_IN);
         mZoomInScreenImage.setOpacity(0.5);

         this.addRenderable(mZoomInScreenImage);
      }

      // Zoom out   
      if (this.mShowZoomOutControls)
      {
         mZoomOutScreenImage = new ScreenImage();
         mZoomOutScreenImage.setImageSource(mZoomOutImagePath);
         mZoomOutScreenImage.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_ZOOM_OUT);
         mZoomOutScreenImage.setOpacity(0.5);

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
               return mPanImagePath;
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

      if (this.mShowPanControls)
      {
         mPanScreenImage.setScreenLocation(centerLocation);
         mPanScreenBounds = new Rectangle(centerLocation.x - mPanImage.getWidth() / 2,
                 centerLocation.y - mPanImage.getHeight() / 2,
                 mPanImage.getWidth(),
                 mPanImage.getHeight());
      }

      if (this.mShowZoomInControls)
      {
         mZoomInScreenImage.setScreenLocation(centerLocation);
         mZoomInScreenBounds = new Rectangle(centerLocation.x - mZoomInImage.getWidth() / 2,
                 centerLocation.y - mZoomInImage.getHeight() / 2,
                 mZoomInImage.getWidth(),
                 mZoomInImage.getHeight());
      }

      if (this.mShowZoomOutControls)
      {
         mZoomOutScreenImage.setScreenLocation(centerLocation);
         mZoomOutScreenBounds = new Rectangle(centerLocation.x - mZoomOutImage.getWidth() / 2,
                 centerLocation.y - mZoomOutImage.getHeight() / 2,
                 mZoomOutImage.getWidth(),
                 mZoomOutImage.getHeight());
      }

      referenceViewport = dc.getView().getViewport();
   }

   public ScreenImage getPanScreenImage()
   {
      return mPanScreenImage;
   }

   public boolean getShowPan()
   {
       return mShowPanControls;
   }
   
   public BufferedImage getPanImage()
   {
      return mPanImage;
   }

   public Rectangle getPanScreenBounds()
   {
      return mPanScreenBounds;
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
      if (mShowPanControls)
      {
         mPanScreenImage.setOpacity(0.5);
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

   public void highlightPan()
   {
      mPanScreenImage.setOpacity(1);
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
   
   public void setShowPan(boolean show)
   {
       mShowPanControls = show;
       reset();
   }
   
   public void setShowZoomInZoomOutPan(boolean showZoomIn, boolean showZoomOut, boolean showPan)
   {
       mShowZoomInControls = showZoomIn;
       mShowZoomOutControls = showZoomOut;
       mShowPanControls = showPan;
       
       reset();
   }
           
}
