package com.hust.buidoandung.weatherapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;

public abstract class RequestToServer extends AsyncTask<String,String,ApiResult> {
    ProgressDialog progressDialog;
    Context context;
    MainActivity mainActivity;

    public RequestToServer(ProgressDialog progressDialog, Context context, MainActivity mainActivity) {
        this.progressDialog = progressDialog;
        this.context = context;
        this.mainActivity = mainActivity;
    }

    @Override
    protected ApiResult doInBackground(String... strings){
        ApiResult result=new ApiResult();
        String response="";
        String[] coords=new String[]{};
        if(strings!=null&&strings.length>0){
            String firstparam=strings[0];
            if(firstparam.equals("coords")){
                String latitude=strings[1];
                String longtitude=strings[2];
                coords=new String[]{latitude,longtitude};
            }
        }
            try {
                URL url=createURL(coords);
                HttpURLConnection connection= (HttpURLConnection) url.openConnection();
                int responseCode=connection.getResponseCode();
                if(responseCode==200){
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                    BufferedReader r = new BufferedReader(inputStreamReader);
                    String line=null;
                    while ((line=r.readLine())!=null){
                        response+=line+"\n";
                    }
                    r.close();
                    connection.disconnect();
                    result.taskResult=ApiResult.TaskResult.SUCCESS;
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    Calendar now = Calendar.getInstance();
                    editor.putLong("lastUpdate", now.getTimeInMillis()).apply();
                }else if(responseCode==429){
                    result.taskResult = ApiResult.TaskResult.TOO_MANY_REQUESTS;

                }else{
                    result.taskResult= ApiResult.TaskResult.BAD_RESPONSE;
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.taskResult= ApiResult.TaskResult.IO_EXCEPTION;

            }

        if(result.taskResult.equals(ApiResult.TaskResult.SUCCESS)){
            ApiResult.ServerResult serverResult = null;
            try {
                serverResult = parseResponse(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (ApiResult.ServerResult.CITY_NOT_FOUND.equals(serverResult)) {
                restorePreviousCity();
            }
            result.serverResult = serverResult;
        }
        return result;
    }

    @Override
    protected void onPostExecute(ApiResult apiResult) {

        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        updateMainUI();
        handleTaskOutput(apiResult);
    }

    public void handleTaskOutput(ApiResult apiResult) {
        switch (apiResult.taskResult) {
        case SUCCESS: {
            ApiResult.ServerResult parseResult = apiResult.serverResult;
            if (ApiResult.ServerResult.CITY_NOT_FOUND.equals(parseResult)) {
                Snackbar.make(mainActivity.findViewById(android.R.id.content), "Specified city is not found.", Snackbar.LENGTH_LONG).show();
            } else if (ApiResult.ServerResult.JSON_EXCEPTION.equals(parseResult)) {
                Snackbar.make(mainActivity.findViewById(android.R.id.content), "Error parsing JSON.", Snackbar.LENGTH_LONG).show();
            }
            break;
        }
        case TOO_MANY_REQUESTS: {
            Snackbar.make(mainActivity.findViewById(android.R.id.content), "Too many requests. Please try again.", Snackbar.LENGTH_LONG).show();
            break;
        }
        case BAD_RESPONSE: {
            Snackbar.make(mainActivity.findViewById(android.R.id.content), "There is a problem with your internet connection.", Snackbar.LENGTH_LONG).show();
            break;
        }
        case IO_EXCEPTION: {
            Snackbar.make(mainActivity.findViewById(android.R.id.content), "Connection not available.", Snackbar.LENGTH_LONG).show();
            break;
        }
        default:
            Snackbar.make(mainActivity.findViewById(android.R.id.content),"Has some error, please try again.",Snackbar.LENGTH_LONG).show();
            break;
    }

    }

    private void restorePreviousCity() {
        if (!TextUtils.isEmpty(mainActivity.recentCity)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putString("city", mainActivity.recentCity);
            editor.commit();
            mainActivity.recentCity = "";
        }
    }

    protected abstract ApiResult.ServerResult parseResponse(String response) throws Exception;

    private URL createURL(String[] coords) throws Exception{
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(context);
        String apiKey=sp.getString("apiKey","fce95bdbd820ccf29a68b9574b50fe50");
        StringBuilder stringBuilder=new StringBuilder("http://api.openweathermap.org/data/2.5/");
        stringBuilder.append(getAPIName()).append("?");
        if(coords.length==2){
            stringBuilder.append("lat=").append(coords[0]).append("&lon=").append(coords[1]);

        }else{
            final String city = sp.getString("city", DefaultValue.DEFAULT_CITY);
            stringBuilder.append("q=").append(URLEncoder.encode(city, "UTF-8"));
        }
        stringBuilder.append("&lang=").append(Locale.getDefault().getLanguage());
        stringBuilder.append("&mode=json");
        stringBuilder.append("&appid=").append(apiKey);
        return new URL(stringBuilder.toString());
    }



    protected abstract String getAPIName() ;

    @Override
    protected void onPreExecute() {
        if(!progressDialog.isShowing()){
            progressDialog.setMessage("Waiting....");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

    }

    protected void updateMainUI() { }

}
