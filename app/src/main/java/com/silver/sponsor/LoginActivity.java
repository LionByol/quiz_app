package com.silver.sponsor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.silver.sponsor.reference.GR;
import com.silver.sponsor.reference.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);

        delayDialog = new ProgressDialog(this);
        delayDialog.setMessage("CARGA...");
        delayDialog.setIndeterminate(true);
        delayDialog.setCanceledOnTouchOutside(false);

        LoginManager.getInstance().logOut();
        LoginButton fblogin = (LoginButton)findViewById(R.id.login_button);
        fblogin.setReadPermissions(Arrays.asList("user_birthday", "email"));
        fblogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback()
                    {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response)
                        {
                            delayDialog.show();
                            try
                            {
                                GR.userunique = object.get("id").toString();
                                GR.userid = object.get("email").toString();
                                GR.username = object.get("name").toString();
                                try
                                {
                                    GR.usergender = object.get("gender").toString();
                                }
                                catch(Exception e){}
                                String birth = "";
                                try
                                {
                                     birth = object.get("birthday").toString();
                                }catch (Exception e){}

                                String tmp[] = birth.split("/");
                                try
                                {
                                    birth = tmp[2] + "/" + tmp[0] + "/" + tmp[1];
                                    GR.useryear = tmp[2];
                                    GR.usermon = tmp[0];
                                    GR.userdate = tmp[1];
                                }catch (Exception e){}

                                HashMap<String, String> paramMap = new HashMap<String, String>();
                                String tmpname = GR.username;
                                paramMap.put("name", tmpname.replace(" ", "_"));
                                paramMap.put("id", GR.userid);
                                paramMap.put("gender", GR.usergender);
                                paramMap.put("birth", birth);
                                RequestParams params = new RequestParams(paramMap);
                                new AsyncHttpClient().get(GR.host + "loginsuccess.php", params, new JsonHttpResponseHandler(){
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        super.onSuccess(statusCode, headers, response);
                                        try
                                        {
                                            GR.score = Integer.parseInt(response.get("score").toString());
                                        }
                                        catch (JSONException e) {
                                            GR.score = 0;
                                        }
                                        try
                                        {
                                            GR.usergender = response.get("gender").toString();
                                        } catch (JSONException e) {
                                            GR.usergender = "_X_";
                                        }
                                        String bir = "";
                                        try
                                        {
                                            bir = response.get("birth").toString();
                                        }
                                        catch (JSONException e) {}
                                        try
                                        {
                                            String tmp[] = bir.split("/");
                                            bir = tmp[0] + "-" + tmp[1] + "-" + tmp[2];
                                            GR.useryear = tmp[0];
                                            GR.usermon = tmp[1];
                                            GR.userdate = tmp[2];
                                        }
                                        catch (Exception e)
                                        {
                                            String tmp[] = bir.split("-");
                                            bir = tmp[0] + "-" + tmp[1] + "-" + tmp[2];
                                            GR.useryear = tmp[0];
                                            GR.usermon = tmp[1];
                                            GR.userdate = tmp[2];
                                        }

                                        if(GR.usergender.equals("_X_") || GR.useryear.equals("_X_") || GR.useryear.equals("0000"))
                                        {
                                            finish();
                                            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                            startActivity(intent);
                                        }
                                        else
                                        {
                                            finish();
                                            Intent intent = new Intent(LoginActivity.this, GameMenu.class);
                                            startActivity(intent);
                                        }
                                    }
                                });

                            } catch (Exception e) {}
                        }
                    });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, email, gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel()
            {
                new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("AVISO")
                    .setMessage("Inicio de sesión cancelada.")
                    .setPositiveButton("Vale", null)
                    .show();
            }

            @Override
            public void onError(FacebookException exception)
            {
                new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("AVISO")
                    .setMessage(exception.getMessage())
                    .setPositiveButton("Vale", null)
                    .show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.aboutgame)
        {
            new AlertDialog.Builder(LoginActivity.this)
                .setTitle("AVISO")
                .setMessage("\n" +
                        "Este software es un juego android nativo.\n" +
                        "Usted será capaz de jugar un juego de preguntas y respuestas. Va 1 punto por cada problema.\n" +
                        "Yuo podrá canjear sus puntos con premios.\n")
                .setPositiveButton("Vale", null)
                .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
    }

    ProgressDialog delayDialog;
    CallbackManager callbackManager;
}
