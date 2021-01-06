# For conditions of distribution and use, see copyright notice in Main.java
#
# Maze maker.
# 
# Make mazes for training and testing maze learning.
# 
# There are three learning goals:
# 1. Maze learning: learn to navigate a sequence of rooms connected by doors.
# 2. Context learning: learn correspondences between room configurations separated
#    by intervening mazes.
# 2. Modular context learning: the intervening mazes are trained independently and
#    presenting only during testing. This measures the ability to dynamically combine 
#    independtly learned sequences to achieve success.
#
# A maze consists of a sequence of rooms connected by doors.
# There are a fixed number of doors.
# The learner outputs a door choice or a wait.
# A room contains a room-specific set of on/off marks.
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
# Input format:
# <room identifier><context room marks><maze entry marks><maze interior marks><context end room marks>
#
# Room identifier format:
context_begin_room = [1,0,0,0,0]
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
# Output dataset files:
output_dataset_module = 'maze_dataset.py'
# Contains:
# X_train_shape = [<number of sequences>, <steps per sequence>, <input size>]
# X_train_seq = [<input sequences (0|1)>]
# y_train_shape = [<number of sequences>, <steps per sequence>, <output size>]
# y_train_seq = [<output sequences>]
# X_test_shape = [<number of sequences>, <steps per sequence>, <input size>]
# X_test_seq = [<input sequences (0|1)>]
# y_test_shape = [<number of sequences>, <steps per sequence>, <output size>]
# y_test_seq = [<output sequences>]
output_dataset_csv = 'maze_dataset.csv'
# Contains:
# Training input shape and sequences:
# <number of sequences>, <steps per sequence>, <input size>
# <input sequences (0|1)>
# Training output shape and sequences:
# <number of sequences>, <steps per sequence>, <output size>
# <output sequences>]
# Testing input shape and sequences:
# <number of sequences>, <steps per sequence>, <input size>
# input sequences (0|1)>
# Testing output shape and sequences:
# <number of sequences>, <steps per sequence>, <output size>
# <output sequences>

from numpy import array
import sys, getopt, random

# Default parameters.
num_room_marks = 5
num_doors = 3
maze_interior_sequence_length = 5
num_context_mazes = 5
num_independent_mazes = 5
random_seed = 4517
verbose = True

# Get options.
usage = 'maze_maker.py [--num_room_marks <quantity>] [--num_doors <quantity>] [--maze_interior_sequence_length <length>] [--num_context_mazes <quantity>] [--num_independent_mazes <quantity>] [--random_seed <seed>] [--verbose on|off]'
try:
  opts, args = getopt.getopt(sys.argv[1:],"h",["help","num_room_marks=","num_doors=","maze_interior_sequence_length=","num_context_mazes=","num_independent_mazes=","random_seed=","verbose="])
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
     if arg == 'on':
         verbose = True
     elif arg == 'off':
         verbose = False
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

off_doors = []
for i in range(num_doors):
    off_doors += [0]
off_room_marks = []
for i in range(num_room_marks):
    off_room_marks += [0]

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
    X_seq += (off_doors + gen_marks() + off_room_marks + off_doors)
    door = random.randrange(0, num_doors)
    for i in range(num_doors + 1):
        if i == door:
            y_seq += [1]
        else:
            y_seq += [0]
    for room in range(maze_interior_sequence_length):
        X_seq += maze_interior
        X_seq += (off_doors + off_room_marks + gen_marks() + off_doors)
        door = random.randrange(0, num_doors)
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
num_inputs = 5 + num_doors + num_room_marks + num_room_marks + num_doors
num_outputs = num_doors + 1
X_train_shape = [num_train_sequences, sequence_steps, num_inputs]
X_train_seq = []
y_train_shape = [num_train_sequences, sequence_steps, num_outputs]
y_train_seq = []

