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
# There are a fixed number of doors.
# The learner outputs a door choice or a wait.
# A room consists of three on/off configurations:
#    Room type.
#    Room marks.
#
# These are the types of rooms:
# 
# 1. Context begin room: in this room the learner is presented with marks having a single
#    on value that corresponds to the correct door choice.
#    This door leads to either a maze entry or directly to a context end room. If it leads to
#    a maze entry room, a maze must be navigated before reaching the context end room. 
#    In the context end room, the learner must choose the door that was marked on in the 
#    context begin room.
#
# 2. Maze entry room: the marks identify the configuration of the upcoming maze sequence,
#    consisting of maze interior rooms. The learner uses this information to navigate
#    the maze.
#    
# 3. Maze interior room: the marks values in this type of room are randomly generated at maze 
#    creation; the marks in the maze entry room determine the correct door choice sequence 
#    to move through the maze.
#
# 4. Context end room: in this room the correct door choice is determined by the context begin
#    room door marks.
#
# Input:
# The input consists of the room type and room mark components:
# <room type><marks>
# 
# Room type format:
context_begin_room = [1,0,0,0,9]
maze_entry = [0,1,0,0,0]
maze_interior = [0,0,1,0,0]
context_end_room = [0,0,0,1,0]
empty_room = [0,0,0,0,1]
#
# Output: 
# A door choice or a wait.
# 
# Training and testing.
# 
# There are three types of sequences:
# 1. Context: context begin room and context end room. To learn context associations.
# 2. Context maze: context begin room, maze entry and interior sequence, and context end room.
# 3. Independent maze: maze entry and maze interior sequence. To learn maze sequences.
# 
# For training, a set of the above sequences are created. This evaluates maze and context
# learning.
# 
# For testing, context-mazes are created from novel context and independent maze combinations
# taken from the training set. This evaluates modular context learning.
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

import sys, getopt, random

# Default parameters.
num_room_marks = 5
num_doors = 3
maze_interior_sequence_length = 5
num_context_mazes = 5
num_independent_mazes = 5
random_seed = 4517
verbose = False

# Get options.
usage = 'maze_maker.py [--num_room_marks <quantity>] [--num_doors <quantity>] [--maze_interior_sequence_length <length>] [--num_context_mazes <quantity>] [--num_independent_mazes <quantity>] [--random_seed <seed>] [--verbose]'
try:
  opts, args = getopt.getopt(sys.argv[1:],"h",["help","verbose","num_room_marks=","num_doors=","maze_interior_sequence_length=","num_context_mazes=","num_independent_mazes=","random_seed="])
except getopt.GetoptError:
  print(usage)
  sys.exit(1)
for opt, arg in opts:
  if opt == '-h' or opt == '--help':
     print(usage)
     sys.exit(0)
  if opt == "--num_room_marks":
     num_room_marks = int(arg)
  elif opt == "--num_doors":
     num_doors = int(arg)
  elif opt == "--maze_interior_sequence_length":
     maze_interior_sequence_length = int(arg)
  elif opt == "--num_context_mazes":
     num_context_mazes = int(arg)
  elif opt == "--num_independent_mazes":
     num_independent_mazes = int(arg)
  elif opt == "--random_seed":
     random_seed = int(arg)
  elif opt == "--verbose":
     verbose = True
  else:
     print(usage)
     sys.exit(1)
if num_room_marks <= 0:
     print(usage)
     sys.exit(1)
if num_doors <= 0:
     print(usage)
     sys.exit(1)
if num_room_marks < num_doors:
    print("Number of doors cannot exceed number of room marks")
    sys.exit(1)
m = 2
n = num_context_mazes  + num_independent_mazes + 1
i = 1
while m < n:
    m *= 2
    i += 1
if i > num_room_marks:
    print("Insufficient number of room marks")
    sys.exit(1)
random.seed(random_seed)

if verbose:
    print("Parameters:")
    print("num_room_marks=", num_room_marks)
    print("num_doors=", num_doors)
    print("maze_interior_sequence_length=", maze_interior_sequence_length)
    print("num_context_mazes=", num_context_mazes)
    print("num_independent_mazes=", num_independent_mazes)
    print("random_seed=", random_seed)

# Generate a maze from marks.
def gen_maze():

    # Generate marks.
    def gen_marks():
        marks = []
        while True:
            marks = []
            for i in range(num_room_marks):
                marks.append(random.randint(0, 1))
            if 1 in marks:
                break
        return marks

    X_seq = []
    y_seq = []
    X_seq += maze_entry
    X_seq += gen_marks()
    door = random.randint(0, num_doors + 1)
    for i in range(num_doors + 1):
        if i == door:
            y_seq += [1]
        else:
            y_seq += [0]
    for room in range(maze_interior_sequence_length):
        X_seq += maze_interior
        X_seq += gen_marks()
        door = random.randint(0, num_doors + 1)
        for i in range(num_doors + 1):
            if i == door:
                y_seq += [1]
            else:
                y_seq += [0]
    return X_seq, y_seq

# Create training set:

# Create shapes.
sequence_steps = maze_interior_sequence_length + 3
num_context_sequences = num_doors
num_context_maze_sequences = num_doors * num_context_mazes
num_independent_maze_sequences = num_independent_mazes
num_train_sequences = num_context_sequences + num_context_maze_sequences + num_independent_maze_sequences
X_train_shape = [num_train_sequences, sequence_steps, num_room_marks + 5]
X_train_seq = []
y_train_shape = [num_train_sequences, sequence_steps, num_doors + 1]
y_train_seq = []

