// For conditions of distribution and use, see copyright notice in Main.java

// Make mazes.

package morphognosis.maze;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MazeMaker
{
   // Random seed.
   public static int RANDOM_SEED = Main.DEFAULT_RANDOM_SEED;

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "    java morphognosis.maze.MazeRNN\n" +
      "     [-numDoors <quantity> (default=" + Parameters.NUM_DOORS + ")]\n" +
      "     [-mazeInteriorLength <length> (default=" + Parameters.MAZE_INTERIOR_LENGTH + ")]\n" +
      "     [-numContextMazes <quantity> (default=" + Parameters.NUM_CONTEXT_MAZES + ")]\n" +
      "     [-numIndependentMazes <quantity> (default=" + Parameters.NUM_INDEPENDENT_MAZES + ")]\n" +
      "     [-randomSeed <seed> (default=" + Main.DEFAULT_RANDOM_SEED + ")]";

   // Make mazes.
   public static void makeMazes() throws IOException
   {
      try
      {
         InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("maze_maker.py");
         if (in == null)
         {
            System.err.println("Cannot access maze_maker.py");
            System.exit(1);
         }
         File             pythonScript = new File("maze_maker.py");
         FileOutputStream out          = new FileOutputStream(pythonScript);
         byte[] buffer = new byte[1024];
         int bytesRead;
         while ((bytesRead = in.read(buffer)) != -1)
         {
            out.write(buffer, 0, bytesRead);
         }
         out.close();
      }
      catch (Exception e)
      {
         System.err.println("Cannot create maze_maker.py");
         System.exit(1);
      }
      ProcessBuilder processBuilder = new ProcessBuilder("python", "maze_maker.py",
                                                         "--num_doors", (Parameters.NUM_DOORS + ""),
                                                         "--maze_interior_length", (Parameters.MAZE_INTERIOR_LENGTH + ""),
                                                         "--num_context_mazes", (Parameters.NUM_CONTEXT_MAZES + ""),
                                                         "--num_independent_mazes", (Parameters.NUM_INDEPENDENT_MAZES + ""),
                                                         "--random_seed", (RANDOM_SEED + "")
                                                         );
      processBuilder.inheritIO();
      Process process = processBuilder.start();
      try
      {
         process.waitFor();
      }
      catch (InterruptedException e) {}
   }


   public static void main(String[] args) throws IOException
   {
      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-numDoors"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numDoors option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.NUM_DOORS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numDoors option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.NUM_DOORS < 0)
            {
               System.err.println("Invalid numDoors option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-mazeInteriorLength"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid mazeInteriorLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.MAZE_INTERIOR_LENGTH = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid mazeInteriorLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.MAZE_INTERIOR_LENGTH < 0)
            {
               System.err.println("Invalid mazeInteriorLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numContextMazes"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numContextMazes option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.NUM_CONTEXT_MAZES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numContextMazes option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.NUM_CONTEXT_MAZES < 0)
            {
               System.err.println("Invalid numContextMazes option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numIndependentMazes"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numIndependentMazes option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.NUM_INDEPENDENT_MAZES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numIndependentMazes option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.NUM_INDEPENDENT_MAZES < 0)
            {
               System.err.println("Invalid numIndependentMazes option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-randomSeed"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid randomSeed option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               RANDOM_SEED = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid randomSeed option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (RANDOM_SEED <= 0)
            {
               System.err.println("Invalid randomSeed option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-help") || args[i].equals("-h") || args[i].equals("-?"))
         {
            System.out.println(Usage);
            System.exit(0);
         }
         System.err.println("Invalid option: " + args[i]);
         System.err.println(Usage);
         System.exit(1);
      }

      // Make mazes.
      makeMazes();
   }
}
