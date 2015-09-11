package net.rudp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jnet.core.connection.DatagramWanEmulator;
import org.jnet.core.helper.Unchecker;
import org.jnet.core.tools.Eventually;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RudpTest implements Eventually {
	private static InetSocketAddress emuAddress = new InetSocketAddress("localhost", 12346);
	private static InetSocketAddress serverAddress = new InetSocketAddress("localhost", 12345);
	private static InetSocketAddress clientAddress = new InetSocketAddress("localhost", 12347);
	private DatagramWanEmulator emu;
	private Socketer socketer;
	
	private final static String largeString;
	
	static {
		StringBuilder largeStringBuilder = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			largeStringBuilder.append("Hello world");
		}
		largeString = largeStringBuilder.toString();

	}

	@Before
	public void setup() throws Exception {
		emu = new DatagramWanEmulator(emuAddress, clientAddress, serverAddress);
		emu.startEmulation();
	}

	@After
	public void tearDown() throws Exception {
		if (socketer != null) {
			socketer.close();
		}
		if (emu != null) {
			emu.stopEmulation();
		}
	}

	@Test
	public void shouldSendAndReceiveMessages() throws Exception {
		socketer = new Socketer(false);
		doTestOnPreparedConnection();
	}

	@Test
	public void shouldSendAndReceiveMessagesWhenPacketsGetLost() throws Exception {
		socketer = new Socketer(true);
		emu.setPackageLoss(.3f);
		doTestOnPreparedConnection();
	}

	@Test
	public void shouldSendAndReceiveMessagesWhenPacketsGetDuplicated() throws Exception {
		socketer = new Socketer(true);
		emu.setPackageDuplication(.3f);
		doTestOnPreparedConnection();
	}
	
	private void doTestOnPreparedConnection() {
		List<String> shouldServerList = new ArrayList<>();
		List<String> shouldClientList = new ArrayList<>();
		for (int i = 0; i < 25; i++) {
			socketer.serverWriter.println("Hello client");
			socketer.clientWriter.println("Hello server");
			shouldClientList.add("Hello client");
			shouldServerList.add("Hello server");
		}
		socketer.serverWriter.println(largeString);
		socketer.clientWriter.println(largeString);
		shouldClientList.add(largeString);
		shouldServerList.add(largeString);

		eventually(5000, () -> {
			assertThat(socketer.clientMessages, is(shouldClientList));
			assertThat(socketer.serverMessages, is(shouldServerList));
		});

	}


	static class Socketer {
		ExecutorService executorService = Executors.newSingleThreadExecutor();

		List<String> clientMessages = new ArrayList<>();
		List<String> serverMessages = new ArrayList<>();
		PrintWriter serverWriter;
		PrintWriter clientWriter;

		private Socket clientSocket;
		private Socket serverSocket;
		private ServerSocket server;

		Socketer(boolean useEmu) throws Exception {
			server = new ReliableServerSocket(serverAddress.getPort());
			Future<Socket> socketFuture = executorService.submit(() -> server.accept());

			if (useEmu) {
				clientSocket = new ReliableSocket(new ReliableSocketProfile(), emuAddress, clientAddress);
			} else {
				clientSocket = new ReliableSocket("localhost", serverAddress.getPort());
			}
			serverSocket = socketFuture.get(3, TimeUnit.SECONDS);
			createListener(serverSocket, serverMessages::add);
			createListener(clientSocket, clientMessages::add);
			serverWriter = new PrintWriter(serverSocket.getOutputStream(), true);
			clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
		}

		public static void createListener(Socket socket, StringListener socketListener) {
			new Thread(() -> {
				Unchecker.uncheck(() -> {
					BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String str = null;
					while ((str = reader.readLine()) != null) {
						socketListener.onString(str);
					}
				});
			}).start();
		}

		public void close() throws Exception {
			clientSocket.close();
			serverSocket.close();
			server.close();
		}

		static interface StringListener {
			void onString(String str);
		}
	}

}