package de.tum.edvsboard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class PipeThread extends StoppableThread {
	private final ServerSocket serverSocket;
	private final MulticastSocket multicastSocket;
	private ServerOutput output;
	private ServerInput input;

	public PipeThread(int serverSocketPort, int multicastSocketPort)
			throws IOException {
		serverSocket = new ServerSocket(serverSocketPort);
		multicastSocket = new MulticastSocket(multicastSocketPort);
		multicastSocket.joinGroup(InetAddress.getByName("239.255.42.99"));
	}

	@Override
	public void run() {
		try {
			while (!stopRequested) {
				final Socket newConnection = serverSocket.accept();
				output = new ServerOutput(newConnection, multicastSocket);
				input = new ServerInput(newConnection, multicastSocket);
				output.start();
				input.start();
				output.join();
				input.join();
			}
		} catch (IOException | InterruptedException e) {
			if (!stopRequested) {
				e.printStackTrace();
			}
		} finally {
			try {
				multicastSocket.close();
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stopRequested() {
		if (input != null)
			input.stopRequested();
		if (output != null)
			output.stopRequested();
		try {
			multicastSocket.close();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.stopRequested();
	}

	private static class ServerOutput extends StoppableThread {
		private final Socket socket;
		private final MulticastSocket multicastSocket;

		private ServerOutput(Socket socket, MulticastSocket multicastSocket) {
			this.socket = socket;
			this.multicastSocket = multicastSocket;
		}

		@Override
		public void run() {
			final byte[] packetBuf = new byte[65535];
			final DatagramPacket packet = new DatagramPacket(packetBuf,
					packetBuf.length);
			try (final OutputStream out = socket.getOutputStream();) {
				System.out.println("new connection");
				while (!stopRequested) {
					multicastSocket.receive(packet);
					if (packet.getLength() < 0)
						break;
					out.write(packetBuf, 0, packet.getLength());
				}
			} catch (IOException e) {
				if (!stopRequested) {
					e.printStackTrace();
					stopRequested();
				}
			}
			System.out.println("output closed");

		}

		@Override
		public void stopRequested() {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			super.stopRequested();
		}
	}

	private static class ServerInput extends StoppableThread {
		private final Socket socket;
		private final MulticastSocket multicastSocket;

		private ServerInput(Socket socket, MulticastSocket multicastSocket) {
			this.socket = socket;
			this.multicastSocket = multicastSocket;
		}

		@Override
		public void run() {
			final byte[] packetBuf = new byte[65535];
			final DatagramPacket packet = new DatagramPacket(packetBuf,
					packetBuf.length);
			try (final InputStream inputStream = socket.getInputStream();) {
				while (!stopRequested) {
					int read = inputStream.read(packetBuf);
					if (read < 0)
						break;
					if (read > 0) {
						packet.setLength(read);
						multicastSocket.send(packet);
					}
				}
			} catch (IOException e) {
				if (!stopRequested) {
					e.printStackTrace();
					stopRequested();
				}
			}
			System.out.println("input closed");

		}

		@Override
		public void stopRequested() {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			super.stopRequested();
		}
	}
}
