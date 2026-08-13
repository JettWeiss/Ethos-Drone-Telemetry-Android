package ethos;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.GeneralUtils;

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

    //Variables
    private String serverURL = "ws:10.1.10.117:8765";
    WebSocketManager websocket;
    private Button messageOne;
    private Button messageTwo;

    private void initView(Context context){
        inflate(context, R.layout.drone_telemetry, this);

        websocket = new WebSocketManager(serverURL);

        Log.d("Entered initView", "Entered initView");
        websocket.connect();
        websocket.send("Testing");

        messageOne = (Button) findViewById(R.id.message_one);
        messageTwo = (Button) findViewById(R.id.message_two);

        //Button Presses
        messageOne.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.isFastDoubleClick()) {
                    return;
                }
                websocket.send("Message 1");
            }
        });
        messageTwo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.isFastDoubleClick()) {
                    return;
                }
                websocket.send("Message 2");
            }
        });
    }



}
