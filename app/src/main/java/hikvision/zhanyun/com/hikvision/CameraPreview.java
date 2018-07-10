package hikvision.zhanyun.com.hikvision;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final String TAG = "CameraDemo";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private String filePath = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "HikVisionData/";
    private MainActivity mainActivity;

    public void takePicture(final int picturesize) {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Camera.Parameters parameters = mCamera.getParameters();
                switch (picturesize) {
                    case 1://320 X 480
                        parameters.setPictureSize(320, 240);
                        break;
                    case 2://640 X 480
                        parameters.setPictureSize(640, 480);
                        break;
                    case 3://640 X 480
                        parameters.setPictureSize(640, 480);
                        break;
                    case 4://1024 X 768
                        parameters.setPictureSize(1024, 768);
                        break;
                    case 5://1024 X 768
                        parameters.setPictureSize(1024, 768);
                        break;
                    case 6://1280 X 720
                        parameters.setPictureSize(1280, 720);
                        break;
                    case 7://1280 X 720
                        parameters.setPictureSize(1280, 720);
                        break;
                    case 8://1920 X 1088
                        parameters.setPictureSize(1920, 1088);
                        break;
                    case 9://1280 X 768
                        parameters.setPictureSize(1280, 768);
                        break;
                    case 10://1280 X 960
                        parameters.setPictureSize(1280, 960);
                        break;
                    case 11://1600 X 1200
                        parameters.setPictureSize(1600, 1200);
                        break;
                    case 12://2048 X 1536
                        parameters.setPictureSize(2048, 1536);
                        break;
                    case 13://2560 X 1440
                        parameters.setPictureSize(2560, 1440);
                        break;
                    case 14://2560 X 1920
                        parameters.setPictureSize(2560, 1920);
                        break;
                    case 15://3600 X 2160
                        parameters.setPictureSize(3600, 2160);
                        break;
                    case 16://4864 X 2736
                        parameters.setPictureSize(4864, 2736);
                        break;
                    default:
                        parameters.setPictureSize(4864, 2736);

                }
                mCamera.setParameters(parameters);
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d(TAG, "Error creating media file, check storage permissions");
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    camera.startPreview();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
            }
        });
    }

    public boolean startRecording() {
        if (prepareVideoRecorder()) {
            mMediaRecorder.start();
            return true;
        } else {
            releaseMediaRecorder();
        }
        return false;
    }

    public void stopRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
        }
        releaseMediaRecorder();
    }

    public boolean isRecording() {
        return mMediaRecorder != null;
    }

    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public Camera getCameraInstance() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                //获取摄像头支持分辨率
                Camera.Parameters params = mCamera.getParameters();
                List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
                int length = pictureSizes.size();
                for (int i = 0; i < length; i++) {
                    Log.i("SupportedPictureSizes", "SupportedPictureSizes : " + pictureSizes.get(i).width + "x" + pictureSizes.get(i).height);
                }

                List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
                length = previewSizes.size();
                for (int i = 0; i < length; i++) {
                    Log.i("SupportedPreviewSizes", "SupportedPreviewSizes : " + previewSizes.get(i).width + "x" + previewSizes.get(i).height);
                }
            } catch (Exception e) {
                Log.d(TAG, "camera is not available");
            }
        }
        return mCamera;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        getCameraInstance();
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }

    private boolean prepareVideoRecorder() {

        mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setVideoSize(640, 480);
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), TAG);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            Phonepiture.fileName = getFileNameCriterion(2, "jpg");
            mediaFile = new File(MainActivity.filePath +
                    Phonepiture.fileName);
        } else if (type == MEDIA_TYPE_VIDEO) {
            Phonepiture.fileNames = getFileNameCriterion(2, "mp4");
            mediaFile = new File(MainActivity.filePath +
                    Phonepiture.fileNames );
            //TODO 上传视频
        } else {
            return null;
        }

        return mediaFile;
    }

    public String getFileNameCriterion(int channelNum, String type) {
        String cardNumber = "ZJ0002";
        String different = null;
        if (channelNum == 1) {
            different = "A";
        } else if (channelNum == 2) {
            different = "B";
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return cardNumber + "_" + different + "_" + "01" + "_" + timeStamp + "." + type;
    }
}