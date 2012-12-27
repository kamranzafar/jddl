/**
 * Copyright 2012 Kamran Zafar 
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
 * 
 */

package org.kamranzafar.jddl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author Kamran
 * 
 */
public class HttpConnector {

	protected int BUFFER_SIZE = 2048;
	protected int DEFAULT_STREAM_BUFFER_SIZE = 3072;
	protected int DEFAULT_CONNECT_TIMEOUT = 13000;

	private int connectionTimeout = DEFAULT_CONNECT_TIMEOUT;

	private final Map<String, String> headers = new HashMap<String, String>();
	private String requestMethod = "GET";

	public HttpConnector() {
		HttpURLConnection.setFollowRedirects(true);
	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	protected URLConnection getConnection(URL url) throws IOException, KeyManagementException, NoSuchAlgorithmException {
		if ("http".equalsIgnoreCase(url.getProtocol()) || "ftp".equalsIgnoreCase(url.getProtocol())) {
			return getConnection(url, null);
		} else if ("https".equalsIgnoreCase(url.getProtocol())) {
			return getSecureConnection(url);
		}

		return null;
	}

	/**
	 * @param url
	 * @param proxy
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	protected URLConnection getConnection(URL url, Proxy proxy) throws IOException, KeyManagementException,
			NoSuchAlgorithmException {
		if ("http".equalsIgnoreCase(url.getProtocol()) || "ftp".equalsIgnoreCase(url.getProtocol())) {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy == null ? Proxy.NO_PROXY : proxy);
			conn.setRequestMethod(requestMethod);
			setHeaders(conn);
			return conn;
		} else if ("https".equalsIgnoreCase(url.getProtocol())) {
			return getSecureConnection(url, proxy);
		}

		return null;
	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	protected HttpsURLConnection getSecureConnection(URL url) throws IOException, KeyManagementException,
			NoSuchAlgorithmException {
		return getSecureConnection(url, null);
	}

	/**
	 * @param url
	 * @param proxy
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	protected HttpsURLConnection getSecureConnection(URL url, Proxy proxy) throws IOException,
			NoSuchAlgorithmException, KeyManagementException {

		SSLContext context = SSLContext.getInstance("TLS");
		context.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());

		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection(proxy == null ? Proxy.NO_PROXY : proxy);
		conn.setRequestMethod(requestMethod);
		setHeaders(conn);

		conn.setSSLSocketFactory(context.getSocketFactory());
		conn.setHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});

		conn.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
		return conn;
	}

	/**
	 * @param conn
	 * @param data
	 * @throws IOException
	 */
	protected void doOutput(URLConnection conn, String data) throws IOException {
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()),
				DEFAULT_STREAM_BUFFER_SIZE);

		wr.write(data);
		wr.flush();
		wr.close();
	}

	/**
	 * @param conn
	 * @return
	 * @throws IOException
	 */
	protected StringBuffer doInput(URLConnection conn) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()), DEFAULT_STREAM_BUFFER_SIZE);

		StringBuffer buff = new StringBuffer();

		char[] bb = new char[BUFFER_SIZE];
		int nob;

		while ((nob = rd.read(bb)) != -1) {
			buff.append(new String(bb, 0, nob));
		}

		rd.close();

		return buff;
	}

	protected final static class DefaultTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

	private void setHeaders(URLConnection uc) {
		Iterator<String> itr = headers.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			uc.addRequestProperty(key, headers.get(key));
		}
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}
}
