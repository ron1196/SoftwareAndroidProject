package ron.fuelmanager.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ron.fuelmanager.DirectionsApi.Json.DownloadUrlTask;
import ron.fuelmanager.DirectionsApi.Json.ParserJsonTask;
import ron.fuelmanager.R;
import ron.fuelmanager.DirectionsApi.Route;

@SuppressLint("MissingPermission")
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionListener, View.OnClickListener {

    private FloatingActionButton btnOk;

    private GoogleMap map;
    private LatLng myLocation = null;
    private Route route = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);

        PlaceAutocompleteFragment locationAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        locationAutoComplete.setOnPlaceSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            public void onSuccess(Location location) {
                myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
    }

    private String getDirectionsApiUrl(LatLng origin_latlng, LatLng dest_latlng) {
        String origin = "origin=" + origin_latlng.latitude + "," + origin_latlng.longitude;
        String dest = "destination=" + dest_latlng.latitude + "," + dest_latlng.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = origin + "&" + dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private void drawRoute(List<List<HashMap<String, String>>> routes) {
        ArrayList points = null;
        PolylineOptions lineOptions = new PolylineOptions();
        MarkerOptions markerOptions = new MarkerOptions();

        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList();
            List<HashMap<String, String>> path = routes.get(i);
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng").toString());
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            lineOptions.addAll(points);
            lineOptions.width(12);
            lineOptions.color(Color.BLUE);
            lineOptions.geodesic(true);
        }

        // Drawing polyline in the Google Map for the i-th route
        map.addPolyline(lineOptions);
    }

    @Override
    public void onPlaceSelected(Place place) {
        map.clear();
        map.addMarker(new MarkerOptions().position(place.getLatLng()));

        try {
            String url = getDirectionsApiUrl(myLocation, place.getLatLng());
            DownloadUrlTask downloadTask = new DownloadUrlTask();
            downloadTask.execute(url);
            String jsonString = downloadTask.get();

            ParserJsonTask parser = new ParserJsonTask();
            parser.execute(jsonString);
            route = parser.get();

            if (route.getDistance() != -1) {
                drawRoute(route.getRoute());
                btnOk.setVisibility(View.VISIBLE);
            } else {
                route = null;
                btnOk.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Unavailable", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException | ExecutionException e) {Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();}
    }

    @Override
    public void onError(Status status) {Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();}


    // if the user choose a valid route, return the distance of the route to the activity who call us
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnOk:
                Intent result = new Intent(this, MainActivity.class);
                result.putExtra("routeDistance", route.getDistance());
                setResult(Activity.RESULT_OK, result);
                finish();
                break;
        }
    }
}
