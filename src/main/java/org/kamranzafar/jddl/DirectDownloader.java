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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.kamranzafar.jddl.util.Base64;

/**
 * @author kamran
 */
public class DirectDownloader extends HttpConnector implements Runnable {
    private int poolSize = 3;
    private int bufferSize = 2048;

    private DirectDownloadThread[] dts;
    private Proxy proxy;
    private final BlockingQueue<DownloadTask> tasks = new LinkedBlockingQueue<>();

    private static Logger logger = Logger.getLogger(DirectDownloader.class.getName());

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

        private boolean cancel = false;
        private boolean stop = false;

        private final BlockingQueue<DownloadTask> tasks;

        public DirectDownloadThread(BlockingQueue<DownloadTask> tasks) {
            this.tasks = tasks;
        }

        protected void download(DownloadTask dt) throws IOException, InterruptedException, KeyManagementException,
                NoSuchAlgorithmException {
            HttpURLConnection conn = (HttpURLConnection) getConnection(dt.getUrl(), proxy);

            if (dt.getAuthentication() != null) {
                conn.setRequestProperty("Authorization", dt.getAuthentication().getRequestHeader());
            }

            conn.setReadTimeout(dt.getTimeout());
            conn.setDoOutput(true);
            conn.connect();

            int fsize = conn.getContentLength();
            String fname;

            String cd = conn.getHeaderField(CONTENT_DISPOSITION);

            if (cd != null) {
                fname = cd.substring(cd.indexOf(CD_FNAME) + 1, cd.length() - 1);
            } else {
                String url = dt.getUrl().toString();
                fname = url.substring(url.lastIndexOf('/') + 1);
            }

            InputStream is = conn.getInputStream();

            OutputStream os = dt.getOutputStream();
            List<DownloadListener> listeners = dt.getListeners();

            byte[] buff = new byte[bufferSize];
            int res;

            for (DownloadListener listener : listeners) {
                listener.onStart(fname, fsize);
            }

            int total = 0;
            while ((res = is.read(buff)) != -1) {
                os.write(buff, 0, res);
                total += res;
                for (DownloadListener listener : listeners) {
                    listener.onUpdate(res, total);
                }

                synchronized (dt) {
                    // cancel download
                    if (cancel || dt.isCancelled()) {
                        close(is, os);
                        for (DownloadListener listener : listeners) {
                            listener.onCancel();
                        }

                        throw new RuntimeException("Cancelled download");
                    }

                    // stop thread
                    if (stop) {
                        close(is, os);
                        for (DownloadListener listener : listeners) {
                            listener.onCancel();
                        }

                        throw new InterruptedException("Shutdown");
                    }

                    // pause thread
                    while (dt.isPaused()) {
                        try {
                            wait();
                        } catch (Exception e) {
                        }
                    }
                }
            }

            for (DownloadListener listener : listeners) {
                listener.onComplete();
            }

            close(is, os);
        }

        private void close(InputStream is, OutputStream os) {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    download(tasks.take());
                } catch (InterruptedException e) {
                    logger.info("Stopping download thread");
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            cancel = true;
        }

        public void shutdown() {
            stop = true;
        }
    }

    public void download(DownloadTask dt) {
        tasks.add(dt);
    }

    public void run() {
        logger.info("Initializing downloader...");

        dts = new DirectDownloadThread[poolSize];

        for (int i = 0; i < dts.length; i++) {
            dts[i] = new DirectDownloadThread(tasks);
            dts[i].start();
        }

        logger.info("Downloader started, waiting for tasks.");
    }

    public void shutdown() {
        for (final DirectDownloadThread dt : dts) {
            if (dt != null) {
                dt.shutdown();
            }
        }
    }

    public void cancelAll() {
        for (final DirectDownloadThread dt : dts) {
            if (dt != null) {
                dt.cancel();
            }
        }
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
