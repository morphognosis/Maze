#!/bin/bash
# Generate results.
# Parameters:
#
#      Maze maker parameters:
#        [-numDoors <quantity> (default=3)]
#        [-mazeInteriorLength <length> (default=4)]
#        [-numContextMazes <quantity> (default=5)]
#        [-numIndependentMazes <quantity> (default=2)]
#      Morphognosis parameters:
#        [-neighborhoodDurations <comma-separated values> (implies number of neighborhoods)]
#      Metamorph Weka neural network parameters:
#        [-NNlearningRate <quantity> (default=0.1)]
#        [-NNmomentum <quantity> (default=0.2)]
#        [-NNhiddenLayers <quantity> (default="50")]
#        [-NNtrainingTime <quantity> (default=5000)];
MIN_NUM_DOORS=3
MAX_NUM_DOORS=5
MIN_MAZE_INTERIOR_LENGTH=4
MAX_MAZE_INTERIOR_LENGTH=6
MIN_CONTEXT_MAZES=10
MAX_CONTEXT_MAZES=12
MIN_NUM_INDEPENDENT_MAZES=10
MAX_NUM_INDEPENDENT_MAZES=12

for (( numDoors=${MIN_NUM_DOORS}; numDoors <= $MAX_NUM_DOORS; ++numDoors)); do
  echo numDoors = $numDoors
  for (( mazeInteriorLength=${MIN_MAZE_INTERIOR_LENGTH}; mazeInteriorLength <= $MAX_MAZE_INTERIOR_LENGTH; ++mazeInteriorLength)); do
    echo mazeInteriorLength = $mazeInteriorLength
    for (( numContextMazes=${MIN_CONTEXT_MAZES}; numContextMazes <= $MAX_CONTEXT_MAZES; ++numContextMazes)); do
      echo numContextMazes = $numContextMazes
      for (( numIndependentMazes=${MIN_NUM_INDEPENDENT_MAZES}; numIndependentMazes <= $MAX_NUM_INDEPENDENT_MAZES; ++numIndependentMazes)); do
        echo numIndependentMazes = $numIndependentMazes
        trials=10
        echo Trials = $trials
        for (( i=0; i< $trials; ++i)); do
            echo trial = $i
            randomSeed=`expr $i + 4517`
            ./maze.sh -batch -responseDriver metamorphNN -randomSeed $randomSeed -numDoors $numDoors -mazeInteriorLength $mazeInteriorLength -numContextMazes $numContextMazes -numIndependentMazes $numIndependentMazes
            python maze_rnn.py
        done
      done
    done
  done
done