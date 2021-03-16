package com.d.commenplayer;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.d.commenplayer.activity.MapActivity;
import com.d.commenplayer.activity.SimpleActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       init();
    }

    /**
     * Author:yejian
     * Function:listener the keyboard press key
     * Data:2019-12-3
     * 按键组件，将要给按键组合使用*/

    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
        switch (event.getKeyCode()){
            case KeyEvent.KEYCODE_F1:
                Toast.makeText(this,"按下按键F1",Toast.LENGTH_SHORT).show();
                break;
            case KeyEvent.KEYCODE_F2:
                Toast.makeText(this,"按下按键F2",Toast.LENGTH_SHORT).show();
                break;
            case KeyEvent.KEYCODE_F3:
                Toast.makeText(this,"按下按键F3",Toast.LENGTH_SHORT).show();
                break;
            case KeyEvent.KEYCODE_F4:
                Toast.makeText(this,"按下按键F4",Toast.LENGTH_SHORT).show();
                break;
            case KeyEvent.KEYCODE_F5:
                Toast.makeText(this,"按下按键F5",Toast.LENGTH_SHORT).show();
                break;
            case KeyEvent.KEYCODE_F6:
                Toast.makeText(this,"按下按键F6",Toast.LENGTH_SHORT).show();
                break;
            case KeyEvent.KEYCODE_F7:
                Toast.makeText(this,"按下按键F7",Toast.LENGTH_SHORT).show();
                break;
            case KeyEvent.KEYCODE_F8:
                Toast.makeText(this,"按下按键F8",Toast.LENGTH_SHORT).show();
                break;
            case KeyEvent.KEYCODE_M:
                Toast.makeText(this,"按下按键M",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,MapActivity.class);
                startActivityForResult(intent,1);
                break;
            case KeyEvent.KEYCODE_V:
                Toast.makeText(this,"按下按键V",Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(MainActivity.this,SimpleActivity.class);
                startActivityForResult(intent2,1);
                break;
        }

        return super.dispatchKeyEvent(event);
    }

    private void init() {
        findViewById(R.id.btn_simple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SimpleActivity.class));
            }
        });
        findViewById(R.id.btn_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ListActivity.class));
            }
        });
    }
}
