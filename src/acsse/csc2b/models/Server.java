package acsse.csc2b.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.List;

/**
 * The Class Server.
 * @author Voldi Muyumba (222031434)
 */
public class Server implements Runnable {

	/** The paths. */
	private List<Path> paths; // a list containing the paths of the files that the seeder can seed

	/**
	 * Instantiates a new server.
	 *
	 * @param paths the paths
	 */
	public Server(List<Path> paths) {
		super();
		this.paths = paths;
	}

	/**
	 * Run.
	 */
	@Override
	public void run() {
		connect();

	}

	/**
	 * Connects
	 */
	private void connect() {
		try (DatagramSocket socket = new DatagramSocket(3000)) {
			while (true) {
				System.out.println("===========ready to send =======================");
				byte[] data = new byte[4];

				DatagramPacket packetRe = new DatagramPacket(data, data.length);
				socket.receive(packetRe);
				String command = new String(data);
				System.out.println("command " + command);
				// only start seeding when this command is sent
				if (command.trim().equals("seed")) { // sending file list
					sendAvailableFiles(packetRe.getAddress(), packetRe.getPort(), socket);
				} else if (command.trim().equals("file")) { // sending files

					data = new byte[1];
					packetRe = new DatagramPacket(data, 1); // get the file number
					socket.receive(packetRe);

					byte fileNumber = data[0];

					System.out.println("file number: " + fileNumber);
					File file = paths.get(fileNumber).toFile();

					if (!file.exists()) {
						String resp = "QUIT";
						DatagramPacket respQuite = new DatagramPacket(resp.getBytes(), resp.length(),
								packetRe.getAddress(), packetRe.getPort());
						socket.send(respQuite);
					} else {
						FileInputStream fin = new FileInputStream(file);
						byte[] buffer = new byte[1024];
						int byteRead = 0;

						while ((byteRead = fin.read(buffer)) != -1) {
							DatagramPacket packetFile = new DatagramPacket(buffer, buffer.length, packetRe.getAddress(),
									packetRe.getPort());
							socket.send(packetFile);
						}

						System.out.println("tried seedning file");
						String resp = "QUIT"; // quit 2 to indicate that the file has been fully sent
						DatagramPacket respQuite = new DatagramPacket(resp.getBytes(), resp.length(),
								packetRe.getAddress(), packetRe.getPort());
						socket.send(respQuite);

					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Send available files.
	 *
	 * @param address the address of the host
	 * @param port    the port of the host
	 * @param client  the client socket
	 */
	private void sendAvailableFiles(InetAddress address, int port, DatagramSocket client) {
		System.out.println("number of files: " + paths.size());
		paths.forEach(p -> {
			System.out.println(p);
			byte[] data = null;
			String fileName = p.getFileName().toString().length() > 128 ? p.getFileName().toString().substring(0, 128)
					: p.getFileName().toString();
			data = fileName.getBytes(); // a file name must only be at most
			// 128 character long

			System.out.println(new String(data));
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, port); // sending the file name
			try {
				client.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		String done = "done";
		// 128 character long
		DatagramPacket sendPacket = new DatagramPacket(done.getBytes(), done.length(), address, port); // sending the
																										// done command
		try {
			client.send(sendPacket);
			System.out.println("done sending");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
