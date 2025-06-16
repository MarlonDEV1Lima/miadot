package com.company.miadot.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.company.miadot.R;
import androidx.appcompat.app.AppCompatActivity;
import com.company.miadot.fragments.LoginFragment;
import com.company.miadot.fragments.RegisterFragment;
import com.google.firebase.database.annotations.Nullable;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return position == 0 ? new LoginFragment() : new RegisterFragment();
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });
    }
}
