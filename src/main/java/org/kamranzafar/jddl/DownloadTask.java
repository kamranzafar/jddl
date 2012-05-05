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

    private boolean paused;
    private final int timeout = 15000;

    public DownloadTask(URL url, OutputStream outputStream) {
        this.url = url;
        this.outputStream = outputStream;
    }

    public DownloadTask(URL url, OutputStream outputStream, DownloadListener listener) {
        this.url = url;
        this.outputStream = outputStream;
        listeners.add( listener );
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public List<DownloadListener> getListeners() {
        return listeners;
    }

    public void addListener(DownloadListener listener) {
        listeners.add( listener );
    }

    public void removeListener(DownloadListener listener) {
        listeners.remove( listener );
    }

    public void removeAllListener() {
        listeners.clear();
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public int getTimeout() {
        return timeout;
    }
}
