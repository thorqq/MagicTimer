/**
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thorqq.magictimer.action;

//import com.android.deskclock2.R;

import com.thorqq.magictimer.AlarmAlertWakeLock;
import com.thorqq.magictimer.R;
import com.thorqq.magictimer.core.Timer;
import com.thorqq.magictimer.core.TimerMgr;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/***
 * Manages alarms and vibe. Runs as a service so that it can continue to play
 * if another activity overrides the AlarmAlert dialog.
 */
//Klaxon:喇叭。 在后台播放闹铃和震动
public class AlarmKlaxon extends Service {

    //TODO: 闹铃持续时间，默认为30秒。需可配置
    private static final int ALARM_TIMEOUT_SECONDS = 30;

    private static final long[] sVibratePattern = new long[] { 500, 500 };

    private boolean mPlaying = false;
    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;
    private Timer mCurrentAlarm;
    private long mStartTime;
    private TelephonyManager mTelephonyManager;
    private int mInitialCallState;

    // Internal messages
    private static final int KILLER = 1000;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KILLER:
                     sendKillBroadcast((Timer) msg.obj);
                    stopSelf();
                    break;
            }
        }
    };

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String ignored) {
            // The user might already be in a call when the alarm fires. When
            // we register onCallStateChanged, we get the initial in-call state
            // which kills the alarm. Check against the initial call state so
            // we don't kill the alarm during a call.
            // 如果有通话进来，则停止闹铃
            if (state != TelephonyManager.CALL_STATE_IDLE
                    && state != mInitialCallState) {
                sendKillBroadcast(mCurrentAlarm);
                stopSelf();
            }
        }
    };

    @Override
    public void onCreate() {
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Listen for incoming calls to kill the alarm.
        // 监听通话状态
        mTelephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(
                mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        //管理屏幕亮度和键盘锁
        AlarmAlertWakeLock.acquireCpuWakeLock(this);
    }

    @Override
    public void onDestroy() {
        // 停止闹铃
        stop();
        // Stop listening for incoming calls.
        // 停止监听来电状态
        mTelephonyManager.listen(mPhoneStateListener, 0);
        AlarmAlertWakeLock.releaseCpuLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 客户端调用 startService(Intent)启动服务的时候会调用此方法
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // No intent, tell the system not to restart us.
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        final Timer alarm = intent.getParcelableExtra(
                TimerMgr.ALARM_INTENT_EXTRA);

        if (alarm == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        //已经存在alarm，则先删除当前的
        if (mCurrentAlarm != null) {
            sendKillBroadcast(mCurrentAlarm);
        }

        //开始播放和震动
        play(alarm);
        mCurrentAlarm = alarm;
        // Record the initial call state here so that the new alarm has the
        // newest state.
        // 记录当前的呼叫状态
        mInitialCallState = mTelephonyManager.getCallState();

        return START_STICKY;
    }

    // 通知界面此闹铃被删掉了
    private void sendKillBroadcast(Timer alarm) {
        long millis = System.currentTimeMillis() - mStartTime;
        int minutes = (int) Math.round(millis / 60000.0);
        Intent alarmKilled = new Intent(TimerMgr.ALARM_KILLED);
        alarmKilled.putExtra(TimerMgr.ALARM_INTENT_EXTRA, alarm);
        alarmKilled.putExtra(TimerMgr.ALARM_KILLED_TIMEOUT, minutes);
        sendBroadcast(alarmKilled);
    }

    // Volume suggested by media team for in-call alarms.
    private static final float IN_CALL_VOLUME = 0.125f;

    private void play(Timer alarm) {
        // stop() checks to see if we are already playing.
        // 先停止铃声和震动
        stop();


//        if (!alarm.silent) 
        {
//            Uri alert = alarm.alert;
//            // Fall back on the default alarm if the database does not have an
//            // alarm stored.
//            // 无法找到指定的铃声，则用默认铃声代替
//            if (alert == null) {
//                alert = RingtoneManager.getDefaultUri(
//                        RingtoneManager.TYPE_ALARM);
//            }
            //TODO 临时使用默认铃声
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            // TODO: Reuse mMediaPlayer instead of creating a new one and/or use
            // RingtoneManager.
            mMediaPlayer = new MediaPlayer();
            //错误捕获
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mp.stop();
                    mp.release();
                    mMediaPlayer = null;
                    return true;
                }
            });

            try {
                // Check if we are in a call. If we are, use the in-call alarm
                // resource at a low volume to not disrupt the call.
                // 检查一下是否正在通话，如果正在通话，则使用低音量的通话内铃声，不打断通话
                if (mTelephonyManager.getCallState()
                        != TelephonyManager.CALL_STATE_IDLE) {
                    mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
                    setDataSourceFromResource(getResources(), mMediaPlayer,
                            R.raw.in_call_alarm);
                } else {
                    mMediaPlayer.setDataSource(this, alert);
                }
                //开始播放闹铃
                //TODO  临时删掉，不播放闹铃，只震动
                //startAlarm(mMediaPlayer);
            } catch (Exception ex) {
                // The alert may be on the sd card which could be busy right
                // now. Use the fallback ringtone.
                // 如果出现异常，则使用默认铃声
                try {
                    // Must reset the media player to clear the error state.
                    mMediaPlayer.reset();
                    setDataSourceFromResource(getResources(), mMediaPlayer,
                            R.raw.fallbackring);
                    startAlarm(mMediaPlayer);
                } catch (Exception ex2) {
                    // At this point we just don't play anything.
                }
            }
        }

        /** Start the vibrator after everything is ok with the media player */
        // 最后开始震动
//        if (alarm.vibrate) {
            mVibrator.vibrate(sVibratePattern, 0);
//        } else {
//            mVibrator.cancel();
//        }

        //在指定时间后删除闹铃声音
        enableKiller(alarm);
        mPlaying = true;
        mStartTime = System.currentTimeMillis();
    }

    // Do the common stuff when starting the alarm.
    // 开始播放闹铃
    private void startAlarm(MediaPlayer player)
            throws java.io.IOException, IllegalArgumentException,
                   IllegalStateException {
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        // do not play alarms if stream volume is 0
        // (typically because ringer mode is silent).
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
            player.prepare();
            player.start();
        }
    }

    private void setDataSourceFromResource(Resources resources,
            MediaPlayer player, int res) throws java.io.IOException {
        AssetFileDescriptor afd = resources.openRawResourceFd(res);
        if (afd != null) {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                    afd.getLength());
            afd.close();
        }
    }

    /***
     * Stops alarm audio and disables alarm if it not snoozed and not
     * repeating
     */
    // 停止铃声和震动，禁用闹铃
    public void stop() {
        if (mPlaying) {
            mPlaying = false;

            Intent alarmDone = new Intent(TimerMgr.ALARM_DONE_ACTION);
            sendBroadcast(alarmDone);

            // Stop audio playing
            //停止播放铃声
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            // Stop vibrator
            //停止震动
            mVibrator.cancel();
        }
        //删除消息接收
        disableKiller();
    }

    /***
     * Kills alarm audio after ALARM_TIMEOUT_SECONDS, so the alarm
     * won't run all day.
     *
     * This just cancels the audio, but leaves the notification
     * popped, so the user will know that the alarm tripped.
     */
    //在指定时间后删除闹铃声音，这样闹铃就不会整天响了。只关闭声音，通知栏上的通知不管
    private void enableKiller(Timer alarm) {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER, alarm),
                1000 * ALARM_TIMEOUT_SECONDS);
    }

    private void disableKiller() {
        mHandler.removeMessages(KILLER);
    }


}

