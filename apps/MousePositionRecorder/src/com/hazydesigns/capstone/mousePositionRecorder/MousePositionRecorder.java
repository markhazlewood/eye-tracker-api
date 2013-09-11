/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hazydesigns.capstone.mousePositionRecorder;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author mhazlewood
 */
public class MousePositionRecorder extends Application
{
   private MainLayoutController mMainController;
   
   @Override
   public void start(Stage stage) throws Exception
   {
      stage.setResizable(false);
      
      // Doing it this way so we can get access to the main controller class,
      // so it can have knowledge of the application exit
      FXMLLoader loader = new FXMLLoader(getClass().getResource("MainLayout.fxml"));      
      Parent root = (Parent)(loader.load());
      mMainController = (MainLayoutController)(loader.getController());

      Scene scene = new Scene(root);

      stage.setScene(scene);
      stage.show();
      
      // Add an event handler for the scene's close request, so we can tell the
      // main layout controller to shut down properly
      scene.getWindow().setOnCloseRequest((WindowEvent t) ->
      {
         mMainController.shutdown();
      });
   }

   /**
    * The main() method is ignored in correctly deployed JavaFX application.
    * main() serves only as fallback in case the application can not be launched
    * through deployment artifacts, e.g., in IDEs with limited FX support.
    * NetBeans ignores main().
    *
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      launch(args);
   }

}
