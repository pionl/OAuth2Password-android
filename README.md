# OAuth2Password-android
Easy integration of OAuth2 login with password grant. Using google-oauth-client and support saving credentials.

# Features
- Saving credentials (can be turned off be different constructor)
- Support multiple instances
- Autologin feature (currently doesn't check if token expired, only if storing credentials is used. )
- Subclass posibility
- Async login request with custom callback

## 1. Gradle dependencies

    compile 'com.google.http-client:google-http-client:1.19.0'
    compile 'com.google.http-client:google-http-client-jackson2:1.19.0'
    compile 'com.google.http-client:google-http-client-android:1.19.0'
    compile 'com.google.http-client:google-http-client-gson:1.19.0'

    compile('com.google.oauth-client:google-oauth-client:1.19.0') {
        exclude module: 'xpp3'
        exclude module: 'httpclient'
        exclude module: 'junit'
        exclude module: 'android'
    }
    
## 2. Setup

First you need to create OAuth2Password instance with url and client id and secret. The credentials file is created with default file name. For every provider you should add a custom file name.

### 2.1 Default storage for credentials

    final OAuth2Password oAuth2Password = new OAuth2Password(getApplicationContext(),
                    "api_oauth_token",
                    "api_oauth_authorize",
                    "clientId",
                    "clientSecret");
                    

### 2.2 Custom storage for credentials

    final OAuth2Password oAuth2Password = new OAuth2Password(getApplicationContext(),
                    "api_oauth_token",
                    "api_oauth_authorize",
                    "clientId",
                    "clientSecret", 
                    "filename");
                    
### 2.3 Init only credentials and build the ACF by your self for custom setters.
With this you can determine when to build the AuthorizationCodeFlow

    final OAuth2Password auth = OAuth2Password("clientId", "clientSecret);
    AuthorizationCodeFlow.Builder builder = auth.prepareAuthorizationFlow("token", "authorize");
    // custom
    auth.buildAuthorizationFlow(builder);
    // standart use
    