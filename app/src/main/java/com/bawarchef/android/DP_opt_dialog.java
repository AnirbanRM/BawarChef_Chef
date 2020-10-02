package com.bawarchef.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

public class DP_opt_dialog extends Activity {

    ImageButton cam,sto,rem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dp_opt_dialog);
        cam = findViewById(R.id.camera);
        sto = findViewById(R.id.storage);
        rem = findViewById(R.id.remove);
        cam.setOnClickListener(clicked);
        sto.setOnClickListener(clicked);
        rem.setOnClickListener(clicked);

    }

    View.OnClickListener clicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String t = "";
            if(((ImageButton)v).getId()==cam.getId())
                t = "CAM";
            if(((ImageButton)v).getId()==sto.getId())
                t = "STO";
            if(((ImageButton)v).getId()==rem.getId())
                t = "REM";

            Intent i = new Intent();
            setResult(Activity.RESULT_OK,i);
            i.putExtra("ACTION",t);
            finish();
        }
    };
}
