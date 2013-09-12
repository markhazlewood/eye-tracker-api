package com.hazydesigns.capstone.worldWindGazeInput.ui;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.render.DrawContext;
import java.awt.Point;
import java.awt.Rectangle;

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

   public GazeControlsLayer()
   {
      super();

      setScale(2);
   }

   //TODO: Override path to UI images
   /*
    protected final static String IMAGE_PAN = "images/view-pan-64x64.png";
    protected final static String IMAGE_LOOK = "images/view-look-64x64.png";
    protected final static String IMAGE_HEADING_LEFT = "images/view-heading-left-32x32.png";
    protected final static String IMAGE_HEADING_RIGHT = "images/view-heading-right-32x32.png";
    protected final static String IMAGE_ZOOM_IN = "images/view-zoom-in-32x32.png";
    protected final static String IMAGE_ZOOM_OUT = "images/view-zoom-out-32x32.png";
    protected final static String IMAGE_PITCH_UP = "images/view-pitch-up-32x32.png";
    protected final static String IMAGE_PITCH_DOWN = "images/view-pitch-down-32x32.png";
    protected final static String IMAGE_FOV_NARROW = "images/view-fov-narrow-32x32.png";
    protected final static String IMAGE_FOV_WIDE = "images/view-fov-wide-32x32.png";
    protected final static String IMAGE_VE_UP = "images/view-elevation-up-32x32.png";
    protected final static String IMAGE_VE_DOWN = "images/view-elevation-down-32x32.png";
    */
   
   //TODO: Override initialize()?
   
   //TODO: Override updatePositions()
   
   @Override
   protected void updatePositions(DrawContext dc)
   {
      boolean horizontalLayout = this.layout.equals(AVKey.HORIZONTAL);

      // horizontal layout: pan button + look button beside 2 rows of 4 buttons
      int width = (showPanControls ? panSize : 0)
              + (showLookControls ? panSize : 0)
              + (showZoomControls ? buttonSize : 0)
              + (showHeadingControls ? buttonSize : 0)
              + (showPitchControls ? buttonSize : 0)
              + (showFovControls ? buttonSize : 0)
              + (showVeControls ? buttonSize : 0);
      int height = Math.max(panSize, buttonSize * 2);

      width = (int) (width * scale);
      height = (int) (height * scale);

      int xOffset = 0;
      int yOffset = (int) (buttonSize * scale);

      if (!horizontalLayout)
      {
         // vertical layout: pan button above look button above 4 rows of 2 buttons
         int temp = height;
         //noinspection SuspiciousNameCombination
         height = width;
         width = temp;
         xOffset = (int) (buttonSize * scale);
         yOffset = 0;
      }

      int halfPanSize = (int) (panSize * scale / 2);
      int halfButtonSize = (int) (buttonSize * scale / 2);

      this.locationCenter = new Vec4(dc.getView().getViewport().width/2, dc.getView().getViewport().height/2, 0, 0);
      Rectangle controlsRectangle = new Rectangle(width, height);
      Point controlsLocation = computeLocation(dc.getView().getViewport(), controlsRectangle);

      // Layout start point
      int x = controlsLocation.x;
      int y = horizontalLayout ? controlsLocation.y : controlsLocation.y + height;

      if (this.showPanControls)
      {
         if (!horizontalLayout)
         {
            y -= (int) (panSize * scale);
         }
         controlPan.setScreenPoint(new Point(x + halfPanSize, y));
         if (horizontalLayout)
         {
            x += (int) (panSize * scale);
         }
      }
      if (this.showLookControls)
      {
         if (!horizontalLayout)
         {
            y -= (int) (panSize * scale);
         }
         controlLook.setScreenPoint(new Point(x + halfPanSize, y));
         if (horizontalLayout)
         {
            x += (int) (panSize * scale);
         }
      }
      if (this.showZoomControls)
      {
         if (!horizontalLayout)
         {
            y -= (int) (buttonSize * scale);
         }
         controlZoomIn.setScreenPoint(new Point(x + halfButtonSize + xOffset, y + yOffset));
         controlZoomOut.setScreenPoint(new Point(x + halfButtonSize, y));
         if (horizontalLayout)
         {
            x += (int) (buttonSize * scale);
         }
      }
      if (this.showHeadingControls)
      {
         if (!horizontalLayout)
         {
            y -= (int) (buttonSize * scale);
         }
         controlHeadingLeft.setScreenPoint(new Point(x + halfButtonSize + xOffset, y + yOffset));
         controlHeadingRight.setScreenPoint(new Point(x + halfButtonSize, y));
         if (horizontalLayout)
         {
            x += (int) (buttonSize * scale);
         }
      }
      if (this.showPitchControls)
      {
         if (!horizontalLayout)
         {
            y -= (int) (buttonSize * scale);
         }
         controlPitchUp.setScreenPoint(new Point(x + halfButtonSize + xOffset, y + yOffset));
         controlPitchDown.setScreenPoint(new Point(x + halfButtonSize, y));
         if (horizontalLayout)
         {
            x += (int) (buttonSize * scale);
         }
      }
      if (this.showFovControls)
      {
         if (!horizontalLayout)
         {
            y -= (int) (buttonSize * scale);
         }
         controlFovNarrow.setScreenPoint(new Point(x + halfButtonSize + xOffset, y + yOffset));
         controlFovWide.setScreenPoint(new Point(x + halfButtonSize, y));
         if (horizontalLayout)
         {
            x += (int) (buttonSize * scale);
         }
      }
      if (this.showVeControls)
      {
         if (!horizontalLayout)
         {
            y -= (int) (buttonSize * scale);
         }
         controlVeUp.setScreenPoint(new Point(x + halfButtonSize + xOffset, y + yOffset));
         controlVeDown.setScreenPoint(new Point(x + halfButtonSize, y));
         if (horizontalLayout)
         {
            x += (int) (buttonSize * scale);
         }
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
}
