package com.silver.sponsor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.silver.sponsor.reference.GR;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

public class GameMenu extends AppCompatActivity {

    private CallbackManager callbackManager;
    LoginButton fblogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);

        callbackManager = CallbackManager.Factory.create();

        fblogin = (LoginButton)findViewById(R.id.login_button);
        fblogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }

        });
        fblogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogout();
            }
        });

        photoview = (ImageView)findViewById(R.id.photoview);
        Bundle params = new Bundle();
        params.putString("fields", "id,email,gender,cover,picture.type(large)");
        new GraphRequest(AccessToken.getCurrentAccessToken(), "me", params, HttpMethod.GET,
            new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    if (response != null) {
                        try {
                            JSONObject data = response.getJSONObject();
                            if (data.has("picture")) {
                                String profilePicUrl = data.getJSONObject("picture").getJSONObject("data").getString("url");
                                GetImageTask task = new GetImageTask();
                                task.execute(new String[] { profilePicUrl });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).executeAsync();

        usernameview = (TextView)findViewById(R.id.usernameview);
        usernameview.setText(GR.username);

        scoreview = (TextView)findViewById(R.id.pointsview);
        scoreview.setText("PUNTOS: "+GR.score);

        playbtn = (Button)findViewById(R.id.playbtn);
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(GameMenu.this, GameActivity.class);
                startActivity(intent);
            }
        });

        prizebtn = (Button)findViewById(R.id.prizebtn);
        prizebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            finish();
            Intent intent = new Intent(GameMenu.this, PrizesActivity.class);
            startActivity(intent);
            }
        });

        historybtn = (Button)findViewById(R.id.historybtn);
        historybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(GameMenu.this, HistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    private class GetImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap map = null;
            for (String url : urls) {
                map = downloadImage(url);
            }
            return map;
        }

        // Sets the Bitmap returned by doInBackground
        @Override
        protected void onPostExecute(Bitmap result) {
            photoview.setImageBitmap(result);
        }

        // Creates Bitmap from InputStream and returns it
        private Bitmap downloadImage(String url) {
            Bitmap bitmap = null;
            InputStream stream = null;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;

            try {
                stream = getHttpConnection(url);
                bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
                stream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bitmap;
        }

        // Makes HttpURLConnection and returns InputStream
        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
        }
    }

    private void onLogout()
    {
        GR.userid = "_X_";
        GR.username = "_X_";
        GR.useryear = "_X_";
        GR.usermon = "_X_";
        GR.userdate = "_X_";
        this.finish();
        Intent intent = new Intent(GameMenu.this, LoginActivity.class);
        startActivity(intent);
        LoginManager.getInstance().logOut();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        onLogout();
    }

    TextView usernameview;
    TextView scoreview;
    Button playbtn;
    Button prizebtn;
    Button historybtn;
    ImageView photoview;

}
