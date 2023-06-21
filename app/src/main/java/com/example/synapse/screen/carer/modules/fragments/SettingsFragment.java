package com.example.synapse.screen.carer.modules.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.synapse.R;
import com.example.synapse.screen.Login;
import com.example.synapse.screen.util.PromptMessage;
import com.example.synapse.screen.util.ReplaceFragment;
import com.example.synapse.screen.util.readwrite.ReadWriteUserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    // global variables
    FirebaseUser mUser;
    DatabaseReference referenceUser;

    ImageView ivUserPicture;
    ImageButton ibBack;

    TextView tvName;
    TextView tvEmail;
    TextView tvLogout;
    TextView tvChangePassword;
    TextView tvUpdateProfile;

    PromptMessage promptMessage = new PromptMessage();
    ReplaceFragment replaceFragment = new ReplaceFragment();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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

    void displayCarerInfo(){
        // display carer profile pic
        referenceUser.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails userProfile = snapshot.getValue(ReadWriteUserDetails.class);
                if(userProfile != null){
                    tvName.setText(userProfile.getFirstName());
                    tvEmail.setText(userProfile.getEmail());
                    Uri uri = mUser.getPhotoUrl();
                    Picasso.get()
                            .load(uri)
                            .transform(new CropCircleTransformation())
                            .into(ivUserPicture);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                promptMessage.defaultErrorMessage(getActivity());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_carer_settings, container, false);

        ivUserPicture = view.findViewById(R.id.ivCarerProfilePic);
        ibBack = view.findViewById(R.id.ibBack);
        tvName = view.findViewById(R.id.tvCarerName);
        tvEmail = view.findViewById(R.id.tvCarerEmail);
        tvLogout = view.findViewById(R.id.tvLogout);
        tvChangePassword = view.findViewById(R.id.tvChangePassword);
        tvUpdateProfile = view.findViewById(R.id.tvUpdateProfile);

        FirebaseAuth user = FirebaseAuth.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        referenceUser = FirebaseDatabase.getInstance().getReference("Users").child("Carers");

        // display carer information
        displayCarerInfo();

        // bring back the user
        ibBack.setOnClickListener(v -> replaceFragment.replaceFragment(new HomeFragment(), getActivity()));

        // change password
        tvChangePassword.setOnClickListener(v -> replaceFragment.replaceFragment(new ChangePassword(), getActivity()));

        // update profile
        tvUpdateProfile.setOnClickListener(v -> replaceFragment.replaceFragment(new UpdateProfileFragment(), getActivity()));

        // sign-out the user and redirect to login screen
        tvLogout.setOnClickListener(v -> {

            SharedPreferences myPrefs = getActivity().getSharedPreferences("seniorKey", Context.MODE_PRIVATE);
            myPrefs.edit().remove("seniorKey").apply();

           user.signOut();
            Intent intent = new Intent(getActivity(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().onBackPressed();
        });

        return view;
    }
}