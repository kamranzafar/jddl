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
	DirectDownloader dd = new DirectDownloader();

	@Test
	public void testSimple() throws MalformedURLException, FileNotFoundException, InterruptedException {
		final Thread t = new Thread(dd);
		final String file = "http://python.org/ftp/python/2.7.2/python-2.7.2.msi";
		final String f = "target/" + file.substring(file.lastIndexOf('/') + 1);

		DownloadTask dt = new DownloadTask(new URL(file), new FileOutputStream(f)).addListener(new DownloadListener() {
			int size;

			public void onUpdate(int bytes, int totalDownloaded) {
				updateProgress((double) totalDownloaded / size);
			}

			public void onStart(String fname, int size) {
				System.out.println("Downloading " + fname + " of size " + size);
				this.size = size;
				updateProgress(0);
			}

			public void onComplete() {
				System.out.println("\n" + f + " downloaded");
			}

			public void onCancel() {

			}
		});

		dd.download(dt);

		t.start();
		t.join();
	}

	void updateProgress(double progressPercentage) {
		final int width = 50;

		System.out.print("\r[");
		int i = 0;
		for (; i <= (int) (progressPercentage * width); i++) {
			System.out.print(".");
		}
		for (; i < width; i++) {
			System.out.print(" ");
		}
		System.out.print("]");
	}

	public static void main(String[] args) throws MalformedURLException, FileNotFoundException, InterruptedException {
		SimpleTest st = new SimpleTest();
		st.testSimple();
	}
}
