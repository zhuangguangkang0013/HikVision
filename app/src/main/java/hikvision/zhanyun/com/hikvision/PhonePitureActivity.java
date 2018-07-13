package hikvision.zhanyun.com.hikvision;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.File;

import Video.CompressListener;
import Video.Compressor;
import Video.InitListener;

public class PhonePitureActivity extends AppCompatActivity {
    private CameraPreview mPreview;
    private Camera camera;
    private Handler mHandler = new Handler();
    public static String fileName;
    public static String fileNames;
    private int picture;
    private int video;
    private int time = 1;
    private Compressor mCompressor;
    private CameraPreview cameraPreview;
    public static String FileVideoName = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator +"HikVisionVideo/";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonepiture);
        initCamera();

        mCompressor = new Compressor(this);
        File file = new File(FileVideoName);
        if (!file.exists()) {
            file.mkdirs();
        }
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

        mCompressor.loadBinary(new InitListener() {
            @Override
            public void onLoadSuccess() {

            }

            @Override
            public void onLoadFail(String reason) {

            }
        });

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
            if (mPreview != null) mPreview.takePicture();
        }
    };
    private Runnable mFinish = new Runnable() {
        @Override
        public void run(){
        String cmd = "-y -i "+FileVideoName+CameraPreview.FileNames+" -strict -2 -vcodec libx264 -preset ultrafast -crf 24 -acodec aac -ar 44100 -ac 2 -b:a 96k -s 480x320 -aspect 16:9 "+MainActivity.filePath+cameraPreview.FileNames;

            execCommand(cmd);
        }
    };
    private void execCommand(String cmd) {
        mCompressor.execCommand(cmd, new CompressListener() {
            @Override
            //压缩成功
            public void onExecSuccess(String message) {
                if (picture == 1) {
                    setResult(5);
                } else if (video == 2) {
                    setResult(4);
                }
                finish();
            }

            @Override
            //压缩失败
            public void onExecFail(String reason) {
                if (picture == 1) {
                    mHandler.postDelayed(phonePicture, 5000);
                    mHandler.postDelayed(mFinish, 10000);
                } else if (video == 2) {
                    mHandler.postDelayed(videos, 5000);
                }
            }
            //执行进度
            @Override
            public void onExecProgress(String message) {
                Log.i("AA", "progress " + message);
                Log.i("AA", (message));

            }
        });
    }




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
