// For conditions of distribution and use, see copyright notice in Main.java

// Maze dashboard.

package morphognosis.maze;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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

   // Dimensions.
   public static final Dimension DISPLAY_SIZE = new Dimension(550, 750);

   // Display.
   public Display display;
   
   // Mouse dashboard.
   public MouseDashboard mouseDashboard;

   // Speed control.
   public SpeedControl speedControl;

   // Step frequency (ms).
   public static final int MIN_STEP_DELAY = 0;
   public static final int MAX_STEP_DELAY = 150;
   public int              stepDelay      = MAX_STEP_DELAY;

   // Quit.
   public boolean quit;

   // Constructor.
   public MazeDashboard(MazeDriver mazeDriver)
   {
      this.mazeDriver   = mazeDriver;
      
      // Create mouse dashboard.
      mouseDashboard = new MouseDashboard(mazeDriver.mouse);

      // Set up display.
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
      setBounds(0, 0, DISPLAY_SIZE.width, DISPLAY_SIZE.height);
      JPanel basePanel = (JPanel)getContentPane();
      basePanel.setLayout(new BorderLayout());

      // Create display.
      Dimension displaySize = new Dimension(DISPLAY_SIZE.width,
                                            (int)((double)DISPLAY_SIZE.height * .7));
      display = new Display(displaySize);
      basePanel.add(display, BorderLayout.NORTH);

      // Create speed control.
      speedControl = new SpeedControl();
      basePanel.add(speedControl, BorderLayout.SOUTH);

      // Make display visible.
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
      // Update display.
      display.update();
      
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
   public void log(String message)
   {
      display.messageText = message;
   }


   // Display.
   public class Display extends Canvas
   {
      private static final long serialVersionUID = 0L;

      // Font.
      public Font font;

      // Message.
      public String messageText;

      // Sizes.
      Dimension canvasSize;
      int       width, height;
      float     cellWidth, cellHeight;

      // Constructor.
      public Display(Dimension canvasSize)
      {
         // Configure canvas.
         this.canvasSize = canvasSize;
         setBounds(0, 0, canvasSize.width, canvasSize.height);
         addMouseListener(new CanvasMouseListener());
         addMouseMotionListener(new CanvasMouseMotionListener());
      }


      // Update display.
      void update()
      {
      }


      // Initialize graphics.
      void initGraphics()
      {
      }

      // Canvas mouse listener.
      class CanvasMouseListener extends MouseAdapter
      {
         // Mouse pressed.
         public void mousePressed(MouseEvent evt)
         {
         }
      }
   }

   // Canvas mouse motion listener.
   class CanvasMouseMotionListener extends MouseMotionAdapter
   {
      // Mouse dragged.
      public void mouseDragged(MouseEvent evt)
      {
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
         setMessage(null);
         setStepDelay(speedSlider.getValue());
      }


      // Button listener.
      public void actionPerformed(ActionEvent evt)
      {
         setMessage(null);

         // Step?
         if (evt.getSource() == (Object)stepButton)
         {
            step();
            return;
         }
      }
   }
}
