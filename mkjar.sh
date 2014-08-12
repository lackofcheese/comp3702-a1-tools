rm visualiser.jar
cd bin
jar cvfe ../visualiser.jar visualiser.Visualiser .
cd ../src
jar uvf ../visualiser.jar .
cd ..
chmod +x visualiser.jar
