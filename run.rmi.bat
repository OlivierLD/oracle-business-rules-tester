@echo off
@setlocal
set JAVA_HOME=d:\Program Files\Java\jdk1.7.0
"%JAVA_HOME%\bin\rmic" -help
"%JAVA_HOME%\bin\rmic" -classpath .\classes -keep -nowrite -verbose -d .\classes remoterules.RulesTesterImplementation
move .\classes\remoterules\*.java .\src\remoterules
@endlocal
