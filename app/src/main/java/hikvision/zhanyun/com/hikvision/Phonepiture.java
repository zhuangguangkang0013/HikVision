package hikvision.zhanyun.com.hikvision;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Phonepiture extends AppCompatActivity {
    private CameraPreview mPreview;
    private Camera camera;
    private Handler mHandler = new Handler();
    public static String fileName;
    public static String fileNames;
    private int picture;
    private int video;
    private int time = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonepiture);
        initCamera();
        Intent intent = getIntent();
        if (intent != null) {
            picture = intent.getIntExtra("Picture",0);
            video = intent.getIntExtra("Video",0);
            time = intent.getIntExtra("Time",0);
        }
        if (picture == 1) {
            mHandler.postDelayed(phonepicture, 5000);
            mHandler.postDelayed(finsh,  10000);
        } else if (video == 2) {
            mHandler.postDelayed(videos, 5000);

        }
    }

    private Runnable videos = new Runnable() {
        @Override
        public void run() {
            mPreview.startRecording();
            mHandler.postDelayed(videoTime, time*1000);
        }
    };
    private Runnable videoTime = new Runnable() {
        @Override
        public void run() {
            mPreview.stopRecording();
            mHandler.postDelayed(finsh, 5000);
        }
    };

    private Runnable phonepicture = new Runnable() {
        @Override
        public void run() {
            mPreview.takePicture(16);
        }
    };
    private Runnable finsh = new Runnable() {
        @Override
        public void run() {
            if(picture == 1){
            setResult(5);}
            else  if(video == 2){
                setResult(4);
            }
            finish();
        }
    };

    private void initCamera() {
        mPreview = new CameraPreview(this);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);

//        SettingsFragment.passCamera(mPreview.getCameraInstance());
//        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
//        SettingsFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
//        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));
    }

    public void onPause() {
        super.onPause();
        mPreview = null;
    }

    public void onResume() {
        super.onResume();
        if (mPreview == null) {
            initCamera();
        }
    }

}
