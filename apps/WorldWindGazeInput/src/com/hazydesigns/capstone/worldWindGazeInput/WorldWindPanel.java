package com.hazydesigns.capstone.worldWindGazeInput;

import com.hazydesigns.capstone.worldWindGazeInput.ui.GazeControlsLayer;
import com.hazydesigns.capstone.worldWindGazeInput.ui.GazeControlsSelectListener;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewLimits;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Container panel for the World Wind view.
 *
 * @author Mark Hazlewood
 *
 * @see JPanel
 * @see WorldWindow
 */
public class WorldWindPanel extends JPanel
{
   private final WorldWindow mWorldWindow;

   private final ScreenAnnotation mCursorImage;
   private final GazeControlsLayer mGazeControlsLayer;
   
   private final Position[] mTestLocations = {  Position.fromDegrees(31.821363, -162.363187),
                                                Position.fromDegrees(51.511157, -0.119940),
                                                Position.fromDegrees(35.693862, 139.691966),
                                                Position.fromDegrees(-15.778414, -47.961173),
                                                Position.fromDegrees(20.769674, -156.409737),
                                                Position.fromDegrees(40.716132, -74.012076)
                             };
   private final Position mStartPosition =      Position.fromDegrees(19.066909, -40.189909, 1.2756274E7);

   /**
    * Creates new form WorldWindPanel
    *
    * @param canvasSize
    */
   public WorldWindPanel(Dimension canvasSize)
   {
      super(new BorderLayout());

      mWorldWindow = createWorldWindow();
      ((Component) mWorldWindow).setPreferredSize(canvasSize);

      // Create the default model as described in the current worldwind properties.
      Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
      mWorldWindow.setModel(m);
      add((Component) mWorldWindow, BorderLayout.CENTER);
      //setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
      hideCursor();
      
      RenderableLayer cursorLayer = new RenderableLayer();      

      AnnotationAttributes ca = new AnnotationAttributes();
      ca.setAdjustWidthToText(AVKey.SIZE_FIXED);
      ca.setInsets(new Insets(0, 0, 0, 0));
      ca.setBorderWidth(0);
      ca.setCornerRadius(0);
      ca.setSize(new Dimension(64, 64));
      ca.setBackgroundColor(new Color(0, 0, 0, 0));
      ca.setImageOpacity(1);
      ca.setScale(0.25);            
      
      mCursorImage = new ScreenAnnotation("", new Point(0, 0), ca);
      mCursorImage.getAttributes().setImageSource("images/cursorImage_big.png");
      mCursorImage.getAttributes().setSize(new Dimension(64, 64));
      cursorLayer.addRenderable(mCursorImage);      
      
      mWorldWindow.addPositionListener((PositionEvent arg0) ->
      {
         if (arg0.getScreenPoint() != null)
         {
            // These two points mysteriously have different origins, even though
            // their accessors have the same name ("screen point"). Thanks NASA.
            int x = arg0.getScreenPoint().x;
            int y = Math.abs(arg0.getScreenPoint().y - mWorldWindow.getView().getViewport().height);
            
            mCursorImage.setScreenPoint(new Point(x, y));
            mWorldWindow.redraw();
         }
      });
      
      mGazeControlsLayer = new GazeControlsLayer();
      mGazeControlsLayer.setName("GazeControlLayer");
      GazeControlsSelectListener controlSelectListener = new GazeControlsSelectListener(mWorldWindow, mGazeControlsLayer, mCursorImage);
      mWorldWindow.addSelectListener(controlSelectListener);
      mWorldWindow.getModel().getLayers().add(mGazeControlsLayer);
      
      mWorldWindow.getModel().getLayers().add(cursorLayer);
      
      setupViewLimits();      
      setupTestLocations();
   }

    private void setupViewLimits()
    {
        mWorldWindow.getView().setEyePosition(mStartPosition);
        OrbitViewLimits limits = ((OrbitView)mWorldWindow.getView()).getOrbitViewLimits();
        if (limits != null)
        {
            double min = mWorldWindow.getModel().getGlobe().getRadius() / 100.0;
            double max = mWorldWindow.getModel().getGlobe().getRadius() * 2.0;
            limits.setZoomLimits(min, max);
        }
    }
   
   public GazeControlsLayer getGazeControlsLayer()
   {
       return mGazeControlsLayer;
   }
   
   private void hideCursor()
   {
      // Transparent 16 x 16 pixel cursor image.
      BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

      // Create a new blank cursor.
      Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
          cursorImg, new Point(0, 0), "blank cursor");

      // Set the blank cursor to this JPanel.
      setCursor(blankCursor);
   }

   /**
    *
    * @return
    */
   private WorldWindow createWorldWindow()
   {
      return new WorldWindowGLCanvas();
   }

   /**
    *
    * @return
    */
   public WorldWindow getWorldWindow()
   {
      return mWorldWindow;
   }

   /**
    * This method is called from within the constructor to initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is always
    * regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents()
   {

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 400, Short.MAX_VALUE)
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 300, Short.MAX_VALUE)
      );
   }// </editor-fold>//GEN-END:initComponents


   // Variables declaration - do not modify//GEN-BEGIN:variables
   // End of variables declaration//GEN-END:variables

    private void setupTestLocations()
    {
        RenderableLayer locationLayer = new RenderableLayer();        
        
        AnnotationAttributes attr = new AnnotationAttributes();
        attr.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        attr.setBackgroundColor(Color.RED.brighter().brighter());
        attr.setFont(Font.decode("Arial-BOLD-26"));
        attr.setBorderWidth(2);
        attr.setBorderColor(Color.BLACK);
        attr.setTextColor(Color.WHITE);
        attr.setFrameShape(AVKey.SHAPE_ELLIPSE);
        attr.setScale(0.5);
        attr.setLeader(AVKey.SHAPE_NONE);
        attr.setTextAlign(AVKey.CENTER);
        
        for (int i = 0; i < mTestLocations.length; i++)
        {
            GlobeAnnotation annotation = new GlobeAnnotation(Integer.toString(i), mTestLocations[i], attr);
            
            locationLayer.addRenderable(annotation);
        }
        
        mWorldWindow.getModel().getLayers().add(locationLayer);
    }
}
