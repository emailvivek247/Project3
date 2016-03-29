# ROAM/eAccept Dev Env Setup

This document will cover how to set up a development environment for three projects: ecom, ecomadmin and sdlecom (ROAM).

Note: For setting up Eclipse, you can either setup the projects in their own separate workspaces or have them share single workspace (there are sections in the document for both approaches). There are advantages and disadvantages to both.

## Install Dependencies (OS X)

The inline code excerpts below should be run from the terminal.

#### Install Homebrew (OS X)

1. Install Homebrew by following the directions at http://brew.sh/
2. Tap the Homebrew extension cask: `brew tap caskroom/cask`

#### Install Git (OS X)

1. Install Git using Homebrew: `brew install git`
2. Install git-flow plugins using Homebrew: `brew install git-flow-avh`

#### Install JDK 8 (OS X)

1. Install JDK 8: `brew cask install java`
2. Modify your terminal profile to set the JAVA_HOME environment variable:
  - Example: JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home

#### Install Apache Tomcat (OS X)

1. Download Apache Tomcat 7 from http://tomcat.apache.org/download-70.cgi
2. Extract the Tomcat archive file in your preferred directory
3. Don’t worry about starting or executing Tomcat, the Tomcat installation is required for setting up servers in Eclipse

#### Install Apache Active MQ (OS X)

1. Install activemq: brew install activemq
2. ActiveMQ can now by started by running: activemq start

#### Install Eclipse (OS X)

1. Download Eclipse Java EE from https://eclipse.org/downloads/
2. Extract Eclipse into preferred directory

## Install Dependencies (Windows)

#### Install Git (Windows)

1. Install Git For Windows: https://git-for-windows.github.io/. Be sure to include the Git BASH interface, which provides a BASH-like terminal environment.
2. Verify git-flow is included: `git flow -h`

#### Install JDK 8 (Windows)

1. Download JDK 8 from http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
2. Set the JAVA_HOME environmental variable
  - Example: C:\Program Files\Java\jdk1.8.0_45

#### Install Apache Tomcat (Windows)

1. Download Apache Tomcat 7 from http://tomcat.apache.org/download-70.cgi
2. Extract the Tomcat archive file in your preferred directory
3. Don’t worry about starting or executing Tomcat, the Tomcat installation is required for setting up servers in Eclipse

#### Install Apache Active MQ (Windows)

1. Download activemq 5.8.0 from http://activemq.apache.org/activemq-580-release.html
2. Extract zipped file into preferred directory
3. Open Command Prompt
4. Navigate to bin folder, where InstallService.bat is located
5. Run InstallService.bat
6. If installation is successful, the Active MQ service should show up in Services list

#### Install Eclipse (Windows)

1. Download Eclipse Java EE from https://eclipse.org/downloads/
2. Extract Eclipse into preferred directory

## Clone The Repositories

1. If desired, and you haven't already done so, set up an SSH key for Github by following the instructions here: https://help.github.com/articles/generating-an-ssh-key/
2. Clone the repositories:
  - SSH
    - `git clone git@github.com:Granicus/eAccept-ROAM.git`
    - `git clone git@github.com:Granicus/eAccept-eCom.git`
    - `git clone git@github.com:Granicus/eAccept-eComAdmin.git`
    - `git clone git@github.com:Granicus/eAccept-Templates-SDLECOM.git`
    - `git clone git@github.com:Granicus/eAccept-Templates-SDL.git`
  - HTTPS
    - `git clone https:/github.com/Granicus/eAccept-ROAM.git`
    - `git clone https:/github.com/Granicus/eAccept-eCom.git`
    - `git clone https:/github.com/Granicus/eAccept-eComAdmin.git`
    - `git clone https:/github.com/Granicus/eAccept-Templates-SDLECOM.git`
    - `git clone https:/github.com/Granicus/eAccept-Templates-SDL.git`

## Setting up Eclipse (Multiple Workspaces)

#### Add sdlecom project to Eclipse

1. Launch Eclipse
2. Enter the path for a new workspace folder named "workspace-sdlecom"
3. Select File -> Import...
4. Select General -> Existing Projects into Workspace
5. Click Browse... and navigate to the projects folder from above and click OK
6. Three projects shoulder appear in the list
  - ecom
  - ecomadmin
  - sdlecom
7. Select sdlecom
8. Click Finish to import the project

#### Setup Tomcat Server in Eclipse for sdlecom

