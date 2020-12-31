// For conditions of distribution and use, see copyright notice in Main.java

// Parameters.

package morphognosis.maze;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import morphognosis.Utility;

public class Parameters
{
   // Morphognosis parameters.
   public static int     NUM_NEIGHBORHOODS       = 6;
   public static int[][] NEIGHBORHOOD_DIMENSIONS = { { 1, 1 }, { 1, 1 }, { 1, 1 }, { 1, 1 }, { 1, 1 }, { 1, 1 } };
   public static int[]   NEIGHBORHOOD_DURATIONS  = { 1, 1, 1, 1, 1, 10 };

   // Metamorph neural network parameters.
   public static double NN_LEARNING_RATE = 0.1;
   public static double NN_MOMENTUM      = 0.2;
   public static String NN_HIDDEN_LAYERS = "50";
   public static int    NN_TRAINING_TIME = 5000;

   // Save.
   public static void save(DataOutputStream writer) throws IOException
   {
      Utility.saveInt(writer, NUM_NEIGHBORHOODS);
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         Utility.saveInt(writer, NEIGHBORHOOD_DIMENSIONS[i][0]);
         Utility.saveInt(writer, NEIGHBORHOOD_DIMENSIONS[i][1]);
      }
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         Utility.saveInt(writer, NEIGHBORHOOD_DURATIONS[i]);
      }
      Utility.saveDouble(writer, NN_LEARNING_RATE);
      Utility.saveDouble(writer, NN_MOMENTUM);
      Utility.saveString(writer, NN_HIDDEN_LAYERS);
      Utility.saveInt(writer, NN_TRAINING_TIME);
      writer.flush();
   }


   // Load.
   public static void load(DataInputStream reader) throws IOException
   {
      NUM_NEIGHBORHOODS       = Utility.loadInt(reader);
      NEIGHBORHOOD_DIMENSIONS = new int[NUM_NEIGHBORHOODS][2];
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         NEIGHBORHOOD_DIMENSIONS[i][0] = Utility.loadInt(reader);
         NEIGHBORHOOD_DIMENSIONS[i][1] = Utility.loadInt(reader);
      }
      NEIGHBORHOOD_DURATIONS = new int[NUM_NEIGHBORHOODS];
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         NEIGHBORHOOD_DURATIONS[i] = Utility.loadInt(reader);
      }
      NN_LEARNING_RATE = Utility.loadDouble(reader);
      NN_MOMENTUM      = Utility.loadDouble(reader);
      NN_HIDDEN_LAYERS = Utility.loadString(reader);
      NN_TRAINING_TIME = Utility.loadInt(reader);
   }


   // Print.
   public static void print()
   {
      System.out.println("NUM_NEIGHBORHOODS = " + NUM_NEIGHBORHOODS);
      System.out.print("NEIGHBORHOOD_DIMENSIONS (element: { <neighborhood dimension>, <sector dimension> })={");
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         System.out.print("{" + NEIGHBORHOOD_DIMENSIONS[i][0] + "," + NEIGHBORHOOD_DIMENSIONS[i][1] + "}");
         if (i < NUM_NEIGHBORHOODS - 1)
         {
            System.out.print(",");
         }
      }
      System.out.println("}");
      System.out.print("NEIGHBORHOOD_DURATIONS={");
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         System.out.print(NEIGHBORHOOD_DURATIONS[i] + "");
         if (i < NUM_NEIGHBORHOODS - 1)
         {
            System.out.print(",");
         }
      }
      System.out.println("}");
      System.out.println("NN_LEARNING_RATE = " + NN_LEARNING_RATE);
      System.out.println("NN_MOMENTUM = " + NN_MOMENTUM);
      System.out.println("NN_HIDDEN_LAYERS = " + NN_HIDDEN_LAYERS);
      System.out.println("NN_TRAINING_TIME = " + NN_TRAINING_TIME);
   }
}
