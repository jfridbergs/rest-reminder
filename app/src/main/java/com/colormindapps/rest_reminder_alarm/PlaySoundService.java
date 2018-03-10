/*
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

package com.colormindapps.rest_reminder_alarm;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;


/**
 * Manages alarms and vibe. Runs as a service so that it can continue to play
 * if another activity overrides the AlarmAlert dialog.
 */
public class PlaySoundService extends Service {

    /** Play alarm up to 10 minutes before silencing */


    private boolean mPlaying = false;
    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;
    private int mInitialCallState;
    private int type;
    private AudioManager audioManager;

    // Internal messages
    private static final int KILLER = 1000;
    private  Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KILLER:
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
            if (state != TelephonyManager.CALL_STATE_IDLE
                    && state != mInitialCallState) {
                stopSelf();
            }
        }
    };

    @Override
    public void onCreate() {
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Listen for incoming calls to kill the alarm.
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        
    }

    @Override
    public void onDestroy() {
        stop();
        // Stop listening for incoming calls.
        PlaySoundWakeLock.releaseCpuLock();
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // No intent, tell the system not to restart us.
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }



        type = intent.getExtras().getInt(RReminder.PERIOD_TYPE);

        play();
        // Record the initial call state here so that the new alarm has the
        // newest state.
        PlaySoundWakeLock.acquireCpuWakeLock(this);
        return START_STICKY;
    }



    // Volume suggested by media team for in-call alarms.
    private static final float IN_CALL_VOLUME = 0.125f;

    private void play() {
        // stop() checks to see if we are already playing.
        stop();


        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {

            // Fall back on the default alarm if the database does not have an
            // alarm stored.


            // TODO: Reuse mMediaPlayer instead of creating a new one and/or use
            // RingtoneManager.
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mp.stop();
                    mp.reset();
                    mp.release();
                    mMediaPlayer = null;
                    stopSelf();
                    return true;
                    
                }
            });
            
            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                	mp.stop();
                    mp.reset();
                    mp.release();
                    //mp = null;
                    mPlaying = false;
                    PlaySoundWakeLock.releaseCpuLock();
                    stopSelf();
                }

            }); 

            try {
                // Check if we are in a call. If we are, use the in-call alarm
                // resource at a low volume to not disrupt the call.
                	mMediaPlayer.setDataSource(this, RReminder.getRingtone(this, type));
                startAlarm(mMediaPlayer);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /* Start the vibrator after everything is ok with the media player */
		if(audioManager.getRingerMode()!=AudioManager.RINGER_MODE_SILENT && type!=RReminder.APPROXIMATE){
   			if (Build.VERSION.SDK_INT >= 11) {
				if(RReminder.isVibrateEnabled(this, mVibrator)){
					// Vibrate for 300 milliseconds
					if(type ==RReminder.APPROXIMATE){
						mVibrator.vibrate(100);
					} else {
						mVibrator.vibrate(300);
					}
				}
			} else {
				if(RReminder.isVibrateEnabledSupport(this)){
					// Vibrate for 300 milliseconds
					if(type ==RReminder.APPROXIMATE){
						mVibrator.vibrate(100);
					} else {
						mVibrator.vibrate(300);
					}	
				}
			}
		}

        mPlaying = true;
    }

    // Do the common stuff when starting the alarm.
    private void startAlarm(MediaPlayer player)
            throws java.io.IOException, IllegalArgumentException,
                   IllegalStateException {
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        // do not play alarms if stream volume is 0
        // (typically because ringer mode is silent).
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(false);
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

    /**
     * Stops alarm audio and disables alarm if it not snoozed and not
     * repeating
     */
    public void stop() {
        if (mPlaying) {
            mPlaying = false;

            // Stop audio playing
            if (mMediaPlayer != null) {

                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            // Stop vibrator
            mVibrator.cancel();
        }
        disableKiller();
    }

    /**
     * Kills alarm audio after ALARM_TIMEOUT_SECONDS, so the alarm
     * won't run all day.
     *
     * This just cancels the audio, but leaves the notification
     * popped, so the user will know that the alarm tripped.
     */


    private void disableKiller() {
        mHandler.removeMessages(KILLER);
    }


}
