import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.Enumeration;

public class Server {

	private static ServerSocket socket;

	/**
	 * @param args
	 * @throws IOException
	 * @throws GeneralSecurityException
	 * @throws PortInUseException
	 * @throws UnsupportedCommOperationException
	 * @throws NoSuchPortException
	 */
	public static void main(String[] args) throws IOException,
			GeneralSecurityException, PortInUseException,
			UnsupportedCommOperationException {
		if (args.length < 1) {
			System.out.println("Usage:");
			System.out.println(Server.class.getName() + " listen_port");
			return;
		} else {
			
		}
			
		Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
		if (!portList.hasMoreElements()) {
			System.out.println("no serial ports");
			return;
		}
		CommPortIdentifier portIdentifier = (CommPortIdentifier) portList
				.nextElement();
		portIdentifier = (CommPortIdentifier) portList
				.nextElement();
		System.out.println("nConnected to " + portIdentifier.getName());
		final SerialPort serialPort = (SerialPort) portIdentifier.open(
				"SerialWrapper", 0);
		serialPort.setSerialPortParams(4000000, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		socket = new ServerSocket(Integer.parseInt(args[0]));
		// When a new connection comes in
		// we launch a new thread to handle that connection
		System.out.println("waiting for connection");
		while (true) {
			final Socket newCon = socket.accept();
			new Thread(new ServerInput(newCon, serialPort)).start();
			new Thread(new ServerOutput(newCon, serialPort)).start();

		}
	}

	private static class ServerOutput implements Runnable {
		private final Socket socket;
		private final SerialPort serialPort;

		private ServerOutput(Socket socket, SerialPort serialPort) {
			this.socket = socket;
			this.serialPort = serialPort;
		}

		@Override
		public void run() {
			try {
				final InputStream inputStream = serialPort.getInputStream();
				final OutputStream out = socket.getOutputStream();
				try {
					System.out.println("new connection");
					final byte[] output = new byte[10240];
					try {
						while (true) {
							int read = inputStream.read(output);
							if (read < 0)
								break;
							out.write(output, 0, read);
						}
					} catch (IOException e) {
						e.printStackTrace();
						checkIfShouldExit(e);
					}
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					checkIfShouldExit(e);
				} finally {
					out.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				checkIfShouldExit(e1);
			}
			System.out.println("output closed");

		}
	}

	private static class ServerInput implements Runnable {
		private final Socket socket;
		private final SerialPort serialPort;

		private ServerInput(Socket socket, SerialPort serialPort) {
			this.socket = socket;
			this.serialPort = serialPort;
		}

		@Override
		public void run() {
			try {
				final InputStream inputStream = socket.getInputStream();
				final OutputStream out = serialPort.getOutputStream();
				try {
					final byte[] output = new byte[10240];
					try {
						while (true) {
							int read = inputStream.read(output);
							if (read < 0)
								break;
							out.write(output, 0, read);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					checkIfShouldExit(e);
				} finally {
					inputStream.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				checkIfShouldExit(e1);
			}
			System.out.println("input closed");

		}
	}

	private static void checkIfShouldExit(Exception e) {
		for (StackTraceElement t : e.getStackTrace()) {
			if (t.getClassName().startsWith("gnu.io")) {
				System.out.println("error with serial port\n exiting.\n");
				System.exit(0);// The serial port disconnected so this is non
								// recoverable.
			}
		}
	}
}
