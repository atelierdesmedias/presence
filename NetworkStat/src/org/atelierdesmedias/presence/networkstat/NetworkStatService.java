package org.atelierdesmedias.presence.networkstat;

import java.io.IOException;
import java.util.Date;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class NetworkStatService extends IntentService {
	private HttpRequestInterceptor preemptiveAuth;

	public NetworkStatService() {
		super(NetworkStatService.class.getName());

		// Prepare preemptive filter
		this.preemptiveAuth = new HttpRequestInterceptor() {
			public void process(final HttpRequest request,
					final HttpContext context) throws HttpException,
					IOException {
				AuthState authState = (AuthState) context
						.getAttribute(ClientContext.TARGET_AUTH_STATE);
				CredentialsProvider credsProvider = (CredentialsProvider) context
						.getAttribute(ClientContext.CREDS_PROVIDER);
				HttpHost targetHost = (HttpHost) context
						.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

				if (authState.getAuthScheme() == null) {
					AuthScope authScope = new AuthScope(
							targetHost.getHostName(), targetHost.getPort());
					Credentials creds = credsProvider.getCredentials(authScope);
					if (creds != null) {
						authState.setAuthScheme(new BasicScheme());
						authState.setCredentials(creds);
					}
				}
			}
		};
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// ////////////////////////
		// Get stats
		// ////////////////////////

		byte[] stats;
		try {
			stats = NeufBoxUtils.getHostsList();
		} catch (Exception e) {
			Log.e(NetworkStatService.class.toString(),
					"Faile to get host list: ", e);

			return;
		}

		// ////////////////////////
		// Send stats
		// ////////////////////////

		try {
			CredentialsProvider credProvider = new BasicCredentialsProvider();

			// TODO: make credentials configurable
			credProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials("Admin", "admin"));

			DefaultHttpClient adminClient = new DefaultHttpClient();
			adminClient.setCredentialsProvider(credProvider);
			adminClient.addRequestInterceptor(preemptiveAuth, 0);

			// TODO: make the URL configurable
			HttpPost put = new HttpPost(
					"http://xwiki.atelier-medias.org/xwiki/bin/get/PresenceCode/StoreService?datetime="
							+ new Date().getTime());

			put.setEntity(new ByteArrayEntity(stats));

			HttpResponse response = adminClient.execute(put);

			StatusLine line = response.getStatusLine();
			if (line.getStatusCode() != 200) {
				Log.e(NetworkStatService.class.toString(),
						"Failed to send host list: " + line.getReasonPhrase()
								+ " (" + line.getStatusCode() + ")");
			}

			adminClient.getConnectionManager().shutdown();
		} catch (Exception e) {
			Log.e(NetworkStatService.class.toString(),
					"Failed to send host list: ", e);
		}
	}
}
