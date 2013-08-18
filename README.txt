A visualiser and tester for COMP3702 Assignment 1, by Dimitri Klimenko (tutor).


(1) Commands and arguments
The runnable files and their arguments are:
Script to run tests:
    runtests [problem-file] [solution-file]
Test class (must be run with JUnit):
    tester.Tester
Visualiser:
    visualiser.Visualiser [problem-file] [solution-file]


(2) Running the Visualiser
To run it, simply run visualiser.jar with Java 7 (double-clicking should work
if Java is installed properly). If this doesn't work or you want to run it
with a different version, I recommend using Eclipse - simply add the contents
of a1-tools.zip to a new project.
Alternatively, see the manual compilation instructions in section (3).

You can also run it from the command line with optional
command-line arguments:
    java -jar visualiser.jar [problem-file] [solution-file]

For example, using the test cases provided here:
    java -jar visualiser.jar problem.txt solution.txt

Note that these commands may require you to use full path to java.exe,
as per section (5).


(3) Running the Tester
The recommended way to run the tests is by doing so using Eclipse.
Simply add the contents of a1-tools.zip to a new project, and 
then make sure to add JUnit 4 to the project's build path (right click on
the project, go to Build Path->Add Libraries, and then choose JUnit 4).

To select which files to run the tests on, edit the values of the variables 
problemPath and solutionPath inside the Tester class.

Alternatively, compile the source code as per section (4), and you can use
either runtests.bat or runtests.sh to run the tests; the usage is:
    runtests [problem-file] [solution-file]
Note that you may need to update your system path or change the commands in
the runtests script as per (5).


(4) Manual Compilation
If you want to compile and run the code manually, you will need to do the
following:
1) Download and install Apache Ant.
2) Extract a1-tools.zip to the desired folder.
3) From within that folder, run the command
    ant

To run the visualiser, you may now use the command
    java -cp bin visualiser/Visualiser
As with the JAR, you can add command-line arguments, e.g.
    java -cp bin visualiser/Visualiser problem.txt solution.txt

The commands above may require full paths; see section (5).


(5) The command line and the system path
Note that for the command-line commands to work Java would have to be on your
system path; if not, you'll have to specify a full path instead of just 
"java", e.g.
"C:\Program Files (x86)\Java\jdk1.7.0_25\bin\java.exe"
or
/usr/java/jdk1.7.0_25/bin/java

If you're using the .bat or .sh scripts you will need to replace the commands
"java" and "javac" with their full paths.
