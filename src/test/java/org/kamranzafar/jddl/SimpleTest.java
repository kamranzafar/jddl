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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author kamran
 * 
 */
@RunWith(JUnit4.class)
public class SimpleTest {
    @Test
    public void testSimple() throws MalformedURLException, FileNotFoundException, InterruptedException {
        DirectDownloader dd = new DirectDownloader();

        final String file = "http://python.org/ftp/python/2.7.2/python-2.7.2.msi";
        final String f = "target/" + file.substring( file.lastIndexOf( '/' ) + 1 );

        DownloadTask dt = new DownloadTask( new URL( file ), new FileOutputStream( f ) );
        dt.addListener( new DownloadListener() {
            int size;

            public void onUpdate(int bytes, int totalDownloaded) {
                updateProgress( (double) totalDownloaded / size );
            }

            public void onStart(String fname, int size) {
                System.out.println( "Downloading " + fname + " of size " + size );
                this.size = size;
                updateProgress( 0 );
            }

            public void onComplete() {
                System.out.println( "\n" + f + " downloaded" );
            }
        } );

        dd.download( dt );

        Thread t = new Thread( dd );
        t.start();
        t.join();
    }

    void updateProgress(double progressPercentage) {
        final int width = 50;

        System.out.print( "\r[" );
        int i = 0;
        for (; i <= (int) ( progressPercentage * width ); i++) {
            System.out.print( "." );
        }
        for (; i < width; i++) {
            System.out.print( " " );
        }
        System.out.print( "]" );
    }

    public static void main(String[] args) throws MalformedURLException, FileNotFoundException, InterruptedException {
        SimpleTest st = new SimpleTest();
        st.testSimple();
    }
}
