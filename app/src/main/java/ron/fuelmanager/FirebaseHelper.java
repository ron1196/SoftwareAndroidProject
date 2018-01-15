package ron.fuelmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import ron.fuelmanager.DataType.User;

/**
 * Created by Ron on 09/01/2018.
 */

public class FirebaseHelper {

    public static void uploadData(final Context context, final String uid, final User user, final Bitmap image, final Runnable onCompleteListener) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(uid).child("address").setValue(user.getAddress());
        mDatabase.child("users").child(uid).child("firstName").setValue(user.getFirstName());
        mDatabase.child("users").child(uid).child("lastName").setValue(user.getLastName());
        final Task<Void> task = mDatabase.child("users").child(uid).child("city").setValue(user.getCity());
        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Saved Data!", Toast.LENGTH_SHORT).show();
                    if (image != null) {
                        uploadImage(context, uid, image, onCompleteListener);
                    } else {
                        if (onCompleteListener != null) onCompleteListener.run();
                    }
                }  else {
                    Toast.makeText(context, "Failed to upload data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void uploadImage(final Context context, String uid, final Bitmap image, final Runnable onCompleteListener) {
        if (image == null) return;

        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        ByteArrayOutputStream byteData = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 20, byteData);
        StorageReference imageRef = mStorage.getReference().child("userImages/" + uid + ".jpg");
        UploadTask uploadTask = imageRef.putBytes(byteData.toByteArray());
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (!task.isSuccessful()) Toast.makeText(context, "Failed to upload Image", Toast.LENGTH_SHORT).show();
                if (onCompleteListener != null) onCompleteListener.run();
            }
        });
    }
}
