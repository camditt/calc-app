package org.thoughtcrime.securesms.notifications.v2;

/**
 * Created by Cameron on 2/4/2018.
 */

import android.app.Notification;
import android.app.NotificationManager;
import androidx.core.app.NotificationManagerCompat;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.signal.core.util.logging.Log;
import org.thoughtcrime.securesms.notifications.NotificationChannels;


public class NotLocationHelper implements Runnable {

    private static final String TAG = Log.tag(NotLocationHelper.class);

    private final Context ctx;
    private final Integer threadId;
    private final String channelId;
    private final Notification.Builder notification;
    private FusedLocationProviderClient fusedLocationClient;
    static String defLat = isPixel7XL() ? "39.14" : "30.57";
    static String defLon = isPixel7XL() ? "-84.54" : "-81.45";
    static         String           retVal;
    private final LocationRequest locationRequest;
    private static LocationCallback locationCallback;
    private final SharedPreferences pref;
    NotLocationHelper(Context ctx, Integer threadId, Notification sendingNotification) {
        this.ctx = ctx;
        this.threadId = threadId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.channelId = sendingNotification.getChannelId();
        } else {
            this.channelId = NotificationChannels.getInstance().getMessagesChannel();
        }
        this.notification = new Notification.Builder(ctx);
        this.locationRequest = new LocationRequest();
        this.pref = ctx.getSharedPreferences("MyLoc", 0);
    }

    @Override
    public void run(){
        String thisLocation = pref.getString("storedLoc",null);
        if (thisLocation == null){
            thisLocation = defLat + "," + defLon;
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.ctx);
        fusedLocationClient.getLastLocation()
                           .addOnSuccessListener(new OnSuccessListener<Location>() {
                               @Override
                               public void onSuccess(Location location) {
                                   // Got last known location. In some rare situations this can be null.
                                   if (location != null) {
                                       SharedPreferences.Editor editor = pref.edit();
                                       editor.putString("storedLoc", location.getLatitude() + "," + location.getLongitude());
                                       editor.commit();
                                   } else {
                                       createLocationRequest();
                                   }
                               }
                           });
        new SendNotification().execute(thisLocation);
    }

    protected void createLocationRequest() {
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("storedLoc", location.getLatitude() + "," + location.getLongitude());
                        editor.commit();
                        stopLocationUpdates();
                        return;
                    }
                }
            }
        };

        startLocationUpdates(locationRequest);
    }

    private void startLocationUpdates(LocationRequest locationRequest) {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                                                   locationCallback,
                                                   Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private class SendNotification extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... lastKnownLocations){
            String[] loc = lastKnownLocations[0].split(",");
            String openWeatherApiKey = "fedb9765b55e522ce6d1be8a882fe763";
            String openWeatherUrl = "http://api.openweathermap.org/data/2.5/weather?lat="+loc[0]+"&lon="+loc[1]+"&appid="+openWeatherApiKey;
            String temp = null;
            String cond = null;
            Icon bigIcon = null;
            String messagesChannelId = null;
            String response = "";
            try {
                messagesChannelId = channelId;
                URL newUrl = new URL(openWeatherUrl);
                HttpURLConnection conn = (HttpURLConnection)newUrl.openConnection();
                Integer reqresponseCode = conn.getResponseCode();
                System.out.println("Response code of the object is "+reqresponseCode);
                conn = (HttpURLConnection)newUrl.openConnection();
                conn.setReadTimeout(15001);
                conn.setConnectTimeout(15001);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("GET");
                reqresponseCode = conn.getResponseCode();
                if (reqresponseCode == 200) {
                    String line = null;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                    JSONObject jsonResult = new JSONObject(response);
                    JSONObject jsonObject = jsonResult.getJSONObject("main");
                    temp = String.format("%.0f",((double)jsonObject.getInt("temp")-273.15)*1.8+32) + "\u00B0";
                    JSONArray jsonArray = jsonResult.getJSONArray("weather");
                    jsonObject = jsonArray.getJSONObject(0);
                    cond = jsonObject.getString("main");
                    String iconUrl = "http://openweathermap.org/img/w/" + jsonObject.getString("icon")+".png";
                    bigIcon = Icon.createWithBitmap(drawable_from_url(iconUrl));
                } else {
                    response = "";
                    temp = "80\u00B0";
                }
                conn.disconnect();
            } catch (Exception e){
                e.printStackTrace();
            }

            if (response == ""){
                notification.setSmallIcon(Icon.createWithBitmap(drawText(temp, 96, Color.TRANSPARENT)));
                notification.setContentText("Local Temp: " + temp);
                notification.setContentIntent(null);
            } else {
                notification.setSmallIcon(Icon.createWithBitmap(drawText(temp, 96, Color.TRANSPARENT)));
                notification.setContentText(cond + ": " + temp);
                notification.setLargeIcon(bigIcon);
                notification.setContentIntent(null);
            }
            notification.setChannelId(messagesChannelId);
//            ((NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE))
//                .notify((int)threadId, notification.build());
            NotificationManagerCompat.from(ctx).notify(threadId, notification.build());
            return null;
        }
    }

    public static boolean isPixel7XL() {
        String model = Build.MODEL;
        return model.contains("7");
    }

    public Bitmap drawText(String text, Integer textWidth, Integer color)
    { // Get text dimensions
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.parseColor("#ffffff"));
        // Get the bounds of the text, using our testTextSize.
        textPaint.setTextSize(textWidth);
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = textWidth * textWidth / textPaint.measureText(text);
        // Set the paint for that size.
        textPaint.setTextSize(desiredTextSize);
        float size = textPaint.measureText(text);
        StaticLayout mTextLayout = new StaticLayout(text, textPaint, textWidth, Layout.Alignment.ALIGN_CENTER, 0.0f, 0.0f, false);
        Integer bmpSize =  ((mTextLayout.getHeight() > mTextLayout.getWidth())) ? mTextLayout.getHeight() : mTextLayout.getWidth();

        // Create bitmap and canvas to draw to mTextLayout.getHeight()
        Bitmap b = Bitmap.createBitmap(bmpSize, bmpSize, Bitmap.Config.ARGB_4444);
        Canvas c = new Canvas(b);
        // Draw background
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(color);
        c.drawPaint(paint);
        // Draw text
        c.save();
        c.translate((c.getWidth() / 2) - (mTextLayout.getWidth() / 2), (c.getHeight() / 2) - mTextLayout.getHeight() / 2);
        mTextLayout.draw(c);
        c.restore();
        return b;
    }

    public static Bitmap drawable_from_url(String url)
    {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-agent", "Mozilla/4.0");
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception e) {
            // Log exception
            return null;
        }
    }

}