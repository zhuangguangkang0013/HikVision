package hikvision.zhanyun.com.hikvision;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import Video.CompressListener;
import Video.Compressor;
import Video.InitListener;

public class PhonePitureActivity extends AppCompatActivity {
    private CameraPreview mPreview;
    private Handler mHandler = new Handler();
    public static String fileName;
    public static String fileNames;
    private int picture;
    private int video;
    private int time = 1;
    private Compressor mCompressor;
    public static String FileVideoName = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "HikVisionVideo/";
    private int count;

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
        public void run() {

            String cmd;

            if (video == 2) {
                cmd = "-y -i " + FileVideoName + fileNames +
                        " -strict -2 -vcodec libx264 -preset ultrafast -crf 24 -acodec aac -ar 44100 -ac 2 -b:a 96k -s 480x320 -aspect 16:9 "
                        + MainActivity.filePath + fileNames;
                execCommand(cmd);

            } else if (picture == 1) {
                boolean isSuccess = getSaveImage(FileVideoName + fileName);
                if (isSuccess) {
                    setResult(5);
                    finish();
                } else {
                    count++;
                    if (picture == 1) {
                        mHandler.postDelayed(phonePicture, 5000);
                        mHandler.postDelayed(mFinish, 10000);
                    }
                    if (count == 5) finish();
                }
            }

        }
    };

    // 图片按比例大小压缩方法并获取压缩后图片的路径
    public static Boolean getSaveImage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        bitmap = compressImage(bitmap);

        String name = fileName;
        String saveDir = MainActivity.filePath;
        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        // 保存入sdCard
        File file2 = new File(saveDir, name);
        try {
            FileOutputStream out = new FileOutputStream(file2);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
                out.close();
                bitmap.recycle();
            }
        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }


        return true;// 压缩好比例大小后再进行质量压缩
    }

    // 我们先看下质量压缩方法
    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }


    private void execCommand(String cmd) {
        if (cmd == null) finish();
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
                count++;
                if (picture == 1) {
                    mHandler.postDelayed(phonePicture, 5000);
                    mHandler.postDelayed(mFinish, 10000);
                } else if (video == 2) {
                    mHandler.postDelayed(videos, 5000);
                }
                if (count == 5) finish();
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
