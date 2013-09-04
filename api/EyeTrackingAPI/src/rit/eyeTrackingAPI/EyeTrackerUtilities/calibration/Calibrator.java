package rit.eyeTrackingAPI.EyeTrackerUtilities.calibration;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * An abstract class for creating a calibration application for a specific type
 * of eye tracking device, for example the SMI RED250
 *
 * @author Corey Engelman
 *
 */
public abstract class Calibrator
{

   protected static final int NUM_VALIDATION_POINTS = 4;
   protected DatagramSocket ds;
   protected DatagramPacket dp;
   protected boolean connected = false;
   protected InetAddress eyeTrackerAddress = null;
   protected Integer stimulusWidth = 1920;
   protected Integer stimulusHeight = 1080;
   protected String eyeTrackerReply;
   protected int localPort;
   protected int eyeTrackerPort;
   protected Point[] calibrationPoints;
   protected int calibrationPointsIndex = 0;
   protected JFrame calibrationArea;
   protected CalibrationCircle calibrationCircle;
   protected int stimulusIndex = 1;
   protected UtilityKeyListener keyListener = new UtilityKeyListener();

   /*
    * Used to format doubles
    */
   protected DecimalFormat df;

   /**
    * Constructs a utility object that will try to communicate with an eye
    * tracker system at the specified IP address on the specified port. Will
    * bind to the socket \"localPort\" when sending/receiving UDP packets.
    * Screen width and height must be passed in so that when performing a
    * calibration, eye tracker can be told how big the screen being used is.
    *
    * @param iViewIP - the ip of the eye tracker machine as a string (example
    * \"129.21.175.1\")
    * @param Port - the port the eye tracker machine is usign (example 4455)
    * @param localPort - the local port that eye tracker will send packets to
    * @param screenWidth - width of the stimulus screen as an int
    * @param screenHeight - height of the stimulus screen as an int
    */
   public Calibrator(String eyeTrackerIP, int eyeTrackerPort, int localPort, int screenWidth, int screenHeight, int stimulusDisplayIndex)
   {
      //copy parameters into field values
      this.localPort = localPort;
      this.eyeTrackerPort = eyeTrackerPort;
      this.stimulusWidth = screenWidth;
      this.stimulusHeight = screenHeight;
      this.stimulusIndex = stimulusDisplayIndex;

      //set the decimal format to show two digits beyond the decimal point
      df = new DecimalFormat("###.##");

      //create a char array from the iViewIP passed in
      char[] temp = eyeTrackerIP.toCharArray();

      //replace .'s with spaces so that the array can easily be split
      for (int i = 0; i < temp.length; i++)
      {
         if (temp[i] == '.')
         {
            temp[i] = ' ';
         }
      }

      //split string into tokens
      eyeTrackerIP = new String(temp);
      String[] IPTokens = eyeTrackerIP.split(" ");

      //create an array of bytes that represents the ip address of the eye tracker system
      byte[] Address = new byte[4];
      Address[0] = new Integer(IPTokens[0]).byteValue();
      Address[1] = new Integer(IPTokens[1]).byteValue();
      Address[2] = new Integer(IPTokens[2]).byteValue();
      Address[3] = new Integer(IPTokens[3]).byteValue();

      //try to get the address from the byte array, print out stack trace and a message saying
      //could not find the eye tracker system if this operation fails
      try
      {
         eyeTrackerAddress = InetAddress.getByAddress(Address);
      }
      catch (UnknownHostException e)
      {
         e.printStackTrace();
         System.err.println("could not find eye tracker system at specified IP: " + eyeTrackerIP);
      }
   }

   /**
    * Calibrate performs a calibration, mimicking the protocol SMI Experiment
    * Center uses for calibration. This means sending the screen resolution.
    * Then requesting configuration, which will cause eye tracker to return the
    * set of points to be used in the calibration. Auto accept is turned on,
    * which means eye tracker will automatically move to the next point.
    * Calibration is then started. The first point waits for manual accept (from
    * space bar in our case). All following points are auto accepted. eye
    * tracker sends an accept string back every time it advances. Each time
    * these strings are received, the calibration circle is moved to the next
    * point. After calibration, a validation is done automatically to check
    * accuracy.
    *
    * @return - the resulting string array produced by the validation done after
    * calibration (see validation method for more details)
    */
   public abstract String[][] calibrate();

   /**
    * Performs validation mimicking the protocol SMI Experiment center uses.
    * Verifies the point at 1/4 the screen width and height, then 3/4 the screen
    * width and height, then 3/4 width 1/4 height, and finally 1/4 width and 3/4
    * height. The results sent back from eye tracker are stored in an array and
    * returned.
    *
    * @return the resulting string array produced by validation, eye tracker
    * sends back strings containing the average x,y coordinate of the users gaze
    * during validation, and the std dev in the x and y directions in visual
    * degrees.
    */
   public abstract String[][] validate();

