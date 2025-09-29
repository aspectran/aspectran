================================================================================
Running as a Windows Service with Apache Procrun
================================================================================

This document provides instructions on how to install, manage, and remove
Aspectran as a native Windows Service using the included Apache Procrun toolset.


------------
1. Overview
------------

The scripts in this directory (`install.bat`, `uninstall.bat`) allow you to
run Aspectran as a background Windows Service. This means it can start
automatically when the server boots and be managed using standard Windows tools.

The core of this functionality is Apache Procrun (`prunsrv.exe`), which acts as
a service wrapper for the Java application.


-----------------
2. Configuration
-----------------

Before installing the service, you can customize its properties by editing the
`procrun.options` file located in this directory.

Key configuration options include:

- `SERVICE_NAME`: The internal name of the service (e.g., "AspectranService").
- `DISPLAY_NAME`: The name shown in the Windows Services list.
- `DESCRIPTION`: A short description of the service.
- `JVM_MS`: Initial Java heap size in MB (e.g., 256).
- `JVM_MX`: Maximum Java heap size in MB (e.g., 1024).

If you plan to run multiple instances of Aspectran as services on the same
server, you must give each one a unique `SERVICE_NAME`.


-----------------------------------------
3. Running with Administrator Privileges
-----------------------------------------

To install or uninstall services, you must run the scripts from a Command Prompt
with Administrator privileges.

To open an Administrator Command Prompt:
1. Click the Start button.
2. Type `cmd`.
3. Right-click on "Command Prompt" in the search results.
4. Select "Run as administrator".
5. Navigate to this directory (`...\app\bin\procrun`).


--------------------------
4. Installing the Service
--------------------------

To install the service, run the `install.bat` script from an Administrator
Command Prompt.

> install.bat [ServiceName]

- `[ServiceName]` (Optional): The name for the service. If not provided, it
  defaults to the value in `procrun.options` or "AspectranService". This name
  must be unique.

Example:
> install.bat MyAspectranInstance


----------------------------
5. Uninstalling the Service
----------------------------

To remove the service, run the `uninstall.bat` script from an Administrator
Command Prompt.

> uninstall.bat [ServiceName]

- `[ServiceName]` (Optional): The name of the service to remove. It should
  match the name used during installation.

Example:
> uninstall.bat MyAspectranInstance


------------------------
6. Managing the Service
------------------------

Once installed, you can start, stop, and manage the service in several ways:

- **Windows Services App:**
  Open the Services application (`services.msc`) and find the service by its
  Display Name.

- **Service Manager GUI:**
  After installation, a GUI tool named `[ServiceName].exe` (e.g.,
  `AspectranService.exe`) is created in this directory. Run it to monitor
  and manage the service.

- **Command Line:**
  Use the `net` command from any Command Prompt:
  > net start [ServiceName]
  > net stop [ServiceName]
