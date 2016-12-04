package com.miao.test.asrsample;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.miao.test.util.ConfigUtil;
import com.miao.test.util.HciCloudAsrHelper;
import com.miao.test.util.HciCloudSysHelper;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnStart;
    private TextView tvState;
    private TextView tvError;
    private TextView tvResult;
    private HciCloudSysHelper mHciCloudSysHelper;
    private HciCloudAsrHelper mHciCloudAsrHelper;

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case HciCloudAsrHelper.RECORDER_RESULT: //显示识别结果
                    Bundle resultBundle = msg.getData();
                    String result = resultBundle.getString("result");
                    tvResult.setText(result);
                    break;
                case HciCloudAsrHelper.RECORDER_ERROR:  //显示错误信息
                    Bundle errorBundle = msg.getData();
                    String error = errorBundle.getString("error");
                    System.out.print(error);
                    if (error.equals("0")) {
                        tvError.setVisibility(View.GONE);
                    } else {
                        tvError.setText(error);
                    }
                    break;
                case HciCloudAsrHelper.RECORDER_STATE:  //显示录音机的状态
                    Bundle stateBundle = msg.getData();
                    String state = stateBundle.getString("state");
                    tvState.setText(state);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        initSinovoice();
    }

    private void initSinovoice() {
        mHciCloudSysHelper = HciCloudSysHelper.getInstance();
        mHciCloudAsrHelper = HciCloudAsrHelper.getInstance();
        int errorCode = mHciCloudSysHelper.init(this);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Toast.makeText(this, "系统初始化失败，错误码=" + errorCode, Toast.LENGTH_SHORT).show();
            return;
        }
        mHciCloudAsrHelper.setMyHander(new MyHandler());
        boolean bool = mHciCloudAsrHelper.initAsrRecorder(this, ConfigUtil.CAP_KEY_ASR_CLOUD_FREETALK);
        if (bool == false) {
            Toast.makeText(this, "录音机初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }else{      //隐藏错误码的状态栏
            Message message = new Message();
            message.arg1 = HciCloudAsrHelper.RECORDER_ERROR;
            Bundle bundle = new Bundle();
            bundle.putString("error", "0");
            message.setData(bundle);
            new MyHandler().sendMessage(message);
        }

    }

    private void initView() {
        btnStart = (Button) findViewById(R.id.btn_start);
        tvState = (TextView) findViewById(R.id.tv_state);
        tvError = (TextView) findViewById(R.id.tv_error);
        tvResult = (TextView) findViewById(R.id.tv_result);

        btnStart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                mHciCloudAsrHelper.startAsrRecorder(ConfigUtil.CAP_KEY_ASR_CLOUD_FREETALK, "common");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (mHciCloudAsrHelper != null) {
            mHciCloudAsrHelper.releaseAsrRecorder();
        }
        if (mHciCloudSysHelper != null) {
            mHciCloudSysHelper.release();
        }
        super.onDestroy();
    }
}