   protected double[] calculateDeviations(String[][] validationStrings)
   {
      double[] meanDeviation = new double[2];
      double xRunningSum = 0;
      double yRunningSum = 0;

      //loop through the results and add the x and y values to the corresponding running sum
      for (String str : validationStrings[1])
      {
         String[] tokens = str.split(" ");

         xRunningSum += new Double(tokens[3].substring(0, tokens[3].length() - 2));
         yRunningSum += new Double(tokens[4].substring(0, tokens[4].length() - 2));

      }

      //calculate the mean deviation by dividing by the length of the results array
      meanDeviation[0] = xRunningSum / validationStrings.length;
      meanDeviation[1] = yRunningSum / validationStrings.length;

      return meanDeviation;
   }


   /*
    * Advances the calibration circle passed in as a parameter to the next calibration point in
    * the array of calibration points received from eye tracker.
    * 
    * @param calibrationCircle - the CalibrationCircle object to advance
    */
   private void advanceToNextCalibrationPoint(CalibrationCircle calibrationCircle)
   {
      //increment the index
      calibrationPointsIndex++;

      //if we are not past the end of the array
      if (calibrationPointsIndex < calibrationPoints.length)
      {
         advanceToNextCalibrationPoint(calibrationCircle, calibrationPoints[calibrationPointsIndex - 1], calibrationPoints[calibrationPointsIndex]);
      }
   }

   protected abstract void advanceToNextCalibrationPoint(CalibrationCircle calibrationCircle, Point oldPoint, Point newPoint);

   /*
    * Updates the calibration area. If the calibration area is null, creates a new one.
    */
   protected abstract void updateCalibrationArea();

   /*
    * This method takes in the results array from a validation and displays grey markers where the validation points were and red
    * markers where eye tracker measure the users gaze. This is done using the ValidationVisualization inner class.
    */
   protected void showResults(String[][] results)
   {
      calibrationArea.remove(calibrationCircle);

      //create a new validation visualizer
      ValidationVisualization validationVisualizer = new ValidationVisualization();

      int i = 0;

      //loop through the results and add user points for each string
      for (String resultString : results[1])
      {
         String[] resultTokens = resultString.split(" ");

         validationVisualizer.addUserPoint(new Integer(resultTokens[1].substring(0, resultTokens[1].indexOf("."))), new Integer(resultTokens[2].substring(0, resultTokens[2].indexOf("."))));
         i++;
      }

      for (String pointUsed : results[0])
      {
         String[] tokens = pointUsed.split(" ");
         validationVisualizer.addCheckPoint(new Integer(tokens[0]), new Integer(tokens[1]));
      }

      //add the visualizer
      calibrationArea.getContentPane().add(validationVisualizer);

      //update
      calibrationArea.validate();
      calibrationArea.repaint();
   }

   /*
    * Change the calibration circles location manually using x, y coordinates. 
    * @param calibrationCircle
    * @param x - the x coordinate
    * @param y - the y coordinate
    */
   protected void setCalibrationCircleLocation(CalibrationCircle calibrationCircle, int x, int y)
   {
      //try to set the coordinates and validate and repaint
      try
      {
         calibrationCircle.setCoordinates(new Point(x, y));
         calibrationCircle.validate();
         calibrationCircle.repaint();
      }
      catch (NullPointerException ex)
      {
         System.err.println("Calibration not started");
      }
   }

   /**
    * Try to bind to the socket eye tracker will be sending packets to
    */
   public synchronized void connect()
   {
      System.out.println("tried to connect");

      try
      {
         ds = new DatagramSocket(localPort);
         connected = true;
         System.out.println(connected);
      }
      catch (BindException ex)
      {
         System.err.println("failed to bind to socket");
         connected = false;
      }
      catch (SocketException e)
      {
         e.printStackTrace();
         connected = false;
      }
   }

   /**
    * returns the value of the connected flag. Note that this only means that
    * the local machine is connected to the proper port/IP, not that the
    * connection with the eye tracking device has been established. To see if
    * connection with the eye tracker has been established, use tesConnection().
    *
    * @return - true if connected to the proper socket, false otherwise
    */
   public synchronized boolean isConnected()
   {
      return connected;
   }

   /**
    * Stop eye tracker with remote command ET_EST, then close datagram socket
    * and set connected flag to false
    */
   public abstract void disconnect();

   /*
    * A runnable for forcing the calibration circle to update itself. Needed when the calibration runs 
    * after a retry command is received from the user, because the circle is in a strange location
    */
   class ForceCalibrationCircleUpdate implements Runnable
   {

      private CalibrationCircle calibrationCircle;

      public ForceCalibrationCircleUpdate(CalibrationCircle calibrationCircle)
      {
         this.calibrationCircle = calibrationCircle;
      }

      @Override
      public void run()
      {
         calibrationCircle.validate();
         calibrationCircle.repaint();
      }
   }

   /**
    * Mutator method for the stimulus dimensions. Use this to set/update the
    * stimulus dimensions (i.e resolution of the screen being used)
    *
    * @param stimulusWidth - the width of the stimulus display
    * @param stimulusHeight - the height of the stimulus display
    */
   public void setStimulusDimensions(int stimulusWidth, int stimulusHeight)
   {
      this.stimulusWidth = stimulusWidth;
      this.stimulusHeight = stimulusHeight;
   }

