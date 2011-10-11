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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DirectDownloader implements Runnable {
    private int poolSize = 3;
    private DirectDownloadThread[] dts;
    private Proxy proxy;
    private final BlockingQueue<DownloadTask> tasks = new LinkedBlockingQueue<DownloadTask>();

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
        private int bufferSize = 1024;
        private final BlockingQueue<DownloadTask> tasks;

        public DirectDownloadThread(BlockingQueue<DownloadTask> tasks) {
            this.tasks = tasks;
        }

        public DirectDownloadThread(BlockingQueue<DownloadTask> tasks, int bufferSize) {
            this.tasks = tasks;
            this.bufferSize = bufferSize;
        }

        protected void download(DownloadTask fdt) throws IOException {
            HttpURLConnection conn = (HttpURLConnection) fdt.getUrl().openConnection(
                    proxy == null ? Proxy.NO_PROXY : proxy );

            conn.setRequestMethod( "GET" );
            conn.setDoOutput( true );
            conn.connect();

            int fsize = conn.getContentLength();

            InputStream is = conn.getInputStream();

            OutputStream os = fdt.getOutputStream();
            DownloadListener listener = fdt.getListener();

            byte[] buff = new byte[bufferSize];
            int res;

            if (listener != null) {
                listener.onStart( fsize );
            }

            int total = 0;
            while (( res = is.read( buff ) ) != -1) {
                os.write( buff, 0, res );
                total += res;
                if (listener != null) {
                    listener.onUpdate( res, total );
                }
            }

            try {
                is.close();
                os.close();
            } catch (IOException e) {
            }

            if (listener != null) {
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
        System.out.println( "Initializing downloader..." );
        dts = new DirectDownloadThread[poolSize];

        for (DirectDownloadThread t : dts) {
            t = new DirectDownloadThread( tasks );
            t.start();
        }

        System.out.println( "Downloader started, waiting for tasks." );
    }
}
