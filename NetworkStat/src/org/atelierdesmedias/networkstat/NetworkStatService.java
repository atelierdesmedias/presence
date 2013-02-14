package org.atelierdesmedias.networkstat;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NetworkStatService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// ////////////////////////
		// Get stats
		// ////////////////////////

		byte[] stats;
		try {
			stats = NeufBoxUtils.getHostsList();
		} catch (Exception e) {
			Log.e(NetworkStatService.class.toString(),
					"Faile to get host list: ", e);

			return START_STICKY;
		}

		// ////////////////////////
		// Send stats
		// ////////////////////////

		try {
			CredentialsProvider credProvider = new BasicCredentialsProvider();
			credProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials("Admin", "admin"));

			DefaultHttpClient adminClient = new DefaultHttpClient();
			adminClient.setCredentialsProvider(credProvider);

			HttpPut put = new HttpPut(
					"http://intra.atelier-medias.org/xwiki/bin/view/Admin/NetworkStatReceiver");

			put.setEntity(new ByteArrayEntity(stats));
		} catch (Exception e) {
			Log.e(NetworkStatService.class.toString(),
					"Failed to send host list: ", e);
		}

		return START_STICKY;
	}
}
