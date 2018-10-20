package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.os.PowerManager;


class PlaySoundWakeLock {
	private static final String LOCK_NAME_STATIC_SOUND="com.colormindapps.rest_reminder_alarm.StaticSound";
    private static PowerManager.WakeLock sCpuWakeLock;

    static void acquireCpuWakeLock(Context context) {
        if (sCpuWakeLock != null) {
            return;
        }

        PowerManager pm =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm!=null)
        sCpuWakeLock = pm.newWakeLock(
        		PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC_SOUND);
        //NOTE: adding a timeout value ended up in inconsistent behaviour, which sometimes ended in problems with launching periodservice
        sCpuWakeLock.acquire();
    }

    static void releaseCpuLock() {
        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}
