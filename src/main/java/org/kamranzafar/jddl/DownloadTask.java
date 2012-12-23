/**
 *  jddl - Java Direct Download Lib
 *
 *  Copyright (C) 2012  Kamran Zafar
 *
 *  This file is part of Jar Class Loader (JCL).
 *  Jar Class Loader (JCL) is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JarClassLoader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JCL.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  @author Kamran Zafar
 *
 *  Contact Info:
 *  Email:  xeus.man@gmail.com
 *  Web:    http://kamranzafar.org
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