1. Go to the Servers tab (usually located at the bottom of the window)
2. Right click within the tab and select New -> Server
3. Under the Apache section, find Tomcat 7
4. Enter "sdlecom - localhost" as the server name
5. Click Next
6. For Tomcat installation directory, select Browse and find the folder where Tomcat was extracted and hit Next
7. From the Available list, select "sdlecom" and Add to the Configured list
8. Click Finish
9. The newly installed Tomcat should now show up under the Servers tab
10. Open server configuration (double click on the server name)
  1. Set the following configuration parameters:
    1. Timeouts:
      - Start: 450
      - Stop: 150
    2. Ports:
      - Admin port: 4003
      - HTTP/1.1: 4001
      - AJP/1.3: 4002
  2. Save your changes
  3. Set up the Memory Settings
    1. Click on Open Launch Configuration under the General Info tab
      1. Go to the Arguments tab
      2. Inside of the VM Arguments section, add the memory settings
        - Example: -Xms256m -Xmx2048m
  4. Set up the Environmental Variables
    1. Click on Open Launch Configuration under the General Info tab
      1. Go to the Environment tab
      2. Click New... to create each environment variable
        - CONFIG_LOCATION: /WEB-INF/conf
        - HIBERNATE_KEY: dookudu
        - PROPERTY_FILE_KEY: dookudu
        - USER_AUTH_KEY: dookudu

#### Add ecom project to Eclipse

1. Launch Eclipse
2. Enter the path for a new workspace folder named "workspace-ecom"
3. Select File -> Import...
4. Select General -> Existing Projects into Workspace
5. Click Browse... and navigate to the projects folder from above and click OK
6. Three projects shoulder appear in the list
  - ecom
  - ecomadmin
  - sdlecom
7. Select ecom
8. Click Finish to import the project

#### Setup Tomcat Server in Eclipse for ecom

1. Go to the Servers tab (usually located at the bottom of the window)
2. Right click within the tab and select New -> Server
3. Under the Apache section, find Tomcat 7
4. Enter "ecom - localhost" as the server name
5. Click Next
6. For Tomcat installation directory, select Browse and find the folder where Tomcat was extracted and hit Next
7. From the Available list, select "ecom" and Add to the Configured list
8. Click Finish
9. The newly installed Tomcat should now show up under the Servers tab
10. Open server configuration (double click on the server name)
  1. Set the following configuration parameters:
    1. Timeouts:
      - Start: 450
      - Stop: 150
    2. Ports:
      - Admin port: 2003
      - HTTP/1.1: 2001
      - AJP/1.3: 2002
  2. Save your changes
  3. Set up the Memory Settings
    1. Click on Open Launch Configuration under the General Info tab
      1. Go to the Arguments tab
      2. Inside of the VM Arguments section, add the memory settings
        - Example: -Xms256m -Xmx2048m
  4. Set up the Environmental Variables
    1. Click on Open Launch Configuration under the General Info tab
      1. Go to the Environment tab
      2. Click New... to create each environment variable
        - CONFIG_LOCATION: /WEB-INF/conf
        - HIBERNATE_KEY: dookudu
        - PROPERTY_FILE_KEY: dookudu
        - USER_AUTH_KEY: dookudu

#### Add ecomadmin project to Eclipse

1. Launch Eclipse
2. Enter the path for a new workspace folder named "workspace-ecomadmin"
3. Select File -> Import...
4. Select General -> Existing Projects into Workspace
5. Click Browse... and navigate to the projects folder from above and click OK
6. Three projects shoulder appear in the list
  - ecom
  - ecomadmin
  - sdlecom
7. Select ecomadmin
8. Click Finish to import the project

#### Setup Tomcat Server in Eclipse for ecomadmin

1. Go to the Servers tab (usually located at the bottom of the window)
2. Right click within the tab and select New -> Server
3. Under the Apache section, find Tomcat 7
4. Enter "ecomadmin - localhost" as the server name
5. Click Next
6. For Tomcat installation directory, select Browse and find the folder where Tomcat was extracted and hit Next
7. From the Available list, select "ecomadmin" and Add to the Configured list
8. Click Finish
9. The newly installed Tomcat should now show up under the Servers tab
10. Open server configuration (double click on the server name)
  1. Set the following configuration parameters:
    1. Timeouts:
      - Start: 450
      - Stop: 150
    2. Ports:
      - Admin port: 3003
      - HTTP/1.1: 3001
      - AJP/1.3: 3002
  2. Save your changes
  3. Set up the Memory Settings
    1. Click on Open Launch Configuration under the General Info tab
      1. Go to the Arguments tab
      2. Inside of the VM Arguments section, add the memory settings
        - Example: -Xms256m -Xmx2048m
  4. Set up the Environmental Variables
    1. Click on Open Launch Configuration under the General Info tab
      1. Go to the Environment tab
      2. Click New... to create each environment variable
        - CONFIG_LOCATION: /WEB-INF/conf
        - HIBERNATE_KEY: dookudu
        - PROPERTY_FILE_KEY: dookudu
        - USER_AUTH_KEY: dookudu

## Setting up Eclipse (Single Workspace)