# Create context sequences.
for door in range(num_context_sequences):
    X_train_seq += context_begin_room
    for i in range(num_doors):
        if i == door:
            X_train_seq += [1]
        else:
            X_train_seq += [0]
    for i in range(num_doors + 1):
        if i == door:
            y_train_seq += [1]
        else:
            y_train_seq += [0]
    X_train_seq += (off_room_marks + off_room_marks + off_doors)
    X_train_seq += context_end_room
    X_train_seq += (off_doors + off_room_marks + off_room_marks)
    for i in range(num_doors):
        X_train_seq += [1]
    for i in range(num_doors + 1):
        if i == door:
            y_train_seq += [1]
        else:
            y_train_seq += [0]
    for i in range(sequence_steps - 2):
        X_train_seq += empty_room
        X_train_seq += (off_doors + off_room_marks + off_room_marks + off_doors)
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
        for i in range(num_doors):
            if i == door:
                X_train_seq += [1]
            else:
                X_train_seq += [0]
        for i in range(num_doors + 1):
            if i == door:
                y_train_seq += [1]
            else:
                y_train_seq += [0]
        X_train_seq += (off_room_marks + off_room_marks + off_doors)
        X_train_seq += X_maze_context_seqs[maze]
        y_train_seq += y_maze_context_seqs[maze]
        X_train_seq += context_end_room
        X_train_seq += (off_doors + off_room_marks + off_room_marks)
        for i in range(num_doors):
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
        X_train_seq += (off_doors + off_room_marks + off_room_marks + off_doors)
        for i in range(num_doors):
            y_train_seq += [0]
        y_train_seq += [1]

# Create testing set:

# Create shapes.
num_test_sequences = num_doors * num_independent_mazes
X_test_shape = [num_test_sequences, sequence_steps, num_inputs]
X_test_seq = []
y_test_shape = [num_test_sequences, sequence_steps, num_outputs]
y_test_seq = []

# Combine contexts with independent mazes.
for door in range(num_context_sequences):
    for maze in range(num_independent_mazes):
        X_test_seq += context_begin_room
        for i in range(num_doors):
            if i == door:
                X_test_seq += [1]
            else:
                X_test_seq += [0]
        X_test_seq += (off_room_marks + off_room_marks + off_doors)
        for i in range(num_doors + 1):
            if i == door:
                y_test_seq += [1]
            else:
                y_test_seq += [0]
        X_test_seq += X_maze_independent_seqs[maze]
        y_test_seq += y_maze_independent_seqs[maze]
        X_test_seq += context_end_room
        X_test_seq += (off_doors + off_room_marks + off_room_marks)
        for i in range(num_doors):
            X_test_seq += [1]
        for i in range(num_doors + 1):
            if i == door:
                y_test_seq += [1]
            else:
                y_test_seq += [0]

# Write dataset.
if verbose:
    print("Writing maze dataset to ", output_dataset_module, " and ", output_dataset_csv, sep='')
with open(output_dataset_module, 'w') as module, open(output_dataset_csv, 'w') as csv:
    if verbose:
        print('Training data:')
        print('X_train_shape = [', num_train_sequences, ',', sequence_steps, ',', num_inputs, ']')
    print('X_train_shape = [', num_train_sequences, ',', sequence_steps, ',', num_inputs, ']', file=module)
    print(num_train_sequences, ',', sequence_steps, ',', num_inputs, sep='', file=csv)
    module.write('X_train_seq = [ ')
    first = True
    for value in X_train_seq:
        if first:
            first = False
        else:
            module.write(", ")
            csv.write(",")
        module.write("%s" % value)
        csv.write("%s" % value)
    module.write(' ]\n')
    csv.write('\n')
    if verbose:
        print('X_train_seq = [ ', end='')
        first = True
        for value in X_train_seq:
            if first:
                first = False
            else:
                print(", ", end='')
            print(value, end='')
        print(' ]')
        print('y_train_shape = [', num_train_sequences, ',', sequence_steps, ',', num_outputs, ']')
    print('y_train_shape = [', num_train_sequences, ',', sequence_steps, ',', num_outputs, ']', file=module)
    print(num_train_sequences, ',', sequence_steps, ',', num_outputs, sep='', file=csv)
    module.write('y_train_seq = [ ')
    first = True
    for value in y_train_seq:
        if first:
            first = False
        else:
            module.write(", ")
            csv.write(",")
        module.write("%s" % value)
        csv.write("%s" % value)
    module.write(' ]\n')
    csv.write('\n')
    if verbose:
        print('y_train_seq = [ ', end='')
        first = True
        for value in y_train_seq:
            if first:
                first = False
            else:
                print(", ", end='')
            print(value, end='')
        print(' ]')
        print('Testing data:')
        print('X_test_shape = [', num_test_sequences, ',', sequence_steps, ',', num_inputs, ']')
    print('X_test_shape = [', num_test_sequences, ',', sequence_steps, ',', num_inputs, ']', file=module)
    print(num_test_sequences, ',', sequence_steps, ',', num_inputs, sep='', file=csv)
    module.write('X_test_seq = [ ')
    first = True
    for value in X_test_seq:
        if first:
            first = False
        else:
            module.write(", ")
            csv.write(",")
        module.write("%s" % value)
        csv.write("%s" % value)
    module.write(' ]\n')
    csv.write('\n')
    if verbose:
        print('X_test_seq = [ ', end='')
        first = True
        for value in X_test_seq:
            if first:
                first = False
            else:
                print(", ", end='')
            print(value, end='')
        print(' ]')
        print('y_test_shape = [', num_test_sequences, ',', sequence_steps, ',', num_outputs, ']')
    print('y_test_shape = [', num_test_sequences, ',', sequence_steps, ',', num_outputs, ']', file=module)
    print(num_test_sequences, ',', sequence_steps, ',', num_outputs, sep='', file=csv)
    module.write('y_test_seq = [ ')
    first = True
    for value in y_test_seq:
        if first:
            first = False
        else:
            module.write(", ")
            csv.write(",")
        module.write("%s" % value)
        csv.write("%s" % value)
    module.write(' ]\n')
    csv.write('\n')
    if verbose:
        print('y_test_seq = [ ', end='')
        first = True
        for value in y_test_seq:
            if first:
                first = False
            else:
                print(", ", end='')
            print(value, end='')
        print(' ]')
        
