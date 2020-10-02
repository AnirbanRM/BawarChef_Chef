package com.bawarchef.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;

public class ImagePicker {

    public static enum Source{CAMERA,INTERNAL};

    private static enum Mode{Activity,Fragment};

    Source src;
    Fragment fragment;
    Activity activity;
    Mode mode;

    int requestCode;

    Uri cropper_image_uri;
    int aspX,aspY;

    public ImagePicker(Source src, Fragment fragment, int reqcode,int aspX,int aspY){
        this.requestCode = reqcode;
        this.fragment = fragment;
        this.src = src;
        mode = Mode.Fragment;
        this.aspX = aspX;
        this.aspY = aspY;
    }

    public ImagePicker(Source src, Activity activity, int reqcode,int aspX,int aspY){
        this.requestCode = reqcode;
        this.activity = activity;
        this.src = src;
        mode = Mode.Activity;
        this.aspX = aspX;
        this.aspY = aspY;
    }

    public void fetchImageByRequestCode(){
        if(src==Source.INTERNAL) {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            if(mode==Mode.Fragment)
                fragment.startActivityForResult(Intent.createChooser(i, "Choose your picture"), requestCode);
            else if(mode==Mode.Activity)
                activity.startActivityForResult(Intent.createChooser(i, "Choose your picture"), requestCode);
        }

        else if(src==Source.CAMERA){
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = getImageFile();
            Uri uri = null;
            if(mode==Mode.Fragment)
                uri = FileProvider.getUriForFile(fragment.getActivity(), BuildConfig.APPLICATION_ID.concat(".provider"), file);
            else if(mode==Mode.Activity)
                uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID.concat(".provider"), file);
            i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            if(mode==Mode.Fragment)
                fragment.startActivityForResult(i , requestCode);
            else if(mode==Mode.Activity)
                activity.startActivityForResult(i , requestCode);
        }
    }

    String temp;

    private File getImageFile() {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";

        File storageDir=null;
        if(mode==Mode.Fragment)
            storageDir = new File(fragment.getActivity().getCacheDir(),"image");
        else if(mode==Mode.Activity)
            storageDir = new File(activity.getCacheDir(),"image");

        if(!storageDir.exists())storageDir.mkdirs();
        File file=null;
        try{
            file = File.createTempFile(imageFileName, ".jpg", storageDir);}catch(IOException e){ Log.e("TEST",e.toString());}
        temp = "file:" + file.getAbsolutePath();
        return file;
    }

    public void fromCamera() {
        Log.e("LOL","Lil0");
        Uri uri = Uri.parse(temp);
        File file = getImageFile();
        cropper_image_uri = Uri.fromFile(file);
        UCrop.Options opts = new UCrop.Options();
        opts.setCircleDimmedLayer(true);
        opts.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        if(mode==Mode.Fragment)
            opts.setToolbarColor(fragment.getActivity().getColor(R.color.button_color));
        if(mode==Mode.Activity)
            opts.setToolbarColor(activity.getColor(R.color.button_color));
        opts.setToolbarWidgetColor(Color.WHITE);
        opts.withMaxResultSize(500,500);
        opts.setCompressionQuality(100);
        opts.withAspectRatio(aspX,aspY);
        opts.setToolbarTitle("Crop your image");
        if(mode==Mode.Fragment)
            opts.setRootViewBackgroundColor(fragment.getActivity().getColor(R.color.button_color));
        else if(mode==Mode.Activity)
            opts.setRootViewBackgroundColor(activity.getColor(R.color.button_color));
        opts.setStatusBarColor(Color.parseColor("#9907F5"));
        if(mode==Mode.Fragment)
            UCrop.of(uri,cropper_image_uri).withOptions(opts).start(fragment.getActivity(),fragment);
        else if(mode==Mode.Activity)
            UCrop.of(uri,cropper_image_uri).withOptions(opts).start(activity);
    }

    public void fromInternal(@Nullable Intent data) {
        Uri srcuri =data.getData();
        File file = getImageFile();
        cropper_image_uri = Uri.fromFile(file);
        UCrop.Options opts = new UCrop.Options();
        opts.setCircleDimmedLayer(true);
        opts.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        if(mode==Mode.Fragment)
            opts.setToolbarColor(fragment.getActivity().getColor(R.color.button_color));
        else if(mode==Mode.Activity)
            opts.setToolbarColor(activity.getColor(R.color.button_color));
        opts.setToolbarWidgetColor(Color.WHITE);
        opts.withMaxResultSize(500,500);
        opts.setCompressionQuality(100);
        opts.withAspectRatio(aspX,aspY);
        opts.setToolbarTitle("Crop your image");
        if(mode==Mode.Fragment)
            opts.setRootViewBackgroundColor(fragment.getActivity().getColor(R.color.button_color));
        else if(mode==Mode.Activity)
            opts.setToolbarColor(activity.getColor(R.color.button_color));
        opts.setStatusBarColor(Color.parseColor("#9907F5"));

        if(mode==Mode.Fragment)
            UCrop.of(srcuri,cropper_image_uri).withOptions(opts).start(fragment.getActivity(),fragment);
        else if(mode==Mode.Activity)
            UCrop.of(srcuri,cropper_image_uri).withOptions(opts).start(activity);
    }
}
