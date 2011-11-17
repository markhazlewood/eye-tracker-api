package eyeTrackingTheoryProject.Controller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import eyeTrackingTheoryProject.myEyeTrackingFunctions.*;


public class MyDelegator implements ActionListener
{	
	private JFrame ui;
	
	public MyDelegator(JFrame ui)
   {
		this.ui = ui;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) 
   {		
		if(event.getActionCommand().equals("nameOfAction"))
      {
			//do action
			new AnAction(ui).doAction();
		}
      else if(event.getActionCommand().equals("nameOfAnotherAction"))
      {
			//do another action
			new AnotherAction(ui).doAction();
		}
		//etc
	}

}
