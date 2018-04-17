package com.jkingone.jchat.imagePicker.entity;

import java.io.Serializable;

/**
 * Created by Eason.Lai on 2015/11/1 10:42
 * contact：easonline7@gmail.com
 */
public class ImageItem implements Serializable{
    public String path;
    public String name;
    public long time;

    public ImageItem(String path, String name, long time){
        this.path = path;
        this.name = name;
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        try {
            ImageItem other = (ImageItem) o;
            return this.path.equalsIgnoreCase(other.path) && this.time == other.time;
        }catch (ClassCastException e){
            e.printStackTrace();
        }
        return super.equals(o);
    }

}
