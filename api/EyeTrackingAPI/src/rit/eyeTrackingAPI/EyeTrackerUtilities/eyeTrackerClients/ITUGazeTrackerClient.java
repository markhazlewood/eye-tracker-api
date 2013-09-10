package rit.eyeTrackingAPI.EyeTrackerUtilities.eyeTrackerClients;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import rit.eyeTrackingAPI.DataConstructs.GazePoint;

public class ITUGazeTrackerClient extends EyeTrackerClient
{

   private DatagramSocket ds;
   private DatagramPacket dp;
   private boolean stop = false;
   private boolean toggleOn = true;
   private InetAddress iViewX = null;
   private InetAddress ituGT;

   public ITUGazeTrackerClient(GazePoint cursor)
   {
      super(cursor);
   }

   @Override
   protected void clientOperation()
   {
      if (!connected)
      {
         connect();
      }

      while (!stop)
      {
         String gtString = null;

         try
         {
            ds.receive(dp);
            gtString = new String(dp.getData(), 0, dp.getLength());
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }

         if (gtString != null)
         {
            String[] tokens = gtString.split(" ");

            if (toggleOn)
            {
               mGazePointContainer.setCoordinates((int) Double.parseDouble(tokens[2]), (int) Double.parseDouble(tokens[3]));
            }
         }
      }
   }

   @Override
   public void connect()
   {
      byte[] b = new byte[50];
      byte[] address = new byte[4];
      address[0] = new Integer(127).byteValue();
      address[1] = new Integer(0).byteValue();
      address[2] = new Integer(0).byteValue();
      address[3] = new Integer(1).byteValue();

      dp = new DatagramPacket(b, b.length);

      try
      {
         ituGT = InetAddress.getByAddress(address);
      }
      catch (UnknownHostException e)
      {
         System.err.println("Cannot find iViewX system");
         for (StackTraceElement ste : e.getStackTrace())
         {
            System.out.println(ste.toString());
         }
      }

      //ds = new DatagramSocket(7777);
      try
      {
         ds = new DatagramSocket(6666);
      }
      catch (BindException ex)
      {
         System.err.println("failed to connect to socket, another program may be using it.");
      }
      catch (SocketException e)
      {
         for (StackTraceElement ste : e.getStackTrace())
         {
            System.out.println(ste.toString());
         }
      }
   }

   @Override
   public void disconnect()
   {
      ds.close();
      connected = false;
   }

   @Override
   public boolean isConnected()
   {
      return connected;
   }

   @Override
   public void requestStop()
   {
      stop = true;
   }

   @Override
   public void toggle()
   {
      if (connected)
      {
         disconnect();
      }
      else
      {
         connect();
      }
   }
}
