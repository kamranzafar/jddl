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

import java.awt.Container;
import java.awt.GridLayout;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author kamran
 * 
 */
@RunWith(JUnit4.class)
public class SwingTest {
    // total progress bar
    private final JProgressBar totalProgressBar = new JProgressBar();

    @Test
    public void testSwing() throws IOException, InterruptedException {
        JFrame f = new JFrame( "jddl Swing example" );
        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        Container content = f.getContentPane();
        content.setLayout( new GridLayout( 4, 1 ) );

        // Some files
        String files[] = { "http://python.org/ftp/python/2.7.2/python-2.7.2.msi",
                "http://www.python.org/ftp/python/3.2.2/python-3.2.2.msi",
                "http://www.python.org/ftp/python/3.2.2/python-3.2.2.amd64.msi" };

        // Progress bars for individual file downloads
        JProgressBar[] progressBar = new JProgressBar[3];

        // Create a DirectDownloader instance
        DirectDownloader fd = new DirectDownloader();

        // Initialize progress bars and create download tasks
        for (int i = 0; i < 3; i++) {
            String fname = files[i].substring( files[i].lastIndexOf( '/' ) + 1 );

            progressBar[i] = new JProgressBar();
            progressBar[i].setStringPainted( true );
            progressBar[i].setBorder( BorderFactory.createTitledBorder( "Downloading " + fname + "..." ) );
            content.add( progressBar[i] );

            fd.download( new DownloadTask( new URL( files[i] ), new FileOutputStream( fname ), new ProgressBarUpdator(
                    progressBar[i] ) ) );

        }

        totalProgressBar.setBorder( BorderFactory.createTitledBorder( "Total" ) );
        totalProgressBar.setStringPainted( true );
        totalProgressBar.setMaximum( 0 );
        content.add( totalProgressBar );

        f.setSize( 300, 200 );
        f.setVisible( true );

        // Start downloading
        Thread t = new Thread( fd );
        t.start();
    }

    // Class that updates the download progress
    class ProgressBarUpdator implements DownloadListener {
        JProgressBar progressBar;
        int size = -1;

        public ProgressBarUpdator(JProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        public void onComplete() {
            if (size == -1) {
                progressBar.setIndeterminate( false );
                progressBar.setValue( 100 );
            }

            ( (TitledBorder) progressBar.getBorder() ).setTitle( "Done" );
            progressBar.repaint();
        }

        public void onStart(int fsize) {
            if (fsize > -1) {
                progressBar.setMaximum( fsize );

                synchronized (totalProgressBar) {
                    totalProgressBar.setMaximum( totalProgressBar.getMaximum() + fsize );
                }

                size = fsize;
            } else {
                progressBar.setIndeterminate( true );
            }
        }

        public void onUpdate(int bytes, int totalDownloaded) {
            if (size == -1) {
                progressBar.setString( "" + totalDownloaded );
            } else {
                progressBar.setValue( totalDownloaded );

                synchronized (totalProgressBar) {
                    totalProgressBar.setValue( totalProgressBar.getValue() + bytes );
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SwingTest st = new SwingTest();
        st.testSwing();
    }
}
