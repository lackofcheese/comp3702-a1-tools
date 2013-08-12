@echo off
javac problem/*.java visualiser/*.java
javac -cp ".;junit-4.11.jar" tester/*.java
