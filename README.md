# Securing Microservices with JJWT Tutorial

The purpose of this tutorial is to demonstrate how the [JJWT](https://github.com/jwtk/jjwt) library can be used to secure microservices.

The only dependencies are the Spring Boot Web Starter and the JJWT library.

Wondering what JWTs and/or the JJWt library is all about? Click [here](https://java.jsonwebtoken.io).

What follows is a little bit of background on Microservices. Feel free to skip that and jump right into the [tutorial](#building-the-app)

## What Does the App Do?

This application demonstrates many of the critical functions of microservices that need to communicate with each other.

This includes:

* Creation of private/public key pair
* Registration of public key from one service to another service
* Creation of JWTs signed with private key
* Verification of JWTs using public key
* Example of Account Resolution Service using signed JWTs

## Building the App

Easy peasy:

```
mvn clean install
```

## Running the App 

To exercise the communication between microservices, you'll want to run at least two instances of the application.

Building the app creates a fully standalone executable jar. You can run multiple instances like so:

```
target/stormpath-jjwt-microservices-tutorial-0.1.0-SNAPSHOT.jar --server.port=8080
target/stormpath-jjwt-microservices-tutorial-0.1.0-SNAPSHOT.jar --server.port=8081
```

This will run one instance on port `8080` and one on `8081`.

You can also use the purple Heroku button below to deploy to your own Heroku account. Setup two different instances
so you can communicate between them.

Note: all service to service communication below uses [httpie]()

## Service Registry

When the application is launched, a private/public keypair is automatically created. All operations involving keys
are handled via the `SecretService` service and exposed via endpoints in the `SecretServiceController`.

Below are the available endpoints from `SecretServiceController`:

1. `/refresh-my-creds` - Create a new private/public key pair for this microservice instance.
2. `/get-my-public-creds` - Return the Base64 URL Encoded version of this microservice instance's Public Key and its `kid`.
3. `/add-public-creds` - Register the Public Key of one microservice instance on another microservice instance.
4. `/test-build` - Returns a JWS signed with the instance's private key. The JWS includes the instance's `kid` as a header param.
5. `/test-parse` - Takes a JWS as a parameter and attempts to parse it by looking up the public key identified by the `kid`.

Let's look at this in action:

Let's first try to have one microservice communicate with the other *without* establishing trust:

`http localhost:8080/test-build`

    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 04:42:09 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "jwt": "eyJraWQiOiI5NzYzMWI5YS0yZjM0LTRhYzQtOGMxYy1kN2U3MmZkYTExMGYiLCJhbGciOiJSUzI1NiJ9...",
        "status": "SUCCESS"
    }

`http localhost:8081/test-parse?jwt=eyJraWQiOiI5NzYzMWI5YS0yZjM0LTRhYzQtOGMxYy1kN2U3MmZkYTExMGYiLCJhbGciOiJSUzI1NiJ9...`

    HTTP/1.1 400 Bad Request
    Connection: close
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 04:42:32 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "exceptionType": "io.jsonwebtoken.JwtException",
        "message": "No public key registered for kid: 97631b9a-2f34-4ac4-8c1c-d7e72fda110f. JWT claims: {iss=Stormpath, sub=msilverman, name=Micah Silverman, hasMotorcycle=true, iat=1466796822, exp=4622470422}",
        "status": "ERROR"
    }
    
Notice that our second microservice cannot parse the JWT since it doesn't have the public key in its registry.

Now, let's register the first microservice's public key with the second microservice and then try the above operation again:

`http localhost:8080/get-my-public-creds`

    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 04:47:26 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "b64UrlPublicKey": "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo6Lfrn...",
        "kid": "97631b9a-2f34-4ac4-8c1c-d7e72fda110f"
    }
    
```
http POST localhost:8081/add-public-creds \
  b64UrlPublicKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo6Lfrn..." \
  kid="97631b9a-2f34-4ac4-8c1c-d7e72fda110f"
```

    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 04:51:25 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "b64UrlPublicKey": "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo6Lfrn...",
        "kid": "97631b9a-2f34-4ac4-8c1c-d7e72fda110f"
    }
    
Now, we can re-run our `/test-parse` endpoint using the same JWT from before:
    
`http localhost:8081/test-parse?jwt=eyJraWQiOiI5NzYzMWI5YS0yZjM0LTRhYzQtOGMxYy1kN2U3MmZkYTExMGYiLCJhbGciOiJSUzI1NiJ9...`
    
    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 04:52:47 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "jws": {
            "body": {
                "exp": 4622470422,
                "hasMotorcycle": true,
                "iat": 1466796822,
                "iss": "Stormpath",
                "name": "Micah Silverman",
                "sub": "msilverman"
            },
            "header": {
                "alg": "RS256",
                "kid": "97631b9a-2f34-4ac4-8c1c-d7e72fda110f"
            },
            "signature": "phsExAX5CflcLJJQ-q4xYEOq9gbtu7DxzokMq_yPKz2Bx-TQz72EdG25HssNGnkiOCCDVH7iSnaARoiIBPgRKj4W8FstVBR1I3hreIS4MrqMZBaDrS62xwyVnCU1HIMvsqOj6hHBwIowQwlTld887C1hznpTjk74Q1__Vk_wZJU"
        },
        "status": "SUCCESS"
    }
    
This time, our second microservice is able to parse the JWT from the first microservice since we registered the public key with it.

## Account Resolution

In this part of the example, we introduce an `AccountResolver`. This interface exposes an `INSTANCE` that can then be used to lookup an `Account`.
For the purposes of the example, three accounts are setup that represent the "database" of accounts.

The `AccountResolver` implementation expects a JWT that has a `userName` claim that will be used to lookup the account.

The microservice that is doing the account resolution will need to retrieve the bearer token from the request (the JWT) and it will need to be able to parse the JWT to pull out the `userName` claim. 
Like before, the public key of the microservice that created the JWT will need to be registered with the microservice that will be parsing the JWT.
 
The `MicroServiceController` exposes two endpoints to manage these interactions:

1. `/auth-builder` - Generate a JWT with a 60-second expiration. It can take in any number of claims. `userName` claim is required.
2. `/restricted` - Return an `Account` based on processing a bearer token
 
Let's see this in action. Note: this assumes that you've registered the public key from the first microservice with the second microservice.

`http POST localhost:8080/auth-builder username=anna`

    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 05:13:56 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "jwt": "eyJraWQiOiI5NzYzMWI5YS0yZjM0LTRhYzQtOGMxYy1kN2U3MmZkYTExMGYiLCJhbGciOiJSUzI1NiJ9...",
        "status": "SUCCESS"
    }
    
`http localhost:8081/restricted Authorization:"Bearer eyJraWQiOiI5NzYzMWI5YS0yZjM0LTRhYzQtOGMxYy1kN2U3MmZkYTExMGYiLCJhbGciOiJSUzI1NiJ9..."`

    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 18 Jul 2016 05:16:26 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked
    
    {
        "account": {
            "firstName": "Anna",
            "lastName": "Apple",
            "userName": "anna"
        },
        "message": "Found Account",
        "status": "SUCCESS"
    }
    
The above request uses the standard `Authorization` header as part of the request to the second microservice using the JWT from the first microservice.

## Implementation Details

