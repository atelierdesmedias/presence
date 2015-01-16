package org.atelierdesmedias.presence.networkstat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent intentService = new Intent(context, NetworkStatService.class);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0,
				intentService, 0);

		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR,
				pendingIntent);

		Intent serviceIntent = new Intent(context, NetworkStatService.class);
		context.startService(serviceIntent);
	}
}