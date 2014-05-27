@echo off
set RESTVAR=
:loop1
if "%1"=="" goto after_loop
set RESTVAR=%RESTVAR% %1
shift
goto loop1

:after_loop
echo %RESTVAR%

java -jar target/pkiservice-client-0.0.1-SNAPSHOT-jar-with-dependencies.jar %RESTVAR%