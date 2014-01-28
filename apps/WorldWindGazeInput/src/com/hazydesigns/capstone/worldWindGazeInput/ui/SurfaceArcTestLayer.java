/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hazydesigns.capstone.worldWindGazeInput.ui;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;

/**
 *
 * @author mhazlewood
 */
public class SurfaceArcTestLayer extends RenderableLayer
{
    public SurfaceArcTestLayer(WorldWindow wwd)
    {
        this.addRenderable(new SurfaceArc(  LatLon.fromDegrees(43.0473, -76.1469), 
                                            Angle.fromDegrees(10.0), 
                                            Angle.fromDegrees(110.0), 
                                            5000, 
                                            500, 
                                            wwd));
        
        this.addRenderable(new SurfaceArc(  LatLon.fromDegrees(43.0473, -76.1469), 
                                            Angle.fromDegrees(130.0), 
                                            Angle.fromDegrees(230.0), 
                                            5000, 
                                            500, 
                                            wwd));
        
        this.addRenderable(new SurfaceArc(  LatLon.fromDegrees(43.0473, -76.1469), 
                                            Angle.fromDegrees(250.0), 
                                            Angle.fromDegrees(350.0), 
                                            5000, 
                                            500, 
                                            wwd));
        
        
        
        this.addRenderable(new SurfaceArc(  LatLon.fromDegrees(43.0473, -76.1469), 
                                            Angle.fromDegrees(10.0), 
                                            Angle.fromDegrees(110.0), 
                                            7000, 
                                            500, 
                                            wwd));
        
        this.addRenderable(new SurfaceArc(  LatLon.fromDegrees(43.0473, -76.1469), 
                                            Angle.fromDegrees(130.0), 
                                            Angle.fromDegrees(230.0), 
                                            7000, 
                                            2000, 
                                            wwd));
        
        this.addRenderable(new SurfaceArc(  LatLon.fromDegrees(43.0473, -76.1469), 
                                            Angle.fromDegrees(250.0), 
                                            Angle.fromDegrees(350.0), 
                                            7000, 
                                            500, 
                                            wwd));
    }
}
