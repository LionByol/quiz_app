package com.silver.sponsor.reference;

import android.os.AsyncTask;

/**
 * Created by silver on 2/12/2016.
 */
public class SendRequest extends AsyncTask<String, Void, Void> {

    private Exception exception;

    protected Void doInBackground(String... url) {
        try {
            HttpRequest.get(url[0]).code();
            return null;
        } catch (Exception e) {
            this.exception = e;
            return null;
        }
    }

}

