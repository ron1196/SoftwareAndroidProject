package ron.fuelmanager.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ron.fuelmanager.DataType.Car;
import ron.fuelmanager.R;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Const for asking for activity result and permission request
    private final static int REQUEST_PERMISSION_LOCATION = 1;
    private final static int GET_RESULT_ROUTE = 1;

    private final static double LITRE_GAS_PRICE = 6.19;
    private final static double MPG_TO_KPL = 0.425143707;  // 1 Mile per gallon = 0.425143707 kilometers per litre

    // UI references.
    private ProgressBar progress_main;
    private LinearLayout view_main;
    private RelativeLayout view_budget;
    private TextView tv_budget;
    private RelativeLayout view_pick_route;
    private EditText btn_pick_route;
    private RelativeLayout view_route_summary;
    private TextView text_route_dis, text_route_price;
    private Button btn_ok, btn_cancel;

    //
    private double budget;
    private double routePrice;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress_main = findViewById(R.id.progress_main);
        view_main = findViewById(R.id.view_main);

        view_budget = findViewById(R.id.view_budget);
        tv_budget = findViewById(R.id.tv_budget);
        tv_budget.setOnClickListener(this);

        btn_pick_route = findViewById(R.id.btn_pick_route);
        view_pick_route = findViewById(R.id.view_pick_route);
        btn_pick_route.setOnClickListener(this);
        view_pick_route.setOnClickListener(this);

        view_route_summary = findViewById(R.id.view_route_summary);
        text_route_dis = findViewById(R.id.text_route_dis);
        text_route_price = findViewById(R.id.text_route_price);
        btn_ok = findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(this);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showProgress(true);
        mDatabase.getReference().child("users").child(mUser.getUid()).child("budget").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                budget = dataSnapshot.getValue(Double.class);
                tv_budget.setText(budget + "");
                showProgress(false);
            }
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error: " + databaseError.toException().getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
        mDatabase.getReference().child("users").child(mUser.getUid()).child("isAdmin").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( dataSnapshot.exists() ) {
                    // Admin
                }
            }
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.optionLogout) {
            mAuth.signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        } else if (item.getItemId() == R.id.optionUserSetting) {
            startActivity(new Intent(getApplicationContext(), UserSettingActivity.class));
        }  else if (item.getItemId() == R.id.optionChangeCar) {
            openChangeCarDialog();
        } else if (item.getItemId() == R.id.optionAboutUs) {
            startActivity(new Intent(getApplicationContext(), ReadmeActivity.class));
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            view_main.setVisibility(show ? View.GONE : View.VISIBLE);
            view_main.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view_main.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progress_main.setVisibility(show ? View.VISIBLE : View.GONE);
            progress_main.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progress_main.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progress_main.setVisibility(show ? View.VISIBLE : View.GONE);
            view_main.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_pick_route:
            case R.id.btn_pick_route:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivityForResult(intent, GET_RESULT_ROUTE);
                break;

            case R.id.tv_budget:
                openSetBudgetDialog();
                break;
            case R.id.tv_currency_sign:
                openSetBudgetDialog();
                break;
            case R.id.btn_ok:
                updateUserBudget(budget - routePrice);
                finish();
                startActivity(getIntent());
                break;

            case R.id.btn_cancel:
                view_route_summary.setVisibility(View.GONE);
                break;
        }
    }

    private void openSetBudgetDialog() {
        final NumberPicker picker = new NumberPicker(getApplicationContext());
        picker.setMinValue(0);
        picker.setMaxValue(500);

        final FrameLayout layout = new FrameLayout(getApplicationContext());
        layout.addView(picker, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));

        new AlertDialog.Builder(this)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        updateUserBudget(picker.getValue());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateUserBudget(double budget) {
        FirebaseDatabase.getInstance().getReference().child("users").child(mUser.getUid()).child("budget").setValue(budget).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if ( task.isSuccessful() ) {
                    Toast.makeText(getApplicationContext(), "Data Saved!", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(getIntent());
                } else {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openChangeCarDialog() {
        LayoutInflater inflater = getLayoutInflater();
        final View dialog_pick_car = inflater.inflate(R.layout.dialog_pick_car, null, false);

        final Spinner spinnerBrands = dialog_pick_car.findViewById(R.id.spinnerBrands);
        final Spinner spinnerCarModels = dialog_pick_car.findViewById(R.id.spinnerCarModels);

        final HashMap<String, Car> cars = new HashMap<>();

        FirebaseDatabase.getInstance().getReference().child("brands").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<String> brands = new ArrayList<String>();

                final Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while ( iterator.hasNext() ) {
                    final DataSnapshot brand = iterator.next();
                    brands.add( brand.getKey() );
                }

                ArrayAdapter<String> adapterBrands = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, brands);
                adapterBrands.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBrands.setAdapter(adapterBrands);

                spinnerBrands.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> adapterView, View view, final int pos, long l) {
                        FirebaseDatabase.getInstance().getReference().child("cars").child(brands.get(pos)).addValueEventListener(new ValueEventListener() {
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                cars.clear();

                                final Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                                while ( iterator.hasNext() ) {
                                    final DataSnapshot dataCar = iterator.next();
                                    String modelName = dataCar.child("model").getValue().toString();
                                    Car car = new Car(brands.get(pos), Integer.parseInt(dataCar.getKey()), dataCar.child("model").getValue().toString(), Double.parseDouble(dataCar.child("mpg").getValue().toString()));
                                    cars.put(modelName, car);
                                }

                                String[] keys = new String[cars.size()];
                                cars.keySet().toArray(keys);
                                ArrayAdapter<String> adapterCars = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, keys);
                                adapterCars.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerCarModels.setAdapter(adapterCars);
                            }

                            public void onCancelled(DatabaseError databaseError) {}
                        });
                    }

                    public void onNothingSelected(AdapterView<?> adapterView) {}
                });
            }

            public void onCancelled(DatabaseError databaseError) {}
        });

        new AlertDialog.Builder(this)
                .setView(dialog_pick_car)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseDatabase.getInstance().getReference().child("users").child(mUser.getUid()).child("car").setValue(cars.get(spinnerCarModels.getSelectedItem())).addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                if ( task.isSuccessful() ) {
                                    Toast.makeText(getApplicationContext(), "Data Saved!", Toast.LENGTH_SHORT).show();
                                    finish();
                                    startActivity(getIntent());
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults[0] == PERMISSION_GRANTED)
                    btn_pick_route.callOnClick();
                else
                    Toast.makeText(getApplicationContext(), "Location permission is required!", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GET_RESULT_ROUTE:
                if (resultCode == RESULT_OK) {
                    showRouteSummary(data.getExtras().getDouble("routeDistance"));
                }
                break;
        }
    }

    private void showRouteSummary(double routeDistance) {
        final double distanceInKm = routeDistance / 1000.0;
        text_route_dis.setText(distanceInKm + "");

        mDatabase.getReference().child("users").child(mUser.getUid()).child("car").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                Car car = dataSnapshot.getValue(Car.class);
                if (car != null) {
                    double carKPL = car.getMpg() * MPG_TO_KPL;
                    double litreForRoute = distanceInKm / carKPL;
                    routePrice = litreForRoute * LITRE_GAS_PRICE;
                    text_route_price.setText(new DecimalFormat(".###").format(routePrice) + "");
                    view_route_summary.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), "Choose Your Car!", Toast.LENGTH_SHORT).show();
                }
            }

            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
