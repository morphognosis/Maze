# Maze maker.
# 
# Make mazes for training and testing maze learning.
# 
# There are three learning goals:
# 1. Maze learning: learn to navigate a sequence of rooms connected by doors.
# 2. Context learning: learn correspondences between room configurations separated
#    by intervening mazes.
# 2. Modular context learning: the intervening mazes are trained independently and
#    presenting only during testing. This measures the ability to combine independtly learned
#    sequences to achieve success.
# 
# A maze consists of a sequence of rooms connected by doors.
# There are a fixed number of doors corresponding to learner outputs.
# A room consists of two on/off configurations:
#    A room type.
#    A room-specific pattern.
# 
# These are the types of rooms:
# 
# 1. Context begin room: in this room the learner is presented with a pattern with a single on value
#    that directly corresponds to the correct door choice.
#    This door leads to either a maze entry or directly to a context end room. If it leads to
#    a maze entry room, a maze must be navigated before reaching the context end room. In the 
#    context end room, the pattern is completely on. The learner must choose the door that was
#    on in the context begin room.
# 
# 2. Maze entry room: the pattern in the maze entry room indicates the configuration of the upcoming
#    maze sequence, consisting of maze interior rooms. The learner uses this information to navigate
#    the maze.
#    
# 3. Maze interior room: the pattern in this type of room is randomly generated at maze creation and 
#    is not related to the correct door choice; rather the pattern in the maze entry room determine the
#    correct door choice sequence for the maze.
#    
# 4. Context end room: in this room the pattern is entirely in the on condition. The door choice is
#    determined by the context begin room pattern.
# 
# Input:
# The input consists of the room type and room pattern components:
# <room type><pattern>
# 
# Room type format:
# 1000: context begin room
# 0100: maze entry
# 0010: maze interior
# 0001: context end room
# 0000: empty room
# 
# Output: 
# The number of outputs equals the size of the room pattern.
# 
# Training and testing.
# 
# There are three types of sequences:
# 1. Context: context begin room and context end room. To learn context associations.
# 2. Maze-module: maze entry and maze interior sequence. To learn maze sequences.
# 3. Context-maze: context begin room, maze-module, and context end room.
# 
# For training, a set of the above sequences are created. This measures maze and context
# learning.
# 
# For testing, context-mazes are created from novel context and maze-module combinations taken
# from the training set. This measures modular context learning.
#
# Output dataset file: maze_dataset.py
# Contains:
# X_train_shape = [<number of sequences>, <steps per sequence>, <input size>]
# X_train_seq = [<input sequences (0|1)>]
# y_train_shape = [<number of sequences>, <steps per sequence>, <output size>]
# y_train_seq = [<output sequences>]
# X_test_shape = [<number of sequences>, <steps per sequence>, <input size>]
# X_test_seq = [<input sequences (0|1)>]
# y_test_shape = [<number of sequences>, <steps per sequence>, <output size>]
# y_test_seq = [<output sequences>]

import sys, getopt

# Default parameters.
num_doors = 3
maze_interior_sequence_length = 5
num_context_mazes = 5
num_non_context_mazes = 5

# Get options.
usage = 'maze_maker.py [--num_doors <number>] [--maze_interior_sequence_length <length>] [--num_context_mazes <number>] [--num_non_context_mazes <number>]'
try:
  opts, args = getopt.getopt(sys.argv[1:],"h",["help","num_doors=","maze_interior_sequence_length=","num_context_mazes=","num_non_context_mazes="])
except getopt.GetoptError:
  print(usage)
  sys.exit(1)
for opt, arg in opts:
  if opt == '-h' or opt == '--help':
     print(usage)
     sys.exit(0)
  if opt == "--num_doors":
     num_doors = int(arg)
  elif opt == "--maze_interior_sequence_length":
     maze_interior_sequence_length = int(arg)
  elif opt == "--num_context_mazes":
     num_context_mazes = int(arg)
  elif opt == "--num_non_context_mazes":
     num_non_context_mazes = int(arg)
  else:
     print(usage)
     sys.exit(1)
     
# Create training set.
total_sequence_length = maze_interior_sequence_length + 3

# Create context sequences.


