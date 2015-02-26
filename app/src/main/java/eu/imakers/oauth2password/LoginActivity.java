package eu.imakers.oauth2password;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;

import eu.imakers.oauth2password.auth2.OAuth2Password;


public class LoginActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            final OAuth2Password oAuth2Password = new OAuth2Password(getApplicationContext(),
                    "api_oauth_token",
                    "api_oauth_authorize",
                    "clientId",
                    "clientSecret");

            // try autologin the last user

            try {
                final OAuth2Password.StoredUser storedUser = oAuth2Password.tryAutologin();

                if (storedUser != null) {
                    Toast.makeText(getBaseContext(), "Loged "+storedUser.getStoredCredential().getAccessToken(), Toast.LENGTH_LONG).show();
                }

                // preapre the inputs and the action

                final EditText usernameEdit = (EditText)findViewById(R.id.usernameEdit);
                final EditText passwordEdit = (EditText)findViewById(R.id.passwordEdit);

                final Activity that = this;

                findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (storedUser != null) {
                            try {
                                oAuth2Password.logout(storedUser.getUsername());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        oAuth2Password.login(that, usernameEdit.getText().toString(), passwordEdit.getText().toString(), new OAuth2Password.Callback() {
                            @Override
                            public void onSuccess(OAuth2Password.StoredUser response) {
                                Toast.makeText(getBaseContext(), "Loged "+response.getStoredCredential().getAccessToken(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onSuccess(TokenResponse response) {
                                // not used in this sample, when the credentials storing is not set.

                                Toast.makeText(getBaseContext(), "Loged "+response.getAccessToken(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onException(Exception exc) {
                                // general error
                                Toast.makeText(getApplicationContext(), "Error login "+exc.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onException(TokenResponseException exc) {
                                // getContent returns string with content of the error page (could be json)
                                Toast.makeText(getApplicationContext(), "Error login "+exc.getContent(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error "+ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }



        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error "+ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
