package com.jkingone.jchat.imagePicker;

import com.jkingone.jchat.imagePicker.entity.ImageSet;

import java.util.List;

/**
 * <b>Listener when data ready</b><br/>
 * Created by Eason.Lai on 2015/11/1 10:42 <br/>
 * contact：easonline7@gmail.com <br/>
 */
public interface OnImagesLoadedListener {
    void onImagesLoaded(List<ImageSet> imageSetList);
}
