Run as a Windows Service
------------------------

Run Aspectran as a Windows Service, which allows you to launch and shutdown
Aspectran using the standard Windows Services administration tool
(typically located in Control Panel > Administrative Tools > Services).

Apache Procrun is used to wrap Aspectran as the Windows Service. It is included
in the same directory as this file.


Commands
--------

Install Service

This command to install Aspectran as a Windows service and must be executed
as Administrator (see instructions below for how to do this):

> install.bat [ServiceName]

ServiceName - [Optional] Specify the name of the Windows Service as stored in
the Windows registry. Defaults to "AspectranService" if not specified. If you
want to have multiple Aspectran services running simultaneously, you will need
to give them different service names.

The Aspectran installation in which the command is run will be the installation
which is installed. Therefore, be sure to set your current directory properly
before executing the command.

Remove Service

This command removes the specified Windows Service from the registry and must
be executed as Administrator (see instructions below for how to do this):

> uninstall.bat [ServiceName]

ServiceName - [Optional] The name of the Windows Service to be removed.
Defaults to "AspectranService" if not specified. Should be the same as the name
specified when you executed "install".