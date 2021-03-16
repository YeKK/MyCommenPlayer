package com.d.commenplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.serialport.SerialPortFinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.d.commenplayer.MainActivity;
import com.d.commenplayer.R;
import com.d.commenplayer.comn.Device;
import com.d.commenplayer.comn.message.IMessage;
import com.d.commenplayer.comn.message.SerialPortManager;
import com.d.commenplayer.fragment.LogFragment;
import com.d.commenplayer.netstate.NetBus;
import com.d.commenplayer.netstate.NetCompat;
import com.d.commenplayer.netstate.NetState;
import com.d.commenplayer.util.Lunar;
import com.d.commenplayer.util.ToastUtil;
import com.d.lib.commenplayer.CommenPlayer;
import com.d.lib.commenplayer.listener.IPlayerListener;
import com.d.lib.commenplayer.listener.OnNetListener;
import com.d.lib.commenplayer.ui.ControlLayout;
import com.d.lib.commenplayer.util.ULog;
import com.d.lib.commenplayer.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.OnClick;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public class SimpleActivity extends Activity implements NetBus.OnNetListener {
    private CommenPlayer player; //播放器类
    private boolean ignoreNet;


    private TextSwitcher tv_switcher; //广告滚动显示窗口
    private TextSwitcher tv_switcher2;
    private TextSwitcher tv_switcher3;
    private TextSwitcher tv_switcher4;
    private TextSwitcher tv_switcher5;

    private Button button1; //更换按键显示文本实现翻页的效果
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;
    private Button button8;

    private boolean running = false; //计时状态
    private int seconds = 0; //计时时间
    private TextView textView_timer;
    private TextView textView_ComStatus;

    private TextView timeUpdate; //显示时间
    private String timeupdate;
    private TextView dataUpdate; //显示日历
    private String dataupdate;
    private TextView nongli; //显示日历
    private String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"; //hh 12 HH 24小时

    private Lunar lunar;

    private Device mDevice;

    private static int count = 1;

    private boolean mOpened = false;

    private LogFragment mLogFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        NetBus.getIns().addListener(this);
        initView();
        initPlayer();
        initView_advertise();

        initFragment();
        initDevice();
        initButton();


        initCommucationView();
        initTimeView();
        runTime();
    }

    private void initCommucationView() {
        textView_timer = findViewById(R.id.timeCount);
        textView_ComStatus = findViewById(R.id.conmiustatus);
        textView_ComStatus.setText("无通话");
    }

    private void initTimeView() {
        timeUpdate = findViewById(R.id.shijian);
        dataUpdate = findViewById(R.id.rili);
        nongli = findViewById(R.id.nongli);
        lunar = new Lunar(Calendar.getInstance());
    }


    private void initButton() {
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button6 = (Button) findViewById(R.id.button6);
        button7 = (Button) findViewById(R.id.button7);
        button8 = (Button) findViewById(R.id.button8);
    }

    private void runTime(){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
                         @Override
                         public void run() {
                             int hour = seconds /3600%24;
                             int minute = seconds%3600/60;
                             String time = String.format("通话时间：%02d:%02d:%02d",hour,minute,seconds%60);
                             textView_timer.setText(time);
                             if(running) seconds++;
                             handler.postDelayed(this,1000);

                             SimpleDateFormat dateFormatter = new SimpleDateFormat(DEFAULT_TIME_FORMAT);
                             timeupdate = dateFormatter.format(Calendar.getInstance().getTime());//获取当前时间
                             dataUpdate.setText(timeupdate.substring(0,11)+lunar.getWeek());
                             timeUpdate.setText(timeupdate.substring(11,19));
                             nongli.setText(lunar.cyclical()+lunar.animalsYear()+"年"+lunar.toString());
                         }
                     }
        );

    }

    @Override

    public boolean dispatchKeyEvent(KeyEvent event){
        play_nocation();
        if (event.getKeyCode() == 217 && event.getAction() == KeyEvent.ACTION_UP){ // 切换按键显示的文本，实现翻页的效果
                count++;
                if(count >=3) count=1;
                switch (count) {
                    case 1:
                        button1.setText("选呼");
                        button2.setText("群呼");
                        button3.setText("短信");
                        button4.setText("扫描");
                        button5.setText("电话");
                        button6.setText("亮度");
                        button7.setText("放大");
                        button8.setText("缩小");
                        break;
                    case 2:
                        button1.setText("定位呼");
                        button2.setText("船位发");
                        button3.setText("本船信息");
                        button4.setText("气象收");
                        button5.setText("待定");
                        button6.setText("对讲单发");
                        button7.setText("信道设置");
                        button8.setText("待定");
                        break;
                    default: break;
                }

        }
        if (event.getKeyCode() == 45 && event.getAction() == KeyEvent.ACTION_UP){
                    switch (count) {
                        case 1:
                            AlertDialog.Builder builder = new AlertDialog.Builder(SimpleActivity.this); //弹窗dialog设置, 选呼按键识别，并弹窗
                            builder.setIcon(R.drawable.bohao);
                            builder.setTitle("选呼");
                            View view = LayoutInflater.from(SimpleActivity.this).inflate(R.layout.dialog_send, null);
                            builder.setView(view);

                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
                            {
                                EditText zhongduanhao = (EditText)view.findViewById(R.id.zhongduanhao);
                                EditText xindaohao = (EditText)view.findViewById(R.id.xindaohao);
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    String a = zhongduanhao.getText().toString().trim();
                                    String b = xindaohao.getText().toString().trim();
                                    //    将输入的用户名和密码打印出来
                                    String data = a+b;
                                    int Length = 3 + data.length();
                                    String Comand = "15";
                                    sendData(sendMingling(Length, Comand, data));
                                    Toast.makeText(SimpleActivity.this, "终端号: " + a + ", 信道号: " + b, Toast.LENGTH_SHORT).show();
                                    textView_ComStatus.setText("正在呼叫。。。。");

                                }
                            });
                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    ToastUtil.show(SimpleActivity.this, "取消选呼");
                                }
                            });
                            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {

                                @Override
                                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                    play_nocation();
                                    if (keyCode == 142 && event.getAction() == KeyEvent.ACTION_UP) {
                                        sendKeyCode(8);
                                        Log.i("按键输入", "dispatchKeyEvent: 输入1");
                                    }
                                    if (keyCode == 132 && event.getAction() == KeyEvent.ACTION_UP) {
                                        sendKeyCode(9);
                                        Log.i("按键输入", "dispatchKeyEvent: 输入2");
                                    }
                                    if (keyCode == 133 && event.getAction() == KeyEvent.ACTION_UP) {
                                        sendKeyCode(10);
                                        Log.i("按键输入", "dispatchKeyEvent: 输入3");
                                    }
                                    if (keyCode == 134 && event.getAction() == KeyEvent.ACTION_UP) {
                                        sendKeyCode(11);
                                        Log.i("按键输入", "dispatchKeyEvent: 输入=4");
                                    }
                                    if (keyCode == 135 && event.getAction() == KeyEvent.ACTION_UP) {
                                        sendKeyCode(12);
                                        Log.i("按键输入", "dispatchKeyEvent: 输入5");
                                    }
                                    if (keyCode == 136 && event.getAction() == KeyEvent.ACTION_UP) {
                                        sendKeyCode(13);
                                        Log.i("按键输入", "dispatchKeyEvent: 输入6");
                                    }
                                    if (keyCode == 137 && event.getAction() == KeyEvent.ACTION_UP) {
                                        sendKeyCode(14);
                                    }
                                    if (keyCode == 138 && event.getAction() == KeyEvent.ACTION_UP) {
                                        sendKeyCode(15);
                                        Log.i("按键输入", "dispatchKeyEvent: 输入8");
                                    }
                                    if (keyCode == 139 && event.getAction() == KeyEvent.ACTION_UP) {
                                        sendKeyCode(16);
                                        Log.i("按键输入", "dispatchKeyEvent: 输入9");
                                    }
                                    if (keyCode == 141 && event.getAction() == KeyEvent.ACTION_UP) {
                                        sendKeyCode(7);
                                        Log.i("按键输入", "dispatchKeyEvent: 输入0");
                                    }
                                    if (keyCode == 217 && event.getAction() == KeyEvent.ACTION_UP) { //退格按键，对应翻页
                                        sendKeyCode(67);
                                        Log.i("按键输入", "dispatchKeyEvent: 输入退格"); //keycode 67
                                    }
                                    if (keyCode == 216 && event.getAction() == KeyEvent.ACTION_UP) { //取消按键,模拟点击屏幕取消按键的地点
                                        sendTouchEvent(582, 477);
                                        Log.i("按键输入", "dispatchKeyEvent: 输入取消");

                                    }
                                    if (keyCode == 215 && event.getAction() == KeyEvent.ACTION_UP) { //确认按键
                                        sendTouchEvent(650,480);
                                        Log.i("按键输入", "dispatchKeyEvent: 输入确认");
                                    }
                                    Log.i("按键输入", "onKey: "+keyCode);
                                    return false;
                                }
                            } );

                            Dialog dialog1 =  builder.create();
                            dialog1.show();
                            WindowManager.LayoutParams params = dialog1.getWindow().getAttributes();
                            params.width = 400;
                            params.height = 280;
                            dialog1.getWindow().setAttributes(params);
                        break;

