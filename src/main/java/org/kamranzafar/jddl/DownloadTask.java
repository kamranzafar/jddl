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

import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kamran
 * 
 */
public class DownloadTask {
	private URL url;
	private OutputStream outputStream;
	private final List<DownloadListener> listeners = new ArrayList<DownloadListener>();

	private boolean paused = false;
	private boolean cancelled = false;
	private int timeout = 15000;

	private Authentication authentication;

	public DownloadTask(URL url, OutputStream outputStream) {
		this.url = url;
		this.outputStream = outputStream;
	}

	public DownloadTask(URL url, OutputStream outputStream, DownloadListener listener) {
		this.url = url;
		this.outputStream = outputStream;
		listeners.add(listener);
	}

	public URL getUrl() {
		return url;
	}

	public DownloadTask setUrl(URL url) {
		this.url = url;
		return this;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public DownloadTask setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
		return this;
	}

	public List<DownloadListener> getListeners() {
		return listeners;
	}

	public DownloadTask addListener(DownloadListener listener) {
		listeners.add(listener);
		return this;
	}

	public DownloadTask removeListener(DownloadListener listener) {
		listeners.remove(listener);
		return this;
	}

	public DownloadTask removeAllListener() {
		listeners.clear();
		return this;
	}

	public boolean isPaused() {
		return paused;
	}

	public DownloadTask setPaused(boolean paused) {
		this.paused = paused;
		return this;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public DownloadTask setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
		return this;
	}

	public int getTimeout() {
		return timeout;
	}

	public DownloadTask setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public Authentication getAuthentication() {
		return authentication;
	}

	public DownloadTask setAuthentication(Authentication authentication) {
		this.authentication = authentication;
		return this;
	}
}
