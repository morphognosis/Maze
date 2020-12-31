// For conditions of distribution and use, see copyright notice in Main.java

// Mouse: morphognosis organism.

package morphognosis.maze;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import morphognosis.Metamorph;
import morphognosis.Morphognostic;
import morphognosis.Orientation;
import morphognosis.Utility;
import morphognosis.Morphognostic.Neighborhood;

public class Mouse
{
   // Random numbers.
   public Random random;

   // Sensors.
   public static int NUM_SENSORS = -1;
   public float[]    sensors;

   // Response.
   public static int NUM_RESPONSES = -1;
   public static int WAIT_RESPONSE = -1;
   public int        response;
   public int        overrideResponse;

   // Driver.
   public int driver;

   // Morphognostic.
   public Morphognostic morphognostic;

   // Metamorphs.
   public int currentMetamorphIdx;
   public ArrayList<Metamorph> metamorphs;

   // Metamorph neural network.
   public MetamorphNN metamorphNN;

   // Metamorph dataset file name.
   public static final String METAMORPH_DATASET_FILE_NAME = "metamorphs.dat";

   // Maximum distance between equivalent morphognostics.
   public static float EQUIVALENT_MORPHOGNOSTIC_DISTANCE = 0.0f;

   // Goal-seeking parameters.
   public static final float SOLVE_MAZE_GOAL_VALUE      = 1.0f;
   public static final float GOAL_VALUE_DISCOUNT_FACTOR = 0.9f;

   /*
    * Morphognostic event.
    *
    *      Sensory values:
    *      {
    *          <room identifier>
    *          <context room marks>
    *          <maze entry marks>
    *          <maze interior marks>
    *          <context end room marks>
    *      }
    */

   // Constructor.
   public Mouse(int numSensors, int numResponses, Random random)
   {
      NUM_SENSORS   = numSensors;
      NUM_RESPONSES = numResponses;
      WAIT_RESPONSE = NUM_RESPONSES - 1;
      this.random   = random;

      sensors = new float[NUM_SENSORS];
      for (int n = 0; n < NUM_SENSORS; n++)
      {
         sensors[n] = 0.0f;
      }
      response         = WAIT_RESPONSE;
      overrideResponse = -1;


      // Initialize Morphognostic.
      int eventDimensions = NUM_SENSORS;
      boolean[][] neighborhoodEventDimensionMap = new boolean[Parameters.NUM_NEIGHBORHOODS][eventDimensions];
      for (int i = 0; i < Parameters.NUM_NEIGHBORHOODS; i++)
      {
         for (int j = 0; j < eventDimensions; j++)
         {
            neighborhoodEventDimensionMap[i][j] = true;
         }
      }
      morphognostic = new Morphognostic(Orientation.NORTH,
                                        eventDimensions,
                                        neighborhoodEventDimensionMap,
                                        Parameters.NUM_NEIGHBORHOODS,
                                        Parameters.NEIGHBORHOOD_DIMENSIONS,
                                        Parameters.NEIGHBORHOOD_DURATIONS);

      // Create metamorphs.
      currentMetamorphIdx = -1;
      metamorphs          = new ArrayList<Metamorph>();

      // Initialize driver.
      driver = Driver.TRAINING_OVERRIDE;
   }


