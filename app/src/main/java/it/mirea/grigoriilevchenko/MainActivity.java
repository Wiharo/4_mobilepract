package it.mirea.grigoriilevchenko;

import java.io.IOException;
import java.util.Random;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import it.mirea.grigoriilevchenko.R;

public class MainActivity extends Activity {

    private static String NOTIFICATION_CHANNEL_ID = "zopa";
    SurfaceView sv;
    SurfaceHolder holder;
    HolderCallback holderCallback;
    Camera camera;

    final int CAMERA_ID = 0;
    final boolean FULL_SCREEN = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createNotificationChannel();
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        sv = (SurfaceView) findViewById(R.id.surfaceView);
        holder = sv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holderCallback = new HolderCallback();
        holder.addCallback(holderCallback);
        String permission = Manifest.permission.CAMERA;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }

        Button button = findViewById(R.id.button); button.setOnClickListener(new View.OnClickListener() { @Override
        public void onClick(View v) {
            showNotification();
        } });
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open(CAMERA_ID);
        setPreviewSize(FULL_SCREEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }

    class HolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            camera.stopPreview();
            setCameraDisplayOrientation(CAMERA_ID);
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

    }

    void setPreviewSize(boolean fullScreen) {


        Display display = getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();


        Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();


        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());


        if (widthIsMax) {

            rectPreview.set(0, 0, size.width, size.height);
        } else {

            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();

        if (!fullScreen) {

            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {

            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }

        matrix.mapRect(rectPreview);


        sv.getLayoutParams().height = (int) (rectPreview.bottom);
        sv.getLayoutParams().width = (int) (rectPreview.right);
    }

    void setCameraDisplayOrientation(int cameraId) {

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;


        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);


        if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else

            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        camera.setDisplayOrientation(result);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,"permission granted", Toast.LENGTH_SHORT).show();


            } else {
                Toast.makeText(MainActivity.this,"permission not granted", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_background).setContentTitle("practic_4.").setContentText("Levchenko Grigorii Olegovich IKBO-07-20");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        Random random = new Random();

        notificationManager.notify(random.nextInt(), builder.build());
    }

    private void createNotificationChannel() {

        CharSequence name = getString("name");
        String description = getString("Description");
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel); }

    private String getString(String name) {
        return name;
    }
}