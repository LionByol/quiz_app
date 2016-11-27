package com.silver.sponsor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.silver.sponsor.reference.GR;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class SingSponsor extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_sponsor);

        username = (TextView)findViewById(R.id.usernameview);
        username.setText(GR.username);
        scoreview = (TextView)findViewById(R.id.pointsview);
        scoreview.setText("PUNTOS: " + GR.score);

        point = Integer.parseInt(getIntent().getStringExtra("point"));
        sponsor = Integer.parseInt(getIntent().getStringExtra("sponsor"));
        prize = Integer.parseInt(getIntent().getStringExtra("prize"));

        pointview = (TextView)findViewById(R.id.pointview);
        pointview.setText("PUNTOS: "+point);

        prizeview = (ImageView)findViewById(R.id.prizeview);
        new GetImageTask().execute();

        pwdtxt = (EditText)findViewById(R.id.pwdview);

        Button redeembtn = (Button)findViewById(R.id.redeembtn);
        redeembtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pwdtxt.getText().toString().equals(""))
                {
                    Toast.makeText(SingSponsor.this, "Por favor ingrese clave de patrocinador.", Toast.LENGTH_LONG).show();
                    return;
                }
                if(GR.score < point)
                {
                    new AlertDialog.Builder(SingSponsor.this)
                        .setTitle("Warning")
                        .setMessage("Si ganas " + (point - GR.score) + " puntos más. Por favor, inténtelo de nuevo.")
                        .setPositiveButton("Vale", null)
                        .show();
                    return;
                }

                HashMap<String, String> paramMap = new HashMap<String, String>();
                paramMap.put("id", GR.userid);
                paramMap.put("score", GR.score+"");
                paramMap.put("sponsor", sponsor+"");
                paramMap.put("prize", prize+"");
                paramMap.put("point", point+"");
                paramMap.put("password", pwdtxt.getText().toString());
                RequestParams params = new RequestParams(paramMap);
                new AsyncHttpClient().get(GR.host+"redeemprize.php", params, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            String result = response.get("res").toString();
                            if(result.equals("over3"))
                            {
                                new AlertDialog.Builder(SingSponsor.this)
                                    .setTitle("AVISO")
                                    .setMessage("Usted debe intentar mañana porque probado 3 veces hoy.")
                                    .setPositiveButton("Vale", null)
                                    .show();
                            }
                            else if(result.equals("wrongpwd"))              //password
                            {
                                new AlertDialog.Builder(SingSponsor.this)
                                    .setTitle("AVISO")
                                    .setMessage("\n" +
                                            "Clave de patrocinador no es válido. Por favor, inténtelo de nuevo.")
                                    .setPositiveButton("Vale", null)
                                    .show();
                            }
                            else                                            //success
                            {
                                new AlertDialog.Builder(SingSponsor.this)
                                    .setTitle("AVISO")
                                    .setMessage("Felicidades. Premio canjeado.")
                                    .setPositiveButton("Vale", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            onBackPressed();
                                        }
                                    })
                                    .show();
                                GR.score = GR.score-point;
                                scoreview.setText("PUNTOS: " + GR.score);
                            }
                        } catch (JSONException e)
                        {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
    }

    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
        startActivity(new Intent(this, PrizesActivity.class));
    }

    class GetImageTask extends AsyncTask<Void, Void, Bitmap>
    {
        @Override
        protected Bitmap doInBackground(Void... urls)
        {
            Bitmap map = null;
            map = downloadImage(GR.host+"getprizeimage.php?sponsor=" + sponsor + "&prize=" + prize);
            return map;
        }

        // Sets the Bitmap returned by doInBackground
        @Override
        protected void onPostExecute(Bitmap result) {
            prizeview.setImageBitmap(result);
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

    TextView username, scoreview, pointview;
    ImageView prizeview;
    EditText pwdtxt;
    int point, sponsor, prize;
}
