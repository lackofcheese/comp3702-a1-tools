@echo off
if [%1] == [] (
    SET PROBLEM_PATH=problem.txt
) else (
    SET PROBLEM_PATH="%1"
)
if [%2] == [] (
    SET SOLUTION_PATH=solution.txt
) else (
    SET SOLUTION_PATH="%2"
)
java -cp "bin;junit-4.11.jar;hamcrest-core-1.3.jar" org.junit.runner.JUnitCore tester.Tester
