package org.atelierdesmedias.presence.networkstat;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.atelierdesmedias.presence.networkstat.ssl.EasySSLSocketFactory;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class NetworkStatService extends IntentService {
	//private HttpRequestInterceptor preemptiveAuth;
	private ClientConnectionManager clientConnectionManager;
	private HttpParams httpParams;
	private HttpContext httpContext;

	public NetworkStatService() throws NoSuchAlgorithmException,
			KeyManagementException {
		super(NetworkStatService.class.getName());

		SchemeRegistry schemeRegistry = new SchemeRegistry();

		// http scheme
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		// https scheme
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(),
				443));

		this.httpParams = new BasicHttpParams();
		this.httpParams.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
		this.httpParams.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
				new ConnPerRouteBean(1));
		this.httpParams.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(httpParams, "utf8");

		/*CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		// TODO: make credentials configurable
		credentialsProvider.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials("Admin", "admin"));*/
		this.clientConnectionManager = new ThreadSafeClientConnManager(httpParams,
				schemeRegistry);

		this.httpContext = new BasicHttpContext();
		/*this.context.setAttribute("http.auth.credentials-provider",
				credentialsProvider);*/

		// Prepare preemptive filter
		/*this.preemptiveAuth = new HttpRequestInterceptor() {
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
		};*/
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
			DefaultHttpClient adminClient = new DefaultHttpClient(this.clientConnectionManager, this.httpParams);

			/*CredentialsProvider credProvider = new BasicCredentialsProvider();

			// TODO: make credentials configurable
			credProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials("Admin", "admin"));

			adminClient.setCredentialsProvider(credProvider);
			adminClient.addRequestInterceptor(preemptiveAuth, 0);*/

			// TODO: make the URL configurable
			HttpPost put = new HttpPost(
					"https://xwiki.atelier-medias.org/xwiki/bin/get/PresenceCode/StoreService?datetime="
							+ new Date().getTime());

			put.setEntity(new ByteArrayEntity(stats));

			HttpResponse response = adminClient.execute(put, this.httpContext);

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