# Print interpreted dataset.
if verbose:

    print('Training mazes:')
    seq = array(X_train_seq)
    X = seq.reshape(X_train_shape[0], X_train_shape[1], X_train_shape[2])
    seq = array(y_train_seq)
    y = seq.reshape(y_train_shape[0], y_train_shape[1], y_train_shape[2])
    for seq in range(X_train_shape[0]):
        print('maze =', seq)
        for step in range(X_train_shape[1]):
            room = X[seq][step][0:5]
            marks = X[seq][step][5:]
            print('input: { room =', room, end='')
            room = list(room)
            if room == context_begin_room:
                print(' (context_begin_room)', end='')
            elif room == maze_entry:
                print(' (maze_entry)        ', end='')
            elif room == maze_interior:
                print(' (maze_interior)     ', end='')
            elif room == context_end_room:
                print(' (context_end_room)  ', end='')
            else:
                print(' (empty_room)        ', end='')
            print(' marks =', marks[0:num_doors], end='')
            print('', marks[num_doors:num_doors + num_room_marks], end='')
            print('', marks[num_doors + num_room_marks:num_doors + (num_room_marks * 2)], end='')
            print('', marks[num_doors + (num_room_marks * 2):num_doors + (num_room_marks * 2) + num_doors], end='')
            print(' }', end='')
            output = y[seq][step]
            print(' output =', output, end='')
            door = list(output).index(1)
            if door < num_doors:
                s = ' (door ' + str(door) + ')'
                print(s)
            else:
                print(' (wait)')

    print('Test mazes:')
    seq = array(X_test_seq)
    X = seq.reshape(X_test_shape[0], X_test_shape[1], X_test_shape[2])
    seq = array(y_test_seq)
    y = seq.reshape(y_test_shape[0], y_test_shape[1], y_test_shape[2])
    for seq in range(X_test_shape[0]):
        print('maze =', seq)
        for step in range(X_test_shape[1]):
            room = X[seq][step][0:5]
            marks = X[seq][step][5:]
            print('input: { room =', room, end='')
            room = list(room)
            if room == context_begin_room:
                print(' (context_begin_room)', end='')
            elif room == maze_entry:
                print(' (maze_entry)        ', end='')
            elif room == maze_interior:
                print(' (maze_interior)     ', end='')
            elif room == context_end_room:
                print(' (context_end_room)  ', end='')
            else:
                print(' (empty_room)        ', end='')
            print(' marks =', marks[0:num_doors], end='')
            print('', marks[num_doors:num_doors + num_room_marks], end='')
            print('', marks[num_doors + num_room_marks:num_doors + (num_room_marks * 2)], end='')
            print('', marks[num_doors + (num_room_marks * 2):num_doors + (num_room_marks * 2) + num_doors], end='')
            print(' }', end='')
            output = y[seq][step]
            print(' output =', output, end='')
            door = list(output).index(1)
            if door < num_doors:
                s = ' (door ' + str(door) + ')'
                print(s)
            else:
                print(' (wait)')