//                        button1.setText("气象发");
//                        button2.setText("船位发");
//                        button3.setText("本船信息");
//                        button4.setText("气象收");
//                        button5.setText("气象发");
//                        button6.setText("对讲单发");
//                        button7.setText("GPS发");
//                        button8.setText("存储状态发");
                        case 2:
                            sendData(sendMingling_nodata(3, "18")); //气象发
                            ToastUtil.show(this, "气象与海况发");
                            break;
                        default: break;

                    }

        }

        if (event.getKeyCode() == 51 && event.getAction() == KeyEvent.ACTION_UP){

            switch (count) {//群呼按键识别
                case 1:
                    AlertDialog.Builder builder = new AlertDialog.Builder(SimpleActivity.this);
                    builder.setIcon(R.drawable.ic_launcher_foreground);
                    builder.setTitle("群呼");
                    View view = LayoutInflater.from(SimpleActivity.this).inflate(R.layout.dialog_send_qunhu, null);
                    builder.setView(view);


                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
                    {
                        EditText qunhuhao = (EditText)view.findViewById(R.id.qunhuhao);
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            String a = qunhuhao.getText().toString().trim();
                            int Length = 3 + a.length();
                            String Comand = "16";
                            sendData(sendMingling(Length, Comand, a));
                            Toast.makeText(SimpleActivity.this, "群呼号: " + a , Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
                    builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            play_nocation();
                            if (keyCode == 142 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(8);
                                Log.i("按键输入", "dispatchKeyEvent: 输入1");
                            }
                            if (keyCode == 132 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(9);
                                Log.i("按键输入", "dispatchKeyEvent: 输入2");
                            }
                            if (keyCode == 133 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(10);
                                Log.i("按键输入", "dispatchKeyEvent: 输入3");
                            }
                            if (keyCode == 134 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(11);
                                Log.i("按键输入", "dispatchKeyEvent: 输入=4");
                            }
                            if (keyCode == 135 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(12);
                                Log.i("按键输入", "dispatchKeyEvent: 输入5");
                            }
                            if (keyCode == 136 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(13);
                                Log.i("按键输入", "dispatchKeyEvent: 输入6");
                            }
                            if (keyCode == 137 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(14);
                            }
                            if (keyCode == 138 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(15);
                                Log.i("按键输入", "dispatchKeyEvent: 输入8");
                            }
                            if (keyCode == 139 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(16);
                                Log.i("按键输入", "dispatchKeyEvent: 输入9");
                            }
                            if (keyCode == 141 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(7);
                                Log.i("按键输入", "dispatchKeyEvent: 输入0");
                            }
                            if (keyCode == 217 && event.getAction() == KeyEvent.ACTION_UP) { //退格按键，对应翻页
                                sendKeyCode(67);
                                Log.i("按键输入", "dispatchKeyEvent: 输入退格"); //keycode 67
                            }
                            if (keyCode == 216 && event.getAction() == KeyEvent.ACTION_UP) { //取消按键,模拟点击屏幕取消按键的地点
                                sendTouchEvent(678, 447);
                                Log.i("按键输入", "dispatchKeyEvent: 输入取消");

                            }
                            if (keyCode == 215 && event.getAction() == KeyEvent.ACTION_UP) { //确认按键
                                sendTouchEvent(742,447);
                                Log.i("按键输入", "dispatchKeyEvent: 输入确认");
                            }
                            Log.i("按键输入", "onKey: "+keyCode);
                            return false;
                        }
                    } );
                    builder.show();
                break;

                case 2:  //船位呼发
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(SimpleActivity.this);
                    builder2.setIcon(R.drawable.ic_launcher_foreground);
                    builder2.setTitle("船位呼发");
                    View view2 = LayoutInflater.from(SimpleActivity.this).inflate(R.layout.dialog_send_qunhu, null);
                    builder2.setView(view2);


                    builder2.setPositiveButton("确定", new DialogInterface.OnClickListener()
                    {
                        EditText chuanweihufa = (EditText)view2.findViewById(R.id.qunhuhao);
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            String a = chuanweihufa.getText().toString().trim();
                            int Length = 3 + a.length();
                            String Comand = "1E";
                            sendData(sendMingling(Length, Comand, a));
                            Toast.makeText(SimpleActivity.this, "船位呼发号: " + a , Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder2.setNegativeButton("取消", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
                    builder2.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == 142 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(8);
                                Log.i("按键输入", "dispatchKeyEvent: 输入1");
                            }
                            if (keyCode == 132 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(9);
                                Log.i("按键输入", "dispatchKeyEvent: 输入2");
                            }
                            if (keyCode == 133 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(10);
                                Log.i("按键输入", "dispatchKeyEvent: 输入3");
                            }
                            if (keyCode == 134 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(11);
                                Log.i("按键输入", "dispatchKeyEvent: 输入=4");
                            }
                            if (keyCode == 135 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(12);
                                Log.i("按键输入", "dispatchKeyEvent: 输入5");
                            }
                            if (keyCode == 136 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(13);
                                Log.i("按键输入", "dispatchKeyEvent: 输入6");
                            }
                            if (keyCode == 137 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(14);
                            }
                            if (keyCode == 138 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(15);
                                Log.i("按键输入", "dispatchKeyEvent: 输入8");
                            }
                            if (keyCode == 139 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(16);
                                Log.i("按键输入", "dispatchKeyEvent: 输入9");
                            }
                            if (keyCode == 141 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(7);
                                Log.i("按键输入", "dispatchKeyEvent: 输入0");
                            }
                            if (keyCode == 217 && event.getAction() == KeyEvent.ACTION_UP) { //退格按键，对应翻页
                                sendKeyCode(67);
                                Log.i("按键输入", "dispatchKeyEvent: 输入退格"); //keycode 67
                            }
                            if (keyCode == 216 && event.getAction() == KeyEvent.ACTION_UP) { //取消按键,模拟点击屏幕取消按键的地点
                                sendTouchEvent(678, 447);
                                Log.i("按键输入", "dispatchKeyEvent: 输入取消");

                            }
                            if (keyCode == 215 && event.getAction() == KeyEvent.ACTION_UP) { //确认按键
                                sendTouchEvent(742,447);
                                Log.i("按键输入", "dispatchKeyEvent: 输入确认");
                            }
                            Log.i("按键输入", "onKey: "+keyCode);
                            return false;
                        }
                    } );
                    builder2.show();
                break;

                default: break;

            }


        }

        if (event.getKeyCode() == 46 && event.getAction() == KeyEvent.ACTION_UP){  //短信 本船信息按键识别
            switch (count) {
                case 1:
                    //sendData(sendMingling_nodata(3, "73"));
                    Toast.makeText(SimpleActivity.this, "短信按键按下", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(SimpleActivity.this);
                    builder2.setIcon(R.drawable.ic_launcher_foreground);
                    builder2.setTitle("本船信息");
                    View view2 = LayoutInflater.from(SimpleActivity.this).inflate(R.layout.dialog_send_qunhu, null);
                    builder2.setView(view2);


                    builder2.setPositiveButton("确定", new DialogInterface.OnClickListener()
                    {
                        EditText chuanweihufa = (EditText)view2.findViewById(R.id.qunhuhao);
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            String a = chuanweihufa.getText().toString().trim();
                            int Length = 3 + a.length();
                            String Comand = "26";
                            sendData(sendMingling(Length, Comand, a));
                            Toast.makeText(SimpleActivity.this, "本船信息: " + a , Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder2.setNegativeButton("取消", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
                    builder2.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == 142 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(8);
                                Log.i("按键输入", "dispatchKeyEvent: 输入1");
                            }
                            if (keyCode == 132 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(9);
                                Log.i("按键输入", "dispatchKeyEvent: 输入2");
                            }
                            if (keyCode == 133 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(10);
                                Log.i("按键输入", "dispatchKeyEvent: 输入3");
                            }
                            if (keyCode == 134 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(11);
                                Log.i("按键输入", "dispatchKeyEvent: 输入=4");
                            }
                            if (keyCode == 135 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(12);
                                Log.i("按键输入", "dispatchKeyEvent: 输入5");
                            }
                            if (keyCode == 136 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(13);
                                Log.i("按键输入", "dispatchKeyEvent: 输入6");
                            }
                            if (keyCode == 137 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(14);
                            }
                            if (keyCode == 138 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(15);
                                Log.i("按键输入", "dispatchKeyEvent: 输入8");
                            }
                            if (keyCode == 139 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(16);
                                Log.i("按键输入", "dispatchKeyEvent: 输入9");
                            }
                            if (keyCode == 141 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(7);
                                Log.i("按键输入", "dispatchKeyEvent: 输入0");
                            }
                            if (keyCode == 217 && event.getAction() == KeyEvent.ACTION_UP) { //退格按键，对应翻页
                                sendKeyCode(67);
                                Log.i("按键输入", "dispatchKeyEvent: 输入退格"); //keycode 67
                            }
                            if (keyCode == 216 && event.getAction() == KeyEvent.ACTION_UP) { //取消按键,模拟点击屏幕取消按键的地点
                                sendTouchEvent(678, 447);
                                Log.i("按键输入", "dispatchKeyEvent: 输入取消");

                            }
                            if (keyCode == 215 && event.getAction() == KeyEvent.ACTION_UP) { //确认按键
                                sendTouchEvent(742,447);
                                Log.i("按键输入", "dispatchKeyEvent: 输入确认");
                            }
                            Log.i("按键输入", "onKey: "+keyCode);
                            return false;
                        }
                    } );
                    builder2.show();
                    break;
                default:break;
            }

        }

        if (event.getKeyCode() == 48 && event.getAction() == KeyEvent.ACTION_UP){  //扫描 气象收按键识别
            switch (count) {
                case 1:
                    sendData(sendMingling_nodata(3, "73"));
                    Toast.makeText(SimpleActivity.this, "非空闲信道扫描", Toast.LENGTH_SHORT).show();
                case 2:
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(SimpleActivity.this);
                    builder2.setIcon(R.drawable.ic_launcher_foreground);
                    builder2.setTitle("气象收信道");
                    View view2 = LayoutInflater.from(SimpleActivity.this).inflate(R.layout.dialog_send_qunhu, null);
                    builder2.setView(view2);


                    builder2.setPositiveButton("确定", new DialogInterface.OnClickListener()
                    {
                        EditText chuanweihufa = (EditText)view2.findViewById(R.id.qunhuhao);
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            String a = chuanweihufa.getText().toString().trim();
                            int Length = 3 + a.length();
                            String Comand = "2A";
                            sendData(sendMingling(Length, Comand, a));
                            Toast.makeText(SimpleActivity.this, "气象与海况收: " + a , Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder2.setNegativeButton("取消", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
                    builder2.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == 142 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(8);
                                Log.i("按键输入", "dispatchKeyEvent: 输入1");
                            }
                            if (keyCode == 132 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(9);
                                Log.i("按键输入", "dispatchKeyEvent: 输入2");
                            }
                            if (keyCode == 133 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(10);
                                Log.i("按键输入", "dispatchKeyEvent: 输入3");
                            }
                            if (keyCode == 134 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(11);
                                Log.i("按键输入", "dispatchKeyEvent: 输入=4");
                            }
                            if (keyCode == 135 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(12);
                                Log.i("按键输入", "dispatchKeyEvent: 输入5");
                            }
                            if (keyCode == 136 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(13);
                                Log.i("按键输入", "dispatchKeyEvent: 输入6");
                            }
                            if (keyCode == 137 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(14);
                            }
                            if (keyCode == 138 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(15);
                                Log.i("按键输入", "dispatchKeyEvent: 输入8");
                            }
                            if (keyCode == 139 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(16);
                                Log.i("按键输入", "dispatchKeyEvent: 输入9");
                            }
                            if (keyCode == 141 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(7);
                                Log.i("按键输入", "dispatchKeyEvent: 输入0");
                            }
                            if (keyCode == 217 && event.getAction() == KeyEvent.ACTION_UP) { //退格按键，对应翻页
                                sendKeyCode(67);
                                Log.i("按键输入", "dispatchKeyEvent: 输入退格"); //keycode 67
                            }
                            if (keyCode == 216 && event.getAction() == KeyEvent.ACTION_UP) { //取消按键,模拟点击屏幕取消按键的地点
                                sendTouchEvent(678, 447);
                                Log.i("按键输入", "dispatchKeyEvent: 输入取消");

                            }
                            if (keyCode == 215 && event.getAction() == KeyEvent.ACTION_UP) { //确认按键
                                sendTouchEvent(742,447);
                                Log.i("按键输入", "dispatchKeyEvent: 输入确认");
                            }
                            Log.i("按键输入", "onKey: "+keyCode);
                            return false;
                        }
                    } );
                    builder2.show();
                    break;
                default:break;
            }

        }

        if (event.getKeyCode() == 159 && event.getAction() == KeyEvent.ACTION_UP){  //电话 气象发求救按键识别
            switch (count) {
                case 1:
                    Toast.makeText(SimpleActivity.this, "电话按键", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    sendData(sendMingling_nodata(3, "18"));
                    Toast.makeText(SimpleActivity.this, "气象与海况发", Toast.LENGTH_SHORT).show();
                    break;
                default:break;
            }
        }

        if (event.getKeyCode() == 213 && event.getAction() == KeyEvent.ACTION_UP){  //电话 对讲单发按键识别
            switch (count) {
                case 1:
                    Toast.makeText(SimpleActivity.this, "亮度按键", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder10 = new AlertDialog.Builder(SimpleActivity.this); //选呼接收
                    builder10.setIcon(R.drawable.ring_icon);
                    builder10.setTitle("选呼收");
                    View view10 = LayoutInflater.from(SimpleActivity.this).inflate(R.layout.dialog_rev_xuanhu, null);
                    builder10.setView(view10);

                    builder10.setPositiveButton("接听", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            sendData(sendMingling_nodata(3, "35"));
                            textView_ComStatus.setText("选呼通话，按取消挂断");
                            running = true;
                        }
                    });
                    builder10.setNegativeButton("拒绝", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            sendData(sendMingling_nodata(3, "25"));
                        }
                    });
                    builder10.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == 216 && event.getAction() == KeyEvent.ACTION_UP) { //取消按键,模拟点击屏幕取消按键的地点
                                sendTouchEvent(620, 500);
                                Log.i("按键输入", "dispatchKeyEvent: 输入取消");

                            }
                            if (keyCode == 215 && event.getAction() == KeyEvent.ACTION_UP) { //确认按键
                                sendTouchEvent(680,500);
                                Log.i("按键输入", "dispatchKeyEvent: 输入确认");
                            }
                            Log.i("按键输入", "onKey: "+keyCode);
                            return false;
                        }
                    } );
                    builder10.show();
                    break;
                case 2:
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(SimpleActivity.this);
                    builder2.setIcon(R.drawable.ic_launcher_foreground);
                    builder2.setTitle("对讲单发");
                    View view2 = LayoutInflater.from(SimpleActivity.this).inflate(R.layout.dialog_send_qunhu, null);
                    builder2.setView(view2);


                    builder2.setPositiveButton("确定", new DialogInterface.OnClickListener()
                    {
                        EditText chuanweihufa = (EditText)view2.findViewById(R.id.qunhuhao);
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            String a = chuanweihufa.getText().toString().trim();
                            int Length = 3 + a.length();
                            String Comand = "2F";
                            sendData(sendMingling(Length, Comand, a));
                            Toast.makeText(SimpleActivity.this, "对讲单发: " + a , Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder2.setNegativeButton("取消", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
                    builder2.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == 142 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(8);
                                Log.i("按键输入", "dispatchKeyEvent: 输入1");
                            }
                            if (keyCode == 132 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(9);
                                Log.i("按键输入", "dispatchKeyEvent: 输入2");
                            }
                            if (keyCode == 133 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(10);
                                Log.i("按键输入", "dispatchKeyEvent: 输入3");
                            }
                            if (keyCode == 134 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(11);
                                Log.i("按键输入", "dispatchKeyEvent: 输入=4");
                            }
                            if (keyCode == 135 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(12);
                                Log.i("按键输入", "dispatchKeyEvent: 输入5");
                            }
                            if (keyCode == 136 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(13);
                                Log.i("按键输入", "dispatchKeyEvent: 输入6");
                            }
                            if (keyCode == 137 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(14);
                            }
                            if (keyCode == 138 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(15);
                                Log.i("按键输入", "dispatchKeyEvent: 输入8");
                            }
                            if (keyCode == 139 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(16);
                                Log.i("按键输入", "dispatchKeyEvent: 输入9");
                            }
                            if (keyCode == 141 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(7);
                                Log.i("按键输入", "dispatchKeyEvent: 输入0");
                            }
                            if (keyCode == 217 && event.getAction() == KeyEvent.ACTION_UP) { //退格按键，对应翻页
                                sendKeyCode(67);
                                Log.i("按键输入", "dispatchKeyEvent: 输入退格"); //keycode 67
                            }
                            if (keyCode == 216 && event.getAction() == KeyEvent.ACTION_UP) { //取消按键,模拟点击屏幕取消按键的地点
                                sendTouchEvent(678, 447);
                                Log.i("按键输入", "dispatchKeyEvent: 输入取消");

                            }
                            if (keyCode == 215 && event.getAction() == KeyEvent.ACTION_UP) { //确认按键
                                sendTouchEvent(742,447);
                                Log.i("按键输入", "dispatchKeyEvent: 输入确认");
                            }
                            Log.i("按键输入", "onKey: "+keyCode);
                            return false;
                        }
                    } );
                    builder2.show();

                    break;
                default:break;
            }
        }

        if (event.getKeyCode() == 218 && event.getAction() == KeyEvent.ACTION_UP){  //放大 GPS发按键识别
            switch (count) {
                case 1:
                    Toast.makeText(SimpleActivity.this, "放大按键", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder9 = new AlertDialog.Builder(SimpleActivity.this) .setIcon(R.drawable.ring_icon); //求救呼收
                    builder9.setTitle("收到求救通话请求");
                    View view2 = LayoutInflater.from(SimpleActivity.this).inflate(R.layout.dialog_send_qiujiu, null);
                    builder9.setView(view2);

                    builder9.setPositiveButton("接听", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            sendData(sendMingling_nodata(3, "35"));
                            textView_ComStatus.setText("求救通话中，点击取消挂断");
                            running = true;
                        }
                    });
                    builder9.setNegativeButton("拒绝", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            sendData(sendMingling_nodata(3, "25"));
                        }
                    });
                    builder9.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == 216 && event.getAction() == KeyEvent.ACTION_UP) { //取消按键,模拟点击屏幕取消按键的地点
                                sendTouchEvent(540, 500);
                                Log.i("按键输入", "dispatchKeyEvent: 输入取消");

                            }
                            if (keyCode == 215 && event.getAction() == KeyEvent.ACTION_UP) { //确认按键
                                sendTouchEvent(600,490);
                                Log.i("按键输入", "dispatchKeyEvent: 输入确认");
                            }
                            Log.i("按键输入", "onKey: "+keyCode);
                            return false;
                        }
                    } );
                    Dialog  dialog = builder9.create();
                    dialog.show();
                    WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                    params.width = 300;
                    params.height = 280;
                    dialog.getWindow().setAttributes(params);
                    break;
                case 2:
                    AlertDialog.Builder builder3 = new AlertDialog.Builder(SimpleActivity.this);
                    builder3.setIcon(R.drawable.ic_launcher_foreground);
                    builder3.setTitle("输入信道号");
                    View view3 = LayoutInflater.from(SimpleActivity.this).inflate(R.layout.dialog_send_qunhu, null);
                    builder3.setView(view3);


                    builder3.setPositiveButton("确定", new DialogInterface.OnClickListener()
                    {
                        EditText chuanweihufa = (EditText)view3.findViewById(R.id.qunhuhao);
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            String a = chuanweihufa.getText().toString().trim();
                            int Length = 3 + a.length();
                            String Comand = "31";
                            sendData(sendMingling(Length, Comand, a));
                            Toast.makeText(SimpleActivity.this, "信道号设置" + a , Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder3.setNegativeButton("取消", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
                    builder3.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == 142 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(8);
                                Log.i("按键输入", "dispatchKeyEvent: 输入1");
                            }
                            if (keyCode == 132 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(9);
                                Log.i("按键输入", "dispatchKeyEvent: 输入2");
                            }
                            if (keyCode == 133 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(10);
                                Log.i("按键输入", "dispatchKeyEvent: 输入3");
                            }
                            if (keyCode == 134 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(11);
                                Log.i("按键输入", "dispatchKeyEvent: 输入=4");
                            }
                            if (keyCode == 135 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(12);
                                Log.i("按键输入", "dispatchKeyEvent: 输入5");
                            }
                            if (keyCode == 136 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(13);
                                Log.i("按键输入", "dispatchKeyEvent: 输入6");
                            }
                            if (keyCode == 137 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(14);
                            }
                            if (keyCode == 138 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(15);
                                Log.i("按键输入", "dispatchKeyEvent: 输入8");
                            }
                            if (keyCode == 139 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(16);
                                Log.i("按键输入", "dispatchKeyEvent: 输入9");
                            }
                            if (keyCode == 141 && event.getAction() == KeyEvent.ACTION_UP) {
                                sendKeyCode(7);
                                Log.i("按键输入", "dispatchKeyEvent: 输入0");
                            }
                            if (keyCode == 217 && event.getAction() == KeyEvent.ACTION_UP) { //退格按键，对应翻页
                                sendKeyCode(67);
                                Log.i("按键输入", "dispatchKeyEvent: 输入退格"); //keycode 67
                            }
                            if (keyCode == 216 && event.getAction() == KeyEvent.ACTION_UP) { //取消按键,模拟点击屏幕取消按键的地点
                                sendTouchEvent(680, 447);
                                Log.i("按键输入", "dispatchKeyEvent: 输入取消");

                            }
                            if (keyCode == 215 && event.getAction() == KeyEvent.ACTION_UP) { //确认按键
                                sendTouchEvent(742,447);
                                Log.i("按键输入", "dispatchKeyEvent: 输入确认");
                            }
                            Log.i("按键输入", "onKey: "+keyCode);
                            return false;
                        }
                    } );
                    builder3.show();
                    break;
                default:break;
            }
        }

        if (event.getKeyCode() == 212 && event.getAction() == KeyEvent.ACTION_UP){  //缩小  存储状态发按键识别
            switch (count) {
                case 1:
                    //AudioManager mAudioMgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                    //mAudioMgr.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
                    //running = false;
                    //seconds = 0;
                    Toast.makeText(SimpleActivity.this, "求救信号发送", Toast.LENGTH_SHORT).show();
                    sendData(sendMingling_nodata(3, "11"));
                    textView_ComStatus.setText("求救通话中,点击取消挂断");
                    running = true;
                    break;
                case 2:
                    sendData(sendMingling_nodata(3, "40"));
                    Toast.makeText(SimpleActivity.this, "存储状体发", Toast.LENGTH_SHORT).show();
                    break;
                default:break;
            }
        }

        if (event.getKeyCode() == 41 && event.getAction() == KeyEvent.ACTION_UP){  //求救呼发按键识别
            AlertDialog.Builder builder2 = new AlertDialog.Builder(SimpleActivity.this);
            builder2.setIcon(R.drawable.ic_launcher_foreground);
            builder2.setTitle("求救呼发");
            View view2 = LayoutInflater.from(SimpleActivity.this).inflate(R.layout.dialog_send_qiujiu, null);
            builder2.setView(view2);


            builder2.setPositiveButton("确定", new DialogInterface.OnClickListener()
            {
                EditText chuanweihufa = (EditText)view2.findViewById(R.id.qunhuhao);
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    sendData(sendMingling_nodata(3, "11"));
                }
            });
            builder2.setNegativeButton("取消", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                }
            });
            builder2.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == 216 && event.getAction() == KeyEvent.ACTION_UP) { //取消按键,模拟点击屏幕取消按键的地点
                        sendTouchEvent(678, 475);
                        Log.i("按键输入", "dispatchKeyEvent: 输入取消");

                    }
                    if (keyCode == 215 && event.getAction() == KeyEvent.ACTION_UP) { //确认按键
                        sendTouchEvent(742,475);
                        Log.i("按键输入", "dispatchKeyEvent: 输入确认");
                    }
                    Log.i("按键输入", "onKey: "+keyCode);
                    return false;
                }
            } );
            builder2.show();

        }

        if (event.getKeyCode() == 114 && event.getAction() == KeyEvent.ACTION_UP){  //求救呼发按键识别
            AlertDialog.Builder builder2 = new AlertDialog.Builder(SimpleActivity.this);
            builder2.setIcon(R.drawable.ic_launcher_foreground);
            builder2.setTitle("求救呼发");
            View view2 = LayoutInflater.from(SimpleActivity.this).inflate(R.layout.dialog_send_qiujiu, null);
            builder2.setView(view2);


            builder2.setPositiveButton("确定", new DialogInterface.OnClickListener()
            {
                EditText chuanweihufa = (EditText)view2.findViewById(R.id.qunhuhao);
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    sendData(sendMingling_nodata(3, "11"));
                }
            });
            builder2.setNegativeButton("取消", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                }
            });
            builder2.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == 216 && event.getAction() == KeyEvent.ACTION_UP) { //取消按键,模拟点击屏幕取消按键的地点
                        sendTouchEvent(678, 475);
                        Log.i("按键输入", "dispatchKeyEvent: 输入取消");

                    }
                    if (keyCode == 215 && event.getAction() == KeyEvent.ACTION_UP) { //确认按键
                        sendTouchEvent(742,475);
                        Log.i("按键输入", "dispatchKeyEvent: 输入确认");
                    }
                    Log.i("按键输入", "onKey: "+keyCode);
                    return false;
                }
            } );
            builder2.show();

        }


        if (event.getKeyCode() == 216 && event.getAction() == KeyEvent.ACTION_UP){  //挂断电话操作
            if(running = true) {
                sendData(sendMingling_nodata(3, "25"));
                running = false;
                seconds = 0;
                textView_ComStatus.setText("通话状态：通话断开");
                Toast.makeText(SimpleActivity.this, "通话挂断", Toast.LENGTH_SHORT).show();
            }

        }


        Log.i("按键输入", "dispatchKeyEvent:" + event.getKeyCode());

        return super.dispatchKeyEvent(event);
    }

    /**
     * 模拟按键输入，转译输入按键的键值
     * @param keyCode 转移后的按键键值，参考Shell中按键值表
     */

    private void sendKeyCode(final int keyCode) {  //最快的一种方式 必须在前台才能使用
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建一个Instrumentation对象
                    Instrumentation inst = new Instrumentation();
                    // 调用inst对象的按键模拟方法
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 按键转译模拟屏幕触摸点击事件
     * @param x 屏幕坐标x
     * @param y 屏幕坐标y
     */
    private void sendTouchEvent(final int x, final int y) {  //最快的一种方式
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建一个Instrumentation对象
                    Instrumentation inst = new Instrumentation();
                    // 调用inst对象的按键模拟方法
                    inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0));
                    inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

        private void initView() {
        player = (CommenPlayer) findViewById(R.id.player);

    }

    private void initPlayer() {
        player.setLive(false);
        player.setOnNetListener(new OnNetListener() {
            @Override
            public void onIgnoreMobileNet() {
                ignoreNet = true;
            }
        }).setOnPlayerListener(new IPlayerListener() {
            @Override
            public void onLoading() {
                player.getControl().setState(ControlLayout.STATE_LOADING);
            }

            @Override
            public void onCompletion(IMediaPlayer mp) {
                player.getControl().setState(ControlLayout.STATE_COMPLETION);
            }

            @Override
            public void onPrepared(IMediaPlayer mp) {
                if (!ignoreNet && NetCompat.getStatus() == NetState.CONNECTED_MOBILE) {
                    player.pause();
                    player.getControl().setState(ControlLayout.STATE_MOBILE_NET);
                } else {
                    player.getControl().setState(ControlLayout.STATE_PREPARED);
                }
            }

            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                player.getControl().setState(ControlLayout.STATE_ERROR);
                return false;
            }

            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                return false;
            }

            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {

            }
        });
        player.play(getResources().getString(R.string.url1));
        Log.i("播放打点", "initPlayer:开始播放 ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    /**
     * 初始化日志Fragment
     */
    protected void initFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        mLogFragment = (LogFragment) fragmentManager.findFragmentById(R.id.log_fragment);
    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return super.onNavigateUp();
    }


    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     *带数据的命令发
     * @param
     * @return
     */
    private static String sendMingling( int length, String m_comand, String s) {
        StringBuilder sm = new StringBuilder();
        StringBuilder checksum = new StringBuilder();
        checksum.append("01B300").append(intToHex(length)).append(m_comand).append(StringToBCDS(s));
        sm.append("FEFCF8F001B300").append(intToHex(length)).append(m_comand)
                .append(StringToBCDS(s)).append(getCheckSum(checksum.toString(),4)).append("FCFEF0F8");
        return sm.toString();
    }

    /**
     *不带数据的命令发
     * @param
     * @return
     */
    private static String sendMingling_nodata( int length, String m_comand) {
        StringBuilder sm = new StringBuilder();
        StringBuilder checksum = new StringBuilder();
        checksum.append("01B300").append(intToHex(length)).append(m_comand);//b3改11
        sm.append("FEFCF8F001B300").append(intToHex(length)).append(m_comand)
                .append(getCheckSum(checksum.toString(),4)).append("FCFEF0F8"); //b3改11
        return sm.toString();
    }

    /**
     * 十进制转为为16进制字符串
     * @param n
     * @return
     */
    private static String intToHex(int n) {
        //StringBuffer s = new StringBuffer();
        StringBuilder sb = new StringBuilder(8);
        String a;
        char []b = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        if (n <= 15) {
            sb.append(0).append(b[n]);
            return  sb.toString();
        }
        while(n != 0){
            sb = sb.append(b[n%16]);
            n = n/16;
        }
        a = sb.reverse().toString();
        return a;
    }

    /**
     *带拨号码转化问BCD字符串
     * @param
     * @return
     */
    private static String StringToBCDS(String s) {
        StringBuilder ss = new StringBuilder();
        int leng = s.length();
        if(leng%2 != 0){
            ss.append('0');
            ss.append(s.charAt(leng-1));
            leng--;
        }

        for (int i = leng-1; i > 0; i= i-2) {
            ss.append(s.charAt(i-1));
            ss.append(s.charAt(i));
        }
        return ss.toString();
    }

    /**
     * 和校验，取最后round位
     * @param round 取后面多少位
     */
    private static String getCheckSum(String cmd, int round) {
        int lenth = cmd.length() / 2;
        int cmmSum = 0;
        for (int i = 0; i < lenth; i++) {
            //每两位转为16进制进行计算
            int c = Integer.valueOf(cmd.substring(0, 2), 0x10);
            cmd = cmd.substring(2);
            cmmSum = cmmSum + c;
        }
        String newString = Integer.toHexString(cmmSum);
        newString = "00000000000000000000000" + newString;//这里黑科技
        //这里获得我们需要返回的位数
        newString = newString.substring(newString.length() - round);
        return newString;
    }

    /**
     * 串口接收数据处理，使用EventBus总线
     * @param message
     */

    @Subscribe (threadMode = ThreadMode.MAIN) //675 440  740 450
    public void onMessageEvent(IMessage message) {
       // Log.i("dingyue", "onMessageEvent: "+message.getMessage()+ "  "+message.getMessage().substring(13,15));  //从getMessage()方法获取串口接收数据，后续进行处理
        Log.i("串口", "onMessageEvent: "+message.getMessage());
        if (message.getMessage().substring(0,4).equals("收到dsadsa")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SimpleActivity.this);
            builder.setIcon(R.drawable.ic_launcher_foreground);
            builder.setTitle("呼叫收");
            View view = LayoutInflater.from(SimpleActivity.this).inflate(R.layout.dialog_recv, null);
            builder.setView(view);
            TextView huijiaochuanhao = (TextView) view.findViewById(R.id.huijiaochuanhao);
            huijiaochuanhao.setText("111111234");
            Uri notification3 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
            final Ringtone r3 = RingtoneManager.getRingtone(getApplicationContext(), notification3);
            r3.play();

            builder.setPositiveButton("接听", new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    ToastUtil.show(SimpleActivity.this, "接听");
                    sendData(sendMingling_nodata(3, "ad")); //发送接通信息
                    textView_ComStatus.setText("通话状态：正在通话中");
                    running = true;
                    r3.stop();
                }
            });
            builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() //不接串口设备不能在这里面写接收语句，否则会自发自收
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    ToastUtil.show(SimpleActivity.this, "拒绝"); //发送拒绝信息
                    sendData(sendMingling_nodata(3, "25"));
                    r3.stop();
                }
            });
            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == 216 && event.getAction() == KeyEvent.ACTION_UP) { //取消按键,模拟点击屏幕取消按键的地点
                        sendTouchEvent(675, 445);
                        Log.i("按键输入", "dispatchKeyEvent: 输入取消");

                    }
                    if (keyCode == 215 && event.getAction() == KeyEvent.ACTION_UP) { //确认按键
                        sendTouchEvent(740,445);
                        Log.i("按键输入", "dispatchKeyEvent: 输入确认");
                    }
                    Log.i("按键输入", "onKey: "+keyCode);
                    return false;
                }
            } );

            builder.show();
        }

        if (message.getMessage().substring(13,15).equals("35")) {
            textView_ComStatus.setText("通话状态：正在通话中");
            running = true;
        }

        if (message.getMessage().substring(16,18).equals("12")) {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(SimpleActivity.this);
            builder2.setIcon(R.drawable.ring_icon);
            builder2.setTitle("收到到求救呼叫");
            View view2 = LayoutInflater.from(SimpleActivity.this).inflate(R.layout.dialog_send_qiujiu, null);
            builder2.setView(view2);

            builder2.setPositiveButton("接听", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    sendData(sendMingling_nodata(3, "35"));
                    textView_ComStatus.setText("求救通话中，按取消挂断");
                    running = true;
                }
            });
            builder2.setNegativeButton("拒绝", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    sendData(sendMingling_nodata(3, "25"));
                }
            });
            builder2.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == 216 && event.getAction() == KeyEvent.ACTION_UP) { //取消按键,模拟点击屏幕取消按键的地点
                        sendTouchEvent(678, 475);
                        Log.i("按键输入", "dispatchKeyEvent: 输入取消");

                    }
                    if (keyCode == 215 && event.getAction() == KeyEvent.ACTION_UP) { //确认按键
                        sendTouchEvent(742,475);
                        Log.i("按键输入", "dispatchKeyEvent: 输入确认");
                    }
                    Log.i("按键输入", "onKey: "+keyCode);
                    return false;
                }
            } );
            builder2.show();

        }

        if (message.getMessage().substring(13,15).equals("17")) {
            textView_ComStatus.setText("选呼收");
            running = true;
        }
    }

    /**
     * 初始化串口设备列表
     */
    private void initDevice() {

        mDevice = new Device("/dev/ttyS2", "115200"); //这里设置的
        switchSerialPort();
    }


    private void play_nocation() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
    }


    private void sendData(String text) {
        SerialPortManager.instance().sendCommand(text);
    }

    /**
     * 打开或关闭串口
     */
    private void switchSerialPort() {
        if (mOpened) {
            SerialPortManager.instance().close();
            mOpened = false;
        } else {
            mOpened = SerialPortManager.instance().open(mDevice) != null;
            if (mOpened) {
                ToastUtil.showOne(this, "成功与底层通信");
            } else {
                ToastUtil.showOne(this, "打开串口失败");
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (player != null) {
            player.onPause();
        }
        super.onPause();
    }

    @Override
    public void onNetChange(int state) {
        if (isFinishing()) {
            return;
        }
        ULog.d("dsiner: Network state--> " + state);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewGroup.LayoutParams lp = player.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            player.setLayoutParams(lp);
        } else {
            lp.height = Util.dip2px(getApplicationContext(), 180);
            player.setLayoutParams(lp);
        }
        if (player != null) {
            player.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        if (player != null && player.onBackPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void finish() {
        if (player != null) {
            player.onDestroy();
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        SerialPortManager.instance().close();
        NetBus.getIns().removeListener(this);
        super.onDestroy();
    }

    private void initView_advertise() {
        tv_switcher = findViewById(R.id.tv_switcher);
        tv_switcher2 = findViewById(R.id.tv_switcher2);
        tv_switcher3 = findViewById(R.id.tv_switcher3);;
        tv_switcher4 = findViewById(R.id.tv_switcher4);
        tv_switcher5 = findViewById(R.id.tv_switcher5);
        tv_switcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return new TextView(SimpleActivity.this);
            }
        });
        tv_switcher2.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return new TextView(SimpleActivity.this);
            }
        });
        tv_switcher3.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return new TextView(SimpleActivity.this);
            }
        });
        tv_switcher4.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return new TextView(SimpleActivity.this);
            }
        });
        tv_switcher5.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return new TextView(SimpleActivity.this);
            }
        });

        ArrayList<String> alist = new ArrayList<>();
        alist.clear();
