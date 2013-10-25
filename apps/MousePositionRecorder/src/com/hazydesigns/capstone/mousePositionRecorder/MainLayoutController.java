/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hazydesigns.capstone.mousePositionRecorder;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.BufferedWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
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
import sun.misc.Timeable;

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
   
   @FXML
   Label timerLabel;

   private boolean mRecording = false;
   private Timer mCountdownTimer;
   private Timer mDotTimer;

   private Thread mMousePollThread;
   private MousePollTask mMousePollTask;
   
   private ArrayList<String> mTextLines;
   
   private static final String TEST_FILE_PATH = System.getProperty("java.io.tmpdir") + "\\simulatedEyeData.txt";
   private static final int SAMPLE_DELAY_milliseconds = 10;

   @Override
   public void initialize(URL url, ResourceBundle rb)
   {
      mRecording = false;      
      mTextLines = new ArrayList<>();
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
         timerLabel.setText("");
         recordButton.setDisable(false);
         
         if (mTextLines.size() > 0)
         {
            try
            {
               Thread.sleep(500);
            }
            catch (InterruptedException ex)
            {
               ex.printStackTrace();
            }

            Path filePath = Paths.get(TEST_FILE_PATH);
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8))
            {
               for (String line : mTextLines)
               {
                  writer.write(line);
                  writer.newLine();
               }
               mTextLines.clear();
            }
            catch (Exception ex)
            {
               ex.printStackTrace();
            }
         }
         
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
               Platform.runLater(() ->
               {
                  if (mRecording == true && formLabel.getText().equals("Recording..."))
                  {
                     formLabel.setText("Recording");
                  }
                  else
                  {
                     formLabel.setText(formLabel.getText() + ".");
                  }
                  
                  long secondsRecording = (SAMPLE_DELAY_milliseconds * mTextLines.size()) / 1000;
                  Duration d = Duration.ofSeconds(secondsRecording);
                  
                  long hours = d.toHours();
                  long minutes = d.minusHours(d.toHours()).toMinutes();
                  long seconds = d.minusMinutes(minutes).minusHours(hours).getSeconds();
                  
                  String label = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                  timerLabel.setText(label);
                  
               });
            }
         }, 500, 500);

         mCountdownTimer = new Timer();
         mCountdownTimer.schedule(new TimerTask()
         {
            @Override
            public void run()
            {
               Platform.runLater(() ->
               {
                  formLabel.setText(formLabel.getText() + " 2 ");
               });
            }

         }, 1500);

         mCountdownTimer.schedule(new TimerTask()
         {
            @Override
            public void run()
            {
               Platform.runLater(() ->
               {
                  formLabel.setText(formLabel.getText() + " 1 ");
               });
            }

         }, 3000);

         mCountdownTimer.schedule(new TimerTask()
         {
            @Override
            public void run()
            {
               Platform.runLater(() ->
               {
                  formLabel.setText("Recording");
                  mRecording = true;
                  
                  mMousePollTask = new MousePollTask();
                  mMousePollThread = new Thread(mMousePollTask);
                  mMousePollThread.start();
                  
                  recordButton.setDisable(false);
                  recordButton.setId("recordButton-recording");
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
            String line = p.x + "," + p.y + "," + SAMPLE_DELAY_milliseconds;
            mTextLines.add(line);

            Thread.sleep(SAMPLE_DELAY_milliseconds);
         }

         return null;
      }
   }
}