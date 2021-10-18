call 0-settings.bat

echo off
echo        [44;93m+-------------------------------------------------------------------------+[0m
echo        [44;93m!                   Now we are building clients...                        ![0m
echo        [44;93m+-------------------------------------------------------------------------+[0m
echo on

IF "%LANGUAGE%"=="php" (
    call :php
)

IF "%LANGUAGE%"=="python" (
    call :python
)

cd %LANGUAGE%
call 4-test.bat
cd %ROOT%

goto :eof

:php
    SET PATH=%PHP_HOME%;%PATH%
goto :eof

:python
    SET PATH=%PYTHON_HOME%;%PATH%
goto :eof