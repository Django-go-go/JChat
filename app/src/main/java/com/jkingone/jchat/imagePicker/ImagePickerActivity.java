package com.jkingone.jchat.imagePicker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jkingone.jchat.R;
import com.jkingone.jchat.imagePicker.data.DataSource;
import com.jkingone.jchat.imagePicker.data.LocalDataSource;
import com.jkingone.jchat.imagePicker.entity.ImageItem;
import com.jkingone.jchat.imagePicker.entity.ImageSet;
import com.jkingone.jchat.utils.ScreenUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImagePickerActivity extends AppCompatActivity {

    private static final String TAG = "ImagePickerActivity";
    private static final int REQUEST_TAKE_PHOTO = 10;
    private static final int REQUEST_VIDEO_CAPTURE = 20;
    private static final int PHOTO_PREVIEW = 30;
    private static final int IMAGE_PREVIEW = 40;

    public static final String IMAGE_MODE = "image";
    public static final String PHOTO_MODE = "photo";

    private RecyclerView mRecyclerView;
    private ListPopupWindow mFolderPopupWindow;

    private RelativeLayout topBar;
    private TextView mTextView_ok;
    private Button mButton_dir;

    private List<String> pathList = new ArrayList<>();
    private List<ImageSet> mImageSets = new ArrayList<>();

    private RecycleAdapter mMyAdapter;

    private DataSource mDataSource;

    private int posFolder = -1;

    private boolean isSingle = true;

    private List<String> resultList = new ArrayList<>();
    private List<String> posList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagepicker);

        hasPermission();

        initView();

    }

    private void initView() {
        topBar = (RelativeLayout) findViewById(R.id.top_bar);
        mTextView_ok = (TextView) findViewById(R.id.btn_ok);
        mTextView_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String p : posList) {
                    resultList.add(pathList.get(Integer.parseInt(p) - 1));
                }
                Intent intent = new Intent();
                intent.putStringArrayListExtra("data", (ArrayList<String>) resultList);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mButton_dir = (Button) findViewById(R.id.btn_dir);
        mButton_dir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFolderPopupWindow == null) {
                    createPopupFolderList(ScreenUtils.getScreenWidth(ImagePickerActivity.this), 
                            ScreenUtils.getScreenHeight(ImagePickerActivity.this) / 2);
                } else {
                    mFolderPopupWindow.show();
                }
            }
        });
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycleView_imagePicker);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        if (mMyAdapter == null) {
            mMyAdapter = new RecycleAdapter();
        }
        mRecyclerView.setAdapter(mMyAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent();
        String mode = intent.getStringExtra("MODE");
        if (PHOTO_MODE.equals(mode)) {
            dispatchTakePictureIntent();
            return;
        }

        mDataSource = new LocalDataSource(this);
        mDataSource.provideMediaItems(new OnImagesLoadedListener() {
            @Override
            public void onImagesLoaded(List<ImageSet> imageSetList) {
                mImageSets.clear();
                mImageSets = imageSetList;

                pathList.clear();
                for (ImageSet imageSet : imageSetList) {
                    for (ImageItem imageItem : imageSet.imageItems) {
                        pathList.add(imageItem.path);
                    }
                }
                ImagePicker.getInstance().setImagePaths(pathList);
                if (mMyAdapter == null) {
                    mMyAdapter = new RecycleAdapter();
                } else {
                    mMyAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private class RecycleAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(ImagePickerActivity.this).inflate(R.layout.item_imagepicker, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ViewHolder viewHolder = (ViewHolder) holder;
            final int pos = position - 1;
            final String strPos = String.valueOf(position);
            if (position == 0) {
                Picasso.with(ImagePickerActivity.this)
                        .load(R.drawable.ic_camera)
                        .centerCrop()
                        .resize(ScreenUtils.getScreenWidth(ImagePickerActivity.this) / 3, ScreenUtils.getScreenWidth(ImagePickerActivity.this) / 3)
                        .placeholder(R.drawable.default_img)
                        .error(R.drawable.default_img)
                        .into(viewHolder.mImageView);
                viewHolder.mCheckBox.setVisibility(View.INVISIBLE);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dispatchTakePictureIntent();
                    }
                });
            } else {
                viewHolder.mCheckBox.setVisibility(View.VISIBLE);
                Picasso.with(ImagePickerActivity.this)
                        .load(new File(pathList.get(pos)))
                        .centerCrop()
                        .resize(ScreenUtils.getScreenWidth(ImagePickerActivity.this) / 3, ScreenUtils.getScreenWidth(ImagePickerActivity.this) / 3)
                        .placeholder(R.drawable.default_img)
                        .error(R.drawable.default_img)
                        .into(viewHolder.mImageView);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCurrentImagePath = pathList.get(pos);
                        Intent intent = new Intent(ImagePickerActivity.this, PreviewActivity.class);
                        intent.putExtra("image", pos);
                        startActivityForResult(intent, IMAGE_PREVIEW);
//                        if (viewHolder.mCheckBox.isChecked()) {
//                            viewHolder.mCheckBox.setChecked(false);
//                        } else {
//                            viewHolder.mCheckBox.setChecked(true);
//                        }
                    }
                });
            }



            viewHolder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (!posList.contains(strPos)) {
                            posList.add(strPos);
                        }
                    } else {
                        if (posList.contains(strPos)) {
                            posList.remove(strPos);
                        }
                    }
                }
            });

            if (posList.contains(strPos)) {
                viewHolder.mCheckBox.setChecked(true);
            } else {
                viewHolder.mCheckBox.setChecked(false);
            }
        }

        @Override
        public int getItemCount() {
            return pathList.size() + 1;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView mImageView;
            CheckBox mCheckBox;

            ViewHolder(View view) {
                super(view);
                mImageView = (ImageView) view.findViewById(R.id.image);
                mCheckBox = (CheckBox) view.findViewById(R.id.checkbox);
            }
        }
    }

    private void createPopupFolderList(int width, int height) {
        mFolderPopupWindow = new ListPopupWindow(this);
        mFolderPopupWindow.setAdapter(new ImageSetAdapter());
        mFolderPopupWindow.setContentWidth(width);
        mFolderPopupWindow.setWidth(width);
        mFolderPopupWindow.setHeight(height);
        mFolderPopupWindow.setAnchorView(topBar);
        mFolderPopupWindow.setModal(true);

        mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                pathList.clear();
                for (ImageItem item : mImageSets.get(i).imageItems) {
                    pathList.add(item.path);
                }
                ImagePicker.getInstance().setImagePaths(pathList);
                mMyAdapter.notifyDataSetChanged();
                mFolderPopupWindow.dismiss();
                mButton_dir.setText(mImageSets.get(i).name);
                posFolder = i;
            }
        });
        mFolderPopupWindow.show();
    }

    private class ImageSetAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mImageSets.size();
        }

        @Override
        public Object getItem(int i) {
            return mImageSets.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                view = LayoutInflater.from(ImagePickerActivity.this).inflate(R.layout.item_list_folder, viewGroup, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.indicator.setVisibility(View.INVISIBLE);
            holder.name.setText(mImageSets.get(i).name);
            holder.size.setText(String.valueOf(mImageSets.get(i).imageItems.size()));
            if (posFolder == i) {
                holder.indicator.setVisibility(View.VISIBLE);
            }

            Picasso.with(ImagePickerActivity.this)
                    .load(new File(mImageSets.get(i).cover.path))
                    .centerCrop()
                    .resize(50, 50)
                    .placeholder(R.drawable.default_img)
                    .error(R.drawable.default_img)
                    .into(holder.cover);

            posList.clear();

            return view;
        }

        class ViewHolder {
            ImageView cover;
            ImageView indicator;
            TextView name;
            TextView size;

            ViewHolder(View view) {
                cover = (ImageView) view.findViewById(R.id.cover);
                indicator = (ImageView) view.findViewById(R.id.indicator);
                name = (TextView) view.findViewById(R.id.name);
                size = (TextView) view.findViewById(R.id.size);
            }

        }
    }

    private void hasPermission() {

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if (PackageManager.PERMISSION_GRANTED
                != ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                && PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && permissions[1].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && permissions[2].equals(Manifest.permission.CAMERA)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                //获取缩略图 photo = (Bitmap) data.getExtras().get("data");
                galleryAddPic();
                Intent intent = new Intent(this, PreviewActivity.class);
                intent.putExtra("photo", mCurrentPhotoPath);
                startActivityForResult(intent, PHOTO_PREVIEW);
            }
        }

        if (requestCode == PHOTO_PREVIEW) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent();
                ArrayList<String> datas = new ArrayList<>();
                datas.add(mCurrentPhotoPath);
                intent.putExtra("data", datas);
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        if (requestCode == IMAGE_PREVIEW) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent();
                ArrayList<String> datas = new ArrayList<>();
                datas.add(mCurrentImagePath);
                intent.putExtra("data", datas);
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
        }
    }

    private String mCurrentImagePath;
    private String mCurrentPhotoPath;
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = new File(storageDir, imageFileName + ".jpg");

        mCurrentPhotoPath = image.getAbsolutePath();

        Log.i(TAG, "createImageFile: " + mCurrentPhotoPath);

        return image;
    }

    /**
     * 打开相机
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "dispatchTakePictureIntent: ", ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            } else {
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * 将图片添加到相册
     */
    private void galleryAddPic() {
        if (mCurrentPhotoPath != null) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        }
    }

    /**
     * 使用相机程序来录制视频
     */
    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

}
