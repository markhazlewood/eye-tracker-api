package com.hazydesigns.capstone.worldWindGazeInput.ui;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenImage;
import gov.nasa.worldwind.render.Size;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

public class ManeuverRecommendationLayer extends RenderableLayer
{
	// TODO: This ...
	
	private boolean initialized = false;
	
	final private ScreenImage backgroundScreenImage = new ScreenImage();	
	final private String backgroundImagePath = "images/exclaim.png";
   
   private AnnotationAttributes contentAttributes = new AnnotationAttributes();;
   private MyScreenAnnotation contentScreenAnnotation;
   
   private Rectangle referenceViewport;
	
	public ManeuverRecommendationLayer()
	{
		
	}
	
	protected void initialize(DrawContext dc)
	{
		if (!this.initialized)
		{
			this.backgroundScreenImage.setImageSource(this.backgroundImagePath);
			this.backgroundScreenImage.setOpacity(0.85);
         this.backgroundScreenImage.setSize(Size.fromPixels(48, 48));
			this.addRenderable(this.backgroundScreenImage);
         
         Font textFont = Font.decode("Verdana-BOLD-16");
         
         this.contentAttributes.setBackgroundColor(new Color(231, 100, 86, 255));
         this.contentAttributes.setFont(textFont);
         this.contentAttributes.setSize(new Dimension(800, 60));
         this.contentAttributes.setAdjustWidthToText(AVKey.SIZE_FIXED);
         this.contentAttributes.setTextColor(new Color(236, 240, 241));
         this.contentAttributes.setBorderColor(new Color(135, 135, 135, 255));
         this.contentAttributes.setCornerRadius(5);
         this.contentAttributes.setInsets(new Insets(20, 50, 3, 5));
         
         this.contentScreenAnnotation = new MyScreenAnnotation("Turn LEFT, 20 degrees", new Point(0, 0), this.contentAttributes);
         this.addRenderable(this.contentScreenAnnotation);
         
         Object test = this.contentAttributes.getBackgroundTexture(dc);
         
         updatePositions(dc);
			this.initialized = true;
		}
	}
	
	@Override
	public void doRender(DrawContext dc)
	{
		if (!this.initialized)
		{
			this.initialize(dc);
		}
      
      if (!this.referenceViewport.equals(dc.getView().getViewport()))
      {
         updatePositions(dc);
      }
		
		super.doRender(dc);
		
	}
   
   protected void updatePositions(DrawContext dc)
   {
      Point centerLocation = new Point(dc.getView().getViewport().width / 2, dc.getView().getViewport().height / 2);
      
      Point imageLoc = new Point(centerLocation.x - 400 + 24, 30);
      this.backgroundScreenImage.setScreenLocation(imageLoc);
      
      Point textLoc = new Point( centerLocation.x, 
                                 dc.getView().getViewport().height - this.contentScreenAnnotation.getBounds(dc).height);
      this.contentScreenAnnotation.setScreenPoint(textLoc);
      
      this.referenceViewport = dc.getView().getViewport();
   }
}
