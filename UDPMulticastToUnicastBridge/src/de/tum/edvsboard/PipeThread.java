package de.tum.edvsboard;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class PipeThread extends StoppableThread {
	private final InetSocketAddress socketAddress;
	private final DatagramSocket serverSocket;
	private final InetSocketAddress multicastAddress;
	private final MulticastSocket multicastSocket;
	private UDPBridge output;
	private UDPBridge input;

	public PipeThread(String serverIP, int serverSocketPort,
			String multicastIP, int multicastSocketPort) throws IOException {
		socketAddress = new InetSocketAddress(InetAddress.getByName(serverIP),
				serverSocketPort);
		serverSocket = new DatagramSocket();
		multicastSocket = new MulticastSocket(multicastSocketPort);
		multicastAddress = new InetSocketAddress(
				InetAddress.getByName(multicastIP), multicastSocketPort);
		multicastSocket.joinGroup(InetAddress.getByName(multicastIP));
	}

	@Override
	public void run() {
		try {
			while (!stopRequested) {
				output = new UDPBridge(serverSocket, socketAddress,
						multicastSocket);
				input = new UDPBridge(multicastSocket, multicastAddress,
						serverSocket);
				output.start();
				input.start();
				output.join();
				input.join();
			}
		} catch (InterruptedException e) {
			if (!stopRequested) {
				e.printStackTrace();
			}
		} finally {
			multicastSocket.close();
			serverSocket.close();
		}
	}

	@Override
	public void stopRequested() {
		if (input != null)
			input.stopRequested();
		if (output != null)
			output.stopRequested();
		multicastSocket.close();
		serverSocket.close();
		super.stopRequested();
	}

	private static class UDPBridge extends StoppableThread {
		private final DatagramSocket output;
		private final DatagramSocket input;
		private final InetSocketAddress outputAddress;

		private UDPBridge(DatagramSocket output,
				InetSocketAddress outputAddress, DatagramSocket input) {
			this.output = output;
			this.outputAddress = outputAddress;
			this.input = input;
		}

		@Override
		public void run() {
			final byte[] packetBuf = new byte[65535];
			final DatagramPacket packet = new DatagramPacket(packetBuf,
					packetBuf.length);
			try {
				while (!stopRequested) {
					input.receive(packet);
					packet.setSocketAddress(outputAddress);
					output.send(packet);
				}
			} catch (IOException e) {
				if (!stopRequested) {
					e.printStackTrace();
					stopRequested();
				}
			}
		}

		@Override
		public void stopRequested() {
			output.close();
			input.close();
			super.stopRequested();
		}
	}

}
