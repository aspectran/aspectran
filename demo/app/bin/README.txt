================================================================================
Aspectran Application Startup Scripts
================================================================================

This directory contains scripts for running the Aspectran application in
various modes on both Windows and Unix-like systems (Linux, macOS, etc.).


-----------------
1. Configuration
-----------------

`run.options`
  - A configuration file for setting Java Virtual Machine (JVM) options such as
    heap size (`JVM_MS`, `JVM_MX`).
  - These settings are applied by all startup scripts in this directory.


---------------------------
2. Unix-like Scripts (.sh)
---------------------------

`shell.sh`
  - Starts the application in an interactive shell mode, allowing you to
    execute commands manually.

`daemon.sh`
  - Runs the application as a simple background daemon process using `nohup`.
  - Use this for basic background execution.
  - Commands: `start`, `stop`, `restart`, `status`.

`jsvc-daemon.sh`
  - Runs the application as a more robust background daemon using Apache
    Commons Daemon's `jsvc`.
  - This is the recommended way to run the application as a daemon on
    production Unix-like systems.
  - Requires `jsvc` to be installed.
  - Commands: `start`, `stop`, `restart`, `status`.


--------------------------
3. Windows Scripts (.bat)
--------------------------

`shell.bat` / `legacy-shell.bat`
  - Starts the application in an interactive shell mode on Windows.

`daemon.bat`
  - Runs the application in the foreground of the current command prompt window.
  - Closing the window will terminate the application.

`procrun\` (directory)
  - Contains scripts to install, manage, and uninstall the application as a
    native Windows Service.
  - This allows the application to start automatically with Windows and run in
    the background without a visible command prompt window.
  - For detailed instructions, see the `README.txt` file inside the `procrun`
    directory.
