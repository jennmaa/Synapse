package com.example.synapse.screen.carer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.synapse.R;
import com.example.synapse.databinding.ActivityCarerBottomNavigationBinding;
import com.example.synapse.screen.carer.modules.fragments.HomeFragment;
import com.example.synapse.screen.carer.modules.fragments.SettingsFragment;
import com.example.synapse.screen.carer.modules.fragments.UpdateProfileFragment;
import com.example.synapse.screen.carer.modules.view.SeniorLocation;
import com.example.synapse.screen.util.ReplaceFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.content.Intent;
import android.os.Bundle;

public class CarerMainActivity extends AppCompatActivity {

    ActivityCarerBottomNavigationBinding binding;
    ReplaceFragment replaceFragment = new ReplaceFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCarerBottomNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment.replaceFragment(new HomeFragment(), CarerMainActivity.this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        FloatingActionButton floatingActionButton = findViewById(R.id.fabLocateSenior);

        bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        floatingActionButton.setOnClickListener(v -> startActivity(new Intent(this, SeniorLocation.class)));

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch(item.getItemId()){
                case R.id.miHome:
                    replaceFragment.replaceFragment(new HomeFragment(), CarerMainActivity.this);
                    break;
                case R.id.miChat:
                    startActivity(new Intent(this, SelectSenior.class));
                    finish();
                    break;
                case R.id.miProfile:
                    replaceFragment.replaceFragment(new UpdateProfileFragment(), CarerMainActivity.this);
                    break;
                case R.id.miSettings:
                    replaceFragment.replaceFragment(new SettingsFragment(), CarerMainActivity.this);
                    break;
            }
            return true;
        });
    }

}