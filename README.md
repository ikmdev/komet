# Komet-java

## Komet Installation Guide - Mac

### Installation Steps

1. Use the Maven Profile *create-installer* to generate the installer in application/target/dist/installer_with_data.
2. Right-click “Komet-1.0.0.pkg” and select "Open", then click the "Open" button on the security popup.
3. Run through the installation steps. You can say yes to putting the installer in the trash.
4. After the installation is complete, verify “Komet.app” is in your Applications folder.
5. If you downloaded Komet with data, perform the following steps:
    - Verify that the "Solor" directory is in your home directory (ex. Users/USER_HOME/Solor/…)
    - Verify there is data inside the sample_data folder in the "Solor" directory.
8. Go to your Applications folder, double click “Komet.app”.
9. Verify “Komet.app” opens properly.
10. Change data source to “Open SpinedArrayStore”.
11. Verify that the “sample_data” option is shown.
12. Double click “sample_data”.
13. Verify that the Komet app opens to the main Komet Window.

### Uninstall Steps

1. To uninstall, go to the application folder where “Komet.app” is installed.
2. Either drag and drop the app to the trash folder or right click “Komet.app” select "Move to Trash".
3. Next, go to the Users home directory i.e. (Users/USER_HOME/….)
4. Find the "Solor" folder in Users or Home.
5. Right click and select “Move to Trash”.

## Komet Installation Guide - Windows

### Installation Steps

1. Use the Maven Profile *create-installer* to generate the installer in application/target/dist/installer_with_data.
2. Double click and install “Komet-1.0.0.msi”
3. Select “Run” when prompted for installation
4. Run through the installation steps
5. After the installation is complete, go the search bar at the bottom of your screen and search for “Komet”. Verify the
   Komet app exists
6. Verify that the "Solor" directory is in your home directory ex. C:/Users/USER_HOME/Solor/sample_data/…
7. Verify there is data inside the sample_data folder in the Solor directory
    - NOTE: When closing the app, do not select the “Red X” at the top right-hand side, or you will get a repeating
      error. Go to your toolbar, open task manager, select “Komet”, and select “End Task”.
8. Go the search bar at the bottom of your screen and search for “Komet”, click to open.
9. Verify “Komet” opens up.
10. Change data source to “Open SpinedArrayStore”
11. Verify that the “sample_data” option is shown
12. Double click “sample_data”
13. Verify that the Komet app opens to the main Komet Window.

### Uninstall Steps

1. To uninstall, go to the search bar at the bottom of the screen and search “Komet”.
2. Select “Uninstall”.
3. You should be brought to the programs and features page, find “Komet”
4. Right click then select “Uninstall”
5. Select “Yes” on the next window.
6. Next, navigate to your home directory and find the "Solor" folder
7. Right-click and select, "Delete"

The line added for testing webhook
