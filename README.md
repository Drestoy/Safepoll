# Safepoll
Android application which allows the user to create groups and, to start polls inside them and vote. This app can use a certificate (previously chained to the user account) to sign user’s votes. 

Server side code is made using PHP and SQL commands to create communication between the app and the MySQL database server.

If you wish to test it, you must write your server connection data in two files:

* In UserFunctions.java in \App files\src\uma\finalproject\database folder
* In config.php in \Server files\Oauth2Serv\include folder
