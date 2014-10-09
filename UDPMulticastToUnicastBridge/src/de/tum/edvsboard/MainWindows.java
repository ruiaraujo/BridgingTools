package de.tum.edvsboard;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class MainWindows {
	private final JFrame frame;
	private final JLabel statusBar;
	private final JButton connectButton;
	private final JTextField udpIPTextField;
	private final JTextField udpPortTextField;
	private final JTextField ipMulticastIPTextField;
	private final JTextField ipMulticastPortTextField;

	private boolean hasServerStarted = false;
	private PipeThread pipeThread;

	private void changeUI(boolean newState) {
		ipMulticastPortTextField.setEnabled(newState);
		udpPortTextField.setEnabled(newState);
		udpIPTextField.setEnabled(newState);
		ipMulticastIPTextField.setEnabled(newState);
	}

	public MainWindows() {
		statusBar = new JLabel("Click Connect");
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!hasServerStarted) {
					try {
						pipeThread = new PipeThread(udpIPTextField.getText(),
								Integer.parseInt(udpPortTextField.getText()),
								ipMulticastIPTextField.getText(), Integer
										.parseInt(ipMulticastPortTextField
												.getText()));
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

		final JLabel ipMulticastIPTextFieldLabel = new JLabel("IP Multicast:");
		final JLabel udpIPTextFieldLabel = new JLabel("UDP Address:");
		final JLabel ipMulticastPortTextFieldLabel = new JLabel(
				"Multicast Port:");
		final JLabel udpPortTextFieldLabel = new JLabel("UDP Port:");

		final DocumentFilter numberFilter = new DocumentFilter() {
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
		};

		final DocumentListener ipFilter = new DocumentListener() {
			void checkDocument(DocumentEvent e) {
				try {
					String text = e.getDocument().getText(0,
							e.getDocument().getLength());
					connectButton.setEnabled(checkString(text));
				} catch (BadLocationException ex) {
					// Do something, OK?
				}
			}

			public void insertUpdate(DocumentEvent e) {
				checkDocument(e);
			}

			public void removeUpdate(DocumentEvent e) {
				checkDocument(e);
			}

			public void changedUpdate(DocumentEvent e) {
				checkDocument(e);
			}
		};

		ipMulticastIPTextField = new JTextField();
		ipMulticastIPTextField.getDocument().addDocumentListener(ipFilter);
		ipMulticastIPTextField.setText("239.255.42.99");

		ipMulticastPortTextField = new JTextField();
		// Allow only number in the port field
		final PlainDocument ipMulticastdoc = new PlainDocument();
		ipMulticastdoc.setDocumentFilter(numberFilter);
		ipMulticastPortTextField.setDocument(ipMulticastdoc);
		ipMulticastPortTextField.setText("1511");

		udpIPTextField = new JTextField();
		udpIPTextField.getDocument().addDocumentListener(ipFilter);
		udpIPTextField.setText("10.162.177.202");

		udpPortTextField = new JTextField();
		final PlainDocument udpdoc = new PlainDocument();
		udpdoc.setDocumentFilter(numberFilter);
		udpPortTextField.setDocument(udpdoc);
		udpPortTextField.setText("56000");

		JPanel panel = new JPanel(new GridLayout(0, 2, 8, 10));
		panel.add(ipMulticastIPTextFieldLabel);
		panel.add(ipMulticastIPTextField);
		panel.add(ipMulticastPortTextFieldLabel);
		panel.add(ipMulticastPortTextField);
		panel.add(udpIPTextFieldLabel);
		panel.add(udpIPTextField);
		panel.add(udpPortTextFieldLabel);
		panel.add(udpPortTextField);
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

	static final Pattern pat = Pattern
			.compile("\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
					+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
					+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
					+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b");

	static boolean checkString(String s) {
		Matcher m = pat.matcher(s);
		return m.matches();
	}
}