# Create context sequences.
for door in range(num_context_sequences):
    X_train_seq += context_begin_room
    for i in range(num_room_marks):
        if i == door:
            X_train_seq += [1]
        else:
            X_train_seq += [0]
    for i in range(num_doors + 1):
        if i == door:
            y_train_seq += [1]
        else:
            y_train_seq += [0]
    X_train_seq += context_end_room
    for i in range(num_room_marks):
        X_train_seq += [1]
    for i in range(num_doors + 1):
        if i == door:
            y_train_seq += [1]
        else:
            y_train_seq += [0]
    for i in range(sequence_steps - 2):
        X_train_seq += empty_room
        for i in range(num_room_marks):
            X_train_seq += [0]
        for i in range(num_doors):
            y_train_seq += [0]
        y_train_seq += [1]

# Create context-maze sequences.

# Create mazes for contexts.
X_maze_context_seqs = []
y_maze_context_seqs = []
n = 0
while n < num_context_mazes:
    X, y = gen_maze()
    if X not in X_maze_context_seqs:
        X_maze_context_seqs.append(X)
        y_maze_context_seqs.append(y)
        n += 1

# Combine contexts with mazes.
for door in range(num_context_sequences):
    for maze in range(num_context_mazes):
        X_train_seq += context_begin_room
        for i in range(num_room_marks):
            if i == door:
                X_train_seq += [1]
            else:
                X_train_seq += [0]
        for i in range(num_doors + 1):
            if i == door:
                y_train_seq += [1]
            else:
                y_train_seq += [0]
        X_train_seq += X_maze_context_seqs[maze]
        y_train_seq += y_maze_context_seqs[maze]
        X_train_seq += context_end_room
        for i in range(num_room_marks):
            X_train_seq += [1]
        for i in range(num_doors + 1):
            if i == door:
                y_train_seq += [1]
            else:
                y_train_seq += [0]

# Create independent mazes.
X_maze_independent_seqs = []
y_maze_independent_seqs = []
n = 0
while n < num_independent_mazes:
    X, y = gen_maze()
    if X not in X_maze_independent_seqs and X not in X_maze_context_seqs:
        X_maze_independent_seqs.append(X)
        y_maze_independent_seqs.append(y)
        n += 1

for maze in range(num_independent_mazes):
    X_train_seq += X_maze_independent_seqs[maze]
    y_train_seq += y_maze_independent_seqs[maze]
    for i in range(2):
        X_train_seq += empty_room
        for i in range(num_room_marks):
            X_train_seq += [0]
        for i in range(num_doors):
            y_train_seq += [0]
        y_train_seq += [1]

# Create testing set:

# Create shapes.
num_test_sequences = num_doors * num_independent_mazes
X_test_shape = [num_test_sequences, sequence_steps, num_room_marks + 5]
X_test_seq = []
y_test_shape = [num_test_sequences, sequence_steps, num_doors + 1]
y_test_seq = []

# Combine contexts with independent mazes.
for door in range(num_context_sequences):
    for maze in range(num_independent_mazes):
        X_test_seq += context_begin_room
        for i in range(num_room_marks):
            if i == door:
                X_test_seq += [1]
            else:
                X_test_seq += [0]
        for i in range(num_doors + 1):
            if i == door:
                y_test_seq += [1]
            else:
                y_test_seq += [0]
        X_test_seq += X_maze_independent_seqs[maze]
        y_test_seq += y_maze_independent_seqs[maze]
        X_test_seq += context_end_room
        for i in range(num_room_marks):
            X_test_seq += [1]
        for i in range(num_doors + 1):
            if i == door:
                y_test_seq += [1]
            else:
                y_test_seq += [0]

# Write dataset.
with open('maze_dataset.py', 'w') as f:
    if verbose:
        print('Training data:')
        print('X_train_shape = [', num_train_sequences, ',', sequence_steps, ',', (num_room_marks + 5), ']')
    print('X_train_shape = [', num_train_sequences, ',', sequence_steps, ',', (num_room_marks + 5), ']', file=f)
    f.write('X_train_seq = [ ')
    for value in X_train_seq:
        f.write("%s " % value)
    f.write(']\n')
    if verbose:
        print('y_train_shape = [', num_train_sequences, ',', sequence_steps, ',', (num_doors + 1), ']')
    print('y_train_shape = [', num_train_sequences, ',', sequence_steps, ',', (num_doors + 1), ']', file=f)
    f.write('y_train_seq = [ ')
    for value in y_train_seq:
        f.write("%s " % value)
    f.write(']\n')
    if verbose:
        print('Testing data:')
        print('X_test_shape = [', num_test_sequences, ',', sequence_steps, ',', (num_room_marks + 5), ']')
    print('X_test_shape = [', num_test_sequences, ',', sequence_steps, ',', (num_room_marks + 5), ']', file=f)
    f.write('X_test_seq = [ ')
    for value in X_test_seq:
        f.write("%s " % value)
    f.write(']\n')
    if verbose:
        print('y_test_shape = [', num_test_sequences, ',', sequence_steps, ',', (num_doors + 1), ']')
    print('y_test_shape = [', num_test_sequences, ',', sequence_steps, ',', (num_doors + 1), ']', file=f)
    f.write('y_test_seq = [ ')
    for value in y_test_seq:
        f.write("%s " % value)
    f.write(']\n')