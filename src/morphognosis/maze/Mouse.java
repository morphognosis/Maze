// For conditions of distribution and use, see copyright notice in Main.java

// Mouse: morphognosis organism.

package morphognosis.maze;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import morphognosis.Metamorph;
import morphognosis.Morphognostic;
import morphognosis.Orientation;
import morphognosis.Utility;

public class Mouse
{
   // Properties.
   public int     id;
   public int     x, y, x2, y2, toX, toY;
   public int     orientation, orientation2;
   public boolean nectarCarry;
   public int     nectarDistanceDisplay;
   public boolean handlingNectar;
   public float   returnToHiveProbability;
   public MazeDriver   driver;
   public int     driver;
   public int     driverResponse;
   public Random  random;

   // Sensors.
   public static final int HIVE_PRESENCE_INDEX          = 0;
   public static final int NECTAR_PRESENCE_INDEX        = 1;
   public static final int NECTAR_DANCE_DIRECTION_INDEX = 2;
   public static final int NECTAR_DANCE_DISTANCE_INDEX  = 3;
   public static final int NUM_SENSORS = 4;
   float[] sensors;

   // Response.
   // Initial responses are changing directions (see Orientation).
   public static final int FORWARD        = Orientation.NUM_ORIENTATIONS;
   public static final int EXTRACT_NECTAR = FORWARD + 1;
   public static final int DEPOSIT_NECTAR = EXTRACT_NECTAR + 1;
   public static final int DISPLAY_NECTAR_LONG_DISTANCE  = DEPOSIT_NECTAR + 1;
   public static final int DISPLAY_NECTAR_SHORT_DISTANCE = DISPLAY_NECTAR_LONG_DISTANCE + 1;
   public static final int WAIT          = DISPLAY_NECTAR_SHORT_DISTANCE + 1;
   public static final int NUM_RESPONSES = WAIT + 1;
   int response;

   // Goal-seeking parameters.
   public static final float DEPOSIT_NECTAR_GOAL_VALUE  = 1.0f;
   public static final float GOAL_VALUE_DISCOUNT_FACTOR = 0.9f;

   // Event symbols.
   public static final int HIVE_PRESENCE_EVENT         = 0;
   public static final int SURPLUS_NECTAR_EVENT        = 2;
   public static final int NECTAR_LONG_DISTANCE_EVENT  = 11;
   public static final int NECTAR_SHORT_DISTANCE_EVENT = 12;

   // Morphognostic.
   public Morphognostic morphognostic;

   /*
    * Morphognostic event.
    *
    *      Event values:
    *      {
    *              <hive presence>,
    *              <nectar presence>,
    *              <surplus nectar presence>,
    *              <nectar dance direction>,
    *              <nectar dance distance>,
    *              <orientation>,
    *              <nectar carry status>
    *      }
    *      <orientation>: [<Orientation point true/false> x8]
    *      <nectar distance>: [<distance type true/false> x<BEE_NUM_DISTANCE_VALUES>]
    */

   // Debugging.
   public static boolean debugAutopilot = false;
   public static boolean debugDB        = false;
   public static boolean debugNN        = false;

