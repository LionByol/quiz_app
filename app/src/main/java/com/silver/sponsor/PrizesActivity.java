package com.silver.sponsor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class PrizesActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prizes);

        username = (TextView)findViewById(R.id.usernameview);
        username.setText(GR.username);
        scoreview = (TextView)findViewById(R.id.pointsview);
        scoreview.setText("PUNTOS: "+GR.score);

        prizelist = (ListView)findViewById(R.id.listView);
        new AsyncHttpClient().get(GR.host + "getprizes.php", null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                prizes = new String[response.length()];
                points = new String[response.length()];
                sponsors = new String[response.length()];
                for(int i=0; i<prizes.length; i++)
                {
                    try {
                        points[i] = ((JSONObject)response.get("pz"+i)).get("points").toString();
                        prizes[i] = ((JSONObject)response.get("pz"+i)).get("idprize").toString();
                        sponsors[i] = ((JSONObject)response.get("pz"+i)).get("idsponsor").toString();
                        PrizesAdapter adapter = new PrizesAdapter(PrizesActivity.this, android.R.id.text1, points, sponsors, prizes);
                        prizelist.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    class PrizesAdapter extends ArrayAdapter<String>
    {
        Context context;
        String point[];
        String sponsor[];
        String prize[];
        ImageView prizeview[];

        public PrizesAdapter(Context context, int resource, String[] point, String sponsor[], String prize[])
        {
            super(context, -1, point);
            this.context = context;
            this.point = point;
            this.sponsor = sponsor;
            this.prize = prize;
            prizeview = new ImageView[sponsor.length];
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.listelement, parent, false);
            TextView pointview = (TextView) rowView.findViewById(R.id.pointview);
            prizeview[position] = (ImageView) rowView.findViewById(R.id.prizeview);
            new GetImageTask().execute(new String[]{GR.host + "getprizeimage.php?sponsor=" + sponsor[position] + "&prize=" + prize[position], position+""});
            pointview.setText("PUNTOS: "+point[position]);
            Button redeembtn = (Button)rowView.findViewById(R.id.redeembtn);
            redeembtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    Intent intent = new Intent(PrizesActivity.this, SingSponsor.class);
                    intent.putExtra("sponsor", sponsor[position]);
                    intent.putExtra("prize", prize[position]);
                    intent.putExtra("point", point[position]);
                    startActivity(intent);
                }
            });
            return rowView;
        }
        private class GetImageTask extends AsyncTask<String, Void, Bitmap> {
            @Override
            protected Bitmap doInBackground(String... urls) {
                Bitmap map = null;
                map = downloadImage(urls[0]);
                position = Integer.parseInt(urls[1]);
                return map;
            }
            int position;
            // Sets the Bitmap returned by doInBackground
            @Override
            protected void onPostExecute(Bitmap result) {
                prizeview[position].setImageBitmap(result);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        scoreview.setText("PUNTOS: "+GR.score);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
        startActivity(new Intent(this, GameMenu.class));
    }

    ListView prizelist;
    TextView username, scoreview;
    String prizes[], sponsors[], points[];
}
