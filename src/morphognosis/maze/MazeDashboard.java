// For conditions of distribution and use, see copyright notice in Main.java

// Maze dashboard.

package morphognosis.maze;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MazeDashboard extends JFrame
{
   private static final long serialVersionUID = 0L;

   // Maze driver.
   public MazeDriver mazeDriver;

   // Run log.
   public TextArea log;

   // Speed control.
   public SpeedControl speedControl;

   // Step frequency (ms).
   public static final int MIN_STEP_DELAY = 0;
   public static final int MAX_STEP_DELAY = 150;
   public int              stepDelay      = MAX_STEP_DELAY;
   
   // Mouse dashboard.
   public MouseDashboard mouseDashboard;

   // Quit.
   public boolean quit;

   // Constructor.
   public MazeDashboard(MazeDriver mazeDriver)
   {
      this.mazeDriver   = mazeDriver;
      
      // Create mouse dashboard.
      mouseDashboard = new MouseDashboard(mazeDriver.mouse);

      // Set up window.
      setTitle("Maze");
      quit = false;
      addWindowListener(new WindowAdapter()
                        {
                           public void windowClosing(WindowEvent e)
                           {
                              quit = true;
                           }
                        }
                        );
      JPanel basePanel = (JPanel)getContentPane();
      basePanel.setLayout(new BorderLayout());

      // Create log.
     log = new TextArea("", 20, 80, TextArea.SCROLLBARS_BOTH);
     log.setEditable(false);
      basePanel.add(log, BorderLayout.NORTH);

      // Create speed control.
      speedControl = new SpeedControl();
      basePanel.add(speedControl, BorderLayout.SOUTH);

      // Make dashboard visible.
      pack();
      setLocation();
      setVisible(true);
   }

   // Set dashboard location.
   public void setLocation()
   {
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int       w   = getSize().width;
      int       h   = getSize().height;
      int       x   = (dim.width - w) / 4;
      int       y   = (dim.height - h) / 2;

      setLocation(x, y);
   }


   private int timer = 0;
   public boolean update()
   {    
      // Update mouse dashboard.
      mouseDashboard.update();

      // Timer loop: count down delay by 1ms.
      for (timer = stepDelay; timer > 0 && !quit; quit = quit || mouseDashboard.quit)
      {
         try
         {
            Thread.sleep(1);
         }
         catch (InterruptedException e) {
            break;
         }

         if (stepDelay < MAX_STEP_DELAY)
         {
            timer--;
         }
      }
      return(!quit);
   }


   // Set step delay.
   public void setStepDelay(int delay)
   {
      stepDelay = timer = delay;
   }


   // Step.
   public void step()
   {
      setStepDelay(MAX_STEP_DELAY);
      speedControl.speedSlider.setValue(MAX_STEP_DELAY);
      timer = 0;
   }


   // Log.
   public void log(String event)
   {
	   log.append(event + "\n");
   }
   
   // Overwrite last log line. 
   public void logLast(String event)
   {
       String [] lines = log.getText().split("\n");
       if (lines.length > 0)
       {
	       lines[lines.length -1] = event + "\n";
	       log.setText(String.join("\n", lines));
       }
   }   
   
   // Speed control panel.
   class SpeedControl extends JPanel implements ActionListener, ChangeListener
   {
      private static final long serialVersionUID = 0L;

      // Components.
      JLabel    stepCounter;
      JSlider   speedSlider;
      JButton   stepButton;

      // Constructor.
      SpeedControl()
      {
         add(new JLabel("Speed:   Fast", Label.RIGHT));
         speedSlider = new JSlider(JSlider.HORIZONTAL, MIN_STEP_DELAY,
                                   MAX_STEP_DELAY, MAX_STEP_DELAY);
         speedSlider.addChangeListener(this);
         add(speedSlider);
         add(new JLabel("Stop", Label.LEFT));
         stepButton = new JButton("Step");
         stepButton.addActionListener(this);
         add(stepButton);
         stepCounter = new JLabel("");
         add(stepCounter);
      }


      // Update step counter display.
      void updateStepCounter(int step)
      {
         stepCounter.setText("Step: " + step);
      }


      void updateStepCounter(int step, int steps)
      {
         stepCounter.setText("Step: " + step + "/" + steps);
      }


      // Speed slider listener.
      public void stateChanged(ChangeEvent evt)
      {
         setStepDelay(speedSlider.getValue());
      }


      // Button listener.
      public void actionPerformed(ActionEvent evt)
      {
         // Step?
         if (evt.getSource() == (Object)stepButton)
         {
            step();
            return;
         }
      }
   }
}
