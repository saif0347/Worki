package com.app.worki.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class SendPushUtil {
    private static final String SERVER_KEY = "AAAAmpqS4SA:APA91bEbsZPcst8OzBnBNA0cLDyqDjG8PoZ8zYiHSgwlbihNraqSLM2LMrdbeZBIiAlBhGyw4qoDFAawn_V2ZzlyFS0dPwzL0kTXAN8uS3hlG32uvUQPUDjf4COcDUJ158dcnuyyl-ja";

    public interface PushResult{
        void success();
        void fail(String message);
    }

    public static void sendFirebasePush(@NonNull Context context, @NonNull JSONObject data,
                                        @NonNull JSONArray tokens, @NonNull final PushResult pushResult){
        JSONObject json = new JSONObject();
        try {
            json.put("data", data);
            json.put("registration_ids", tokens);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("onResponse", "" + response.toString());
                try {
                    int success = response.getInt("success");
                    if(success == 1){
                        pushResult.success();
                    }
                    else {
                        pushResult.fail(response.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    pushResult.fail("parse error");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error == null) {
                    pushResult.fail("error null");
                    return;
                }
                pushResult.fail(error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "key="+SERVER_KEY);
                params.put("Content-Type","application/json");
                return params;
            }
        };
        Volley.newRequestQueue(context).add(jsonObjectRequest);
    }
}
