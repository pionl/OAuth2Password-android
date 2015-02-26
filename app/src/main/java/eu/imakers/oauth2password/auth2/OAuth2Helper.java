package eu.imakers.oauth2password.auth2;

import android.content.Context;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by pion on 26.02.15.
 */
public class OAuth2Helper {
    static String DEFAULT_STORAGE_NAME = "credentials";

    // internal properties

    final private JacksonFactory jsonFactory = new JacksonFactory();

    final private HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();

    private ClientParametersAuthentication clientParametersAuthentication;
    private DataStore<StoredCredential> dataStore;
    private AuthorizationCodeFlow flow;

    /**
     * Setups only auth credentials for client. You need to build the flow by our self by calling
     * buildAuthorizationFlow
     *
     * @param clientId
     * @param clientSecret
     */
    public OAuth2Helper(String clientId, String clientSecret) {
        // first prepare the default store

        prepareClientParametersAuthentication(clientId, clientSecret);
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
    public OAuth2Helper(Context context, String authTokenUrl, String authAuthorizeUrl, String clientId, String clientSecret) throws IOException {
        this(context, authTokenUrl, authAuthorizeUrl, clientId, clientSecret, DEFAULT_STORAGE_NAME);
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
    public OAuth2Helper(Context context, String authTokenUrl, String authAuthorizeUrl, String clientId, String clientSecret, String storeFile) throws IOException {
        this(clientId, clientSecret );

        buildAuthorizationFlow(prepareAuthorizationFlow(authTokenUrl, authAuthorizeUrl), context, storeFile);
    }

    /**
     * Builds given AuthorizationCodeFlow builder without storage init. If dataStore is not null
     * the storage is added.
     *
     * @param builder
     */
    public void buildAuthorizationFlow(AuthorizationCodeFlow.Builder builder) {

        if (dataStore != null) {
            builder.setCredentialDataStore(dataStore);
        }

        flow = builder.build();
    }

    /**
     * Builds fiven AuthorizationCodeFlow builder with default storage file
     *
     * @param builder
     * @param context       context to get filesDir
     */
    public void buildAuthorizationFlow(AuthorizationCodeFlow.Builder builder, Context context) throws IOException {
        buildAuthorizationFlow(builder, context, DEFAULT_STORAGE_NAME);
    }

    /**
     * Builds given builder and with data storage
     *
     * @param builder
     * @param credentialStoreFile   the file name of the data store for credentials
     */
    public void buildAuthorizationFlow(AuthorizationCodeFlow.Builder builder, Context context, String credentialStoreFile) throws IOException {
        dataStore = prepareDefaultStore(context, credentialStoreFile);

        buildAuthorizationFlow(builder);
    }

    /**
     * Prepares client parameters auth.
     *
     * @param clientId
     * @param clientSecret
     */
    protected void prepareClientParametersAuthentication(String clientId, String clientSecret) {
        clientParametersAuthentication = new ClientParametersAuthentication(clientId, clientSecret);
    }

    /**
     * Prepares basic settings. ClientParametersAuthentication must be set.
     *
     * @param authTokenUrl
     * @param authAuthorizeUrl
     *
     * @uses httpTransport, jsonFactory, clientParametersAuthentication, dataStore (can be null)
     * @return
     *
     * @throws NullPointerException when clientParametersAuthentication is null
     */
    protected AuthorizationCodeFlow.Builder prepareAuthorizationFlow(String authTokenUrl, String authAuthorizeUrl) throws NullPointerException{

        if (clientParametersAuthentication == null) {
            throw new NullPointerException();
        }

        AuthorizationCodeFlow.Builder builder = new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(), httpTransport, jsonFactory,
                new GenericUrl(authTokenUrl),clientParametersAuthentication, clientParametersAuthentication.getClientId(),
                authAuthorizeUrl);

        return builder;
    }

    /**
     * Reeturns credentials data store. You can overide this method to use own DataStore
     *
     * @param context       context to get filesDir
     * @param storeFile     the file name of the data store for credentials
     *
     * @return new data store
     *
     * @throws IOException
     */
    protected DataStore<StoredCredential> prepareDefaultStore(Context context, String storeFile)  throws IOException {
        return StoredCredential.getDefaultDataStore(
                    new FileDataStoreFactory(new File(context.getFilesDir(), storeFile)));
    }

    /**
     * An autorization flow
     *
     * @return
     */
    public AuthorizationCodeFlow getFlow() {
        return flow;
    }

    /**
     * ClientId and ClientSecret
     * @return
     */
    public ClientParametersAuthentication getClientParametersAuthentication() {
        return clientParametersAuthentication;
    }

    /**
     * Data store for the credentials
     * @return
     */
    protected DataStore<StoredCredential> getDataStore() {
        return dataStore;
    }

    /**
     * The json factory for some internal use
     * @return
     */
    public JacksonFactory getJsonFactory() {
        return jsonFactory;
    }
}
