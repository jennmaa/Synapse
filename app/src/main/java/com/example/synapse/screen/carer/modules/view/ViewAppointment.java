package com.example.synapse.screen.carer.modules.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.synapse.R;
import com.example.synapse.screen.util.AuditTrail;
import com.example.synapse.screen.util.PromptMessage;
import com.example.synapse.screen.util.readwrite.ReadWriteAppointment;
import com.example.synapse.screen.util.readwrite.ReadWriteMedication;
import com.example.synapse.screen.util.readwrite.ReadWritePhysicalActivity;
import com.example.synapse.screen.util.readwrite.ReadWriteUserDetails;
import com.example.synapse.screen.util.adapter.ItemAppointmentType;
import com.example.synapse.screen.util.adapter.ItemViewAppointmentSpecialist;
import com.example.synapse.screen.util.notifications.AlertReceiver;
import com.example.synapse.screen.util.notifications.FcmNotificationsSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import org.aviran.cookiebar2.CookieBar;
import org.checkerframework.checker.units.qual.A;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class ViewAppointment extends AppCompatActivity implements AdapterView.OnItemSelectedListener, TimePickerDialog.OnTimeSetListener {

    PromptMessage promptMessage = new PromptMessage();
    AuditTrail auditTrail = new AuditTrail();

    private FirebaseUser mUser;
    private DatabaseReference referenceReminders;
    private DatabaseReference referenceProfile;

    final String[] APPOINTMENT_SPECIALIST = {"Geriatrician","General Doctor","Cardiologist",
            "Rheumatologist","Urologist", "Ophthalmologist","Dentist","Psychologist","Audiologist"};
    Spinner spinner_appointment_specialist;

    private String time;
    private String selected_specialist;
    private String concern;
    private String drName;
    private String specialist;
    private String appointmentID;
    private String selected_appointment_type;

    TextView tvDateAndTime, tvDelete;
    TextInputEditText etConcern, etDrName;
    ItemViewAppointmentSpecialist adapter1;

    private Intent intent;
    private final Calendar calendar = Calendar.getInstance();
    private Long request_code;
    int code, requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointment);

        spinner_appointment_specialist = findViewById(R.id.spinner_specialist);
        ImageButton ibBack = findViewById(R.id.ibBack);
        MaterialButton btnChangeSchedule = findViewById(R.id.btnChangeSchedule);
        AppCompatButton btnUpdate = findViewById(R.id.btnUpdate);
        etConcern = findViewById(R.id.etConcern);
        etDrName = findViewById(R.id.etDrName);
        tvDateAndTime = findViewById(R.id.tvDateAndTime);
        tvDelete = findViewById(R.id.tvDelete);

        referenceReminders = FirebaseDatabase.getInstance().getReference("Appointment Reminders");
        referenceProfile = FirebaseDatabase.getInstance().getReference("Users").child("Carers");
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        adapter1 = new ItemViewAppointmentSpecialist(ViewAppointment.this,
                APPOINTMENT_SPECIALIST);
        adapter1.notifyDataSetChanged();
        spinner_appointment_specialist.setAdapter(adapter1);
        spinner_appointment_specialist.setOnItemSelectedListener(ViewAppointment.this);

        // we need  to check if user clicked the notification
        // then retrieve id first
        // so we can display the right information with the right key
        appointmentID = getIntent().getStringExtra( "userKey");
        String key = getIntent().getStringExtra("key");
        if(key != null) showAppointmentInfo(key);
        else showAppointmentInfo(appointmentID);

        tvDelete.setOnClickListener(v -> deleteAppointment());
        ibBack.setOnClickListener(v -> finish());

        btnUpdate.setOnClickListener(v -> updateAppointment(appointmentID));
        btnChangeSchedule.setOnClickListener(v -> {
            DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                }
            };
            new DatePickerDialog(ViewAppointment.this, dateSetListener,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.spinner_specialist){
            selected_specialist = APPOINTMENT_SPECIALIST[position];
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        updateTimeText(calendar);
    }

    private void updateTimeText(Calendar c) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("MMMM dd yyyy hh:mm a", Locale.ENGLISH);
        tvDateAndTime.setText(simpleDateFormat.format(calendar.getTime()));
        time = simpleDateFormat.format(calendar.getTime());
    }

    // set the alarm manager and listen for broadcast
    private void startAlarm(Calendar c) {
        requestCode = (int)calendar.getTimeInMillis()/1000;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(this, AlertReceiver.class);
        intent.putExtra("Appointment", 3);
        PendingIntent pendingIntent;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, 0);
        }
        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    public void cancelAlarm(int requestCode){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(this,requestCode, intent, 0);
        }
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public void showAppointmentInfo(String appointmentID){
        // userID >
        referenceReminders.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds1: snapshot.getChildren()){
                    String key1 = ds1.getKey();

                    // userID > seniorID
                    referenceReminders.child(mUser.getUid()).child(key1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds2: snapshot.getChildren()) {

                                // userID > seniorID > medicineID
                                referenceReminders.child(mUser.getUid()).child(key1).child(appointmentID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @SuppressLint("UseCompatLoadingForDrawables")
                                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            ReadWriteAppointment readWriteAppointment = snapshot.getValue(ReadWriteAppointment.class);
                                            adapter1.notifyDataSetChanged();

                                            time = readWriteAppointment.getTime();
                                            concern = readWriteAppointment.getConcern();
                                            drName = readWriteAppointment.getDrName();
                                            specialist = readWriteAppointment.getSpecialist();

                                            tvDateAndTime.setText(time);
                                            if(concern != null) etConcern.setText(concern);
                                            if(drName != null) etDrName.setText(drName);


                                            switch(specialist){
                                                case "Geriatrician":
                                                    spinner_appointment_specialist.setSelection(0);
                                                    break;
                                                case "General Doctor":
                                                    spinner_appointment_specialist.setSelection(1);
                                                    break;
                                                case "Cardiologist":
                                                    spinner_appointment_specialist.setSelection(2);
                                                    break;
                                                case "Rheumatologist":
                                                    spinner_appointment_specialist.setSelection(3);
                                                    break;
                                                case "Urologist":
                                                    spinner_appointment_specialist.setSelection(4);
                                                    break;
                                                case "Ophthalmologist":
                                                    spinner_appointment_specialist.setSelection(5);
                                                    break;
                                                case "Dentist":
                                                    spinner_appointment_specialist.setSelection(6);
                                                    break;
                                                case "Psychologist":
                                                    spinner_appointment_specialist.setSelection(7);
                                                    break;
                                                case "Audiologist":
                                                    spinner_appointment_specialist.setSelection(8);
                                                    break;
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        promptMessage.defaultErrorMessage(ViewAppointment.this);
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            promptMessage.defaultErrorMessage(ViewAppointment.this);
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                promptMessage.defaultErrorMessage(ViewAppointment.this);
            }
        });
    }

    // delete appointment for both carer and senior nodes
    public void deleteAppointment(){
            referenceReminders.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds1: snapshot.getChildren()){
                        String key = ds1.getKey();

                        referenceReminders.child(mUser.getUid()).child(key).child(appointmentID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                ReadWriteAppointment readWriteAppointment = snapshot.getValue(ReadWriteAppointment.class);
                                request_code = readWriteAppointment.getRequestCode();
                                code = request_code.intValue();

                                // cancel the alarm
                                cancelAlarm(code);

                                auditTrail.auditTrail(
                                        "Deleted Appointment Reminder",
                                        etDrName.getText().toString() + " - " + selected_specialist,
                                        "Appointment", "Carer", referenceProfile, mUser);

                                referenceReminders.child(mUser.getUid()).child(key).child(appointmentID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            referenceReminders.child(key).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for(DataSnapshot ds2: snapshot.getChildren()){
                                                        String appointment_key = ds2.getKey();

                                                        referenceReminders.child(key).child(mUser.getUid()).child(appointment_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                for(DataSnapshot ignored : snapshot.getChildren()){
                                                                    ReadWriteAppointment rw = snapshot.getValue(ReadWriteAppointment.class);
                                                                    Long request_code2 = rw.getRequestCode();

                                                                    if (request_code2.equals(request_code)) {
                                                                        referenceReminders.child(key).child(mUser.getUid()).child(appointment_key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                finish();
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                                promptMessage.defaultErrorMessage(ViewAppointment.this);
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    promptMessage.defaultErrorMessage(ViewAppointment.this);
                                                }
                                            });
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                promptMessage.defaultErrorMessage(ViewAppointment.this);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    promptMessage.defaultErrorMessage(ViewAppointment.this);
                }
            });
    }

    public void updateAppointment(String appointmentID){
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("Specialist", selected_specialist);
        hashMap.put("AppointmentType", selected_appointment_type);
        hashMap.put("Time", time);
        hashMap.put("DrName", Objects.requireNonNull(etDrName.getText()).toString());
        hashMap.put("Concern", Objects.requireNonNull(etConcern.getText()).toString());
        hashMap.put("RequestCode", requestCode);

        auditTrail.auditTrail(
                "Updated Appointment Reminder",
                etDrName.getText().toString() + " - " + selected_specialist,
                "Appointment", "Carer", referenceProfile, mUser);

        referenceReminders.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds1 : snapshot.getChildren()){
                    String seniorID = ds1.getKey();

                    referenceReminders.child(mUser.getUid()).child(seniorID).child(appointmentID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ignored : snapshot.getChildren()){
                                ReadWritePhysicalActivity rw1 = snapshot.getValue(ReadWritePhysicalActivity.class);
                                Long carer_request_code = rw1.getRequestCode();
                                int i = carer_request_code.intValue();

                                String time1 = rw1.getTime();
                                String time2 = tvDateAndTime.getText().toString();

                                if(!time1.equals(time2)){

                                    // if the alarm was updated by the user, then we need to cancel the old alarm
                                    cancelAlarm(i);

                                    // store the new requestCode
                                    hashMap.put("RequestCode", requestCode);

                                    // start the new alarm
                                    startAlarm(calendar);
                                }

                                referenceReminders.child(seniorID).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ds3 : snapshot.getChildren()){
                                            String senior_medicineID  = ds3.getKey();

                                            referenceReminders.child(seniorID).child(mUser.getUid()).child(senior_medicineID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    ReadWriteMedication rw2 = snapshot.getValue(ReadWriteMedication.class);
                                                    Long senior_request_code = rw2.getRequestCode();

                                                    // update both nodes
                                                    if(senior_request_code.equals(carer_request_code)){
                                                        referenceReminders.child(mUser.getUid()).child(seniorID).child(appointmentID).updateChildren(hashMap).addOnCompleteListener(task ->
                                                                referenceReminders.child(seniorID).child(mUser.getUid()).child(senior_medicineID).updateChildren(hashMap).addOnCompleteListener(task1 -> {
                                                                    adapter1.notifyDataSetChanged();
                                                                }));
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    promptMessage.defaultErrorMessage(ViewAppointment.this);
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        promptMessage.defaultErrorMessage(ViewAppointment.this);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            promptMessage.defaultErrorMessage(ViewAppointment.this);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                promptMessage.defaultErrorMessage(ViewAppointment.this);
            }
        });

        promptMessage.displayMessage("Physical Activity Info","Successfully updated the physical activity information", R.color.dark_green, this);
    }

}