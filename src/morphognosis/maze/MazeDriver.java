// For conditions of distribution and use, see copyright notice in Main.java

// Maze driver.

package morphognosis.maze;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MazeDriver
{
   // Mazes.
   public static final String MAZE_DATASET_FILE_NAME = "maze_dataset.csv";
   public ArrayList<Maze> trainingMazes;
   public ArrayList<Maze> testingMazes;
   public int numInputs;
   public int numOutputs;
  
   // Mouse.
   public Mouse mouse;

   // Random numbers.
   public Random random;
   public int    randomSeed;

   // Constructor.
   public MazeDriver(int randomSeed)
   {
      // Random numbers.
      random          = new Random();
      this.randomSeed = randomSeed;
      random.setSeed(randomSeed);
      
      // Load mazes.
      loadMazes();

      // Create mouse.
      mouse = new Mouse(random);
   }
   
   // Load mazes.
   public void loadMazes()
   {
	   trainingMazes = new ArrayList<Maze>();
	   testingMazes = new ArrayList<Maze>();
	   List<Integer> X_train_shape = null;
	   List<Boolean> X_train_seq = null;
	   List<Integer> y_train_shape = null;
	   List<Boolean> y_train_seq = null;
	   List<Integer> X_test_shape = null;
	   List<Boolean> X_test_seq = null;
	   @SuppressWarnings("unused")
	   List<Integer> y_test_shape = null;
	   List<Boolean> y_test_seq = null;		   
	   try (BufferedReader br = new BufferedReader(new FileReader(MAZE_DATASET_FILE_NAME))) 
	   {
		   // Load dataset.
	       String line;
	       if ((line = br.readLine()) != null) 
	       {
	           X_train_shape = Arrays.stream(line.split(","))
	                   .map(Integer::parseInt)
	                   .collect(Collectors.toList());           
	       } else {
	    	   System.err.println("Cannot load X_train_shape from file " + MAZE_DATASET_FILE_NAME);
	    	   System.exit(1);
	       }
	       if ((line = br.readLine()) != null) 
	       {
	           X_train_seq = Arrays.stream(line.split(","))
	                   .map(Boolean::parseBoolean)
	                   .collect(Collectors.toList());
	       } else {
	    	   System.err.println("Cannot load X_train_seq from file " + MAZE_DATASET_FILE_NAME);
	    	   System.exit(1);	           
	       }
	       if ((line = br.readLine()) != null) 
	       {
	           y_train_shape = Arrays.stream(line.split(","))
	                   .map(Integer::parseInt)
	                   .collect(Collectors.toList());           
	       } else {
	    	   System.err.println("Cannot load y_train_shape from file " + MAZE_DATASET_FILE_NAME);
	    	   System.exit(1);
	       }
	       if ((line = br.readLine()) != null) 
	       {
	           y_train_seq = Arrays.stream(line.split(","))
	                   .map(Boolean::parseBoolean)
	                   .collect(Collectors.toList());
	       } else {
	    	   System.err.println("Cannot load y_train_seq from file " + MAZE_DATASET_FILE_NAME);
	    	   System.exit(1);	           
	       }
	       if ((line = br.readLine()) != null) 
	       {
	           X_test_shape = Arrays.stream(line.split(","))
	                   .map(Integer::parseInt)
	                   .collect(Collectors.toList());           
	       } else {
	    	   System.err.println("Cannot load X_test_shape from file " + MAZE_DATASET_FILE_NAME);
	    	   System.exit(1);
	       }
	       if ((line = br.readLine()) != null) 
	       {
	           X_test_seq = Arrays.stream(line.split(","))
	                   .map(Boolean::parseBoolean)
	                   .collect(Collectors.toList());
	       } else {
	    	   System.err.println("Cannot load X_test_seq from file " + MAZE_DATASET_FILE_NAME);
	    	   System.exit(1);	           
	       }
	       if ((line = br.readLine()) != null) 
	       {
	           y_test_shape = Arrays.stream(line.split(","))
	                   .map(Integer::parseInt)
	                   .collect(Collectors.toList());           
	       } else {
	    	   System.err.println("Cannot load y_test_shape from file " + MAZE_DATASET_FILE_NAME);
	    	   System.exit(1);
	       }
	       if ((line = br.readLine()) != null) 
	       {
	           y_test_seq = Arrays.stream(line.split(","))
	                   .map(Boolean::parseBoolean)
	                   .collect(Collectors.toList());
	       } else {
	    	   System.err.println("Cannot load y_test_seq from file " + MAZE_DATASET_FILE_NAME);
	    	   System.exit(1);	           
	       }
	       
	       // Set mouse sensor and response parameters.
	       Mouse.NUM_SENSORS = X_train_shape.get(2);
	       Mouse.NUM_RESPONSES = y_train_shape.get(2);
	       Mouse.WAIT_RESPONSE = Mouse.NUM_RESPONSES - 1;
	       
	       // Initialize training mazes.
	       int X_idx = 0;
	       int y_idx = 0;
	       int num_seqs = X_train_shape.get(0);
	       int num_steps = X_train_shape.get(1);
	       int num_sensors = X_train_shape.get(2);
	       int num_responses = y_train_shape.get(2);
	       for (int seq = 0; seq < num_seqs; seq++)
	       {
	    	   Maze maze = new Maze();
	    	   for (int step = 0; step < num_steps; step++)
	    	   {
	    		   boolean[] sensors = new boolean[num_sensors];
	    		   for (int i = 0; i < num_sensors; i++)
	    		   {
	    			   sensors[i] = X_train_seq.get(X_idx++);
	    		   }
	    		   maze.addSensors(sensors);
	    		   boolean[] responses = new boolean[num_responses];
	    		   for (int i = 0; i < num_responses; i++)
	    		   {
	    			   responses[i] = y_train_seq.get(y_idx++);
	    		   }
	    		   maze.addResponses(responses);	    		   
	    	   }
	    	   trainingMazes.add(maze);
	       }
	       
	       // Initialize testing mazes.
	       X_idx = 0;
	       y_idx = 0;
	       num_seqs = X_test_shape.get(0);
	       for (int seq = 0; seq < num_seqs; seq++)
	       {
	    	   Maze maze = new Maze();
	    	   for (int step = 0; step < num_steps; step++)
	    	   {
	    		   boolean[] sensors = new boolean[num_sensors];
	    		   for (int i = 0; i < num_sensors; i++)
	    		   {
	    			   sensors[i] = X_test_seq.get(X_idx++);
	    		   }
	    		   maze.addSensors(sensors);
	    		   boolean[] responses = new boolean[num_responses];
	    		   for (int i = 0; i < num_responses; i++)
	    		   {
	    			   responses[i] = y_test_seq.get(y_idx++);
	    		   }
	    		   maze.addResponses(responses);	    		   
	    	   }
	    	   testingMazes.add(maze);
	       }	       
	   } catch (FileNotFoundException e) 
	   {
		   System.err.println("Cannot find file " + MAZE_DATASET_FILE_NAME);
		   System.exit(1);
		} catch (Exception e) 
		{
			   System.err.println("Cannot load mazes from " + MAZE_DATASET_FILE_NAME);
			   System.exit(1);
		}	   
   }


   // Reset.
   public void reset()
   {
      random = new Random();
      random.setSeed(randomSeed);
      for (Maze maze : trainingMazes)
      {
    	  maze.resetCursors();
      }
      for (Maze maze : testingMazes)
      {
    	  maze.resetCursors();
      }
      mouse.reset();
   }

   // Step mouse.
   public void stepMouse()
   {

   }


   // Get mouse sensors.
   public float[] getSensors(Mouse mouse)
   {
      float[] sensors = new float[numInputs];

      return(sensors);
   }
}
