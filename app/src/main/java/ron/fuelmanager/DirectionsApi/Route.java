package ron.fuelmanager.DirectionsApi;

import android.util.Log;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Ron on 10/01/2018.
 */

public class Route {

    private List<List<HashMap<String, String>>> route;
    private double distance;

    public Route(List<List<HashMap<String, String>>> route, double distance) {
        this.route = route;
        this.distance = distance;
    }

    public List<List<HashMap<String, String>>> getRoute() {
        return route;
    }

    public double getDistance() {
        return distance;
    }
}
