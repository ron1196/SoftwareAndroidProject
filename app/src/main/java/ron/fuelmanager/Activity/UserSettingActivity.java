package ron.fuelmanager.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.io.InputStream;

import ron.fuelmanager.FirebaseHelper;
import ron.fuelmanager.R;
import ron.fuelmanager.DataType.User;

public class UserSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int INTENT_RESULT_FILE = 1;
    private static final int INTENT_RESULT_CAMERA = 2;

    // Download data.
    private User user = null;
    private Bitmap image = null;

    // UI references.
    private ProgressBar progress_user_setting;
    private RelativeLayout view_user_setting;
    private EditText etEmail, etFirstName, etLastName, etCity, etAddress;
    private ImageButton btnImage;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        progress_user_setting = findViewById(R.id.progress_user_setting);
        view_user_setting = findViewById(R.id.view_user_setting);

        etEmail = findViewById(R.id.etEmail);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etCity = findViewById(R.id.etCity);
        etAddress = findViewById(R.id.etAddress);
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        btnImage = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(this);

        showProgress(true);
        downloadData();
        downloadImage();
    }

    /**
     * Shows the progress UI and hides the view.
     */
    @TargetApi(VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            view_user_setting.setVisibility(show ? View.GONE : View.VISIBLE);
            view_user_setting.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view_user_setting.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progress_user_setting.setVisibility(show ? View.VISIBLE : View.GONE);
            progress_user_setting.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progress_user_setting.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progress_user_setting.setVisibility(show ? View.VISIBLE : View.GONE);
            view_user_setting.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void downloadData() {
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        etEmail.setText(mUser.getEmail());
        mDatabase.child("users").child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                etFirstName.setText(user.getFirstName());
                etLastName.setText(user.getLastName());
                etCity.setText(user.getCity());
                etAddress.setText(user.getAddress());
                showProgress(false);
            }
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error: " + databaseError.toException().getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
    private void downloadImage() {
        StorageReference userImageRef = FirebaseStorage.getInstance().getReference().child("userImages/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
        userImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(btnImage);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch ( view.getId() ) {
            case R.id.btnImage:
                selectImage();
                break;

            case R.id.btnSave:
                user.setFirstName(etFirstName.getText().toString());
                user.setLastName(etLastName.getText().toString());
                user.setAddress(etAddress.getText().toString());
                user.setCity(etCity.getText().toString());
                FirebaseHelper.uploadData(getApplicationContext(), FirebaseAuth.getInstance().getCurrentUser().getUid(), user, image, new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                finish();
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
                        intent.setAction(Intent.ACTION_GET_CONTENT);//
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
