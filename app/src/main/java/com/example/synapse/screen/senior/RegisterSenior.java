package com.example.synapse.screen.senior;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import com.example.synapse.R;
import com.example.synapse.screen.PickRole;
import com.example.synapse.screen.carer.register.RegisterCarer;
import com.example.synapse.screen.carer.register.TermsAndDataPolicy;
import com.example.synapse.screen.carer.verification.OTP;
import com.example.synapse.screen.util.PromptMessage;
import com.example.synapse.screen.util.readwrite.ReadWriteUserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class RegisterSenior extends AppCompatActivity {

    PromptMessage promptMessage = new PromptMessage();
    private static final String TAG_1 = "RegisterActivity";
    private static final String TAG = "";
    private ImageView ivProfilePic;
    private TextInputEditText dropdown_dob;
    private DatePickerDialog datePickerDialog;
    private static final int PICK_IMAGE_REQUEST = 1;
    private final String imageURL = "";
    private Uri uriImage;
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private Uri seniorImage;

    ProgressBar progressBar;
    AutoCompleteTextView autocompleteBarangay;
    AutoCompleteTextView autocompleteGender;

    private TextInputEditText
            etFirstName,
            etMiddle,
            etLastName,
            etEmail,
            etPassword,
            etMobileNumber;

    private TextInputLayout
            tilFirstName,
            tilMiddle,
            tilLastName,
            tilDOB,
            tilGender,
            tilEmail,
            tilAddress,
            tilMobileNumber,
            tilPassword;

    String  token,
            textFirstName,
            textMiddle,
            textLastName,
            textEmail,
            textPassword,
            textMobileNumber,
            textDOB,
            textAddress,
            textCity,
            textGender,
            textToken,
            userType,
            textDate,
            textCarerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_senior);

        Bundle extras = getIntent().getExtras();
        textCarerID  = extras.getString("carerID");
        auth = FirebaseAuth.getInstance();

        AppCompatImageView chooseProfilePic = findViewById(R.id.ic_senior_choose_profile_pic);
        Button btnSignup = findViewById(R.id.btnSignupSenior);
        TextView termsAndDataPolicy = findViewById(R.id.termsAndDataPolicy);
        progressBar = findViewById(R.id.progressBarRegister);
        autocompleteBarangay = findViewById(R.id.drop_barangay);
        autocompleteGender = findViewById(R.id.drop_gender);
        etFirstName = findViewById(R.id.etSeniorFirstName);
        etMiddle = findViewById(R.id.etSeniorMiddle);
        etLastName = findViewById(R.id.etSeniorLastName);
        etEmail = findViewById(R.id.etSeniorEmail);
        etPassword = findViewById(R.id.etRegisterSeniorPassword);
        etMobileNumber = findViewById(R.id.etSeniorMobileNumber);
        ivProfilePic = findViewById(R.id.ibSeniorProfilePic);
        dropdown_dob = findViewById(R.id.dropdown_dob);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilMiddle = findViewById(R.id.tilMiddle);
        tilLastName = findViewById(R.id.tilLastName);
        tilDOB = findViewById(R.id.tilDOB);
        tilGender = findViewById(R.id.tilGender);
        tilEmail = findViewById(R.id.tilEmail);
        tilAddress = findViewById(R.id.menuDrop);
        tilMobileNumber = findViewById(R.id.tilMobileNumber);
        tilPassword = findViewById(R.id.tilPassword);

        // get user token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        token = task.getResult();

                        // Log and toast
                        String msg = token;
                        Log.d("Token:", msg);
                    }
                });

        String [] barangays = {"Addition Hills","Bagong Silang","Barangay Drive","Barangka Ibaba",
                "Barangka Ilaya","Barangka Bato","Burol","Hagdang Bato Itaas","Hagdang Bato Libis",
                "Harapin Ang Bukas","Highway Hills","Hulo","Mabini-J. Rizal","Malamig","Mauway",
                "Namayan","New Zañiga","Pag-asa","Plainview","Pleasant Hills","Poblacion","San Jose",
                "Vergara","Wack-Wack Greehills"};

        ArrayAdapter<String> itemAdapter1 = new ArrayAdapter<String>(RegisterSenior.this, R.layout.dropdown_items, barangays);
        autocompleteBarangay.setAdapter(itemAdapter1);

        String [] gender = {"Male","Female"};
        ArrayAdapter<String> itemAdapter2 = new ArrayAdapter<>(RegisterSenior.this, R.layout.dropdown_items, gender);
        autocompleteGender.setAdapter(itemAdapter2);

        ImageButton ibBack = findViewById(R.id.ibRegisterSeniorBack);
        ibBack.setOnClickListener(view -> startActivity(new Intent(RegisterSenior.this, PickRole.class)));

        // open file dialog for profile pic
        chooseProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                openFileChooser();
            }
        });

        // open date picker
        initDatePicker();
        dropdown_dob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            // 60yrs and above 2022 - 60 = 1962
            calendar.add(Calendar.YEAR, - 60);
           // dropdown_dob.setText(getTodaysDate());
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            datePickerDialog.show();

            //dropdown_dob.setText(getTodaysDate());
            datePickerDialog.show();
        });

        etFirstName.addTextChangedListener(textWatcher);
        etMiddle.addTextChangedListener(textWatcher);
        etLastName.addTextChangedListener(textWatcher);
        dropdown_dob.addTextChangedListener(textWatcher);
        autocompleteGender.addTextChangedListener(textWatcher);
        autocompleteBarangay.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etMobileNumber.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);

        btnSignup.setOnClickListener(v -> checkValidation());
        termsAndDataPolicy.setOnClickListener(view -> startActivity(new Intent(RegisterSenior.this, TermsAndDataPolicy.class)));
        isAppRunning();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        auth.signOut();
    }

    private boolean isAppRunning() {
        ActivityManager m = (ActivityManager) this.getSystemService( ACTIVITY_SERVICE );
        List<ActivityManager.RunningTaskInfo> runningTaskInfoList =  m.getRunningTasks(10);
        Iterator<ActivityManager.RunningTaskInfo> itr = runningTaskInfoList.iterator();
        int n=0;
        while(itr.hasNext()){
            n++;
            itr.next();
        }
        if(n==1){ // App is killed
            auth.signOut();
            return false;
        }

        return true; // App is in background or foreground
    }

    public void checkValidation(){
            // obtain the entered data
            Date timestamp = Calendar.getInstance().getTime();
            textToken = token;
            userType = "Senior";
            textFirstName = etFirstName.getText().toString();
            textMiddle = etMiddle.getText().toString();
            textLastName = etLastName.getText().toString();
            textEmail = etEmail.getText().toString();
            textPassword = etPassword.getText().toString();
            textMobileNumber = etMobileNumber.getText().toString();
            textDOB = dropdown_dob.getText().toString();
            textAddress = autocompleteBarangay.getText().toString();
            textCity = "Mandaluyong City";
            textGender = autocompleteGender.getText().toString();
            textDate = timestamp.toString();

            // validate mobile number using matcher and regex
            String mobileRegex = "^(09|\\+639)\\d{9}$"; // first no. can be {09 or +639} and rest 9 no. can be any no.
            Matcher mobileMatcher;
            Pattern mobilePattern = Pattern.compile(mobileRegex);
            mobileMatcher = mobilePattern.matcher(etMobileNumber.getText());

            // Password contain at least 6 characters, one digit, and one upper case letter
            String passwordRegex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])\\S{6,}$";
            Matcher passwordMatcher;
            Pattern passwordPattern = Pattern.compile(passwordRegex);
            passwordMatcher = passwordPattern.matcher(etPassword.getText());

            if(checkIfEmpty(etFirstName)){
                tilFirstName.setError("This field can't be empty");
                tilFirstName.requestFocus();
            } else if(checkIfEmpty(etMiddle)){
                tilMiddle.setError("This field can't be empty");
                tilMiddle.requestFocus();
            } else if(checkIfEmpty(etLastName)){
                tilLastName.setError("This field can't be empty");
                tilLastName.requestFocus();
            } else if(checkIfEmpty(dropdown_dob)){
                tilDOB.setError("This field can't be empty");
                tilDOB.requestFocus();
            } else if(TextUtils.isEmpty(autocompleteGender.getText())){
                tilGender.setError("This field can't be empty");
                tilGender.requestFocus();
            } else if(TextUtils.isEmpty(autocompleteBarangay.getText())){
                tilAddress.setError("This field can't be empty");
                tilAddress.requestFocus();
            } else if(!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText()).matches()){
                tilEmail.setError("Invalid email. Please re-enter your email");
                tilEmail.requestFocus();
            } else if(checkIfEmpty(etMobileNumber)){
                tilMobileNumber.setError("This field can't be empty");
                tilMobileNumber.requestFocus();
            } else if(etMobileNumber.getText().length() != 11){
                tilMobileNumber.setError("Mobile no. should be 11 digits. e.g. 09166992880");
                tilMobileNumber.requestFocus();
            } else if(!mobileMatcher.find()){
                tilMobileNumber.setError("Mobile no. is not valid. e.g. 09166992880");
                tilMobileNumber.requestFocus();
            } else if(checkIfEmpty(etPassword)){
                tilPassword.setError("This field can't be empty");
                tilPassword.requestFocus();
            } else if(!passwordMatcher.find()){
                tilPassword.setError("Password must contain at least 6 characters, 1 digit, and 1 upper case");
                tilPassword.requestFocus();
            } else if(uriImage == null){
                promptMessage.displayMessage("Empty profile picture",
                        "Please select your profile picture", R.color.red_decline_request, RegisterSenior.this);
            }

            else{
                signupUser(textFirstName,textMiddle, textLastName, textEmail,textMobileNumber,textPassword,textDOB,textAddress, textCity,
                        textGender,userType,imageURL,textToken, textDate);
            }
    }

    public boolean checkIfEmpty(TextInputEditText tit){
        return TextUtils.isEmpty(tit.getText());
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            tilFirstName.setError(null);
            tilMiddle.setError(null);
            tilLastName.setError(null);
            tilGender.setError(null);
            tilEmail.setError(null);
            tilAddress.setError(null);
            tilDOB.setError(null);
            tilMobileNumber.setError(null);
            tilPassword.setError(null);
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };


    private String getTodaysDate(){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month  = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private void initDatePicker(){
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
               month = month +1;
               String date = makeDateString(day, month, year);
               dropdown_dob.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    private String makeDateString(int day, int month, int year){
        return month + " " + day + " " + year;
    }

    // register User using the credentials given
    private void signupUser(String textFirstName, String textMiddle, String textLastName, String textEmail, String textMobileNumber, String textPassword, String textDOB, String textAddress, String textCity,
                            String textGender, String userType, String imageURL, String textToken, String textDateCreated){


        // Create UserProfile
        auth.createUserWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener(RegisterSenior.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            progressBar.setVisibility(View.VISIBLE);

                            // enter user data into the firebase realtime database
                            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textFirstName, textMiddle, textLastName,
                                    textEmail, textMobileNumber, textPassword, textDOB, textAddress, textCity, textGender, userType,
                                    imageURL, textToken, textDateCreated);

                            // extracting user reference from database for "registered user"
                            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users").child("Seniors");
                            FirebaseUser firebaseUser = auth.getCurrentUser();

                            // store profile picture of carer
                            storageReference = FirebaseStorage.getInstance().getReference("ProfilePics");

                            referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    // If user change upload profile pic
                                    if(uriImage != null){
                                        // save the image
                                        StorageReference fileReference = storageReference.child(firebaseUser.getUid() + "."
                                                + getFileExtension(uriImage));

                                        fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {

                                                        Log.d(TAG_1, "Download URL = "+ uri.toString());

                                                       // seniorImage = uri;

                                                        //Adding that URL to Realtime database
                                                        referenceProfile.child(firebaseUser.getUid()).child("imageURL").setValue(uri.toString());

                                                        // finally set the display image of the user after upload
                                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                                .setPhotoUri(uri).build();
                                                        firebaseUser.updateProfile(profileUpdates);

                                                        DatabaseReference referenceCarerModule = FirebaseDatabase.getInstance().getReference().child("AssignedSeniors");
                                                        HashMap<String,Object> hashMap = new HashMap<>();
                                                        hashMap.put("seniorID", firebaseUser.getUid());
                                                        hashMap.put("firstName", textFirstName);
                                                        hashMap.put("middle", textMiddle);
                                                        hashMap.put("lastName", textLastName);
                                                        hashMap.put("barangay", textAddress);
                                                        hashMap.put("dob", textDOB);
                                                        hashMap.put("city", textCity);
                                                        hashMap.put("image",uri.toString());

                                                        String key = referenceCarerModule.push().getKey();

                                                        referenceCarerModule
                                                                .child(textCarerID)
                                                                .child(key)
                                                                .setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            Log.w("Assigned", "Assigned Successfully", task.getException());
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });

                                            }
                                        });
                                    }

                                    // if all inputs are valid
                                    if(task.isSuccessful()){
                                        // send verification email
                                        firebaseUser.sendEmailVerification();

                                        // send mobile intent
                                        Intent intent = new Intent(RegisterSenior.this, OTP.class);
                                        Bundle data = new Bundle();
                                        data.putString("Mobile", textMobileNumber);
                                        intent.putExtras(data);
                                        startActivity(intent);
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                       //auth.signOut();
                                    }else{
                                        Toast.makeText(RegisterSenior.this, "User registered failed. Please try again",
                                                Toast.LENGTH_LONG).show();
                                    }
                                    finish();
                                }
                            });
                        }else{
                            try{
                                throw task.getException();
                            }catch(FirebaseAuthWeakPasswordException e){
                                promptMessage.displayMessage("Password is too weak","Password must contain at least 6 characters, 1 digit, and 1 upper case", R.color.red1, RegisterSenior.this);
                                etPassword.requestFocus();
                            }catch(FirebaseAuthInvalidCredentialsException e){
                                promptMessage.displayMessage("Invalid Email","Your email is invalid or already in use. Kindly re-enter", R.color.red1, RegisterSenior.this);
                                etPassword.requestFocus();
                            }catch(FirebaseAuthUserCollisionException e){
                                promptMessage.displayMessage("Email already taken","Please use another email", R.color.red1, RegisterSenior.this);
                                etPassword.requestFocus();
                            }catch(Exception e){
                                Log.e(TAG_1, e.getMessage());
                                Toast.makeText(RegisterSenior.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null & data.getData() != null){
            uriImage = data.getData();
            Picasso.get()
                    .load(uriImage)
                    //.fit()
                    .transform(new CropCircleTransformation())
                    .into(ivProfilePic);
        }
    }

    // obtain file extension of the image
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}