//        for (int i = 0; i < 10; i++) {
//            alist.add("我是"+i);
        alist.add("大连昊洋科技发展有限公司1");
        alist.add("大连昊洋科技发展有限公司2");
        alist.add("大连昊洋科技发展有限公司3");
        alist.add("大连昊洋科技发展有限公司4");
        alist.add("大连昊洋科技发展有限公司5");



        ArrayList<String> alist2 = new ArrayList<>();
        alist2.clear();
        alist2.add("大连昊洋科技发展有限公司2");
        alist2.add("大连昊洋科技发展有限公司3");
        alist2.add("大连昊洋科技发展有限公司4");
        alist2.add("大连昊洋科技发展有限公司5");
        alist2.add("大连昊洋科技发展有限公司1");

        ArrayList<String> alist3 = new ArrayList<>();
        alist3.clear();
        alist3.add("大连昊洋科技发展有限公司3");
        alist3.add("大连昊洋科技发展有限公司4");
        alist3.add("大连昊洋科技发展有限公司5");
        alist3.add("大连昊洋科技发展有限公司1");
        alist3.add("大连昊洋科技发展有限公司2");

        ArrayList<String> alist4 = new ArrayList<>();
        alist4.clear();
        alist4.add("大连昊洋科技发展有限公司4");
        alist4.add("大连昊洋科技发展有限公司5");
        alist4.add("大连昊洋科技发展有限公司1");
        alist4.add("大连昊洋科技发展有限公司2");
        alist4.add("大连昊洋科技发展有限公司3");

        ArrayList<String> alist5 = new ArrayList<>();
        alist5.clear();
        alist5.add("大连昊洋科技发展有限公司5");
        alist5.add("大连昊洋科技发展有限公司1");
        alist5.add("大连昊洋科技发展有限公司2");
        alist5.add("大连昊洋科技发展有限公司3");
        alist5.add("大连昊洋科技发展有限公司4");

        new TextSwitcherAnimation(tv_switcher,alist).create();
        new TextSwitcherAnimation(tv_switcher2,alist2).create();
        new TextSwitcherAnimation(tv_switcher3,alist3).create();
        new TextSwitcherAnimation(tv_switcher4,alist4).create();
        new TextSwitcherAnimation(tv_switcher5,alist5).create();

    }
}
