package com.bawarchef.android.Fragments;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;

import org.w3c.dom.Text;

public class Home extends Fragment implements MessageReceiver{

    View v;

    TextView pending,completed,score,rating,rank;
    TextView kingname,kingscore;
    ImageView kingdp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.home,container,false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pending = v.findViewById(R.id.pendingbox);
        completed = v.findViewById(R.id.fulfilledbox);
        score = v.findViewById(R.id.scorebox);
        rating = v.findViewById(R.id.ratingbox);
        rank = v.findViewById(R.id.rankbox);

        kingname = v.findViewById(R.id.kingname);
        kingscore = v.findViewById(R.id.kingscore);
        kingdp = v.findViewById(R.id.kingimg);

        Message newm = new Message(Message.Direction.CLIENT_TO_SERVER,"FETCH_STATS");
        try {
            EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(newm), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
            AsyncExecutor executor = new AsyncExecutor();
            executor.execute(ep);
        }catch (Exception e){}
    }

    @Override
    public void process(Message m) {
        if(m.getMsg_type().equals("RESP_STATS_FETCH")){
            getActivity().runOnUiThread(()->{
                pending.setText(String.valueOf(m.getProperty("PEND")));
                completed.setText(String.valueOf(m.getProperty("COMP")));
                rating.setText(String.format("%.1f",m.getProperty("RATE")));
                rank.setText(String.valueOf(m.getProperty("RANK")));
                score.setText(String.format("%.2f",m.getProperty("SCORE")));

                if(m.getProperty("COTMName")==null){
                    kingname.setText("NOT YET DECLARED");
                    kingscore.setText("N.A.");
                    kingdp.setVisibility(View.INVISIBLE);
                }else {
                    kingname.setText((String) m.getProperty("COTMName"));
                    kingscore.setText(String.format("%.2f", m.getProperty("COTMScore")));
                }

                byte[] kingdpbyte = (byte[])m.getProperty("COTMDP");

                if(kingdpbyte!=null && kingdpbyte.length!=0){
                    kingdp.setImageBitmap(BitmapFactory.decodeByteArray(kingdpbyte,0,kingdpbyte.length));
                }

                if(dialog!=null&&dialog.isShowing()){

                    dialog.dismiss();
                    dialog = null;
                }
            });
        }
    }

    private ProgressDialog dialog;

    class AsyncExecutor extends AsyncTask<EncryptedPayload,Void,Void> {

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Getting stats...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            ((ThisApplication)getActivity().getApplication()).mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }
}
