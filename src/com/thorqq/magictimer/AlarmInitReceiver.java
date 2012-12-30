/**
 * Copyright (C) 2007 The Android Open Source Project
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

package com.thorqq.magictimer;

import com.thorqq.magictimer.core.TimerMgr;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

public class AlarmInitReceiver extends BroadcastReceiver {

    /***
     * Sets alarm on ACTION_BOOT_COMPLETED.  Resets alarm on
     * TIME_SET, TIMEZONE_CHANGED
     */
    // 系统系统、时间设置、时区调整时被调用
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (context.getContentResolver() == null) {
            return;
        }
        // ACTION_BOOT_COMPLETED 开机时所发出的系统intent
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            TimerMgr.disableSnoozeTimer(context);
            TimerMgr.disableExpiredTimers();
        }
        // 计算下一个闹铃
        TimerMgr.setNextTimer(context, TimerMgr.SPEC_TIME_ID_NONE);
    }
}

