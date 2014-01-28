/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hazydesigns.capstone.worldWindGazeInput.ui;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.SurfacePolygon;
import java.util.ArrayList;

/**
 *
 * @author mhazlewood
 */
public class SurfaceArc extends SurfacePolygon implements SelectListener
{
    private final LatLon mCenterLocation;
    private final Angle mBeginAngle;
    private final Angle mEndAngle;
    private final float mDistFromCenter;
    private final float mThickness;
    
    final private Globe mGlobe;
    
    public SurfaceArc(  LatLon centerLocation,      
                        Angle beginAngle,           
                        Angle endAngle,             
                        float distFromCenter,       
                        float thickness,
                        WorldWindow wwd)
    {
        super();
        
        mCenterLocation = centerLocation;
        mBeginAngle = beginAngle;
        mEndAngle = endAngle;
        mDistFromCenter = distFromCenter;
        mThickness = thickness;
        mGlobe = wwd.getModel().getGlobe();
        wwd.addSelectListener(this);
        
        initialize();
    }
    
    private void initialize()
    {
        Iterable<LatLon> locations = makeLocations();
        super.setLocations(locations);
    }
    
    private Iterable<LatLon> makeLocations()
    {
        ArrayList<LatLon> locations = new ArrayList<>();    
        
        // Calculate points along the inner arc, at 1-degree resolution
        double dist = (mDistFromCenter - (mThickness / 2.0)) / mGlobe.getRadius();
        for (double angle = mBeginAngle.radians; angle <= mEndAngle.radians; angle += Math.toRadians(1.0))
        {
            locations.add(LatLon.fromRadians(   mCenterLocation.latitude.radians + (Math.cos(angle) * dist),
                                                mCenterLocation.longitude.radians + (Math.sin(angle) * dist)));
        }
        
        // Calculate points along the outer arc, at 1-degree resolution, in reverse order
        dist = (mDistFromCenter + (mThickness / 2.0)) / mGlobe.getRadius();
        for (double angle = mEndAngle.radians; angle >= mBeginAngle.radians; angle -= Math.toRadians(1.0))
        {   
            locations.add(LatLon.fromRadians(   mCenterLocation.latitude.radians + (Math.cos(angle) * dist),
                                                mCenterLocation.longitude.radians + (Math.sin(angle) * dist)));
        }
        
        return locations;
    }

    @Override
    public void selected(SelectEvent event)
    {
        if (event != null)
        {
            if (event.getEventAction().equals(SelectEvent.HOVER))
            {
                // Highlight, change geometry, etc. etc.
            }
        }
    }
}
