package ethos;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.FrameLayout;

import com.dji.sdk.sample.R;

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
    private Button messageOne;
    private Button messageTwo;

    private void initView(Context context){
        messageOne = (Button) findViewById(R.id.message_one);
        messageTwo = (Button) findViewById(R.id.message_two);
    }
}
