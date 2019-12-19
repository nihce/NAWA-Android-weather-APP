package fe.tnuv.nawa;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class City extends AppCompatActivity {

    //KONSTANTE:
    final int THRESHOLD = 3; //koliko crk mora vpisati uporabnik, da se zacnejo pojavljati predlogi
    final String WEATHER_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?"; //zacetek URL-a je vedno enak
    final String API_KEY = "AIzaSyAUR3oJ5uBK_OyXUD2RUKA9aADMAtw3NIA"; //kljuc ki smo ga dobili od googla
    final String TYPES = "(cities)";
    final String LANGUAGE = "sl";


    //SPREMENLJIVKE:
    public String[] suggestions = new String[] {
            "",
            ""
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //povezemo activity z layout datoteko
        setContentView(R.layout.city_layout);

        //elementi na zaslonu
        final AutoCompleteTextView editTextField = (AutoCompleteTextView) findViewById(R.id.queryET);
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);



        //na gumb postavis listener ce si uporabnik premisli in pritisne "backButton"
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ugasnemo City.java aktivnost
                //sprostimo pomnilnik
                finish();
            }
        });

        //to je dodano zato da predlaga uporabniku mesta
        //tukaj gledamo ali se je spremenil tekst v polju za vnos mesta
        editTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String partialCytyName = editTextField.getText().toString(); //shrani vneseno besedilo
                RequestParams params = new RequestParams();
                //za obliko klica glej dokumentacijo Place Autocomplete API
                //sestavi se v nekaj takega:
                //https://maps.googleapis.com/maps/api/place/autocomplete/json?input=Lju&types=(cities)&language=sl&key=AIzaSyAUR3oJ5uBK_OyXUD2RUKA9aADMAtw3NIA
                params.put("input", partialCytyName);
                params.put("types", TYPES);
                params.put("language", LANGUAGE);
                params.put("key", API_KEY);

                //v ozadju deluje zato da je aplikacija se vedno odzivna
                AsyncHttpClient client = new AsyncHttpClient();
                //HTTP GET request
                client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
                    //v primeru uspesnega responsa
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            //parsanje JSON formata
                            suggestions[0] = response.getJSONArray("predictions").getJSONObject(0).getString("description");
                            suggestions[1] = response.getJSONArray("predictions").getJSONObject(1).getString("description");

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, suggestions);
                            editTextField.setThreshold(THRESHOLD);
                            editTextField.setAdapter(adapter);

                            //editTextField.NotifyDataSetChanged();
                            //((BaseAdapter) editTextField.getAdapter()).notifyDataSetChanged();
                            //Toast.makeText(getApplicationContext(), "datset changed", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {

                        }
                    }
                    //v primeru neuspeha
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                        //opozori uporabnika
                        Toast.makeText(getApplicationContext(), "Failed! Status code = " + statusCode, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        //ime mesta prenesemo iz City -> Weather s pomocjo Intent + Extra
        editTextField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //to tukaj se izvede ko je pritisnjen ENTER na tipkovnici
                String newCity = editTextField.getText().toString(); //shrani vneseno besedilo
                //to besedilo sedaj predamo preko intenta
                Intent newCityIntent = new Intent(City.this, Weather.class);
                newCityIntent.putExtra("City", newCity); //key, value struktura
                startActivity(newCityIntent);
                return false;
            }
        });

    }
}
