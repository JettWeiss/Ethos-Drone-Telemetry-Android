package com.dji.sdk.sample.demo.battery;

import android.content.Context;
import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.view.BasePushDataView;
import dji.common.battery.BatteryState;

/**
 * Class for getting the battery information.
 */

import ethos.WebSocketManager;

public class PushBatteryDataView extends BasePushDataView {
    private String serverURL = "ws://10.1.10.117:8765"; //TODO: Swap from hardcoded
    WebSocketManager websocket;

    public PushBatteryDataView(Context context) {
        super(context);
        websocket = new WebSocketManager(serverURL);
        websocket.connect();
    }



    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        try {
            DJISampleApplication.getProductInstance().getBattery().setStateCallback(new BatteryState.Callback() {
                @Override
                public void onUpdate(BatteryState djiBatteryState) {
                    stringBuffer.delete(0, stringBuffer.length());

                    stringBuffer.append("BatteryEnergyRemainingPercent: ").
                        append(djiBatteryState.getChargeRemainingInPercent()).
                                    append("%\n");



                    stringBuffer.append("CurrentVoltage: ").
                        append(djiBatteryState.getVoltage()).append("mV\n");
                    stringBuffer.append("CurrentCurrent: ").
                        append(djiBatteryState.getCurrent()).append("mA\n");

                    showStringBufferResult();

                    //Send to websocket
                    websocket.send(Integer.toString(djiBatteryState.getChargeRemainingInPercent()));
                    websocket.send(Integer.toString(djiBatteryState.getVoltage()));
                    websocket.send(Integer.toString(djiBatteryState.getCurrent()));
                }
            });
        } catch (Exception ignored) {

        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        try {
            DJISampleApplication.getProductInstance().getBattery().setStateCallback(null);
        } catch (Exception ignored) {

        }
    }

    @Override
    public int getDescription() {
        return R.string.battery_listview_push_info;
    }
}
