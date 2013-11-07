package com.hazydesigns.capstone.worldWindGazeInput;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionListener;
import rit.eyeTrackingAPI.ApplicationUtilities.EyeTrackingFilterListener;
import rit.eyeTrackingAPI.SmoothingFilters.Filter;

/**
 *
 * @author mhazlewood
 */
public class EyeTrackerListener extends EyeTrackingFilterListener
{

    private Robot mRobot;

    private boolean mListening = true;

    /**
     *
     * @param filter
     * @param actionListener
     * @param paintingFixations
     * @param display
     */
    public EyeTrackerListener(Filter filter,
                              ActionListener actionListener,
                              boolean paintingFixations,
                              int display)
    {
        super(filter, actionListener, paintingFixations, display);

        try
        {
            mRobot = new Robot();
        }
        catch (AWTException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * This function will be called whenever the filter owned by this class has
     * a new gaze point to report.
     *
     * @param newUserGazePoint, the new gaze point
     */
    @Override
    protected void newPoint(Point newUserGazePoint)
    {
        if (mListening)
        {
            mRobot.mouseMove(newUserGazePoint.x, newUserGazePoint.y);
        }
    }
    
    public void setListening(boolean listen)
    {
        mListening = listen;
    }

    /**
     *
     */
    @Override
    protected void updateCursorCoordinates()
    {

    }
}
