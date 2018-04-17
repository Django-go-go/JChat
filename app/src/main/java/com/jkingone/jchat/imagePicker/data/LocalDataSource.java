package com.jkingone.jchat.imagePicker.data;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.jkingone.jchat.imagePicker.OnImagesLoadedListener;
import com.jkingone.jchat.imagePicker.entity.ImageItem;
import com.jkingone.jchat.imagePicker.entity.ImageSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>DataSource of Android local Database</b><br/>
 */
public class LocalDataSource implements DataSource, LoaderManager.LoaderCallbacks<Cursor> {

    private final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media._ID };

    public static final int LOADER_ALL = 0;
    public static final int LOADER_CATEGORY = 1;

    private OnImagesLoadedListener imagesLoadedListener;
    private Context mContext;

    private ArrayList<ImageSet> mImageSetList = new ArrayList<>();

    @Override
    public void provideMediaItems(OnImagesLoadedListener loadedListener) {
        this.imagesLoadedListener = loadedListener;
        if(mContext instanceof FragmentActivity) {
            ((FragmentActivity)mContext).getSupportLoaderManager().initLoader(LOADER_ALL,null,this);
        }
    }

    public LocalDataSource(Context ctx){
        this.mContext = ctx;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == LOADER_ALL) {
            CursorLoader cursorLoader = new CursorLoader(mContext,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    null, null, IMAGE_PROJECTION[2] + " DESC");
            return cursorLoader;
        } else if(id == LOADER_CATEGORY) {
            CursorLoader cursorLoader = new CursorLoader(mContext,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    IMAGE_PROJECTION[0]+" like '%"+args.getString("path")+"%'", null, IMAGE_PROJECTION[2] + " DESC");
            return cursorLoader;
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mImageSetList.clear();
        if(data != null){
            List<ImageItem> allImages = new ArrayList<>();
            int count = data.getCount();
            if(count <= 0 ){
                return;
            }

            data.moveToFirst();
            do{
                String imagePath = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                String imageName = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                long imageAddedTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));

                ImageItem item = new ImageItem(imagePath,imageName,imageAddedTime);
                allImages.add(item);

                File imageFile = new File(imagePath);
                File imageParentFile = imageFile.getParentFile();

                ImageSet imageSet = new ImageSet();
                imageSet.name = imageParentFile.getName();
                imageSet.path = imageParentFile.getAbsolutePath();
                imageSet.cover = item;

                if(!mImageSetList.contains(imageSet)){
                    List<ImageItem> imageList = new ArrayList<>();
                    imageList.add(item);
                    imageSet.imageItems = imageList;
                    mImageSetList.add(imageSet);
                } else {
                    mImageSetList.get(mImageSetList.indexOf(imageSet)).imageItems.add(item);
                }

            }while(data.moveToNext());

            ImageSet imageSetAll = new ImageSet();
            imageSetAll.name= "所有";
            imageSetAll.cover = allImages.get(0);
            imageSetAll.imageItems = allImages;
            imageSetAll.path = "/";

            if(mImageSetList.contains(imageSetAll)){
                mImageSetList.remove(imageSetAll);//the first item_imagepick_grid is "all images"
            }
            mImageSetList.add(0,imageSetAll);

            imagesLoadedListener.onImagesLoaded(mImageSetList);//notify the data changed
        }

    }

    @Override public void onLoaderReset(Loader<Cursor> loader) { }


}
