package eyeTrackingTheoryProject.myEyeTrackingFunctions;

import javax.swing.JFrame;

public abstract class Action
{

   protected JFrame ui;

   public Action(JFrame ui)
   {
      this.ui = ui;
   }

   public abstract void doAction();
}
