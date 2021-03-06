package com.example.postapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.dx.dxloadingbutton.lib.LoadingButton;
import com.google.android.material.textfield.TextInputEditText;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUp extends AppCompatActivity implements IPickResult {
     Bitmap bitmap;
      LoadingButton button;
      TextInputEditText text;
       CircleImageView imageView;
    private String email, password;
    BackendlessUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        button = findViewById(R.id.setup_btn);
        text = findViewById(R.id.setup_ED_name);
        imageView = findViewById(R.id.setup_image);
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
    }

    public void picImage(View view) {
        PickImageDialog.build(new PickSetup().setButtonOrientation(LinearLayout.HORIZONTAL)).show(this);
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            bitmap = r.getBitmap();
            imageView.setImageBitmap(bitmap);

        } else {
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void setUpFinish(View view) {
        if (bitmap == null) {
            Toast.makeText(this, "please take image first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(text.getText())) {
            Toast.makeText(this, "name is required", Toast.LENGTH_SHORT).show();
            text.setError("this filed is required");
            return;
        }
        createAccount();
    }

    private void createAccount() {


        mUser = new BackendlessUser();
        mUser.setProperty("email", email);
        mUser.setPassword(password);
        mUser.setProperty("phone", null);
        mUser.setProperty("location", null);
        mUser.setProperty("name", text.getText().toString());
        button.startLoading();
        Backendless.UserService.register(mUser, new AsyncCallback<BackendlessUser>() {
            public void handleResponse(BackendlessUser registeredUser) {
                setUserProperty(mUser);

            }

            public void handleFault(BackendlessFault fault) {
                button.loadingFailed();
                Toast.makeText(SetUp.this,fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setUserProperty(BackendlessUser user) {


        String name = text.getText().toString();
        SimpleDateFormat Format = new SimpleDateFormat("hh:mm a s, dd MMM yyyy", Locale.ENGLISH);
        String date=  Format.format(new Date());

        //upload image
        Backendless.Files.Android.upload(bitmap, Bitmap.CompressFormat.PNG, 30
                , name + date + ".png"
                , "userProfilePic", new AsyncCallback<BackendlessFile>() {
                    @Override
                    public void handleResponse(BackendlessFile response) {
                        String pic = response.getFileURL();
                        user.setProperty("profilePic", pic);


                        Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>() {
                            public void handleResponse(BackendlessUser user) {
                                button.loadingSuccessful();
                                button.loadingSuccessful();
                                button.reset();
                                sendToMain();
                            }

                            public void handleFault(BackendlessFault fault) {
                                button.loadingFailed();
                                Toast.makeText(SetUp.this,fault.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        button.loadingFailed();
                        Toast.makeText(SetUp.this,fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendToMain() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }
}