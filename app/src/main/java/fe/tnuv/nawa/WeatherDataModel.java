package fe.tnuv.nawa;

/*
    To je class za podatke o vremenu, ki ga sparsamo iz JSON-a
 */

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataModel {

    private String mTemperature;
    private String mCity;
    private String mIconName;
    private int mCondition;


    public static WeatherDataModel fromJson(JSONObject jsonObject) {
        //preverjanje izjem/napak
        try {
            WeatherDataModel weatherData = new WeatherDataModel();

            //shranjevanje vrednosti iz JSON v spremenljivke
            weatherData.mCity = jsonObject.getString("name"); //name je tipa string
            //medtem ko je id tipa int znotraj objekta 0 in seznama weather
            weatherData.mCondition = jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id");
            weatherData.mIconName = updateWeatherIcon(weatherData.mCondition);
            //temperaturo moramo pretvoriti iz K -> C
            double temp = jsonObject.getJSONObject("main").getDouble("temp") - 273.15;
            weatherData.mTemperature = Double.toString(temp);
            return weatherData;

        } catch (JSONException e) {
            return null;
        }

    }

    //tu iz kode preveris katero sliko bos prikazal
    private static String updateWeatherIcon(int condition) {

        if (condition >= 0 && condition < 300) {
            return "tstorm1";
        } else if (condition >= 300 && condition < 500) {
            return "light_rain";
        } else if (condition >= 500 && condition < 600) {
            return "shower3";
        } else if (condition >= 600 && condition <= 700) {
            return "snow4";
        } else if (condition >= 701 && condition <= 771) {
            return "fog";
        } else if (condition >= 772 && condition < 800) {
            return "tstorm3";
        } else if (condition == 800) {
            return "sunny";
        } else if (condition >= 801 && condition <= 804) {
            return "cloudy2";
        } else if (condition >= 900 && condition <= 902) {
            return "tstorm3";
        } else if (condition == 903) {
            return "snow5";
        } else if (condition == 904) {
            return "sunny";
        } else if (condition >= 905 && condition <= 1000) {
            return "tstorm3";
        }

        return "dunno"; //ce ne ustreza nobenemu narisi vprasaj
    }

    //metode za dostopanje do private spremenljivk od zunaj
    public String getTemperature() {
        return mTemperature + "°C";
    }

    public String getCity() {
        return mCity;
    }

    public String getIconName() {
        return mIconName;
    }
}
