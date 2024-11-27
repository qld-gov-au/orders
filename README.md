# Orders documentation

## Local build set up:
Create file in $HOME/.orders.key with contents: "testkey"

## Running locally:

Must have java 21 installed
```bash
mvn install
cd orders-war
mvn spring-boot:run
```

go to http://localhost:8091/test



## Deployment:
Create a file in the home directoy of the user running the application: .orders.key with a complex password.
Change the permissions on this file to be readable only by the application user.

Create a production version of application.properties and log4j.properties based on the test copies.
Any property value in application.properties can be encrypted in the format: ENC("encrypted value"), you should have your database and Payment Gateway credentials encrypted at minimum.
The encryption algorithm being used is: PBEWITHSHA256AND128BITAES-CBC-BC



