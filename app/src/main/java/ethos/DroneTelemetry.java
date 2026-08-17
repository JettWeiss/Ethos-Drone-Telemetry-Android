package ethos;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.GeneralUtils;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;

import dji.common.battery.BatteryState;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
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

    //Drone Variables
    private Camera camera;
    private FlightController flightController;

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




        //Button Presses
        IPInputButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.isFastDoubleClick()) {
                    return;
                }
                websocket.send("IPInputButton");
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
                websocket.send("shootPhotoButton");
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







    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        //Inits
        if (ModuleVerificationUtil.isCameraModuleAvailable()){
            camera = DJISampleApplication.getAircraftInstance().getCamera();
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
    }
}