   /**
    * Means of setting/updating both the ip and port of the eye tracker machine
    *
    * @param iViewIP - the IP address of the eye tracker machine
    * @param iViewPort - the port the eye tracker machine is using
    */
   public void setIViewIPAndPort(String iViewIP, Integer iViewPort)
   {
      //create a char array from the iViewIP passed in
      char[] temp = iViewIP.toCharArray();

      //replace .'s with spaces so that the array can easily be split
      for (int i = 0; i < temp.length; i++)
      {
         if (temp[i] == '.')
         {
            temp[i] = ' ';
         }
      }

      //split string into tokens
      iViewIP = new String(temp);
      String[] iViewIPTokens = iViewIP.split(" ");

      //create an array of bytes that represents the ip address of the eye tracker system
      byte[] Address = new byte[4];
      Address[0] = new Integer(iViewIPTokens[0]).byteValue();
      Address[1] = new Integer(iViewIPTokens[1]).byteValue();
      Address[2] = new Integer(iViewIPTokens[2]).byteValue();
      Address[3] = new Integer(iViewIPTokens[3]).byteValue();

      //try to get the address from the byte array, print out stack trace and a message saying
      //could not find the eye tracker system if this operation fails
      try
      {
         eyeTrackerAddress = InetAddress.getByAddress(Address);
      }
      catch (UnknownHostException e)
      {
         e.printStackTrace();
         System.err.println("could not find eye tracker system at specified IP: " + iViewIP);
      }

      this.eyeTrackerPort = iViewPort;
   }

   /**
    * A means of setting/updating the local port
    *
    * @param localPort - the port eye tracker is sending packets to
    */
   public void setLocalPort(Integer localPort)
   {
      this.localPort = localPort;
   }

   /**
    * A means of testing the whether eye tracker system is reachable. Connect
    * simply means that the program successfully connected to the socket on the
    * local machine. To test if the eye tracker is reachable, ping the machine,
    * then wait for ten seconds.
    *
    * @return boolean - true if eye tracker replies within ten seconds, false
    * otherwise
    */
   public abstract boolean testConnection();

   /*
    * A class that creates a graphical representation of the results of the validation. Displays black marks where the
    * validation points were and red marks where the eye tracker system measured the users gaze.
    */
   class ValidationVisualization extends JComponent
   {

      private ArrayList<Point> greyCheckPoints;
      private ArrayList<Point> redUserPoints;
      private static final int LINE_LENGTH = 20;

      public ValidationVisualization()
      {
         greyCheckPoints = new ArrayList<Point>();
         redUserPoints = new ArrayList<Point>();
      }

      //override the paint method, paint all marks.
      public void paint(Graphics g)
      {
         Graphics2D g2d = (Graphics2D) g;

         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2d.setStroke(new BasicStroke(3f));

         g2d.setPaint(new Color(0, 0, 0));

         for (Point pnt : greyCheckPoints)
         {
            g2d.drawLine(pnt.x - (LINE_LENGTH / 2), pnt.y, pnt.x + (LINE_LENGTH / 2), pnt.y);
            g2d.drawLine(pnt.x, pnt.y - (LINE_LENGTH / 2), pnt.x, pnt.y + (LINE_LENGTH / 2));
         }

         g2d.setPaint(new Color(250, 0, 0));

         for (Point pnt : redUserPoints)
         {
            g2d.drawLine(pnt.x - (LINE_LENGTH / 2), pnt.y, pnt.x + (LINE_LENGTH / 2), pnt.y);
            g2d.drawLine(pnt.x, pnt.y - (LINE_LENGTH / 2), pnt.x, pnt.y + (LINE_LENGTH / 2));
         }
         //g2d.drawImage();
      }

      /*
       * Adds a new validation point. This is where the validation was performed. Will be displayed as a black
       * mark.
       * @param x - the x coordinate
       * @param y - the y coordinate
       */
      public void addCheckPoint(int x, int y)
      {
         greyCheckPoints.add(new Point(x, y));
      }

      /*
       * Adds a new user point. This is where the users gaze was measured. Will be displayed as a red
       * mark.
       * @param x - the x coordinate
       * @param y - the y coordinate
       */
      public void addUserPoint(int x, int y)
      {
         redUserPoints.add(new Point(x, y));
      }

   }

   class UtilityKeyListener implements KeyListener
   {

      @Override
      public void keyPressed(KeyEvent e)
      {
      }

      @Override
      public void keyReleased(KeyEvent e)
      {
         if (e.getKeyCode() == KeyEvent.VK_SPACE)
         {
            onSpaceBarPressed();
         }
      }

      @Override
      public void keyTyped(KeyEvent e)
      {
      }

   }

   protected abstract void onSpaceBarPressed();

   public void setStimulusDisplay(int stimulusIndex)
   {
      this.stimulusIndex = stimulusIndex;
   }
}
