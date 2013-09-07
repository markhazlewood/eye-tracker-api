/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mousepositionrecorder;

import java.awt.MouseInfo;
import java.awt.Point;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 *
 * @author mhazlewood
 */
public class MainLayoutController implements Initializable
{
   @FXML
   Button recordButton;

   @FXML
   Label formLabel;

   private boolean mRecording = false;
   private Timer mCountdownTimer;
   private Timer mDotTimer;

   private Thread mMousePollThread;
   private MousePollTask mMousePollTask;

   @Override
   public void initialize(URL url, ResourceBundle rb)
   {
      mRecording = false;
      formLabel.setText("");
   }
   
   public void shutdown()
   {
      killThreads();
   }
   
   public void killThreads()
   {
      if (mMousePollTask != null)
      {
         mMousePollTask.cancel();
         mMousePollTask = null;
      }
      
      if (mMousePollThread != null)
      {
         mMousePollThread = null;
      }

      if (mDotTimer != null)
      {
         mDotTimer.cancel();
         mDotTimer.purge();
         mDotTimer = null;
      }

      if (mCountdownTimer != null)
      {
         mCountdownTimer.cancel();
         mCountdownTimer.purge();
         mCountdownTimer = null;
      }
   }

   @FXML
   public void handleRecordButtonClick(ActionEvent event)
   {
      // Avoid double clicking before stuff happens
      recordButton.setDisable(true);
      
      if (mRecording == true)
      {
         mRecording = false;
         recordButton.setId("recordButton");

         killThreads();

         formLabel.setText("Awaiting orders");
         recordButton.setDisable(false);
      }
      else
      {
         formLabel.setText("3 ");

         mDotTimer = new Timer();
         mDotTimer.scheduleAtFixedRate(new TimerTask()
         {
            @Override
            public void run()
            {
               Platform.runLater(new Runnable()
               {
                  @Override
                  public void run()
                  {
                     if (mRecording == true && formLabel.getText().equals("Recording..."))
                     {
                        formLabel.setText("Recording");
                     }
                     else
                     {
                        formLabel.setText(formLabel.getText() + ".");
                     }
                  }
               });
            }
         }, 500, 500);

         mCountdownTimer = new Timer();
         mCountdownTimer.schedule(new TimerTask()
         {
            @Override
            public void run()
            {
               Platform.runLater(new Runnable()
               {
                  @Override
                  public void run()
                  {
                     formLabel.setText(formLabel.getText() + " 2 ");
                  }
               });
            }

         }, 1500);

         mCountdownTimer.schedule(new TimerTask()
         {
            @Override
            public void run()
            {
               Platform.runLater(new Runnable()
               {

                  @Override
                  public void run()
                  {
                     formLabel.setText(formLabel.getText() + " 1 ");
                  }
               });
            }

         }, 3000);

         mCountdownTimer.schedule(new TimerTask()
         {
            @Override
            public void run()
            {
               Platform.runLater(new Runnable()
               {
                  @Override
                  public void run()
                  {
                     formLabel.setText("Recording");
                     mRecording = true;

                     mMousePollTask = new MousePollTask();
                     mMousePollThread = new Thread(mMousePollTask);
                     mMousePollThread.start();                     
                     
                     recordButton.setDisable(false);
                     recordButton.setId("recordButton-recording");
                  }
               });
            }

         }, 4500);

         //TODO: Update label(s) to show current position and number of recorded points
      }
   }

   private class MousePollTask extends Task<Object>
   {
      @Override
      protected Object call() throws Exception
      {
         while (true)
         {
            if (isCancelled())
            {
               break;
            }

            Point p = MouseInfo.getPointerInfo().getLocation();
            System.out.println(p.x + ", " + p.y);

            Thread.sleep(500);
         }

         return null;
      }
   }
}
