package ethos;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.graphics.SurfaceTexture;

import androidx.annotation.NonNull;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.GeneralUtils;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import dji.common.battery.BatteryState;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.media.DownloadListener;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;
import dji.sdk.products.Aircraft;
import ethos.WebSocketManager;

public class DroneTelemetry extends FrameLayout {
    //Constructors
    public DroneTelemetry(Context context) {
        this(context, null, 0);
    }

    public DroneTelemetry(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DroneTelemetry(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    //UI Variables
    private String serverURL = "ws://10.1.10.117:8765"; //TODO: Swap from hardcoded
    WebSocketManager websocket;
    private ImageButton IPInputButton;
    private ImageButton livestreamButton;
    private ImageButton settingsButton;
    private ImageButton videoSwapButton;
    private ImageButton shootPhotoButton;
    private ImageButton cameraSettingsButton;
    private TextView batteryLevel;
    private TextView altitudeText;
    private TextView horizontalSpeedText;
    private TextView verticalSpeedText;
    private TextView longLatText;
    TextInputLayout ipInputLayout;
    TextInputEditText ipInput;

    //Drone Variables
    private FlightController flightController;
    //Camera Variables
    private Camera camera;
    private TextureView cameraStream;
    private VideoFeeder.VideoDataListener videoDataListener;
    private DJICodecManager codecManager;
    private MediaManager mediaManager;
    private ByteArrayOutputStream photoBytes;


    //Location Variables
    private LocationCoordinate3D location;
    private double longitude;
    private double latitude;
    private float altitude;
    private float velocityX;
    private float velocityY;
    private float velocityZ;

    private void initView(Context context){
        inflate(context, R.layout.drone_telemetry, this);
        websocket = new WebSocketManager(serverURL);
        websocket.connect();

        IPInputButton = (ImageButton) findViewById(R.id.ip_input_button);
        livestreamButton = (ImageButton) findViewById(R.id.livestream_button);
        settingsButton = (ImageButton) findViewById(R.id.settings_button);
        videoSwapButton = (ImageButton) findViewById(R.id.video_swap_button);
        shootPhotoButton = (ImageButton) findViewById(R.id.shoot_photo_button);
        cameraSettingsButton = (ImageButton) findViewById(R.id.camera_settings_button);
        batteryLevel = (TextView) findViewById(R.id.battery_text);
        altitudeText = (TextView) findViewById(R.id.altitude_text);
        horizontalSpeedText = (TextView) findViewById(R.id.horizontal_speed_text);
        verticalSpeedText = (TextView) findViewById(R.id.vertical_speed_text);
        longLatText = (TextView) findViewById(R.id.long_lat_text);
        cameraStream = (TextureView) findViewById(R.id.camera_view);
        ipInputLayout = findViewById(R.id.ip_TextInputLayout);
        ipInput = findViewById(R.id.ip_textInputEditText);





        //Button Presses
        IPInputButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.isFastDoubleClick()) {
                    return;
                }
                websocket.send("IPInputButton");
                serverURL = "ws://10.1.10.117:8765";//ipInput.getText().toString();
                websocket = new WebSocketManager(serverURL);
                websocket.connect();
            }
        });
        livestreamButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.isFastDoubleClick()) {
                    return;
                }
                websocket.send("livestreamButton");
            }
        });
        settingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.isFastDoubleClick()) {
                    return;
                }
                websocket.send("settingsButton");
            }
        });
        videoSwapButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.isFastDoubleClick()) {
                    return;
                }
                websocket.send("videoSwapButton");
            }
        });

        shootPhotoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.isFastDoubleClick()) {
                    return;
                }

                DJISampleApplication.getProductInstance().getCamera().startShootPhoto(new CommonCallbacks.CompletionCallback(){
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            Log.e("CAMERA", "Take Photo Error: " + djiError.getDescription());
                            return;
                        }
                    }
                });

                mediaManager.refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null){
                            Log.e("CAMERA", "SD Card Access Failed: " + djiError.getDescription());
                            return;
                        }

                        sendPhoto();
                    }
                });
            }
        });

        cameraSettingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.isFastDoubleClick()) {
                    return;
                }
                websocket.send("cameraSettingsButton");
            }
        });
    }


    private void sendPhoto(){
        List<MediaFile> files = mediaManager.getSDCardFileListSnapshot();
        if (files == null || files.isEmpty()) {
            Log.e("CAMERA", "No files found");
            return;
        }

        MediaFile photo = files.get(files.size()-1);
        photo.fetchFileByteData(0, new DownloadListener<String>() {
            @Override
            public void onStart() {
                photoBytes = new ByteArrayOutputStream();
            }

            @Override
            public void onRateUpdate(long l, long l1, long l2) {

            }

            @Override
            public void onRealtimeDataUpdate(byte[] bytes, long l, boolean b) {
                try{
                    photoBytes.write(bytes);
                } catch (IOException e){
                    Log.e("CAMERA", "Failed to add bytes");
                }
            }

            @Override
            public void onProgress(long l, long l1) {
                    Log.d("CAMERA", "Progress = " + l1 + "/" + l);
            }

            @Override
            public void onSuccess(String s) {
                byte[] completePhoto = photoBytes.toByteArray();
                websocket.send(completePhoto);
            }

            @Override
            public void onFailure(DJIError djiError) {
                Log.e("CAMERA", "Failed to fetch photo: " + djiError.getDescription());
            }
        });
    }





    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        //Inits
        if (ModuleVerificationUtil.isCameraModuleAvailable()){
            camera = DJISampleApplication.getAircraftInstance().getCamera();
            mediaManager = camera.getMediaManager();
        }

        flightController = ((Aircraft) DJISampleApplication.getProductInstance()).getFlightController();

        //Battery
        try {
            DJISampleApplication.getProductInstance().getBattery().setStateCallback(new BatteryState.Callback() {
                @Override
                public void onUpdate(BatteryState djiBatteryState) {
                    batteryLevel.post(() -> {
                        batteryLevel.setText(
                                djiBatteryState.getChargeRemainingInPercent() + "%"
                        );
                    });
                }
            });
        } catch (Exception ignored) {
        }

        //Flight Info
        try {
            flightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                    location = flightControllerState.getAircraftLocation();
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    altitude = location.getAltitude();
                    velocityX = flightControllerState.getVelocityX();
                    velocityY = flightControllerState.getVelocityY();
                    velocityZ = flightControllerState.getVelocityZ();


                    altitudeText.post(() -> {
                        altitudeText.setText(
                                altitude + " m"
                        );
                    });
                    horizontalSpeedText.post(() -> {
                        horizontalSpeedText.setText(
                                (Math.sqrt(velocityX * velocityX + velocityY * velocityY)) + " m/s"
                        );
                    });
                    verticalSpeedText.post(() -> {
                        verticalSpeedText.setText(
                                velocityZ + " m/s"
                        );
                    });
                    longLatText.post(() -> {
                        longLatText.setText(
                                latitude + ", " + longitude
                        );
                    });

                    websocket.send("Altitude: " + altitude + "\nHorizontal Speed: " + (Math.sqrt(velocityX * velocityX + velocityY * velocityY)) + "\nVertical Speed: " + velocityZ + "\nLatitude Longitude: " + latitude + " " + longitude + "\n\n");
                }
            });
        } catch (Exception ignored){
        }



        //Screen View
        videoDataListener = new VideoFeeder.VideoDataListener() {
            @Override
            public void onReceive(byte[] bytes, int size) {
                if (codecManager != null) {
                    codecManager.sendDataToDecoder(bytes, size);
                }
            }
        };

        cameraStream.setSurfaceTextureListener(
                new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                        codecManager = new DJICodecManager(getContext(), surface, width, height);
                        VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(videoDataListener);
                        Log.d("CAMERA", "TextureView: " + width + " x " + height);
                    }

                    @Override
                    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height){}

                    @Override
                    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){
                        return false;
                    }

                    @Override
                    public void onSurfaceTextureUpdated(SurfaceTexture surface){}
                }
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (videoDataListener != null){
            VideoFeeder.getInstance().getPrimaryVideoFeed().removeVideoDataListener(videoDataListener);
            videoDataListener = null;
        }

        if (codecManager != null){
            codecManager.cleanSurface();
            codecManager = null;
        }

    }
}
