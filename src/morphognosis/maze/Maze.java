// For conditions of distribution and use, see copyright notice in Main.java

// Maze.

package morphognosis.maze;

import java.util.ArrayList;

public class Maze
{
	// Sensor sequence.
   public ArrayList<float[]>        sensorSequence;
   
   // Response sequence.
   public ArrayList<Integer> responseSequence;
   
   // Sequence cursors.
   public int sensorsCursor;
   public int responseCursor;

   // Constructor.
   public Maze()
   {
	   sensorSequence = new ArrayList<float[]>();
	   responseSequence = new ArrayList<Integer>();
	   sensorsCursor = responseCursor = 0;
   }
   
   // Add sensors to sequence.
   public void addSensors(float[] sensors)
   {
	   sensorSequence.add(sensors);
   }
   
   // Add response to sequence.
   public void addResponse(int response)
   {
	   responseSequence.add(response);
   }
   
   // Reset cursors.
   public void reset()
   {
	   sensorsCursor = responseCursor = 0;
   }
   
   // Next sensors.
   public float[] nextSensors()
   {
	   if (sensorsCursor < sensorSequence.size()) 
	   {
		   return sensorSequence.get(sensorsCursor++);
	   } else {
		   return null;
	   }
   }
   
   // Next response.
   public int nextResponse()
   {
	   if (responseCursor < responseSequence.size()) 
	   {
		   return responseSequence.get(responseCursor++);
	   } else {
		   return -1;
	   }
   }
}
