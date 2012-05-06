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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

/**
 * @author Kamran
 * 
 *         This is just an template/example that shows how jddl can be used in
 *         Android apps, the following code should be updated as per
 *         requirements.
 * 
 */
public class AndroidExample extends Activity {
    public static final int DOWNLOAD_PROGRESS_DIALOG_ID = 0;

    private DirectDownloader dd = new DirectDownloader();

    private ProgressDialog mProgressDialog;

    File downloadDir = new File( Environment.getExternalStorageDirectory()
            + "/Android/data/org.kamranzafar.android.test/files" );

    private final Handler callback = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO: update UI etc.
        };
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        // TODO: initializations etc.
        // this should be updated as per requirements

        downloadDir.mkdirs();

        // start downloading... this can be called from button clicks etc.
        new DownloadFileAsync().download( "http://www.python.org/ftp/python/3.2.2/python-3.2.2.msi" );
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DOWNLOAD_PROGRESS_DIALOG_ID:
            mProgressDialog = new ProgressDialog( this );
            mProgressDialog.setMessage( "Downloading..." );
            mProgressDialog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
            mProgressDialog.setCancelable( true );
            mProgressDialog.setOnCancelListener( new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    dd.cancelAll();
                }
            } );
            mProgressDialog.show();

            return mProgressDialog;
        default:
            return null;
        }
    }

    /**
     * shutdown jddl
     */
    @Override
    protected void onPause() {
        dd.shutdown();
        dd = null;

        super.onPause();
    }

    /**
     * start jddl
     */
    @Override
    protected void onResume() {
        super.onResume();

        dd = new DirectDownloader();
        new Thread( dd ).start();
    }

    // This is an example download listner, that can be used to update UI and to
    // track download progress
    class DownloadFileAsync implements DownloadListener {
        private int fsize = -1;
        private String fname;

        private File downloadFile;

        public String download(String url) {
            fname = url.substring( url.lastIndexOf( '/' ) + 1 );
            downloadFile = new File( downloadDir, fname );
            try {
                DownloadTask dt = new DownloadTask( new URL( url ), new FileOutputStream( downloadFile ), this );
                dd.download( dt );
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // show progress dialog
                showDialog( DOWNLOAD_PROGRESS_DIALOG_ID );
            }
            return null;
        }

        public void onComplete() {
            // dismiss progress dialog
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            // an example call back to update UI etc.
            Bundle b = new Bundle();
            b.putString( "my_message", "download complete" );

            Message m = new Message();
            m.setData( b );

            callback.sendMessage( m );
        }

        public void onStart(String fname, int arg0) {
            fsize = arg0;
        }

        public void onUpdate(int arg0, int arg1) {
            // update progress dialog
            mProgressDialog.setProgress( ( arg1 * 100 ) / fsize );
        }

        public void onCancel() {
            // delete partly downloaded file if the download is cancelled.
            if (downloadFile.exists()) {
                downloadFile.delete();
            }
        }
    }
}