#### Add Projects to Eclipse

1. Launch Eclipse
2. Create a new workspace
3. Select File -> Import...
4. Select General -> Existing Projects into Workspace
5. Click Browse... and navigate to the projects folder from above and click OK
6. Three projects shoulder appear in the list
  - ecom
  - ecomadmin
  - sdlecom
7. Import all three projects

#### Setup Tomcat Server in Eclipse for sdlecom

1. Go to the Servers tab (usually located at the bottom of the window)
2. Right click within the tab and select New -> Server
3. Under the Apache section, find Tomcat 7
4. Enter "sdlecom - localhost" as the server name
5. Click Next
6. For Tomcat installation directory, select Browse and find the folder where Tomcat was extracted and hit Next
7. From the Available list, select "sdlecom" and Add to the Configured list
8. Click Finish
9. The newly installed Tomcat should now show up under the Servers tab
10. Open server configuration (double click on the server name)
  1. Set the following configuration parameters:
    1. Timeouts:
      - Start: 450
      - Stop: 150
    2. Ports:
      - Admin port: 4003
      - HTTP/1.1: 4001
      - AJP/1.3: 4002
  2. Save your changes
  3. Set up the Memory Settings
    1. Click on Open Launch Configuration under the General Info tab
      1. Go to the Arguments tab
      2. Inside of the VM Arguments section, add the memory settings
        - Example: -Xms256m -Xmx2048m
  4. Set up the Environmental Variables
    1. Click on Open Launch Configuration under the General Info tab
      1. Go to the Environment tab
      2. Click New... to create each environment variable
        - CONFIG_LOCATION: /WEB-INF/conf
        - HIBERNATE_KEY: dookudu
        - PROPERTY_FILE_KEY: dookudu
        - USER_AUTH_KEY: dookudu

#### Setup Tomcat Server in Eclipse for ecom

1. Go to the Servers tab (usually located at the bottom of the window)
2. Right click within the tab and select New -> Server
3. Under the Apache section, find Tomcat 7
4. Enter "ecom - localhost" as the server name
5. Click Next
6. For Tomcat installation directory, select Browse and find the folder where Tomcat was extracted and hit Next
7. From the Available list, select "ecom" and Add to the Configured list
8. Click Finish
9. The newly installed Tomcat should now show up under the Servers tab
10. Open server configuration (double click on the server name)
  1. Set the following configuration parameters:
    1. Timeouts:
      - Start: 450
      - Stop: 150
    2. Ports:
      - Admin port: 2003
      - HTTP/1.1: 2001
      - AJP/1.3: 2002
  2. Save your changes
  3. Set up the Memory Settings
    1. Click on Open Launch Configuration under the General Info tab
      1. Go to the Arguments tab
      2. Inside of the VM Arguments section, add the memory settings
        - Example: -Xms256m -Xmx2048m
  4. Set up the Environmental Variables
    1. Click on Open Launch Configuration under the General Info tab
      1. Go to the Environment tab
      2. Click New... to create each environment variable
        - CONFIG_LOCATION: /WEB-INF/conf
        - HIBERNATE_KEY: dookudu
        - PROPERTY_FILE_KEY: dookudu
        - USER_AUTH_KEY: dookudu

#### Setup Tomcat Server in Eclipse for ecomadmin

1. Go to the Servers tab (usually located at the bottom of the window)
2. Right click within the tab and select New -> Server
3. Under the Apache section, find Tomcat 7
4. Enter "ecomadmin - localhost" as the server name
5. Click Next
6. For Tomcat installation directory, select Browse and find the folder where Tomcat was extracted and hit Next
7. From the Available list, select "ecomadmin" and Add to the Configured list
8. Click Finish
9. The newly installed Tomcat should now show up under the Servers tab
10. Open server configuration (double click on the server name)
  1. Set the following configuration parameters:
    1. Timeouts:
      - Start: 450
      - Stop: 150
    2. Ports:
      - Admin port: 3003
      - HTTP/1.1: 3001
      - AJP/1.3: 3002
  2. Save your changes
  3. Set up the Memory Settings
    1. Click on Open Launch Configuration under the General Info tab
      1. Go to the Arguments tab
      2. Inside of the VM Arguments section, add the memory settings
        - Example: -Xms256m -Xmx2048m
  4. Set up the Environmental Variables
    1. Click on Open Launch Configuration under the General Info tab
      1. Go to the Environment tab
      2. Click New... to create each environment variable
        - CONFIG_LOCATION: /WEB-INF/conf
        - HIBERNATE_KEY: dookudu
        - PROPERTY_FILE_KEY: dookudu
        - USER_AUTH_KEY: dookudu

## Install Elasticsearch

For running the Elasticsearch version of ROAM, following the elasticsearch-install.md guide in this directory.