package de.tum.edvsboard;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class MainWindows {
	private final JFrame frame;
	private final JLabel statusBar;
	private final JButton connectButton;
	private final JTextField tcpPortTextField;
	private final JTextField udpPortTextField;

	private boolean hasServerStarted = false;
	private PipeThread pipeThread;

	private void changeUI(boolean newState) {
		udpPortTextField.setEnabled(newState);
		tcpPortTextField.setEnabled(newState);
	}

	public MainWindows() {
		statusBar = new JLabel("Click Connect");
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!hasServerStarted) {
					try {
						pipeThread = new PipeThread(Integer
								.parseInt(tcpPortTextField.getText()), Integer
								.parseInt(udpPortTextField.getText()));
						pipeThread.start();
						hasServerStarted = true;
						connectButton.setText("Disconnect");
						changeUI(false);
					} catch (IOException e1) {
						e1.printStackTrace();
						setErrorMessage("Could not start server");
					}

				} else {
					pipeThread.stopRequested();
					connectButton.setText("Connect");
					changeUI(true);
					hasServerStarted = false;
				}
			}
		});

		final JLabel ucpPortTextFieldLabel = new JLabel("UDP Port:");
		final JLabel tcpPortTextFieldLabel = new JLabel("TCP Port:");

		// Allow only number in the port field
		final PlainDocument doc = new PlainDocument();
		doc.setDocumentFilter(new DocumentFilter() {
			@Override
			public void insertString(FilterBypass fb, int off, String str,
					AttributeSet attr) throws BadLocationException {
				fb.insertString(off, str.replaceAll("\\D++", ""), attr); // remove
																			// non-digits
			}

			@Override
			public void replace(FilterBypass fb, int off, int len, String str,
					AttributeSet attr) throws BadLocationException {
				fb.replace(off, len, str.replaceAll("\\D++", ""), attr); // remove
																			// non-digits
			}
		});
		tcpPortTextField = new JTextField();
		tcpPortTextField.setDocument(doc);
		tcpPortTextField.setText("1511");

		udpPortTextField = new JTextField();
		udpPortTextField.setDocument(doc);
		udpPortTextField.setText("1511");

		JPanel panel = new JPanel(new GridLayout(0, 2, 8, 10));
		panel.add(ucpPortTextFieldLabel);
		panel.add(udpPortTextField);
		panel.add(tcpPortTextFieldLabel);
		panel.add(tcpPortTextField);
		panel.add(new Label());
		panel.add(connectButton);
		frame = new JFrame("Serial Port Server");
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		frame.getContentPane().add(statusBar, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
	}

	private void setErrorMessage(String msg) {
		statusBar.setText(msg);
	}

	private void setVisible(boolean visible) {
		frame.setVisible(visible);
	}

	public static void main(String[] args) {

		final MainWindows window = new MainWindows();
		window.setVisible(true);
	}

}
