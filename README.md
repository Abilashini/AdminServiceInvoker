# AdminServiceInvoker

## Purpose
This tool is used to invoke the 'changePassword' endpoint of admin service in WSO2 IS periodically and record any unusual activities.

## Steps to get it work

1. Checkout the source and build using the command,

`mvn clean install`

2. Copy the target/ChangePasswordInvoker-jar-with-dependencies.jar and folder conf/ into a same location.

3. Configure the config.properties file located inside /conf with relevant values and save it. 
    - BackendURL : IS backend URL. Eg- https://localhost:9443
    - AdminUserName : username of the admin user.
    - AdminPassword : password of the admin user.
    - UserName : user to be changed the password.
    - NewPassword : new password for the above user.
    - TimeInterval : interavel in seconds between 2 invokes.
    
4. Execute the jar file in the terminal using the command,
		
    `java -jar ChangePasswordInvoker-jar-with-dependencies.jar`

A file named response.log will be created in the same location and if there is any unusual response found by the tool, those response will be recorded with the time of execution and total exection duration.
