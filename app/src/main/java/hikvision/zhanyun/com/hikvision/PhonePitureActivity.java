package hikvision.zhanyun.com.hikvision;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

public class PhonePitureActivity extends AppCompatActivity {
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
            picture = intent.getIntExtra("Picture", 0);
            video = intent.getIntExtra("Video", 0);
            time = intent.getIntExtra("Time", 0);
        }
        if (picture == 1) {
            mHandler.postDelayed(phonePicture, 5000);
            mHandler.postDelayed(mFinish, 10000);
        } else if (video == 2) {
            mHandler.postDelayed(videos, 5000);
        }


    }

    private Runnable videos = new Runnable() {
        @Override
        public void run() {
            mPreview.startRecording();
            mHandler.postDelayed(videoTime, time * 1000);
        }
    };
    private Runnable videoTime = new Runnable() {
        @Override
        public void run() {
            mPreview.stopRecording();
            mHandler.postDelayed(mFinish, 5000);
        }
    };

    private Runnable phonePicture = new Runnable() {
        @Override
        public void run() {
            if (mPreview != null) mPreview.takePicture(16);
        }
    };
    private Runnable mFinish = new Runnable() {
        @Override
        public void run() {
            if (picture == 1) {
                setResult(5);
            } else if (video == 2) {
                setResult(4);
            }
            finish();
        }
    };

    private void initCamera() {
        mPreview = new CameraPreview(this);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);
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
