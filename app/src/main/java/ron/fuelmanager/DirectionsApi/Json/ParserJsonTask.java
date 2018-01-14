package ron.fuelmanager.DirectionsApi.Json;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import ron.fuelmanager.DirectionsApi.DirectionsApi;
import ron.fuelmanager.DirectionsApi.Route;

/**
 * Created by Ron on 10/01/2018.
 */

public class ParserJsonTask extends AsyncTask<String, Integer, Route> {

    // Parsing the data in non-ui thread
    @Override
    protected Route doInBackground(String... jsonData) {
        double dis = -1;
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;
        try {
            jObject = new JSONObject(jsonData[0]);
            DirectionsApi parser = new DirectionsApi();
            routes = parser.parse(jObject);
            dis = jObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getDouble("value");
        } catch (Exception e) {e.printStackTrace();}
        return new Route(routes, dis);
    }

}
