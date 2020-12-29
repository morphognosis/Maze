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
   public int numSensors;
   public int numResponses;
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
      mouse = new Mouse(numSensors, numResponses, random);
   }
   
   // Load mazes.
   @SuppressWarnings("unused")
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
	       numSensors = X_train_shape.get(2);
	       numResponses = y_train_shape.get(2);
	       
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
	    		   float[] sensors = new float[num_sensors];
	    		   for (int i = 0; i < num_sensors; i++)
	    		   {
	    			   if (X_train_seq.get(X_idx++))
	    			   {
	    				   sensors[i] = 1.0f;
	    			   } else {
	    				   sensors[i] = 0.0f;
	    			   }
	    		   }
	    		   maze.addSensors(sensors);
	    		   for (int i = 0; i < num_responses; i++)
	    		   {
	    			   if (y_train_seq.get(y_idx++))
	    			   {
	    				   maze.addResponse(i);
	    			   }
	    		   }    		   
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
	    		   float[] sensors = new float[num_sensors];
	    		   for (int i = 0; i < num_sensors; i++)
	    		   {
	    			   if (X_test_seq.get(X_idx++))
	    			   {
	    				   sensors[i] = 1.0f;
	    			   } else {
	    				   sensors[i] = 0.0f;
	    			   }
	    		   }
	    		   for (int i = 0; i < num_responses; i++)
	    		   {
	    			   if (y_test_seq.get(y_idx++))
	    			   {
	    				   maze.addResponse(i);
	    			   }
	    		   }    		       		   
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
    	  maze.reset();
      }
      for (Maze maze : testingMazes)
      {
    	  maze.reset();
      }
      mouse.reset();
   }

   // Train mouse on mazes.
   public void train()
   {
	   System.out.println("Train");
	   mouse.driver = Driver.TRAINING_OVERRIDE;
	   for (Maze maze : trainingMazes)
	   {
		   maze.reset();
		   mouse.reset();
		   float[] sensors = null;
		   while ((sensors = maze.nextSensors()) != null)
		   {
			   mouse.overrideResponse = maze.nextResponse();
			   mouse.cycle(sensors);
		   }
	   }
   }
   
   // Validate training.
   public void validate()
   {
	   System.out.println("Validate training");
	   mouse.driver = Driver.METAMORPH_DB;
	   int n = 0;
	   for (Maze maze : trainingMazes)
	   {
		   System.out.println("Maze " + n++);
		   maze.reset();
		   mouse.reset();
		   float[] sensors = null;
		   while ((sensors = maze.nextSensors()) != null)
		   {
			   int response = mouse.cycle(sensors);
			   int correctResponse = maze.nextResponse();
			   System.out.println("response=" + response + ", correct response=" + correctResponse);
		   }
	   }
   }
   
   // Test mouse on mazes.
   public void test()
   {
	   System.out.println("Test");
	   mouse.driver = Driver.METAMORPH_DB;
	   int n = 0;
	   for (Maze maze : testingMazes)
	   {
		   System.out.println("Maze " + n++);
		   maze.reset();
		   mouse.reset();
		   float[] sensors = null;
		   while ((sensors = maze.nextSensors()) != null)
		   {
			   int response = mouse.cycle(sensors);
			   int correctResponse = maze.nextResponse();
			   System.out.println("response=" + response + ", correct response=" + correctResponse);
		   }
	   }
   }
}