   // Constructor.
   public HoneyBee(int id, World world, Random random)
   {
      this.id     = id;
      this.world  = world;
      this.random = random;

      // Initialize bee.
      for (int i = 0; i < 100; i++)
      {
         int dx = random.nextInt(Parameters.HIVE_RADIUS + 1);
         if (random.nextBoolean()) { dx = -dx; }
         int dy = random.nextInt(Parameters.HIVE_RADIUS + 1);
         if (random.nextBoolean()) { dy = -dy; }
         x = x2 = (Parameters.WORLD_WIDTH / 2) + dx;
         y = y2 = (Parameters.WORLD_HEIGHT / 2) + dy;
         if (world.cells[x][y].hive && (world.cells[x][y].bee == null))
         {
            world.cells[x][y].bee = this;
            break;
         }
         if (i == 99)
         {
            System.err.println("Cannot place bee in world");
            System.exit(1);
         }
      }
      orientation             = orientation2 = random.nextInt(Orientation.NUM_ORIENTATIONS);
      nectarCarry             = false;
      nectarDistanceDisplay   = -1;
      handlingNectar          = false;
      returnToHiveProbability = 0.0f;
      sensors = new float[NUM_SENSORS];
      for (int n = 0; n < NUM_SENSORS; n++)
      {
         sensors[n] = 0.0f;
      }
      response = WAIT;

      // Initialize Morphognostic.
      int eventDimensions =
         1 +                                       // <hive presence>
         1 +                                       // <nectar presence>
         1 +                                       // <surplus nectar presence>
         Orientation.NUM_ORIENTATIONS +            // <nectar dance direction>
         2 +                                       // <nectar dance distance>
         Orientation.NUM_ORIENTATIONS +            // <orientation>
         1;                                        // <nectar carry status>
      int[] eventValueDimensions = new int[eventDimensions];
      for (int i = 0; i < eventDimensions; i++)
      {
         eventValueDimensions[i] = 1;
      }
      boolean[][] neighborhoodEventMap = new boolean[Parameters.NUM_NEIGHBORHOODS][eventDimensions];
      for (int i = 0; i < Parameters.NUM_NEIGHBORHOODS; i++)
      {
         switch (i)
         {
         case 0:
            for (int j = 0; j < eventDimensions; j++)
            {
               switch (j)
               {
               case SURPLUS_NECTAR_EVENT:
               case NECTAR_LONG_DISTANCE_EVENT:
               case NECTAR_SHORT_DISTANCE_EVENT:
                  neighborhoodEventMap[i][j] = false;
                  break;

               default:
                  neighborhoodEventMap[i][j] = true;
                  break;
               }
            }
            break;

         case 1:
            for (int j = 0; j < eventDimensions; j++)
            {
               switch (j)
               {
               case HIVE_PRESENCE_EVENT:
               case SURPLUS_NECTAR_EVENT:
               case NECTAR_SHORT_DISTANCE_EVENT:
                  neighborhoodEventMap[i][j] = true;
                  break;

               default:
                  neighborhoodEventMap[i][j] = false;
                  break;
               }
            }
            break;

         case 2:
            for (int j = 0; j < eventDimensions; j++)
            {
               switch (j)
               {
               case HIVE_PRESENCE_EVENT:
               case SURPLUS_NECTAR_EVENT:
               case NECTAR_LONG_DISTANCE_EVENT:
                  neighborhoodEventMap[i][j] = true;
                  break;

               default:
                  neighborhoodEventMap[i][j] = false;
                  break;
               }
            }
            break;

         case 3:
            for (int j = 0; j < eventDimensions; j++)
            {
               switch (j)
               {
               case HIVE_PRESENCE_EVENT:
                  neighborhoodEventMap[i][j] = true;
                  break;

               default:
                  neighborhoodEventMap[i][j] = false;
                  break;
               }
            }
            break;
         }
      }
      morphognostic = new Morphognostic(Orientation.NORTH,
                                        eventValueDimensions,
                                        neighborhoodEventMap,
                                        Parameters.WORLD_WIDTH, Parameters.WORLD_HEIGHT,
                                        Parameters.NUM_NEIGHBORHOODS,
                                        Parameters.NEIGHBORHOOD_DIMENSIONS,
                                        Parameters.NEIGHBORHOOD_DURATIONS,
                                        Parameters.BINARY_VALUE_AGGREGATION);

      String[] eventNames = new String[eventDimensions];
      eventNames[0]       = "hive presence";
      eventNames[1]       = "nectar presence";
      eventNames[2]       = "surplus nectar presence";
      eventNames[3]       = "nectar dance direction north";
      eventNames[4]       = "nectar dance direction northeast";
      eventNames[5]       = "nectar dance direction east";
      eventNames[6]       = "nectar dance direction southeast";
      eventNames[7]       = "nectar dance direction south";
      eventNames[8]       = "nectar dance direction southwest";
      eventNames[9]       = "nectar dance direction west";
      eventNames[10]      = "nectar dance direction northwest";
      eventNames[11]      = "nectar dance distance long";
      eventNames[12]      = "nectar dance distance short";
      eventNames[13]      = "orientation north";
      eventNames[14]      = "orientation northeast";
      eventNames[15]      = "orientation east";
      eventNames[16]      = "orientation southeast";
      eventNames[17]      = "orientation south";
      eventNames[18]      = "orientation southwest";
      eventNames[19]      = "orientation west";
      eventNames[20]      = "orientation northwest";
      eventNames[21]      = "nectar carry";
      morphognostic.nameEvents(eventNames);

      // Initialize driver.
      driver         = Driver.AUTOPILOT;
      driverResponse = WAIT;
   }


