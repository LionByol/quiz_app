package com.silver.sponsor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.silver.sponsor.reference.GR;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        delayDialog = new ProgressDialog(this);
        delayDialog.setMessage("CARGA...");
        delayDialog.setIndeterminate(true);
        delayDialog.setCanceledOnTouchOutside(false);
        delayDialog.show();

        userview = (TextView)findViewById(R.id.usernameview);
        userview.setText(GR.username);

        scoreview = (TextView)findViewById(R.id.pointsview);
        scoreview.setText("PUNTOS: "+GR.score);

        historyview = (ListView)findViewById(R.id.historylist);

        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("id", GR.userid);
        RequestParams params = new RequestParams(paramMap);
        new AsyncHttpClient().get(GR.host + "history.php", params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                historylist = new String[response.length()];
                for(int i=0; i<historylist.length; i++)
                {
                    try {
                        historylist[i] = ((JSONObject)response.get("pz"+i)).get("regdate").toString() +
                            "\n   PUNTOS: -" + ((JSONObject)response.get("pz"+i)).get("points").toString() + "      "+((JSONObject)response.get("pz"+i)).get("sponsor").toString();
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(HistoryActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, historylist);
                        historyview.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                delayDialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
        startActivity(new Intent(this, GameMenu.class));
    }

    ListView historyview;
    TextView userview;
    TextView scoreview;
    ProgressDialog delayDialog;

    String historylist[];
}
