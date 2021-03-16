package com.d.commenplayer.comn.message;

import android.util.Log;

import com.d.commenplayer.util.TimeUtil;


/**
 * 收到的日志
 */

public class RecvMessage implements IMessage {

    private String command;
    private String message;

    public RecvMessage(String command) {
        this.command = command;
        //this.message = TimeUtil.currentTime() + "    收到命令：" + command;
        this.message =  command;
        Log.i("命令", "SendMessage: "+"    收到命令：" + command);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isToSend() {
        return false;
    }
}
