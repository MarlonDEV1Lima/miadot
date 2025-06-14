package com.company.miadot;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "dwgh9zkyj");
        config.put("api_key", "833194418889222");
        config.put("api_secret", "cOZhAM5b_xJBjMlu0H9t2_6Cpgk");

        MediaManager.init(this, config);
    }
}
