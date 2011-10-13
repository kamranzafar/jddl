/**
 *  jddl - Java Direct Download Lib
 *
 *  Copyright (C) 2011  Kamran Zafar
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * @author kamran
 * 
 */
public class DirectDownloader implements Runnable {
    private int poolSize = 3;
    private int bufferSize = 2048;

    private DirectDownloadThread[] dts;
    private Proxy proxy;
    private final BlockingQueue<DownloadTask> tasks = new LinkedBlockingQueue<DownloadTask>();

    private static Logger logger = Logger.getLogger( DirectDownloader.class.getName() );

    public DirectDownloader() {
    }

    public DirectDownloader(int poolSize) {
        this.poolSize = poolSize;
    }

    public DirectDownloader(Proxy proxy) {
        this.proxy = proxy;
    }

    public DirectDownloader(Proxy proxy, int poolSize) {
        this.poolSize = poolSize;
        this.proxy = proxy;
    }

    protected class DirectDownloadThread extends Thread {
        private static final String CD_FNAME = "fname=";
        private static final String CONTENT_DISPOSITION = "Content-Disposition";
        private static final String GET = "GET";

        private final BlockingQueue<DownloadTask> tasks;

        public DirectDownloadThread(BlockingQueue<DownloadTask> tasks) {
            this.tasks = tasks;
        }

        protected void download(DownloadTask dt) throws IOException {
            HttpURLConnection conn = (HttpURLConnection) dt.getUrl().openConnection(
                    proxy == null ? Proxy.NO_PROXY : proxy );

            conn.setReadTimeout( dt.getTimeout() );
            conn.setRequestMethod( GET );
            conn.setDoOutput( true );
            conn.connect();

            int fsize = conn.getContentLength();
            String fname;

            String cd = conn.getHeaderField( CONTENT_DISPOSITION );

            if (cd != null) {
                fname = cd.substring( cd.indexOf( CD_FNAME ) + 1, cd.length() - 1 );
            } else {
                String url = dt.getUrl().toString();
                fname = url.substring( url.lastIndexOf( '/' ) + 1 );
            }

            InputStream is = conn.getInputStream();

            OutputStream os = dt.getOutputStream();
            List<DownloadListener> listeners = dt.getListeners();

            byte[] buff = new byte[bufferSize];
            int res;

            for (DownloadListener listener : listeners) {
                listener.onStart( fname, fsize );
            }

            int total = 0;
            while (( res = is.read( buff ) ) != -1) {
                os.write( buff, 0, res );
                total += res;
                for (DownloadListener listener : listeners) {
                    listener.onUpdate( res, total );
                }

                synchronized (dt) {
                    while (dt.isPaused()) {
                        try {
                            wait();
                        } catch (Exception e) {
                        }
                    }
                }
            }

            try {
                is.close();
                os.close();
            } catch (IOException e) {
            }

            for (DownloadListener listener : listeners) {
                listener.onComplete();
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    download( tasks.take() );
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public void download(DownloadTask dt) {
        tasks.add( dt );
    }

    public void run() {
        logger.info( "Initializing downloader..." );

        dts = new DirectDownloadThread[poolSize];

        for (DirectDownloadThread t : dts) {
            t = new DirectDownloadThread( tasks );
            t.start();
        }

        logger.info( "Downloader started, waiting for tasks." );
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}