   // Reset.
   void reset()
   {
      x                       = x2;
      y                       = y2;
      orientation             = orientation2;
      nectarCarry             = false;
      nectarDistanceDisplay   = -1;
      handlingNectar          = false;
      returnToHiveProbability = 0.0f;
      world.cells[x][y].bee   = this;
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         sensors[i] = 0.0f;
      }
      response = WAIT;
      morphognostic.clear();
      driverResponse = WAIT;
   }


   // Save bee to file.
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


   // Save bee.
   public void save(DataOutputStream writer) throws IOException
   {
      Utility.saveInt(writer, id);
      Utility.saveInt(writer, x);
      Utility.saveInt(writer, y);
      Utility.saveInt(writer, orientation);
      Utility.saveInt(writer, x2);
      Utility.saveInt(writer, y2);
      Utility.saveInt(writer, orientation2);
      if (nectarCarry)
      {
         Utility.saveInt(writer, 1);
      }
      else
      {
         Utility.saveInt(writer, 0);
      }
      Utility.saveInt(writer, nectarDistanceDisplay);
      if (handlingNectar)
      {
         Utility.saveInt(writer, 1);
      }
      else
      {
         Utility.saveInt(writer, 0);
      }
      Utility.saveFloat(writer, returnToHiveProbability);
      morphognostic.save(writer);
      Utility.saveInt(writer, driver);
      Utility.saveInt(writer, driverResponse);
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


   // Load bee.
   public void load(DataInputStream reader) throws IOException
   {
      id           = Utility.loadInt(reader);
      x            = Utility.loadInt(reader);
      y            = Utility.loadInt(reader);
      orientation  = Utility.loadInt(reader);
      x2           = Utility.loadInt(reader);
      y2           = Utility.loadInt(reader);
      orientation2 = Utility.loadInt(reader);
      if (Utility.loadInt(reader) == 1)
      {
         nectarCarry = true;
      }
      else
      {
         nectarCarry = false;
      }
      nectarDistanceDisplay = Utility.loadInt(reader);
      if (Utility.loadInt(reader) == 1)
      {
         handlingNectar = true;
      }
      else
      {
         handlingNectar = false;
      }
      returnToHiveProbability = Utility.loadFloat(reader);
      morphognostic           = Morphognostic.load(reader);
      driver                = Utility.loadInt(reader);
      driverResponse        = Utility.loadInt(reader);
      world.cells[x][y].bee = this;
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
      handlingNectar = false;
      switch (driver)
      {
      case Driver.AUTOPILOT:
         handlingNectar = autopilotResponse(false);
         if (debugAutopilot)
         {
            if (handlingNectar)
            {
               if (response == EXTRACT_NECTAR)
               {
                  try
                  {
                     Thread.sleep(3000);
                  }
                  catch (InterruptedException e) {}
               }
            }
         }
         break;

      case Driver.METAMORPH_DB:
         metamorphDBresponse();
         break;

      case Driver.METAMORPH_NN:
         metamorphNNresponse();
         break;

      case Driver.AUTOPILOT_GOAL_SEEKING:
         handlingNectar = autopilotResponse(true);
         if (debugAutopilot)
         {
            if (handlingNectar)
            {
               if (response == EXTRACT_NECTAR)
               {
                  try
                  {
                     Thread.sleep(3000);
                  }
                  catch (InterruptedException e) {}
               }
            }
         }
         break;

      case Driver.METAMORPH_GOAL_SEEKING_DB:
         metamorphGoalSeekingDBresponse();
         break;

      case Driver.METAMORPH_GOAL_SEEKING_NN:
         response = WAIT;
         break;

      default:
         response = driverResponse;
         break;
      }

      // Update metamorphs if learning.
      if ((driver == Driver.AUTOPILOT_GOAL_SEEKING) || ((driver == Driver.AUTOPILOT) && handlingNectar))
      {
         world.updateMetamorphs(morphognostic, response, goalValue(sensors, response));
      }

      return(response);
   }


   // Determine sensory-response goal value.
   public float goalValue(float[] sensors, int response)
   {
      if ((sensors[HIVE_PRESENCE_INDEX] == 1.0f) && nectarCarry && (response == DEPOSIT_NECTAR))
      {
         return(DEPOSIT_NECTAR_GOAL_VALUE);
      }
      else
      {
         return(0.0f);
      }
   }


   // Update morphognostic.
   public void updateMorphognostic()
   {
      int[] eventValues = new int[morphognostic.eventDimensions];
      eventValues[0]    = (int)sensors[HIVE_PRESENCE_INDEX];
      eventValues[1]    = eventValues[2] = 0;
      if (nectarCarry)
      {
         // Surplus nectar?
         if ((int)sensors[NECTAR_PRESENCE_INDEX] == 1.0f)
         {
            eventValues[2] = 1;
         }
      }
      else
      {
         // Nectar present?
         if ((int)sensors[NECTAR_PRESENCE_INDEX] == 1.0f)
         {
            eventValues[1] = 1;
         }
      }
      if (sensors[NECTAR_DANCE_DIRECTION_INDEX] != -1)
      {
         eventValues[3 + (int)sensors[NECTAR_DANCE_DIRECTION_INDEX]] = 1;
      }
      if (sensors[NECTAR_DANCE_DISTANCE_INDEX] != -1)
      {
         eventValues[3 + Orientation.NUM_ORIENTATIONS + (int)sensors[NECTAR_DANCE_DISTANCE_INDEX]] = 1;
      }
      eventValues[3 + Orientation.NUM_ORIENTATIONS + 2 + orientation] = 1;
      if (nectarCarry)
      {
         eventValues[3 + Orientation.NUM_ORIENTATIONS + 2 + Orientation.NUM_ORIENTATIONS] = 1;
      }
      morphognostic.update(eventValues, x, y);
   }


   // Autopilot response.
   // Returns: true if handling nectar; false if randomly foraging.
   public boolean autopilotResponse(boolean goalSeeking)
   {
      int width  = Parameters.WORLD_WIDTH;
      int height = Parameters.WORLD_HEIGHT;

      // If in hive clear probability to return to hive.
      if (sensors[HIVE_PRESENCE_INDEX] == 1.0f)
      {
         returnToHiveProbability = 0.0f;
      }
      else
      {
         // Cannot locate hive?
         if (morphognostic.locateEvent(3, HIVE_PRESENCE_EVENT, false) == -1)
         {
            // Drop nectar and return to hive.
            nectarCarry = false;
            morphognostic.clearEvent(SURPLUS_NECTAR_EVENT);
            returnToHiveProbability = 1.0f;
            response = moveTo(width / 2, height / 2);
            return(false);
         }
      }

      // Carrying nectar?
      if (nectarCarry)
      {
         // In hive?
         if (sensors[HIVE_PRESENCE_INDEX] == 1.0f)
         {
            // Deposit nectar.
            response = DEPOSIT_NECTAR;
         }
         else
         {
            // Continue to hive.
            response = moveTo(width / 2, height / 2);
         }
         return(true);
      }

      // Not carrying nectar.

      // Found nectar to extract?
      if (sensors[NECTAR_PRESENCE_INDEX] == 1.0f)
      {
         response = EXTRACT_NECTAR;
         return(true);
      }

      // Turn at edge of world.
      if ((toX < 0) || (toX >= width) || (toY < 0) || (toY >= height))
      {
         response = random.nextInt(Orientation.NUM_ORIENTATIONS);
         return(false);
      }

      // Sense nectar dance?
      if (sensors[NECTAR_DANCE_DIRECTION_INDEX] != -1.0f)
      {
         // Turn toward nectar.
         response = (int)sensors[NECTAR_DANCE_DIRECTION_INDEX];
         return(true);
      }
      else if ((morphognostic.locateEvent(2, NECTAR_LONG_DISTANCE_EVENT, false) != -1) ||
               (morphognostic.locateEvent(1, NECTAR_SHORT_DISTANCE_EVENT, false) != -1))
      {
         // Move in direction of nectar.
         response = FORWARD;
         return(true);
      }

      // In hive?
      if (sensors[HIVE_PRESENCE_INDEX] == 1.0f)
      {
         // Surplus nectar short distance detected?
         int o = morphognostic.locateEvent(1, SURPLUS_NECTAR_EVENT, false);
         if ((o != -1) && (o < Orientation.NUM_ORIENTATIONS))
         {
            // Orient toward nectar?
            if (o != orientation)
            {
               response = o;
               return(true);
            }

            // Dance display of nectar short distance to bees in hive.
            response = DISPLAY_NECTAR_SHORT_DISTANCE;
            return(true);
         }

         // Check for surplus long distance nectar.
         o = morphognostic.locateEvent(2, SURPLUS_NECTAR_EVENT, false);
         if ((o != -1) && (o < Orientation.NUM_ORIENTATIONS))
         {
            // Orient toward nectar?
            if (o != orientation)
            {
               response = o;
               return(true);
            }

            // Dance display of nectar long distance to bees in hive.
            response = DISPLAY_NECTAR_LONG_DISTANCE;
            return(true);
         }
      }
      else
      {
         // Return to hive?
         if (random.nextFloat() < returnToHiveProbability)
         {
            response = moveTo(width / 2, height / 2);
            return(false);
         }
         else
         {
            // Increase tendency to return.
            returnToHiveProbability += Parameters.BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT;
            if (returnToHiveProbability > 1.0f)
            {
               returnToHiveProbability = 1.0f;
            }
         }
      }

      // Continue foraging.
      if (goalSeeking)
      {
         // Fly directly to flower.
         for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
         {
            for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
            {
               if (world.cells[x][y].flower != null)
               {
                  if ((response = moveTo(x, y)) == -1)
                  {
                     response = WAIT;
                  }
                  return(false);
               }
            }
         }
         response = WAIT;
         return(false);
      }
      else
      {
         // Semi-random foraging.
         if (random.nextFloat() < Parameters.BEE_TURN_PROBABILITY)
         {
            response = random.nextInt(Orientation.NUM_ORIENTATIONS);
         }
         else
         {
            response = FORWARD;
         }
      }
      return(false);
   }


   // Get response that moves to destination cell.
   // Return -1 if at destination.
   public int moveTo(int dX, int dY)
   {
      // At destination?
      if ((dX == x) && (dY == y))
      {
         return(-1);
      }

      if (dX < x)
      {
         if (dY < y)
         {
            if (orientation == Orientation.SOUTHWEST)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.SOUTHWEST);
            }
         }
         else if (dY > y)
         {
            if (orientation == Orientation.NORTHWEST)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.NORTHWEST);
            }
         }
         else
         {
            if (orientation == Orientation.WEST)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.WEST);
            }
         }
      }
      else if (dX > x)
      {
         if (dY < y)
         {
            if (orientation == Orientation.SOUTHEAST)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.SOUTHEAST);
            }
         }
         else if (dY > y)
         {
            if (orientation == Orientation.NORTHEAST)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.NORTHEAST);
            }
         }
         else
         {
            if (orientation == Orientation.EAST)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.EAST);
            }
         }
      }
      else
      {
         if (dY < y)
         {
            if (orientation == Orientation.SOUTH)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.SOUTH);
            }
         }
         else if (dY > y)
         {
            if (orientation == Orientation.NORTH)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.NORTH);
            }
         }
         else
         {
            return(FORWARD);
         }
      }
   }


   // Get response that orients toward destination cell.
   public int orientToward(int dX, int dY)
   {
      float mX = (float)Math.abs(dX - x);
      float mY = (float)Math.abs(dY - y);
      float r;

      if (dX < x)
      {
         if (dY < y)
         {
            r = mY / mX;
            if (r < 0.5f)
            {
               return(Orientation.WEST);
            }
            else if (r > 2.0f)
            {
               return(Orientation.SOUTH);
            }
            else
            {
               return(Orientation.SOUTHWEST);
            }
         }
         else if (dY > y)
         {
            r = mY / mX;
            if (r < 0.5f)
            {
               return(Orientation.WEST);
            }
            else if (r > 2.0f)
            {
               return(Orientation.NORTH);
            }
            else
            {
               return(Orientation.NORTHWEST);
            }
         }
         else
         {
            return(Orientation.WEST);
         }
      }
      else if (dX > x)
      {
         if (dY < y)
         {
            r = mY / mX;
            if (r < 0.5f)
            {
               return(Orientation.EAST);
            }
            else if (r > 2.0f)
            {
               return(Orientation.SOUTH);
            }
            else
            {
               return(Orientation.SOUTHEAST);
            }
         }
         else if (dY > y)
         {
            r = mY / mX;
            if (r < 0.5f)
            {
               return(Orientation.EAST);
            }
            else if (r > 2.0f)
            {
               return(Orientation.NORTH);
            }
            else
            {
               return(Orientation.NORTHEAST);
            }
         }
         else
         {
            return(Orientation.EAST);
         }
      }
      else
      {
         if (dY < y)
         {
            return(Orientation.SOUTH);
         }
         else if (dY > y)
         {
            return(Orientation.NORTH);
         }
         else
         {
            // Co-located.
            return(WAIT);
         }
      }
   }


   // Get metamorph DB response.
   public void metamorphDBresponse()
   {
      // Handling nectar?
      if (handlingNectar = autopilotResponse(false))
      {
         Metamorph metamorph = null;
         float     d         = 0.0f;
         float     d2;
         for (Metamorph m : world.metamorphs)
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
            response = WAIT;
         }
         if (debugDB)
         {
            if (response == EXTRACT_NECTAR)
            {
               try
               {
                  Thread.sleep(3000);
               }
               catch (InterruptedException e) {}
            }
            int checkLongDist = morphognostic.locateEvent(2, NECTAR_LONG_DISTANCE_EVENT, false);
            System.out.println("bee=" + id + ",response=" + response + ",checkLongDist=" + checkLongDist);
         }
      }
   }


   // Get metamorph neural network response.
   public void metamorphNNresponse()
   {
      // Handling nectar?
      if (handlingNectar = autopilotResponse(false))
      {
         if (world.metamorphNN != null)
         {
            // Cannot locate hive?
            if (!world.cells[x][y].hive &&
                (morphognostic.locateEvent(3, HIVE_PRESENCE_EVENT, false) == -1))
            {
               // Drop nectar and force return to hive.
               handlingNectar = false;
               nectarCarry    = false;
               morphognostic.clearEvent(SURPLUS_NECTAR_EVENT);
               returnToHiveProbability = 1.0f;
               response = moveTo(Parameters.WORLD_WIDTH / 2, Parameters.WORLD_HEIGHT / 2);
               return;
            }

            // Get NN response.
            response = world.metamorphNN.respond(morphognostic);
            if (debugNN)
            {
               if (response == EXTRACT_NECTAR)
               {
                  try
                  {
                     Thread.sleep(3000);
                  }
                  catch (InterruptedException e) {}
               }
               int checkLongSurplus  = morphognostic.locateEvent(2, SURPLUS_NECTAR_EVENT, false);
               int checkShortSurplus = morphognostic.locateEvent(1, SURPLUS_NECTAR_EVENT, false);
               int checkLongDist     = morphognostic.locateEvent(2, NECTAR_LONG_DISTANCE_EVENT, false);
               int checkShortDist    = morphognostic.locateEvent(1, NECTAR_SHORT_DISTANCE_EVENT, false);
               int i = 3;
               for ( ; i < 11; i++)
               {
                  if (morphognostic.locateEvent(0, i, false) != -1) { break; }
               }
               System.out.println("bee=" + id + ",response=" + response + ",checkLongSurplus=" + checkLongSurplus + ",checkLongDist=" + checkLongDist + ",checkShortSurplus=" + checkShortSurplus + ",checkShortDist=" + checkShortDist + ",checko=" + (i - 3) + ",distanceDisplay=" + nectarDistanceDisplay);
            }
         }
         else
         {
            System.err.println("Must train metamorph neural network");
            response = WAIT;
         }
      }
   }


   // Get goal-seeking DB response.
   public void metamorphGoalSeekingDBresponse()
   {
      Metamorph metamorph    = null;
      float     minCompare   = 0.0f;
      float     maxGoalValue = 0.0f;

      for (int i = 0, j = world.metamorphs.size(); i < j; i++)
      {
         Metamorph m       = world.metamorphs.get(i);
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
         response = WAIT;
      }
      if (debugDB)
      {
         if (response == EXTRACT_NECTAR)
         {
            try
            {
               Thread.sleep(3000);
            }
            catch (InterruptedException e) {}
         }
         int checkLongDist = morphognostic.locateEvent(2, NECTAR_LONG_DISTANCE_EVENT, false);
         System.out.println("bee=" + id + ",response=" + response + ",checkLongDist=" + checkLongDist);
      }
   }


   // Response value from name.
   public static int getResponseValue(String name)
   {
      if (name.equals("turn north"))
      {
         return(Orientation.NORTH);
      }
      if (name.equals("turn northeast"))
      {
         return(Orientation.NORTHEAST);
      }
      if (name.equals("turn east"))
      {
         return(Orientation.EAST);
      }
      if (name.equals("turn southeast"))
      {
         return(Orientation.SOUTHEAST);
      }
      if (name.equals("turn south"))
      {
         return(Orientation.SOUTH);
      }
      if (name.equals("turn southwest"))
      {
         return(Orientation.SOUTHWEST);
      }
      if (name.equals("turn west"))
      {
         return(Orientation.WEST);
      }
      if (name.equals("turn northwest"))
      {
         return(Orientation.NORTHWEST);
      }
      if (name.equals("move forward"))
      {
         return(FORWARD);
      }
      if (name.equals("extract nectar"))
      {
         return(EXTRACT_NECTAR);
      }
      if (name.equals("deposit nectar"))
      {
         return(DEPOSIT_NECTAR);
      }
      if (name.equals("display nectar long distance"))
      {
         return(DISPLAY_NECTAR_LONG_DISTANCE);
      }
      if (name.equals("display nectar short distance"))
      {
         return(DISPLAY_NECTAR_SHORT_DISTANCE);
      }
      if (name.equals("wait"))
      {
         return(WAIT);
      }
      return(-1);
   }


   // Get response name.
   public static String getResponseName(int response)
   {
      switch (response)
      {
      case Orientation.NORTH:
         return("turn north");

      case Orientation.NORTHEAST:
         return("turn northeast");

      case Orientation.EAST:
         return("turn east");

      case Orientation.SOUTHEAST:
         return("turn southeast");

      case Orientation.SOUTH:
         return("turn south");

      case Orientation.SOUTHWEST:
         return("turn southwest");

      case Orientation.WEST:
         return("turn west");

      case Orientation.NORTHWEST:
         return("turn northwest");

      case FORWARD:
         return("move forward");

      case EXTRACT_NECTAR:
         return("extract nectar");

      case DEPOSIT_NECTAR:
         return("deposit nectar");

      case DISPLAY_NECTAR_LONG_DISTANCE:
         return("display nectar long distance");

      case DISPLAY_NECTAR_SHORT_DISTANCE:
         return("display nectar short distance");

      case WAIT:
         return("wait");
      }

      return("unknown");
   }
}
