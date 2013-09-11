package com.hazydesigns.capstone.worldWindGazeInput;

import gov.nasa.worldwind.layers.ViewControlsLayer;

/**
 * A customization of the WorldWind-standard {@link ViewControlsLayer}. Overrides
 * a minimal amount of the base class to load a different UI suitable for gaze
 * input. Extending this was very useful as the code in ViewControlsSelectListener
 * did not need to be updated or modified, only the UI presentation changes.
 * 
 * @author Mark Hazlewood
 * 
 * @see gov.nasa.worldwind.layers.ViewControlsLayer
 * @see gov.nasa.worldwind.layers.ViewControlsSelectListener
 */
public class GazeControlsLayer extends ViewControlsLayer
{
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
}
