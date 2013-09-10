package rit.eyeTrackingAPI.EyeTrackerUtilities.eyeTrackerClients;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import rit.eyeTrackingAPI.DataConstructs.GazePoint;

/**
 * A class used to set up communication with the mTrackerServerCommandAddress
 * eye tracking device. This class runs on a seperate thread. Necessary, because
 * otherwise issues occur when communicating with the
 * mTrackerServerCommandAddress and trying to draw to the GLCanvas at the same
 * time.
 *
 * @author Corey Engelman
 *
 */
public class IViewXClient extends EyeTrackerClient
{
   // <editor-fold defaultstate="expanded" desc="Private Members">

   private boolean stop = false;
   private final boolean toggleOn = true;
   private InetAddress mTrackerServerCommandAddress = null;

   // Set up some members to handle the connection to the tracker, with defaults
   // (Should be able to change these before connecting, or between connections)
   private String mTrackerServerIP = "127.0.0.1";
   private final int mTrackerServerCommandPort = 6665;
   private final int mIncomingDataBindPort = 7777;
   private final int mDesiredDataRate = 60;

   // Two sockets, one to receive incoming data and one to send commands to the tracker
   private DatagramSocket mSendSocket;
   private DatagramSocket mReceiveSocket;

   // Packet object used to store incoming data from the tracker
   private DatagramPacket mUdpPacket;

   private BufferedWriter dataDump = null;
   private FileWriter writer = null;

   // </editor-fold>
   // <editor-fold defaultstate="expanded" desc="Constants">
   private final String OUTPUT_PATH = "C:" + File.separator + "dataDump.txt";

   private final int MAX_RESPONSE_SIZE_BYTES = 50;

   private final String PING_COMMAND = "ET_PNG\n";
   private final String DISCONNECT_COMMAND = "ET_EST\n";
   private final String FORMAT_REQUEST_COMMAND = "ET_FRM \"%ET %TS %SX %SY\"\n";
   private final String DATA_REQUEST_COMMAND = "ET_STR " + Integer.toString(mDesiredDataRate) + "\n";
   private final String RESPONSE_DATA_STRING = "ET_SPL";

   // </editor-fold>
   
   // <editor-fold defaultstate="expanded" desc="Constructor(s)">
   /**
    * Constructor for the IViewXComm class. Sets the cursor object associated
    * with this IViewXComm instance to cursor.
    *
    * @param filter - the cursor associated with this IViewXComm
    */
   public IViewXClient(GazePoint cursor)
   {
      super(cursor);

      try
      {
         writer = new FileWriter(OUTPUT_PATH);
      }
      catch (IOException e)
      {
         for (StackTraceElement ste : e.getStackTrace())
         {
            System.out.println(ste.toString());
         }
      }

      dataDump = new BufferedWriter(writer);
   }

   /**
    * Constructor for the IViewXComm class. Sets the cursor object associated
    * with this IViewXComm instance to cursor and the ip of the system running
    * mTrackerServerCommandAddress
    *
    * @param filter - the cursor associated with this IViewXComm
    * @param ipAddress - the ipAddress as an array of bytes
    */
   public IViewXClient(GazePoint cursor, String ipAddress)
   {
      super(cursor);

      mTrackerServerIP = ipAddress;

      try
      {
         writer = new FileWriter(OUTPUT_PATH);
      }
      catch (IOException e)
      {
         for (StackTraceElement ste : e.getStackTrace())
         {
            System.out.println(ste.toString());
         }
      }

      dataDump = new BufferedWriter(writer);
   }

   // </editor-fold>
   // <editor-fold defaultstate="expanded" desc="Working Functions">
   /**
    * Overrides the run method. First sets up UDP connection with the
    * mTrackerServerCommandAddress, then enters a loop and continuously tries to
    * receive data from the mTrackerServerCommandAddress eye tracking device
    * until the requestStop() method is called.
    */
   @Override
   public void run()
   {
      clientOperation();
   }

