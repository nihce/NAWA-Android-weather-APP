package fe.tnuv.nawa;

import android.Manifest;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class Weather extends AppCompatActivity {

    //https://openweathermap.org/appid  <- tu vidis kako se klice OpenWeather API

    //KONSTANTE:
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather"; //zacetek URL-a je vedno enak
    final String APP_ID = "c7d7c9238c59906daa83774a044f5ecd"; // OpenWeather App ID
    //cas med posodobitvami lokacije = 5000 ms
    final long MIN_TIME = 5000;
    //razdalja med posodbitvami lokacije = 1000 m
    final float MIN_DISTANCE = 1000;
    final int REQUEST_CODE = 123;


    //SPREMENLJIVKE:
    TextView mCityText;
    ImageView mWeatherImage;
    TextView mTemperatureText;

    //tako dobis podatek o lokaciji preko omrezja, ne preko GPS
    //to uporabim zato, ker je v Manifestu klican ACCESS_COARSE_LOCATION
    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;

    LocationManager mLocationManager; //zacne ali konca location update
    LocationListener mLocationListener; //ta je obvescen ce se lokacija dejansko spremeni

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //povezemo activity z layout datoteko
        setContentView(R.layout.weather_layout);

        mCityText = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureText = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);
        ImageButton myLocationButton = (ImageButton) findViewById(R.id.myLocationButton);

        //dodamo poslusalca na gumb za vnos mesta
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //zamenjamo prikaz UI na city_layout.xml s pomocjo "Intent" (https://developer.android.com/reference/android/content/Intent)
                //to storis tako da zamenjas cel activity in se avtomatsko zamenja tudi prikaz
                Intent myIntent = new Intent(Weather.this, City.class);
                startActivity(myIntent); //zazene novo aktivnost City.java
            }
        });

        //dodamo poslusalca na gumb za prikaz vremena za naso trenutno pozicijo
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //spremenis temperatura TV
                mTemperatureText.setText(R.string.default_temp);
                //spremenis mesto TV
                mCityText.setText(R.string.default_location);
                //spremenis ikona IV
                mWeatherImage.setImageResource(R.drawable.dunno);

                getWeatherForCurrentLocation();
            }
        });
    }


    //to se izvede po klicu onCreate in !!! ko prides iz druge aktivnosti (City.java)!!!
    @Override
    protected void onResume() {
        super.onResume();
        //preveris ce v intentu obstaja key = "City"
        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("City");
        //ce si prisel iz City.java potem obstaja
        if (city != null) {
            getWeatherForNewCity(city);
        }
        //ce si ravno zagnal aplikacijo pa ne obstaja, alice si pritisnil back button
        else {
            getWeatherForCurrentLocation();
        }
    }


    //podatki o vremenu za rocno vneseno mesto
    private void getWeatherForNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city); //za obliko klica glej dokumentacijo OpenWeather API
        params.put("appid", APP_ID);

        networkCall(params); //klic funkcije napisane spodaj
    }


    //podatki o vremenu za trenutno lokacijo
    private void getWeatherForCurrentLocation() {
        //klic LocationManagerja
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); //spremenimo object, ki ga vrne getSystemService v LocationManager
        //klic LocationListenerja
        mLocationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                //shrani podatke o lokaciji ki ti jih da Android OS
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());


                
                //sestavimo parametre ki jih bomo poslali na server (http knjiznica)
                RequestParams params = new RequestParams();
                params.put("lat", latitude); //za obliko klica glej dokumentacijo OpenWeather API
                params.put("lon", longitude);
                params.put("appid", APP_ID);

                networkCall(params); //klic funkcije napisane spodaj
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(Weather.this, R.string.network_error, Toast.LENGTH_LONG).show();
            }
        };

        //ta if stavek je za preverjanje, ali je uporabnik dovolil uporabo lokacije
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //s tem preveris ali je dal dovoljenje
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            return;
        }
        //sedaj gledamo za spremembe lokacije
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);

    }


    //preverjanje kako se je uporabnik odlocil glede dovoljenja o lokaciji
    //!!!se izvede le prvic, ko mora uporabnik dati dovoljenje!!!
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //preverjamo ali je to funkcijo res klicalo nase dovoljenje za lokacijo ki vsebuje REQUEST_CODE = 123
        if (requestCode == REQUEST_CODE) {
            //preverimo ali je uporabnik kliknil "dovoli"
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getWeatherForCurrentLocation(); //posodobis vreme (funkcija zgoraj)
            }
            else {
                //uporabnik je zavrnil dovoljenje za lokacijo
            }
        }
    }


    //posiljanje GET requesta -> klic parsanja JSON-a -> posodobitev UI
    public void networkCall(RequestParams params) {
        //v ozadju deluje zato da je aplikacija se vedno odzivna
        AsyncHttpClient client = new AsyncHttpClient();
        //HTTP GET request
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            //v primeru uspesnega responsa
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //ustvari objekt WeatherDataModel z imenom weatherData iz prejetega JSON-a (ki ga sparsas v WeatherDataModel.java)
                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                //posodobitev uporabniskega vmesnika
                updateUI(weatherData);
            }

            //v primeru neuspeha
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                //opozori uporabnika
                Toast.makeText(Weather.this, "Failed! Status code = " + statusCode, Toast.LENGTH_LONG).show();
            }
        });
    }


    //posodobitev temperature in mesta v UI
    private void updateUI(WeatherDataModel weather) {
        //spremenis temperatura TV
        mTemperatureText.setText(weather.getTemperature());
        //spremenis mesto TV
        mCityText.setText(weather.getCity());
        //spremenis ikona IV
        //za sliko moras dolocit resource
        int resourceID = getResources().getIdentifier(weather.getIconName(), "drawable", getPackageName());
        mWeatherImage.setImageResource(resourceID);
    }


    //sproscanje virov in porabe energije ce aplikacija ni v ospredju
    @Override
    protected void onPause() {
        super.onPause();

        //prenehaj prejemati update o lokaciji ce ze imas lokacijo
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }


}
