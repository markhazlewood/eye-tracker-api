package com.hazydesigns.capstone.worldWindGazeInput.ui;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLUtil;
import gov.nasa.worldwind.util.WWIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
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
public class GazeControlsLayer extends ViewControlsLayer
{
   private final String mFullUserInterfaceImagePath = "images/ui.png";   
   private final String mPanImagePath = "images/gaze_pan.png";
   private final String mZoomInImagePath = "images/gaze_zoomIn.png";
   private final String mZoomOutImagePath = "images/gaze_zoomOut.png";   
   
   private BufferedImage mFullUserInterfaceImage = null;
   private BufferedImage mPanImage = null;
   private BufferedImage mZoomInImage = null;
   private BufferedImage mZoomOutImage = null;
   
   public GazeControlsLayer()
   {
      super();

      showPanControls = true;
      showLookControls = false;
      showZoomControls = false;
      showHeadingControls = false;
      showPitchControls = false;
      showFovControls = false;
      showVeControls = false;

      panSize = 870;
      setScale(1);
      
      try
      {         
         mFullUserInterfaceImage = 
                 ImageIO.read((InputStream)WWIO.getFileOrResourceAsStream( mFullUserInterfaceImagePath, 
                                                                           this.getClass()));
         mPanImage = 
                 ImageIO.read((InputStream)WWIO.getFileOrResourceAsStream( mPanImagePath, 
                                                                           this.getClass()));
         mZoomInImage = 
                 ImageIO.read((InputStream)WWIO.getFileOrResourceAsStream( mZoomInImagePath, 
                                                                           this.getClass()));
         mZoomOutImage = 
                 ImageIO.read((InputStream)WWIO.getFileOrResourceAsStream( mZoomOutImagePath, 
                                                                           this.getClass()));
      }
      catch (IOException ex)
      {
         ex.printStackTrace();
      }
   }

   //TODO: Override initialize()?
   @Override
   protected void initialize(DrawContext dc)
   {
      if (this.initialized)
      {
         return;
      }

      // Setup user interface - common default attributes
      AnnotationAttributes ca = new AnnotationAttributes();
      ca.setAdjustWidthToText(AVKey.SIZE_FIXED);
      ca.setInsets(new Insets(0, 0, 0, 0));
      ca.setBorderWidth(0);
      ca.setCornerRadius(0);      
      ca.setBackgroundColor(new Color(0, 0, 0, 0));
      ca.setImageOpacity(.5);
      ca.setScale(scale);

      final String NOTEXT = "";
      final Point ORIGIN = new Point(0, 0);
      
      if (this.showPanControls)
      {
         // Pan
         controlPan = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
         controlPan.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
         controlPan.getAttributes().setImageSource(mPanImagePath);
         controlPan.getAttributes().setSize(new Dimension(panSize, panSize));
         this.addRenderable(controlPan);
      }
      
      if (this.showZoomControls)
      {
         // Zoom in
         controlZoomIn = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
         controlZoomIn.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_ZOOM_IN);
         controlZoomIn.getAttributes().setImageSource(mZoomInImagePath);
         controlZoomIn.getAttributes().setSize(new Dimension(191, 191));
         this.addRenderable(controlZoomIn);
         
         // Zoom out
         controlZoomOut = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
         controlZoomOut.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_ZOOM_OUT);
         controlZoomOut.getAttributes().setImageSource(mZoomOutImagePath);
         controlZoomOut.getAttributes().setSize(new Dimension(1920, 1080));
         this.addRenderable(controlZoomOut);
      }

      // Place controls according to layout and viewport dimension
      updatePositions(dc);

      this.initialized = true;
   }
   
   @Override
   protected Object getImageSource(String control)
    {
        if (control.equals(AVKey.VIEW_PAN))
            return mPanImagePath;
        else if (control.equals(AVKey.VIEW_ZOOM_IN))
            return mZoomInImagePath;
        else if (control.equals(AVKey.VIEW_ZOOM_OUT))
            return mZoomOutImagePath;
        else
           return super.getImageSource(control);
    }

   @Override
   protected void updatePositions(DrawContext dc)
   {
      updateControlPositions(dc);
   }
   
   public void updateControlPositions(DrawContext dc)
   {
      this.locationCenter = new Vec4(dc.getView().getViewport().width / 2, dc.getView().getViewport().height / 2, 0, 0);
      Point centerLocation = new Point(dc.getView().getViewport().width / 2, dc.getView().getViewport().height / 2);
      
      int xOffset = 0;
      int yOffset = 0;

      if (this.showPanControls)
      {
         //xOffset = controlPan.getAttributes().getSize().width / 2;
         yOffset = controlPan.getAttributes().getSize().height / 2;
         controlPan.setScreenPoint(new Point(centerLocation.x + xOffset, centerLocation.y - yOffset));
      }
      
      if (this.showZoomControls)
      {
//         xOffset = controlZoomIn.getAttributes().getSize().width / 2;
         yOffset = controlZoomIn.getAttributes().getSize().height / 2;
         controlZoomIn.setScreenPoint(new Point(centerLocation.x + xOffset, centerLocation.y - yOffset));
         
//         xOffset = controlZoomOut.getAttributes().getSize().width / 2;
         yOffset = controlZoomOut.getAttributes().getSize().height / 2;
         controlZoomOut.setScreenPoint(new Point(centerLocation.x + xOffset, centerLocation.y - yOffset));
      }

      this.referenceViewport = dc.getView().getViewport();
   }

   /**
    * Compute the screen location of the controls overall rectangle bottom right
    * corner according to either the location center if not null, or the screen
    * position.
    *
    * @param viewport the current viewport rectangle.
    * @param controlDimensions the overall controls rectangle
    *
    * @return the screen location of the bottom left corner - south west corner.
    */
   @Override
   protected Point computeLocation(Rectangle viewport, Rectangle controlDimensions)
   {
      double x;
      double y;

      if (this.locationCenter != null)
      {
         x = this.locationCenter.x - controlDimensions.width / 2;
         y = this.locationCenter.y - controlDimensions.height / 2;
      }
      else if (this.position.equals(AVKey.NORTHEAST))
      {
         x = viewport.getWidth() - controlDimensions.width - this.borderWidth;
         y = viewport.getHeight() - controlDimensions.height - this.borderWidth;
      }
      else if (this.position.equals(AVKey.SOUTHEAST))
      {
         x = viewport.getWidth() - controlDimensions.width - this.borderWidth;
         y = 0d + this.borderWidth;
      }
      else if (this.position.equals(AVKey.NORTHWEST))
      {
         x = 0d + this.borderWidth;
         y = viewport.getHeight() - controlDimensions.height - this.borderWidth;
      }
      else if (this.position.equals(AVKey.SOUTHWEST))
      {
         x = 0d + this.borderWidth;
         y = 0d + this.borderWidth;
      }
      else // use North East as default
      {
         x = viewport.getWidth() - controlDimensions.width - this.borderWidth;
         y = viewport.getHeight() - controlDimensions.height - this.borderWidth;
      }

      if (this.locationOffset != null)
      {
         x += this.locationOffset.x;
         y += this.locationOffset.y;
      }

      return new Point((int) x, (int) y);
   }
   
   public BufferedImage getFullUserInterfaceImage()
   {
      return mFullUserInterfaceImage;
   }
   
   public BufferedImage getPanImage()
   {
      return mPanImage;
   }
   
   public BufferedImage getZoomInImage()
   {
      return mZoomInImage;
   }
   
   public BufferedImage getZoomOutImage()
   {
      return mZoomOutImage;
   }
}
