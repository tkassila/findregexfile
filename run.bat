@echo off
call \java\jdk11fxcp.bat
rem java version 11 should be or later:
java -version
#set M2_HOME=C:\Users\tkassila\.m2\repository
ho %0
set dt_exec_drive=%~d0
set dt_exec_path=%~p0
set dt_exec_path2=%dt_exec_drive%%dt_exec_path%

set GJARSAN2_HOME=%dt_exec_path2%
java -cp %GJARSAN2_HOME%findregexfile.jar; com.metait.findregexfile.FindRegexFileApplication  %*
rem pause