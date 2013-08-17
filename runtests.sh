#!/bin/sh
if [ -z "$1" ]
then
    export PROBLEM_PATH=problem.txt
else
    export PROBLEM_PATH="$1"
fi
if [ -z "$2" ]
then
    export SOLUTION_PATH=solution.txt
else
    export SOLUTION_PATH="$2"
fi
java -cp "bin;junit-4.11.jar;hamcrest-core-1.3.jar" org.junit.runner.JUnitCore tester.Tester
