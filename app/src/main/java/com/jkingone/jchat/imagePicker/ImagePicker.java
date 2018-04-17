package com.jkingone.jchat.imagePicker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jkingone on 2018/3/20.
 */

public final class ImagePicker {
    private static volatile ImagePicker instance = null;
    private List<String> imagePaths = new ArrayList<>();

    private ImagePicker() {}

    public static ImagePicker getInstance() {
        if (instance == null) {
            synchronized (ImagePicker.class) {
                if (instance == null) {
                    instance = new ImagePicker();
                }
            }
        }
        return instance;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> paths) {
        imagePaths = paths;
    }
}
