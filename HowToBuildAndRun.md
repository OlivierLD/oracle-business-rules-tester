## Build and Run ##
You need:
  * An svn client (like SlikSVN), make sure the `svn` executable is in your path
  * JDeveloper 11.1.1.5.0 or newer

### Check-out the code ###
Create a new directory on your system, navigate to it in a console, and type
```
Prompt> svn checkout http://oracle-business-rules-tester.googlecode.com/svn/trunk/ OBRTester
```
Then, in JDeveloper, open the project created in the directory OBRTester, named `RulesTestingFramework.jpr` (create an Application to host it, if necessary).
<br />
Go to the Project Properties, edit the Libraries and Classpath, and make sure Tools.jar is resolved (this is a jar, not a library).
<br />
Once Tools.jar is OK, you can deploy the project. It should generate a jar in OBRTester/deploy, named JarRulesTestingFramework.jar.
<br />
You can run the GUI from JDev (there is a Run Configuration in the project), or from the command line.
### Run from the command line ###
  * Edit the file named `run` on Linux/MacOS, `run.bat` on Windows.
  * Make sure the system variable named **JDEV\_HOME** is set correctly
  * Make sure the tools.jar (last element of the classpath) is correctly set
  * Run it.