# bmatch.gateway Microservice  
The `bmatch.gateway` microservice serves as a gateway for various applications, ensuring routing, as well as user registration, authentication, and profile management.  

## Core Features:  
* Gateway: Routes requests to the appropriate microservices.  
* Registration & Authentication: Handles user account creation and authentication.  
* Profile Management: Fetches and stores user profiles in Redis.  

## API Endpoints:  
* `/api/user/login` - User login.  
* `/api/user/create` - Register a new user.  
* `/api/user/profile` - Fetch a user's profile.  
* `/api/user/**` - Routes user-related requests to the bmatch-user microservice.  

## Security Configurations:  
* Routes prefixed with `/api/admin/**` require ADMINISTRATOR role.  
* Paths `/api/user/create`, `/api/user/login`, and /api/user/profile are publicly accessible.  
* Access to `/api/user/**` and `/api/restaurant/**` requires PLAYER authority.  
* `/actuator/health` is publicly accessible for service health checks.  

## Environment Variables:  
The microservice configuration uses the following environment variables:  

* ## Database Configurations:
    * `DB_USERNAME`: Database username. Default - `bmatch`.  
    * `DB_PASSWORD`: Database password. Default - `newpassword`.  
    * `DB_SCHEME`: Database name. Default - `bmatch`.  
    * `DB_HOST`: Database host. Default - `localhost`.  

* ## Redis Configurations:
    * `REDIS_PORT`: Redis port. Default - `6379`.  
    * `REDIS_HOST`: Redis host. Default - `localhost`.  

* ## JWT & Production Settings:  

    * `IS_PROD`: Indicates if the environment is production. Default - false.
    * `JWT_TOKEN`: Secret key for JWT token. Default - `YourSecretKeyShouldBeVerySecureAndNotPublic`.

* ## SSL Configuration:
    * `SSL_KEYSTORE`: Keystore password. Default - `mypassword`.  

* ## Consul Configuration:
    * `SPRING_CLOUD_CONSUL_HOST`: Consul host. Default - `localhost`.  

## Generating SSL Certificate:
To generate a self-signed SSL certificate for development or testing:  

1. Generate a keystore:
```shell
keytool -genkeypair -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 365 -alias bmatch.gateway -dname "CN=localhost, OU=MyOrg, O=MyOrg, L=City, ST=State, C=Country" -storepass mypassword -keypass mypassword
```
2. For API testing tools like Insomnia that require a PEM format:  
    a. Export the certificate from keystore in DER format:
    ```shell
    keytool -export -keystore keystore.jks -alias bmatch.gateway -file mycert.der -storepass mypassword
    ```
   b. Convert DER format to PEM:
    ```shell
    openssl x509 -inform der -in mycert.der -out mycert.pem
    ```

## Setup & Launch:
1. Clone the repository.  
2. Ensure all required dependencies are installed and appropriate environment variables are set.  
3. Launch the microservices (Redis, Consul, Postgresql) using docker
4. ```shell
    gradle clean build -x test --no-daemon
   ```