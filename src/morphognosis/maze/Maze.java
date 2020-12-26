// For conditions of distribution and use, see copyright notice in Main.java

// Maze.

package morphognosis.maze;

import java.util.ArrayList;

public class Maze
{
	// Sensor sequence.
   public ArrayList<boolean[]>        sensorSequence;
   
   // Response sequence.
   public ArrayList<boolean[]> responseSequence;
   
   // Sequence cursors.
   public int sensorsCursor;
   public int responseCursor;

   // Constructor.
   public Maze()
   {
	   sensorSequence = new ArrayList<boolean[]>();
	   responseSequence = new ArrayList<boolean[]>();
	   sensorsCursor = responseCursor = 0;
   }
   
   // Add sensors to sequence.
   public void addSensors(boolean[] sensors)
   {
	   sensorSequence.add(sensors);
   }
   
   // Add response to sequence.
   public void addResponses(boolean[] responses)
   {
	   responseSequence.add(responses);
   }
   
   // Reset cursors.
   public void resetCursors()
   {
	   sensorsCursor = responseCursor = 0;
   }
   
   // Next sensors.
   public boolean[] nextSensors()
   {
	   if (sensorsCursor < sensorSequence.size()) 
	   {
		   return sensorSequence.get(sensorsCursor++);
	   } else {
		   return null;
	   }
   }
   
   // Next response.
   public boolean[] nextResponse()
   {
	   if (responseCursor < responseSequence.size()) 
	   {
		   return responseSequence.get(responseCursor++);
	   } else {
		   return null;
	   }
   }
}
