/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>ItemIcon</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLItemIcon.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLItemIcon extends KMLAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLItemIcon(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getHref()
    {
        return (String) this.getField("href");
    }

    public String getState()
    {
        return (String) this.getField("state");
    }
}
