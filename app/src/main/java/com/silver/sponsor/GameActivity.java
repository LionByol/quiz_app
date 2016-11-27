package com.silver.sponsor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.silver.sponsor.reference.GR;
import com.silver.sponsor.reference.Problem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Random;

import cz.msebera.android.httpclient.Header;

public class GameActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initValue();

        tflayer = (LinearLayout)findViewById(R.id.tflayer);
        nextlayer = (LinearLayout)findViewById(R.id.nextlayer);

        trueview = (ImageView)findViewById(R.id.trueview);
        falseview = (ImageView)findViewById(R.id.falseview);
        prizeview = (ImageView)findViewById(R.id.prizeview);

        usernameview = (TextView)findViewById(R.id.usernameview);
        usernameview.setText(GR.username);

        scoreview = (TextView)findViewById(R.id.pointsview);
        scoreview.setText("PUNTOS: " + GR.score);

        problemview = (TextView)findViewById(R.id.problemview);
        problemview.setText("CARGA...");

        delayDialog = new ProgressDialog(this);
        delayDialog.setMessage("CARGA...");
        delayDialog.setIndeterminate(true);
        delayDialog.setCanceledOnTouchOutside(false);
        delayDialog.show();

        new AsyncHttpClient().get(GR.host + "getproblems.php", null, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                super.onSuccess(statusCode, headers, response);
                problems = new Problem[response.length()];
                for (int i = 0; i < problems.length; i++)
                {
                    problems[i] = new Problem();
                    try
                    {
                        problems[i].id = ((JSONObject) response.get("pro" + i)).get("id").toString();
                        problems[i].description = ((JSONObject) response.get("pro" + i)).get("description").toString();
                        problems[i].answer = ((JSONObject) response.get("pro" + i)).get("answer").toString();
                    } catch (JSONException e){}
                }
                total = problems.length;
                getAndSetProblem();
            }
        });

        nextbtn = (ImageView)findViewById(R.id.nextbtn);
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delayDialog.show();
                trueview.setVisibility(View.GONE);
                falseview.setVisibility(View.GONE);
                nextlayer.setVisibility(View.GONE);
                tflayer.setVisibility(View.VISIBLE);
                prizeview.setVisibility(View.VISIBLE);
                getAndSetProblem();
            }
        });

        backbtn = (ImageView)findViewById(R.id.backbtn);
        backbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            finish();
            startActivity(new Intent(GameActivity.this, GameMenu.class));
            }
        });

        backbtn2 = (ImageView)findViewById(R.id.backbtn2);
        backbtn2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
                startActivity(new Intent(GameActivity.this, GameMenu.class));
            }
        });


        truebtn = (ImageView)findViewById(R.id.truebtn);
        truebtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                answer = "T";
                checkAnswer();
            }
        });

        falsebtn = (ImageView)findViewById(R.id.falsebtn);
        falsebtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                answer = "F";
                checkAnswer();
            }
        });
    }

    void initValue()
    {
        falseN = 0;
    }

    void checkAnswer()
    {
        int correlative = 0;
        tflayer.setVisibility(View.GONE);
        prizeview.setVisibility(View.GONE);
        if(problems[proN].answer.equals(answer))        //it is true
        {
            trueview.setVisibility(View.VISIBLE);
            GR.score ++;
            scoreview.setText("PUNTOS: "+GR.score);
            correlative = 1;
        }
        else                                            //it is false
        {
            falseview.setVisibility(View.VISIBLE);
            falseN ++;
        }

        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("id", GR.userid);
        paramMap.put("score", GR.score+"");
        paramMap.put("proid", problems[proN].id);
        paramMap.put("answer", answer);
        paramMap.put("correlative", correlative+"");
        RequestParams params = new RequestParams(paramMap);
        new AsyncHttpClient().get(GR.host+"recordstate.php", params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                nextlayer.setVisibility(View.VISIBLE);
                if(falseN >=5)
                {
                    new AlertDialog.Builder(GameActivity.this)
                        .setTitle("Fin")
                        .setMessage("No 5 veces en un juego. Por favor, inténtelo de nuevo.")
                        .setPositiveButton("Vale", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                startActivity(new Intent(GameActivity.this, GameMenu.class));
                            }
                        })
                        .show();
                }
            }
        });

    }

    void getAndSetProblem()
    {
        proN = new Random().nextInt(total);
        problemview.setText(problems[proN].description);
        getAdvertise();
    }

    void getAdvertise()
    {
        new GetImageTask().execute();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
        startActivity(new Intent(this, GameMenu.class));
    }

    class GetImageTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... urls) {
            Bitmap map = null;
            map = downloadImage(GR.host+"getadvertise.php");
            return map;
        }

        // Sets the Bitmap returned by doInBackground
        @Override
        protected void onPostExecute(Bitmap result) {
            prizeview.setImageBitmap(result);
            delayDialog.dismiss();
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

    int falseN;

    ProgressDialog delayDialog;
    TextView usernameview;
    TextView scoreview;
    TextView problemview;
    ImageView nextbtn, backbtn, backbtn2;
    ImageView trueview, falseview, prizeview;
    ImageView truebtn, falsebtn;
    LinearLayout tflayer, nextlayer;
    Problem problems[];
    int total;
    int proN;
    String answer;
}
