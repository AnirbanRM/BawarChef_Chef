package com.bawarchef.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bawarchef.android.Fragments.MyProfile;
import com.yalantis.ucrop.UCrop;

public class SpecialityCardActivity extends Activity {

    SpecialityItem si;
    ImageView img;
    EditText text;

    boolean toCropper = false;

    ImageButton del,cam,stor;
    ImagePicker imagePicker;

    Bitmap bmp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_speciality_card);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        img = findViewById(R.id.sp_img);
        del = findViewById(R.id.del);
        cam = findViewById(R.id.cam);
        stor = findViewById(R.id.stor);
        text = findViewById(R.id.sp_text);

        text.addTextChangedListener(textchanged);

        cam.setOnClickListener(camclicked);
        stor.setOnClickListener(storclicked);;

        si = (SpecialityItem)ThisApplication.sharableObject.get(0);

        setData();
    }

    TextWatcher textchanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            si.setCaption(text.getText().toString());
        }
    };

    View.OnClickListener camclicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toCropper=true;
            imagePicker = new ImagePicker(ImagePicker.Source.CAMERA, SpecialityCardActivity.this, 100,16,9);
            imagePicker.fetchImageByRequestCode();
        }
    };

    View.OnClickListener storclicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toCropper = true;
            imagePicker = new ImagePicker(ImagePicker.Source.INTERNAL, SpecialityCardActivity.this, 200,16,9);
            imagePicker.fetchImageByRequestCode();
        }
    };

    private void setData() {
        if(si.getBmp()!=null)
            img.setImageBitmap(si.getBmp());
        if(si.getCaption()!=null)
            text.setText(si.getCaption());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            imagePicker.fromCamera();
        }
        else if (requestCode == 200) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    imagePicker.fromInternal(data);
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            try {
                bmp = MediaStore.Images.Media.getBitmap(SpecialityCardActivity.this.getContentResolver(), UCrop.getOutput(data));
                img.setImageBitmap(bmp);
                si.setBmp(bmp);
                toCropper = false;
            }catch(Exception e){}
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!toCropper) {
            Intent i = new Intent();
            i.putExtra("ACTION", "");
            setResult(Activity.RESULT_OK, i);
            finish();
        }
    }
}
