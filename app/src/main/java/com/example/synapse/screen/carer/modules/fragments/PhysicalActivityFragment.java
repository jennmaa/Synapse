package com.example.synapse.screen.carer.modules.fragments;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.synapse.R;
import com.example.synapse.screen.carer.CarerMainActivity;
import com.example.synapse.screen.carer.modules.view.ViewPhysicalActivity;
import com.example.synapse.screen.util.AuditTrail;
import com.example.synapse.screen.util.PromptMessage;
import com.example.synapse.screen.util.ReplaceFragment;
import com.example.synapse.screen.util.TimePickerFragment;
import com.example.synapse.screen.util.adapter.ItemPhysicalActivityAdapter;
import com.example.synapse.screen.util.notifications.AlertReceiver;
import com.example.synapse.screen.util.notifications.FcmNotificationsSender;
import com.example.synapse.screen.util.readwrite.ReadWritePhysicalActivity;
import com.example.synapse.screen.util.readwrite.ReadWriteUserDetails;
import com.example.synapse.screen.util.viewholder.PhysicalActivityViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.aviran.cookiebar2.CookieBar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhysicalActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhysicalActivityFragment extends Fragment implements AdapterView.OnItemSelectedListener, TimePickerDialog.OnTimeSetListener {

    FirebaseUser mUser;
    DatabaseReference referenceReminders;
    DatabaseReference referenceCarer;

    PromptMessage promptMessage = new PromptMessage();
    ReplaceFragment replaceFragment = new ReplaceFragment();
    AuditTrail auditTrail = new AuditTrail();

    FloatingActionButton fabAddPhysicalActivity;
    AppCompatEditText etDuration;
    AppCompatButton btnMon;
    AppCompatButton btnTue;
    AppCompatButton btnWed;
    AppCompatButton btnThu;
    AppCompatButton btnFri;
    AppCompatButton btnSat;
    AppCompatButton btnSun;
    AppCompatButton btnAddSchedule;

    String token;
    String time;
    String type_of_activity;
    String seniorID;
    String clickedRepeatBtn;

    MaterialButtonToggleGroup toggleGroup;
    Button btnAll;
    Button btnDone;
    Button btnNotDone;

    RequestQueue requestQueue;
    RecyclerView recyclerView;
    Query query;
    int requestCode;
    int count = 0;
    Dialog dialog;

    boolean isClicked = false;
    ImageView profilePic;
    final Calendar calendar = Calendar.getInstance();
    final String[] physical_activity = {"Stretching", "Walking","Yoga","Aerobics"};
    GifImageView gifImageView;
    MaterialCardView btn2hoursRepeat, btn4hoursRepeat, btnOnceADay, btnNever;
    TextView tvTime, tv2hours,tv4hours,tvOnceADay, tvNever;
    LinearLayoutManager mLayoutManager;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PhysicalActivityFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PhysicalActivityFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhysicalActivityFragment newInstance(String param1, String param2) {
        PhysicalActivityFragment fragment = new PhysicalActivityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_carer_physical_activity, container, false);

        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_dialog_box_add_physical_activity);
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(getActivity(), R.drawable.dialog_background2));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.getWindow().getAttributes().gravity = Gravity.BOTTOM;
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation1;

        referenceReminders = FirebaseDatabase.getInstance().getReference("Physical Activity Reminders");
        referenceCarer = FirebaseDatabase.getInstance().getReference("Users").child("Carers");
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        requestQueue = Volley.newRequestQueue(getActivity());

        // listen for broadcast
        //getActivity().registerReceiver(broadcastReceiver, new IntentFilter("NOTIFY_PHYSICAL_ACTIVITY"));

        // variables for dialog
        MaterialButton btnAdd = dialog.findViewById(R.id.ibAdd);
        MaterialButton btnMinus = dialog.findViewById(R.id.ibMinus);
        btnAddSchedule = dialog.findViewById(R.id.btnAddSchedule);
        tvTime = dialog.findViewById(R.id.tvTime);
        etDuration = dialog.findViewById(R.id.etDuration);
        AppCompatImageButton ibTimePicker = dialog.findViewById(R.id.ibTimePicker);
        gifImageView = dialog.findViewById(R.id.gifImage);
        btn2hoursRepeat = dialog.findViewById(R.id.repeat2hours);
        btn4hoursRepeat = dialog.findViewById(R.id.repeat4hours);
        btnOnceADay = dialog.findViewById(R.id.repeatOnceADay);
        btnNever = dialog.findViewById(R.id.repeatNever);
        tv2hours = dialog.findViewById(R.id.tv2hours);
        tv4hours = dialog.findViewById(R.id.tv4hours);
        tvOnceADay = dialog.findViewById(R.id.tvOnceADay);
        tvNever = dialog.findViewById(R.id.tvRepeatNever);

        // variables for view
        profilePic = view.findViewById(R.id.ivCarerProfilePic);
        ImageButton ibBack = view.findViewById(R.id.ibBack);
        btnMon = view.findViewById(R.id.btnMON);
        btnTue = view.findViewById(R.id.btnTUE);
        btnWed = view.findViewById(R.id.btnWED);
        btnThu = view.findViewById(R.id.btnTHU);
        btnFri = view.findViewById(R.id.btnFRI);
        btnSat = view.findViewById(R.id.btnSAT);
        btnSun = view.findViewById(R.id.btnSUN);

        // show status bar
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        etDuration.setShowSoftInputOnFocus(false);
        ibBack.setOnClickListener(v -> startActivity(new Intent(getActivity(), CarerMainActivity.class)));
        Spinner spinner_physical_activity = dialog.findViewById(R.id.spinner_physical_activity);
        ItemPhysicalActivityAdapter adapter = new ItemPhysicalActivityAdapter(getActivity(), physical_activity);
        adapter.notifyDataSetChanged();
        spinner_physical_activity.setAdapter(adapter);
        spinner_physical_activity.setOnItemSelectedListener(this);

        showUserProfile();
        loadScheduleForPhysicalActivity();
        onclickRepeatButtons();
        displayCurrentDay();
        addButton();

        btnMinus.setOnClickListener(this::decrement);
        btnAdd.setOnClickListener(this::increment);
        ibTimePicker.setOnClickListener(v -> {
            DialogFragment timePicker = new TimePickerFragment(this::onTimeSet);
            timePicker.show(getChildFragmentManager(), "time picker");
            isClicked = true;
        });

        toggleGroup = view.findViewById(R.id.toggleButtonGroup);
        btnAll = view.findViewById(R.id.btnAll);
        btnDone = view.findViewById(R.id.btnDone);
        btnNotDone = view.findViewById(R.id.btnNotDone);

        return view;
    }

    // onclick listener for repeat buttons
    void onclickRepeatButtons(){
        btn2hoursRepeat.setOnClickListener(v -> {
            clickedRepeatBtn = "2hours";
            displayClickedRepeatButton(btn2hoursRepeat, btn4hoursRepeat, btnOnceADay, btnNever, tv2hours, tv4hours, tvOnceADay, tvNever);
        });
        btn4hoursRepeat.setOnClickListener(v -> {
            clickedRepeatBtn = "4hours";
            displayClickedRepeatButton(btn4hoursRepeat, btn2hoursRepeat, btnOnceADay, btnNever, tv4hours, tv2hours, tvOnceADay, tvNever);
        });
        btnOnceADay.setOnClickListener(v -> {
            clickedRepeatBtn = "OnceADay";
            displayClickedRepeatButton(btnOnceADay, btn2hoursRepeat, btn4hoursRepeat, btnNever, tvOnceADay, tv2hours, tv4hours, tvNever);
        });
        btnNever.setOnClickListener(v -> {
            clickedRepeatBtn = "Never";
            displayClickedRepeatButton(btnNever, btn2hoursRepeat, btn4hoursRepeat, btnOnceADay, tvNever, tv2hours, tv4hours, tvOnceADay);
        });
    }

    // change gif based on selected item on spinner
    public void displayPhysicalActivity(int gif1){
        new Handler().postDelayed(() -> {
            gifImageView.setImageResource(gif1);
        }, 200);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // layout for recycle view
        recyclerView = view.findViewById(R.id.recyclerview_physical_activity);
        mLayoutManager = new LinearLayoutManager(requireActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        // display dialog after floating action button was clicked
        fabAddPhysicalActivity = view.findViewById(R.id.btnAddPhysicalActivity);
        fabAddPhysicalActivity.setOnClickListener(v -> dialog.show());
    }

    // prevent error when using back pressed inside fragment
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                this.setEnabled(false);
                replaceFragment.replaceFragment(new HomeFragment(), getActivity());
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    // check what item was selected in spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.spinner_physical_activity){
            type_of_activity = physical_activity[position];
            if(type_of_activity.equals(physical_activity[0])){
                displayPhysicalActivity(R.drawable.stretch5);
            }else if(type_of_activity.equals(physical_activity[1])){
                displayPhysicalActivity(R.drawable.walking);
            }else if(type_of_activity.equals(physical_activity[2])){
                displayPhysicalActivity(R.drawable.yoga1);
            }else{
                displayPhysicalActivity(R.drawable.aerobics1);
            }
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // get the selected time
    Calendar getCalendar(){
        return calendar;
    }

    // increment for dose input
    public void increment(View v) {
        count++;
        etDuration.setText("");
        etDuration.setText("" + count + " minutes");
    }

    // decrement for dose input
    public void decrement(View v) {
        if (count <= 0) count = 0;
        else count--;
        etDuration.setText("");
        etDuration.setText("" + count + " minutes");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        updateTimeText(calendar);
    }

    // update the textview after selecting the time
    private void updateTimeText(Calendar calendar) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        tvTime.setText(simpleDateFormat.format(calendar.getTime()));
        time = simpleDateFormat.format(calendar.getTime());
    }

    // we need to destroy the broadcast if we register one
   // @Override
   // public void onDestroy() {
   //     super.onDestroy();
   //     getActivity().unregisterReceiver(broadcastReceiver);
   // }

    void recycleviewPhysicalActivity(Query query){
        FirebaseRecyclerOptions<ReadWritePhysicalActivity> options = new FirebaseRecyclerOptions.Builder<ReadWritePhysicalActivity>().setQuery(query, ReadWritePhysicalActivity.class).build();
        FirebaseRecyclerAdapter<ReadWritePhysicalActivity, PhysicalActivityViewHolder> adapter = new FirebaseRecyclerAdapter<ReadWritePhysicalActivity, PhysicalActivityViewHolder>(options) {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull PhysicalActivityViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull ReadWritePhysicalActivity model) {

                String activity = model.getActivity();
                String isDone = model.getIsDone();

                switch (activity) {
                    case "Stretching":
                        holder.ic_activity.setBackground(AppCompatResources.getDrawable(getActivity(),R.drawable.ic_bg_stretching));
                        break;
                    case "Walking":
                        holder.ic_activity.setBackground(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_bg_walking));
                        break;
                    case "Yoga":
                        holder.ic_activity.setBackground(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_bg_yoga));
                        break;
                    case "Aerobics":
                        holder.ic_activity.setBackground(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_bg_aerobics));
                        break;
                }

                holder.name.setText(model.getActivity());
                holder.duration.setText("Duration: " + model.getDuration());
                holder.time.setText(model.getTime());

                if(isDone.equals("Done")){
                    holder.ivIsDone.setBackground(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_is_taken));
                }

                // send the key to another activity
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ViewPhysicalActivity.class);
                        intent.putExtra("userKey", getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }
            @NonNull
            @Override
            public PhysicalActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_carer_physical_activity_schedule, parent, false);
                return new PhysicalActivityViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }


    private void loadScheduleForPhysicalActivity() {
        referenceReminders.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ignored : snapshot.getChildren()) {
                        for (DataSnapshot ds2 : snapshot.getChildren()) {
                            query = ds2.getRef();
                            recycleviewPhysicalActivity(query);
                            int buttonID = toggleGroup.getCheckedButtonId();
                            toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                                @Override
                                public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                                    if(isChecked){
                                        if(checkedId == R.id.btnAll){
                                            query = ds2.getRef();
                                            recycleviewPhysicalActivity(query);
                                        }else if(checkedId == R.id.btnDone){
                                            query = ds2.getRef().orderByChild("IsDone").equalTo("Done");
                                            recycleviewPhysicalActivity(query);
                                        }else if(checkedId == R.id.btnNotDone){
                                            query = ds2.getRef().orderByChild("IsDone").equalTo("Not Done");
                                            recycleviewPhysicalActivity(query);
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                promptMessage.defaultErrorMessage(getActivity());
            }
        });
    }

    // set the alarm manager and listen for broadcast
    private void startAlarm(Calendar c, String key) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), AlertReceiver.class);
        intent.putExtra("PhysicalActivity", 2);
        intent.putExtra("physical_id", key);
        intent.putExtra("request_code", requestCode);

        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent
                    .getBroadcast(getActivity(),
                            requestCode,
                            intent,
                            PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ONE_SHOT);
        } else {
            pendingIntent = PendingIntent
                    .getBroadcast(getActivity(),
                            requestCode,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
        }

        //check whether the time is earlier than current time. If so, set it to tomorrow. Otherwise, all alarms for earlier time will fire
        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }

        switch (clickedRepeatBtn) {
            case "OnceADay":
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                break;
            case "Never":
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                break;
            case "2hours":
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent);
                break;
        }

        // set alarm for everyday
        // alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
        //         calendar.getTimeInMillis(),
        //         AlarmManager.INTERVAL_DAY,
        //         pendingIntent);

    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    // store schedule for medicine
    void addSchedule() {
        requestCode = (int) getCalendar().getTimeInMillis()/1000;
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("Activity", type_of_activity);
        hashMap.put("Duration", Objects.requireNonNull(etDuration.getText()).toString());
        hashMap.put("IsDone", "Not Done");
        hashMap.put("Time", time);
        hashMap.put("RepeatMode", clickedRepeatBtn);
        hashMap.put("RequestCode", requestCode);

        String key = referenceReminders.push().getKey();
        referenceReminders
                .child(getDefaults("seniorKey",getActivity()))
                .child(mUser.getUid())
                .child(key)
                .setValue(hashMap).addOnCompleteListener(new OnCompleteListener() {

             @Override
             public void onComplete(@NonNull Task task) {
                 if (task.isSuccessful()) {
                     referenceReminders
                             .child(mUser.getUid())
                             .child(getDefaults("seniorKey",getActivity()))
                             .child(key)
                             .setValue(hashMap).addOnCompleteListener(task1 -> {

                     auditTrail.auditTrail(
                             "Added Physical Activity",
                             type_of_activity,
                             "Physical Activity", "Carer", referenceCarer, mUser);

                         if (task1.isSuccessful()) {
                             dialog.dismiss();
                             tvTime.setText("Add New Physical Activity");
                             etDuration.setText("");
                             Toast.makeText(getActivity(), "Alarm has been set", Toast.LENGTH_SHORT).show();
                             // start alarm and retrieve the unique id of newly created medicine
                             // so we can send it to alert receiver.
                             startAlarm(calendar, key);
                         }
                     });
                 }
             }
         });

    }

    // store schedule when add button was clicked
    void addButton(){
        btnAddSchedule.setOnClickListener(v -> {
         String duration = etDuration.getText().toString();
         if (TextUtils.isEmpty(duration)) {
             //promptMessage.displayMessage("Empty field", "Please enter the duration of the physical activity",R.color.red_decline_request, getActivity());
             Toast.makeText(getActivity(), "Please enter the duration of the physical activity", Toast.LENGTH_SHORT).show();
         }else if(!isClicked){
             //promptMessage.displayMessage("Empty field", "Please pick a schedule for the physical activity",R.color.red_decline_request, getActivity());
             Toast.makeText(getActivity(), "Please pick a schedule for the physical activity", Toast.LENGTH_SHORT).show();
         }else if(clickedRepeatBtn == null){
             //promptMessage.displayMessage("Choose repetition", "Please pick a repetition for the physical activity", R.color.dark_green, getActivity());
             Toast.makeText(getActivity(), "Please pick a repetition for the physical activity", Toast.LENGTH_SHORT).show();
         }
         else {
             addSchedule();
             FragmentTransaction ft = getFragmentManager().beginTransaction();
             if (Build.VERSION.SDK_INT >= 26) {ft.setReorderingAllowed(false);}
             ft.detach(PhysicalActivityFragment.this).attach(PhysicalActivityFragment.this).commit();
         }
     });
   }

    // change the background the current day to white
    public void displayCurrentDay(){
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
        Calendar calendar = Calendar.getInstance();
        String day = dayFormat.format(calendar.getTime());
        switch (day){
            case "Sunday":
                btnSun.setBackgroundTintList(AppCompatResources.getColorStateList(getActivity(),R.color.white));
                break;
            case "Saturday":
                btnSat.setBackgroundTintList(AppCompatResources.getColorStateList(getActivity(),R.color.white));
                break;
            case "Monday":
                btnMon.setBackgroundTintList(AppCompatResources.getColorStateList(getActivity(),R.color.white));
                break;
            case "Tuesday":
                btnTue.setBackgroundTintList(AppCompatResources.getColorStateList(getActivity(),R.color.white));
                break;
            case "Wednesday":
                btnWed.setBackgroundTintList(AppCompatResources.getColorStateList(getActivity(),R.color.white));
                break;
            case "Thursday":
                btnThu.setBackgroundTintList(AppCompatResources.getColorStateList(getActivity(),R.color.white));
                break;
            case "Friday":
                btnFri.setBackgroundTintList(AppCompatResources.getColorStateList(getActivity(),R.color.white));
                break;
        }
    }

    // display carer's profile pic
    private void showUserProfile(){
        // display carer profile pic
        referenceCarer.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails userProfile = snapshot.getValue(ReadWriteUserDetails.class);
                if(userProfile != null){
                    Uri uri = mUser.getPhotoUrl();
                    Picasso.get()
                            .load(uri)
                            .transform(new CropCircleTransformation())
                            .into(profilePic);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                promptMessage.defaultErrorMessage(getActivity());
            }
        });
    }

    // change background of clicked repeat's button and textview
    public void displayClickedRepeatButton(
            MaterialCardView btnClicked, MaterialCardView btn1, MaterialCardView btn2,
            MaterialCardView btn3, TextView tvClicked, TextView tv1, TextView tv2, TextView tv3){
        btnClicked.setCardBackgroundColor(getResources().getColor(R.color.dark_violet));
        tvClicked.setTextColor(getResources().getColor(R.color.white));
        btn1.setCardBackgroundColor(getResources().getColor(R.color.grey2));
        btn2.setCardBackgroundColor(getResources().getColor(R.color.grey2));
        btn3.setCardBackgroundColor(getResources().getColor(R.color.grey2));
        tv1.setTextColor(getResources().getColor(R.color.grey1));
        tv2.setTextColor(getResources().getColor(R.color.grey1));
        tv3.setTextColor(getResources().getColor(R.color.grey1));
    }

    // listen if alarm is currently running so we can send notification to senior
   // BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
   //     @Override
   //     public void onReceive(Context context, Intent intent) {
   //         if (intent != null) {
   //             // if(clickedRepeatBtn.equals("OnceADay")){

   //             //     startAlarm(calendar, key);
   //             //     Intent intent_request_code = new Intent(getActivity(), ViewPhysicalActivity.class);
   //             //     intent_request_code.putExtra("new_request_code",requestCode);
   //             //     promptMessage.displayMessage("requestcode", "" + requestCode,  R.color.dark_green, getActivity());
   //             //     Toast.makeText(getActivity(), " " + requestCode, Toast.LENGTH_SHORT).show();
   //             // }
   //             referenceCompanion.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
   //                 @Override
   //                 public void onDataChange(@NonNull DataSnapshot snapshot) {
   //                     for (DataSnapshot ds : snapshot.getChildren()) {
   //                         seniorID = ds.getKey();

   //                         referenceProfile.child(seniorID).addValueEventListener(new ValueEventListener() {
   //                             @Override
   //                             public void onDataChange(@NonNull DataSnapshot snapshot) {
   //                                 ReadWriteUserDetails seniorProfile = snapshot.getValue(ReadWriteUserDetails.class);
   //                                 token = seniorProfile.getToken();
   //                                 FcmNotificationsSender notificationsSender = new FcmNotificationsSender(token,
   //                                         "Physical Activity Reminder",
   //                                         "It's time to do your physical activity",
   //                                         getActivity());
   //                                 notificationsSender.SendNotifications();
   //                             }
   //                             @Override
   //                             public void onCancelled(@NonNull DatabaseError error) {
   //                                 promptMessage.defaultErrorMessage(getActivity());
   //                             }
   //                         });
   //                     }
   //                 }
   //                 @Override
   //                 public void onCancelled(@NonNull DatabaseError error) {
   //                     promptMessage.defaultErrorMessage(getActivity());

   //                 }
   //             });
   //         }
   //     }
   // };


}