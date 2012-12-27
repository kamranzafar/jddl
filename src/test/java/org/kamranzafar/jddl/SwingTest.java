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

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
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
	private static final String STOP = "Stop";
	private static final String CANCEL = "Cancel";
	private static final String PAUSE = "Pause";
	private static final String RESUME = "Resume";
	private static final String TOTAL = "Total";

	// total progress bar
	private final JProgressBar totalProgressBar = new JProgressBar();

	@Test
	public void testSwing() throws IOException, InterruptedException {
		JFrame f = new JFrame("jddl Swing example");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = f.getContentPane();
		content.setLayout(new GridLayout(4, 1));

		// Some files
		String files[] = { "http://python.org/ftp/python/2.7.2/python-2.7.2.msi",
				"http://www.python.org/ftp/python/3.2.2/python-3.2.2.msi",
				"http://www.python.org/ftp/python/3.2.2/python-3.2.2.amd64.msi" };
		// Create a DirectDownloader instance
		final DirectDownloader fd = new DirectDownloader();

		// Progress bars for individual file downloads
		JProgressBar[] progressBar = new JProgressBar[3];

		// Pause/Resume buttons
		JButton[] pauseButton = new JButton[3];

		// Cancel
		JButton[] cancelButton = new JButton[3];

		JButton stopButton = new JButton(STOP);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fd.cancelAll();
			}
		});

		// Initialize progress bars and create download tasks
		for (int i = 0; i < 3; i++) {
			String fname = files[i].substring(files[i].lastIndexOf('/') + 1);

			progressBar[i] = new JProgressBar();
			pauseButton[i] = new JButton(PAUSE);
			cancelButton[i] = new JButton(CANCEL);

			JPanel panel = new JPanel();
			BoxLayout box = new BoxLayout(panel, BoxLayout.X_AXIS);

			panel.setLayout(box);

			final DownloadTask dt = new DownloadTask(new URL(files[i]), new FileOutputStream(fname));
			dt.addListener(new ProgressBarUpdator(fname, progressBar[i]));

			pauseButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (dt.isPaused()) {
						dt.setPaused(false);
						((JButton) e.getSource()).setText(PAUSE);
					} else {
						dt.setPaused(true);
						((JButton) e.getSource()).setText(RESUME);
					}
				}
			});

			cancelButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					dt.setCancelled(true);
				}
			});

			progressBar[i].setStringPainted(true);
			progressBar[i].setBorder(BorderFactory.createTitledBorder("Downloading " + fname + "..."));

			panel.add(progressBar[i]);
			panel.add(pauseButton[i]);
			panel.add(cancelButton[i]);

			content.add(panel);

			fd.download(dt);
		}

		totalProgressBar.setBorder(BorderFactory.createTitledBorder(TOTAL));
		totalProgressBar.setStringPainted(true);
		totalProgressBar.setMaximum(0);

		JPanel panel = new JPanel();
		BoxLayout box = new BoxLayout(panel, BoxLayout.X_AXIS);

		panel.setLayout(box);
		panel.add(totalProgressBar);
		panel.add(stopButton);

		content.add(panel);

		f.setSize(400, 200);
		f.setVisible(true);

		// Start downloading
		Thread t = new Thread(fd);
		t.start();
	}

	// Class that updates the download progress
	class ProgressBarUpdator implements DownloadListener {
		private static final String DONE = "Done";
		JProgressBar progressBar;
		int size = -1;

		String fname;

		public ProgressBarUpdator(String fname, JProgressBar progressBar) {
			this.progressBar = progressBar;
			this.fname = fname;
		}

		public void onComplete() {
			if (size == -1) {
				progressBar.setIndeterminate(false);
				progressBar.setValue(100);
			}

			((TitledBorder) progressBar.getBorder()).setTitle(DONE);
			progressBar.repaint();
		}

		public void onStart(String fname, int fsize) {
			if (fsize > -1) {
				progressBar.setMaximum(fsize);

				synchronized (totalProgressBar) {
					totalProgressBar.setMaximum(totalProgressBar.getMaximum() + fsize);
				}

				size = fsize;
			} else {
				progressBar.setIndeterminate(true);
			}
		}

		public void onUpdate(int bytes, int totalDownloaded) {
			if (size == -1) {
				progressBar.setString("" + totalDownloaded);
			} else {
				progressBar.setValue(totalDownloaded);

				synchronized (totalProgressBar) {
					totalProgressBar.setValue(totalProgressBar.getValue() + bytes);
				}
			}
		}

		public void onCancel() {
			new File(fname).delete();
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		SwingTest st = new SwingTest();
		st.testSwing();
	}
}
