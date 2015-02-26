package eu.imakers.oauth2password.auth2;

import android.app.Activity;
import android.content.Context;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import java.io.IOException;

/**
 * Created by pion on 26.02.15.
 */
public class OAuth2Password extends OAuth2Helper {

    /**
     * An interface for login callback
     */
    public interface Callback {
        /**
         * When data store is null
         * @param response
         */
        public void onSuccess(final TokenResponse response);

        /**
         * When data store is on
         * @param user
         */
        public void onSuccess(final StoredUser user);

        void onException(Exception exc);
        void onException(TokenResponseException exc);
    }

    /**
     * Representation of user from stored credentials
     */
    public class StoredUser {
        /**
         * Stored credentials by the AuthorizationCodeFlow
         */
        protected StoredCredential mStoredCredential;

        /**
         * User name used for login and as a key for storing the credentials
         */
        protected String mUsername;

        /**
         * Creates the StoredUser
         * @param storedCredential
         * @param username
         */
        StoredUser(StoredCredential storedCredential, String username) {
            mStoredCredential = storedCredential;
            mUsername = username;
        }

        /**
         * A stored credentials by the AuthorizationCodeFlow
         * @return
         */
        public StoredCredential getStoredCredential() {
            return mStoredCredential;
        }

        /**
         * An user name used for the login
         * @return
         */
        public String getUsername() {
            return mUsername;
        }
    }


    /**
     * Setups only auth credentials for client. You need to build the flow by our self by calling
     * buildAuthorizationFlow
     *
     * @param clientId
     * @param clientSecret
     */
    public OAuth2Password(String clientId, String clientSecret) {
        super(clientId, clientSecret);
    }

    /**
     * Creates auth helper with standard flow and default storage
     *
     * @param context           context context to get filesDir
     * @param authTokenUrl
     * @param authAuthorizeUrl
     * @param clientId
     * @param clientSecret
     * @throws IOException
     */
    public OAuth2Password(Context context, String authTokenUrl, String authAuthorizeUrl, String clientId, String clientSecret) throws IOException {
        super(context, authTokenUrl, authAuthorizeUrl, clientId, clientSecret);
    }


    /**
     * Creates auth helper with standard flow  with custom storage file name for credentials
     *
     * @param context           context context to get filesDir
     * @param authTokenUrl
     * @param authAuthorizeUrl
     * @param clientId
     * @param clientSecret
     * @param storeFile         the file name of the data store for credentials
     *
     * @throws IOException
     */
    public OAuth2Password(Context context, String authTokenUrl, String authAuthorizeUrl, String clientId, String clientSecret, String storeFile) throws IOException {
        super(context, authTokenUrl, authAuthorizeUrl, clientId, clientSecret, storeFile);
    }

    /**
     *  Tries to get access token and if data store is not null, saves the credential by the user name
     *
     * @param username
     * @param password
     * @param callback is not triggered on main thread!
     */
    public void login(final Activity context, final String username, final String password, final Callback callback) throws NullPointerException {
        final AuthorizationCodeTokenRequest authorizationCodeTokenRequest = getFlow().newTokenRequest("");

        authorizationCodeTokenRequest.setGrantType("password");
        authorizationCodeTokenRequest.set("password", username);
        authorizationCodeTokenRequest.set("username", password);

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    final TokenResponse tokenResponse = authorizationCodeTokenRequest.execute();

                    // store the method input properties so we dont loose them

                    final Callback subCallback = callback;
                    final String subUsername = username;

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getDataStore() != null) {

                                try {
                                    // store the response
                                    getFlow().createAndStoreCredential(tokenResponse, subUsername);

                                    // return the credentials
                                    subCallback.onSuccess(getStoredUserCredentials(subUsername));
                                } catch (Exception e) {
                                    subCallback.onException(e);
                                    e.printStackTrace();
                                }

                            } else {
                                subCallback.onSuccess(tokenResponse);
                            }
                        }
                    });
                } catch (final TokenResponseException token) {
                    token.printStackTrace();

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onException(token);
                        }
                    });
                }
                catch (final Exception e) {
                    e.printStackTrace();

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onException(e);
                        }
                    });
                }
            }
        });



        thread.start();
    }

    /**
     * Gets the first credentials with access token and returns its
     *
     * @return StoredCredential or null
     *
     * @throws NullPointerException
     * @throws IOException
     */
    public StoredUser tryAutologin() throws NullPointerException, IOException {

        if (!getDataStore().isEmpty()) {
            String username = getDataStore().keySet().iterator().next();

            return getStoredUserCredentials(username);
        }

        return null;
    }

    /**
     * Returns stored credential based on username
     *
     * @return StoredUser or null
     *
     * @throws NullPointerException
     * @throws IOException
     */
    public StoredUser getStoredUserCredentials(String username) throws NullPointerException, IOException {
        if (getDataStore().containsKey(username)) {
            return new StoredUser(getDataStore().get(username), username);
        }

        return null;
    }

    /**
     * Logout the user via the username
     *
     * @param username
     * @throws NullPointerException
     * @throws IOException
     */
    public void logout(String username) throws NullPointerException, IOException{
        getDataStore().delete(username);
    }
}
