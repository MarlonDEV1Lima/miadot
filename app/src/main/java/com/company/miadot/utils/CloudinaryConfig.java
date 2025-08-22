package com.company.miadot.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.util.Map;

public class CloudinaryConfig {

    private static Cloudinary cloudinary;

    static {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dwgh9zkyj",
                "api_key", "833194418889222",
                "api_secret", "cOZhAM5b_xJBjMlu0H9t2_6Cpgk"
        ));
    }

    public static Map uploadImage(byte[] imageBytes) throws Exception {
        return cloudinary.uploader().upload(imageBytes, ObjectUtils.emptyMap());
    }
}
