/*Programming Assignment 1
*Author: Ang Xuan Yin Joel
*ID: 1001075
*Date: 12/03/16 */

Purpose of program:
The program executes a graph of programs in parallel.
It ensures control and data dependencies between processes such that all the processes that are dependent (child processes) 
will wait for their parent processes to complete before running.

How to compile the program:
The program is compiled using JVM. Using the command prompt window and cd to the 
directory with the file processmgt.java is located. 
Type in javac processmgt.java to compile the java file.
To run, type java processmgt <file-name.txt>.
The .txt file will contain the nodes of programs to run in the following format:
command:child nodes:input:output
Noting that each line represents one program.

What exactly the program does:
The program parses the text file as defined by its argument when it runs. 
It creates a data structure for each program defined in each line of the text file.
Then, the program identifies the root nodes of the graph of processes and runs them.
The program will check that the parents for each following child node has completed before allowing the child program to run.
Once all the programs have been run, the program will end.

The program can handle the identify and handle the following errors:
1) IO errors (not being able to locate specified file, invalid commands, etc)
2) Formatting errors with regards to the text file (where the command:child nodes:input:output format is not followed)
3) Dependency cycles (when the dependent processes form a cycle and wait for each other to completed indefinitely) and identify which processes are involved in it
4) Errors in running processes and prematurely terminating child processes of this faulty process (using processError() function)
5) Invalid child nodes (parent processes that specify a child node number that does not exist (>number of processes or a negative value))
6) Interruption errors when dealing with threads
