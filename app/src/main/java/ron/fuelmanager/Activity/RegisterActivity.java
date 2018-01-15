package ron.fuelmanager.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.FileNotFoundException;
import java.io.InputStream;

import ron.fuelmanager.FirebaseHelper;
import ron.fuelmanager.R;
import ron.fuelmanager.DataType.User;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int INTENT_RESULT_FILE = 1;
    private static final int INTENT_RESULT_CAMERA = 2;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private FirebaseAnalytics mFirebaseAnalytics;

    private Bitmap image = null;

    private EditText etEmail, etPassword, etFirstName, etLastName, etCity, etAddress;
    private ImageButton btnImage;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etCity = findViewById(R.id.etCity);
        etAddress = findViewById(R.id.etAddress);

        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);

        btnImage = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch ( view.getId() ) {
            case R.id.btnImage:
                selectImage();
                break;

            case R.id.btnRegister:
                final User user = new User(etFirstName.getText().toString(), etLastName.getText().toString(), etCity.getText().toString(), etAddress.getText().toString(), 0, null);
                mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            mDatabase.child("users").child(firebaseUser.getUid()).setValue(user);
                            FirebaseHelper.uploadImage(getApplicationContext(), firebaseUser.getUid(), image, null);

                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, null);

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            finish();
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
        }
    }

    private void requestPermission(final Context context) {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return;
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    private void selectImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo");
        builder.setItems(R.array.select_image_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int item) {
                if (getResources().getStringArray(R.array.select_image_items)[item].equals("Take Photo")) {
                    requestPermission(getApplicationContext());
                    if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, INTENT_RESULT_CAMERA);
                    }
                } else if (getResources().getStringArray(R.array.select_image_items)[item].equals("Choose from Library")) {
                    requestPermission(getApplicationContext());
                    if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select File"), INTENT_RESULT_FILE);
                    }
                } else if (getResources().getStringArray(R.array.select_image_items)[item].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog show = builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == INTENT_RESULT_FILE) {
                try {
                    Uri selectedImage = data.getData();
                    InputStream imageStream = null;
                    imageStream = getContentResolver().openInputStream(selectedImage);
                    Bitmap image = BitmapFactory.decodeStream(imageStream);
                    setButtonBitmap(image);

                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
            else if (requestCode == INTENT_RESULT_CAMERA) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                setButtonBitmap(image);
            }
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.prompt_image_load_success), Toast.LENGTH_SHORT).show();
        }
    }

    private void setButtonBitmap(Bitmap image) {
        image = Bitmap.createScaledBitmap(image, btnImage.getWidth(), btnImage.getHeight(), false);
        this.image = image;
        btnImage.setImageBitmap(image);
    }

}