   @Override
   public synchronized void disconnect()
   {
      byte[] stopCommand = createIViewXCommandFromString(DISCONNECT_COMMAND);
      try
      {
         mSendSocket.send(new DatagramPacket(stopCommand, stopCommand.length));
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      //requestStop();
      mSendSocket.close();
      mReceiveSocket.close();
      connected = false;
   }

   @Override
   public synchronized void connect()
   {
      System.out.println("Initiating connection handshake ...");

      try
      {
         mTrackerServerCommandAddress = InetAddress.getByName(mTrackerServerIP);
      }
      catch (UnknownHostException e)
      {
         System.err.println("Cannot find iViewX system");
         System.out.println(e.getStackTrace());
      }

      try
      {
         // Initialize both send and receive sockets
         mReceiveSocket = new DatagramSocket(mIncomingDataBindPort);
         mSendSocket = new DatagramSocket();

         connected = true;
         System.out.println(connected);
      }
      catch (BindException ex)
      {
         System.err.println("failed to create send/receive sockets, another program may be using them.");
         connected = false;
      }
      catch (SocketException e)
      {
         e.printStackTrace();
         connected = false;
      }

      try
      {
         // Ping the tracker to make sure someone is home ...
         System.out.println("Pinging iViewX server @ " + mTrackerServerIP + ":" + mTrackerServerCommandPort + " ...");
         byte[] msg = createIViewXCommandFromString(PING_COMMAND);
         mSendSocket.send(new DatagramPacket(msg, msg.length, mTrackerServerCommandAddress, mTrackerServerCommandPort));

         // Grab ping response, should be immediate
         byte[] recvBuff = new byte[MAX_RESPONSE_SIZE_BYTES];
         DatagramPacket recvPacket = new DatagramPacket(recvBuff, recvBuff.length);
         mReceiveSocket.receive(recvPacket);

         String pingResponse = new String(recvPacket.getData(), 0, recvPacket.getLength());// receive
         System.out.println(">>> Ping response \t" + pingResponse);

         /* send format of data to mTrackerServerCommandAddress
          * Format is eye type, time stamp, gaze x, gaze y
          * each format token needs a % symbol in front of it, and the entire
          * format string needs to be in quotes, hence the \" 
          */
         msg = createIViewXCommandFromString(FORMAT_REQUEST_COMMAND);
         mSendSocket.send(new DatagramPacket(msg, msg.length, mTrackerServerCommandAddress, mTrackerServerCommandPort));

         //send request for data stream to mTrackerServerCommandAddress, 250 notes the sampling rate
         msg = createIViewXCommandFromString(DATA_REQUEST_COMMAND);
         mSendSocket.send(new DatagramPacket(msg, msg.length, mTrackerServerCommandAddress, mTrackerServerCommandPort));

      }
      catch (SocketException ex)
      {
         if (ex.getClass().getName().equals("BindException"))
         {
            System.err.println("Not connected to an Eye Tracking device");
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Toggles eye tracking on and off
    */
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

   /**
    * A means of stopping this thread.
    */
   @Override
   public void requestStop()
   {
      stop = true;
      connected = false;
      try
      {
         dataDump.close();
         writer.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public byte[] createIViewXCommandFromString(String command)
   {
      char[] commandAsCharacters = command.toCharArray();
      byte[] commandAsBytes = new byte[commandAsCharacters.length];

      for (int i = 0; i < commandAsCharacters.length; i++)
      {
         commandAsBytes[i] = (byte) commandAsCharacters[i];
      }

      return commandAsBytes;
   }

   @Override
   protected void clientOperation()
   {
      if (!connected)
      {
         connect();
      }

      String responseString = "";
      try
      {
         byte[] recvBuff = new byte[MAX_RESPONSE_SIZE_BYTES];
         mUdpPacket = new DatagramPacket(recvBuff, recvBuff.length);
         stop = false;

         while (!stop)
         {
            if (connected)
            {
               if (responseString != null)
               {

                  try
                  {
                     if (mReceiveSocket != null)
                     {
                        mReceiveSocket.receive(mUdpPacket);
                     }

                     responseString = new String(mUdpPacket.getData(), 0, mUdpPacket.getLength());   // receive
                     // file
                     // contents
                     if (responseString.contains(RESPONSE_DATA_STRING))
                     {
                        //System.out.println("From eye tracker " + responseString);
                        String[] tokens = responseString.split(" ");

                        if (toggleOn)
                        {
                           int screenX = 0;
                           int screenY = 0;

                           // Incoming data format should be as follows:
                           //                           [ET]                     [TS]                           [SX]                                               [SY]
                           //       "ET_SPL { (l)eft, (r)ight, (b)inocular } {timestamp} {X position of left eye} {X position of right eye} {Y position of left eye} {Y position of right eye}"
                           //
                           // So here we want to get a single X,Y screen location by averaging the two reported eye positions
                           try
                           {
                              screenX = (Integer.parseInt(tokens[3].trim()) + Integer.parseInt(tokens[4].trim())) / 2;
                              screenY = (Integer.parseInt(tokens[5].trim()) + Integer.parseInt(tokens[6].trim())) / 2;

                              // Report a new gaze point to the "cursor" object assigned
                              // to this listener
                              cursor.setCoordinates(screenX, screenY);

                           }
                           catch (NumberFormatException ex)
                           {
                              screenX = (Integer.parseInt(tokens[2].trim()) + Integer.parseInt(tokens[3].trim())) / 2;
                              screenY = (Integer.parseInt(tokens[4].trim()) + Integer.parseInt(tokens[5].trim())) / 2;

                              // Report a new gaze point to the "cursor" object assigned
                              // to this listener
                              cursor.setCoordinates(screenX, screenY);
                           }
                           catch (ArrayIndexOutOfBoundsException ex)
                           {
                              System.err.println("out of bounds exception");
                           }
                        }
                     }
                  }
                  catch (SocketException ex)
                  {
                     //System.err.println("Disconnected");
                  }
               }
            }
         }
      }
      catch (IOException ex)
      {
         System.err.println("I/O error");
         ex.printStackTrace();
      }
      catch (NullPointerException ex)
      {
         ex.printStackTrace();
      }

      try
      {
         mReceiveSocket.close();
      }
      catch (NullPointerException ex)
      {
         ex.printStackTrace();
      }
   }

   // </editor-fold>
   // <editor-fold defaultstate="expanded" desc="Public Properties">
   @Override
   public synchronized boolean isConnected()
   {
      return connected;
   }

    // </editor-fold>
}