   // Reset.
   void reset()
   {
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         sensors[i] = 0.0f;
      }
      response         = WAIT_RESPONSE;
      overrideResponse = -1;
      morphognostic.clear();
      currentMetamorphIdx = -1;
   }


   // Save mouse to file.
   public void save(String filename) throws IOException
   {
      DataOutputStream writer;

      try
      {
         writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(filename))));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open output file " + filename + ":" + e.getMessage());
      }
      save(writer);
      writer.close();
   }


   // Save mouse.
   public void save(DataOutputStream writer) throws IOException
   {
      morphognostic.save(writer);
      Utility.saveInt(writer, driver);
      writer.flush();
   }


   // Load bee from file.
   public void load(String filename) throws IOException
   {
      DataInputStream reader;

      try
      {
         reader = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(filename))));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open input file " + filename + ":" + e.getMessage());
      }
      load(reader);
      reader.close();
   }


   // Load mouse.
   public void load(DataInputStream reader) throws IOException
   {
      morphognostic = Morphognostic.load(reader);
      driver        = Utility.loadInt(reader);
   }


   // Sense/response cycle.
   public int cycle(float[] sensors)
   {
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         this.sensors[i] = sensors[i];
      }

      // Update morphognostic.
      updateMorphognostic();

      // Respond.
      switch (driver)
      {
      case Driver.TRAINING_OVERRIDE:
         response = overrideResponse;
         break;

      case Driver.METAMORPH_DB:
         metamorphDBresponse();
         break;

      case Driver.METAMORPH_NN:
         metamorphNNresponse();
         break;

      case Driver.METAMORPH_GOAL_SEEKING_DB:
         metamorphGoalSeekingDBresponse();
         break;

      case Driver.METAMORPH_GOAL_SEEKING_NN:
         response = 0;
         break;
      }

      // Update metamorphs if training.
      if (driver == Driver.TRAINING_OVERRIDE)
      {
         updateMetamorphs(morphognostic, response, goalValue(sensors, response));
      }

      return(response);
   }


   // Determine sensory-response goal value.
   public float goalValue(float[] sensors, int response)
   {
      return(0.0f);
   }


   // Update morphognostic.
   public void updateMorphognostic()
   {
      morphognostic.update(sensors, 0, 0);
   }


   // Get metamorph DB response.
   public void metamorphDBresponse()
   {
      Metamorph metamorph = null;
      float     d         = 0.0f;
      float     d2;

      for (Metamorph m : metamorphs)
      {
         d2 = morphognostic.compare(m.morphognostic);
         if ((metamorph == null) || (d2 < d))
         {
            d         = d2;
            metamorph = m;
         }
         else
         {
            if (d2 == d)
            {
               if (random.nextBoolean())
               {
                  d         = d2;
                  metamorph = m;
               }
            }
         }
      }
      if (metamorph != null)
      {
         response = metamorph.response;
      }
      else
      {
         response = -1;
      }
   }


   // Get metamorph neural network response.
   public void metamorphNNresponse()
   {
      if (metamorphNN != null)
      {
         response = metamorphNN.respond(morphognostic);
      }
      else
      {
         System.err.println("Must train metamorph neural network");
         response = -1;
      }
   }


   // Get goal-seeking DB response.
   public void metamorphGoalSeekingDBresponse()
   {
      Metamorph metamorph    = null;
      float     minCompare   = 0.0f;
      float     maxGoalValue = 0.0f;

      for (int i = 0, j = metamorphs.size(); i < j; i++)
      {
         Metamorph m       = metamorphs.get(i);
         float     compare = m.morphognostic.compare(morphognostic);
         if (metamorph == null)
         {
            metamorph    = m;
            minCompare   = compare;
            maxGoalValue = metamorph.goalValue;
         }
         else if (compare < minCompare)
         {
            metamorph    = m;
            minCompare   = compare;
            maxGoalValue = metamorph.goalValue;
         }
         else if (compare == minCompare)
         {
            if (m.goalValue > maxGoalValue)
            {
               metamorph    = m;
               maxGoalValue = metamorph.goalValue;
            }
         }
      }
      if (metamorph != null)
      {
         response = metamorph.response;
      }
      else
      {
         response = -1;
      }
   }


   // Update metamorphs.
   public void updateMetamorphs(Morphognostic morphognostic, int response, float goalValue)
   {
      Metamorph metamorph = new Metamorph(morphognostic.clone(), response,
                                          goalValue, getResponseName(response));

      metamorph.morphognostic.orientation = Orientation.NORTH;
      int foundIdx = -1;
      for (int i = 0, j = metamorphs.size(); i < j; i++)
      {
         Metamorph m = metamorphs.get(i);
         if (m.morphognostic.compare(metamorph.morphognostic) <= EQUIVALENT_MORPHOGNOSTIC_DISTANCE)
         {
            foundIdx = i;
            break;
         }
      }
      if (foundIdx == -1)
      {
         metamorphs.add(metamorph);
         foundIdx = metamorphs.size() - 1;
      }
      if (currentMetamorphIdx != -1)
      {
         Metamorph currentMetamorph = metamorphs.get(currentMetamorphIdx);
         for (int i = 0, j = currentMetamorph.effectIndexes.size(); i < j; i++)
         {
            if (currentMetamorph.effectIndexes.get(i) == foundIdx)
            {
               foundIdx = -1;
               break;
            }
         }
         if (foundIdx != -1)
         {
            currentMetamorph.effectIndexes.add(foundIdx);
            metamorphs.get(foundIdx).causeIndexes.add(currentMetamorphIdx);

            // Propagate goal value.
            propagateGoalValue(currentMetamorph, metamorphs.get(foundIdx).goalValue);
         }
      }
      currentMetamorphIdx = foundIdx;
   }


   // Propagate goal value.
   public void propagateGoalValue(Metamorph metamorph, float effectGoalValue)
   {
      float v = effectGoalValue * Mouse.GOAL_VALUE_DISCOUNT_FACTOR;

      if (v > metamorph.goalValue)
      {
         metamorph.goalValue = v;
         for (int i = 0, j = metamorph.causeIndexes.size(); i < j; i++)
         {
            propagateGoalValue(metamorphs.get(metamorph.causeIndexes.get(i)), v);
         }
      }
   }


   // Train metamorph neural network.
   public void trainMetamorphNN()
   {
      metamorphNN = new MetamorphNN(random);
      metamorphNN.train(metamorphs);
   }


   // Save metamorph neural network.
   public void saveMetamorphNN(String filename)
   {
      if (metamorphNN != null)
      {
         metamorphNN.saveModel(filename);
      }
      else
      {
         System.err.println("Cannot save null metamorph neural network to file " + filename);
      }
   }


   // Load metamorph neural network.
   public void loadMetamorphNN(String filename)
   {
      if (metamorphNN == null)
      {
         metamorphNN = new MetamorphNN(random);
      }
      metamorphNN.loadModel(filename);
   }


   // Clear metamorphs.
   public void clearMetamorphs()
   {
      metamorphs.clear();
      currentMetamorphIdx = -1;
   }


   // Write metamporph dataset.
   public void writeMetamorphDataset(String filename) throws Exception
   {
      FileOutputStream output;

      try
      {
         output = new FileOutputStream(new File(filename));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open output file " + filename + ":" + e.getMessage());
      }
      if (metamorphs.size() > 0)
      {
         Morphognostic morphognostic = metamorphs.get(0).morphognostic;
         String        oldlinesep    = System.getProperty("line.separator");
         System.setProperty("line.separator", "\n");
         PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)));
         for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
         {
            int n = morphognostic.neighborhoods.get(i).sectors.length;
            for (int x = 0; x < n; x++)
            {
               for (int y = 0; y < n; y++)
               {
                  for (int d = 0; d < morphognostic.eventDimensions; d++)
                  {
                     writer.print(i + "-" + x + "-" + y + "-" + d + ",");
                  }
               }
            }
         }
         writer.println("response");
         for (Metamorph m : metamorphs)
         {
            writer.println(morphognostic2csv(m.morphognostic) + "," + m.response);
         }
         writer.flush();
         writer.close();
         System.setProperty("line.separator", oldlinesep);
      }
      output.close();
   }


   // Flatten morphognostic to csv string.
   public String morphognostic2csv(Morphognostic morphognostic)
   {
      String  output    = "";
      boolean skipComma = true;
      int     dx        = 0;

      for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
      {
         Neighborhood neighborhood = morphognostic.neighborhoods.get(i);
         float[][] values = neighborhood.rectifySectorValues();
         int n = neighborhood.sectors.length;
         for (int j = 0, j2 = n * n; j < j2; j++)
         {
            for (int d = dx, d2 = morphognostic.eventDimensions; d < d2; d++)
            {
               if (skipComma)
               {
                  skipComma = false;
               }
               else
               {
                  output += ",";
               }
               output += (values[j][d] + "");
            }
         }
      }
      return(output);
   }


   // Get response name.
   public static String getResponseName(int response)
   {
      if (response >= 0)
      {
         if (response < WAIT_RESPONSE)
         {
            return("door " + response);
         }
         else
         {
            return("wait");
         }
      }
      else
      {
         return("invalid");
      }
   }
}
