
package acsse.csc2b.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * The Class Leecher.
 * 
 *  @author Voldi Muyumba (222031434)
 * 
 */
public class Leecher extends VBox {

	/** The file list container. */
	private ScrollPane fileListContainer = new ScrollPane();

	/** The leech btn. */
	private Button leechBtn = new Button("Start leeching");

	/** The load btn. */
	private Button loadBtn = new Button("Update available files");

	/** The host name input. */
	private TextArea hostNameInput = new TextArea();

	/** The port number input. */
	private TextArea portNumberInput = new TextArea();

	/** The file name input. */
	private TextArea fileNameInput = new TextArea();

	/** The heading. */
	private Text heading = new Text("Available files to leeech: ");

	/** The list container. */
	private VBox listContainer = new VBox();

	/** The btn container. */
	private HBox btnContainer = new HBox();

	/** The file names. */
	private List<String> fileNames = new ArrayList<>();
    
	private Text status = new Text("Not leaching");
	
	
	/**
	 * Instantiates a new leecher.
	 */
	public Leecher() {
		setupUI();

		loadBtn.setOnAction(e -> {
			loadFiles();
		});

		leechBtn.setOnAction(e -> {
			downloadFile();
		});

	}

	/**
	 * Setup UI.
	 */
	private void setupUI() {
		setPadding(new Insets(10, 10, 10, 10));
		setSpacing(10);
		fileListContainer.setContent(listContainer);
		fileListContainer.setPrefSize(400, 200);

		fileNameInput.setPrefSize(400, 40);
		fileNameInput.setPromptText("Enter file number in full to leech");

		hostNameInput.setPrefSize(400, 40);
		hostNameInput.setPromptText("Enter hostname");

		portNumberInput.setPrefSize(400, 40);
		portNumberInput.setPromptText("Enter host port number");

		btnContainer.getChildren().addAll(loadBtn, leechBtn);
		btnContainer.setSpacing(10);
		btnContainer.setAlignment(Pos.BOTTOM_RIGHT);

		getChildren().addAll(heading,status ,fileListContainer, fileNameInput, hostNameInput, portNumberInput, btnContainer);

		setAlignment(Pos.TOP_CENTER);
		getChildren().addAll();

	}

	/**
	 * Load files.
	 */
	private void loadFiles() {
		
		String name;
		
		// the os will attach this process to a port that is available
		try (DatagramSocket socket = new DatagramSocket()) {
			String command = "seed";
			InetAddress hostName = InetAddress.getByName(hostNameInput.getText());
			int hostport = Integer.parseInt(portNumberInput.getText());
			DatagramPacket packetSendCommand = new DatagramPacket(command.getBytes(), command.length(), hostName,
					hostport);
			socket.send(packetSendCommand);
			// the seeder will respond with a list of files which we need to download
			String fileName = "";
			int i = 0;
            fileNames.clear(); // clear the existing file names
			while (true) {
				byte[] data = new byte[128];
				DatagramPacket receivePacket = new DatagramPacket(data, data.length);
				socket.receive(receivePacket);
				fileName = new String(data);
				if (fileName.contains("done")) {
					updateFiles();
					break;
				}

				fileNames.add(fileName);
				i++;
			}

		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update files.
	 */
	private void updateFiles() {
		ObservableList<Node> children = listContainer.getChildren();
		System.out.println(fileNames);
		listContainer.getChildren().clear();
		fileNames.forEach(filename -> {
			children.add(new Text(filename));
		});
	}

	/**
	 * Download file.
	 */
	private void downloadFile() {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setContentText("Make sure your file number is at least greater than one and at most equal to the number of"
				+ "files available");
		
		try {
			int fileNumber = Integer.parseInt(fileNameInput.getText());
		
		
		if (fileNumber > 0 && fileNumber <= fileNames.size() && fileNumber < 127) {
			try (DatagramSocket socket = new DatagramSocket()) {
				status.setText("Leaching file...."); // a loader

				InetAddress hostName = InetAddress.getByName(hostNameInput.getText());
				int hostport = Integer.parseInt(portNumberInput.getText());
				String command = "file";
				DatagramPacket commandPacket = new DatagramPacket(command.getBytes(), command.length(), hostName,
						hostport);
				socket.send(commandPacket);

				byte[] data = new byte[] { (byte) (fileNumber - 1) };

				DatagramPacket fileNumberPacket = new DatagramPacket(data, data.length, hostName, hostport);
				socket.send(fileNumberPacket); // sending the file which the client wants to retrieve
                String fileName = fileNames.get(fileNumber - 1).trim();
				byte[] buffer = new byte[1024];
				File file = new File("data/leeching/" + fileName); // the trim method will
																								// remove all the
																								// unneeded whitespaces
				if (!file.exists()) {
					file.createNewFile();
				}
				try (FileOutputStream fout = new FileOutputStream(file)) {
					while (true) {
						DatagramPacket packetRec = new DatagramPacket(buffer, buffer.length);
						socket.receive(packetRec);
						String str = new String(buffer);
						if (str.contains("QUIT")) {
							break;
						}

						fout.write(buffer);
						fout.flush();
					}
				}
				
				status.setText(fileName + " was downloaded sucessfully");

			} catch (IOException | NumberFormatException e) {
				status.setText("file was not downloaded sucessfully please try again");
				e.printStackTrace();
			}

		} else {
			alert.show();
		
		}
		
		} catch(NumberFormatException e) {
			alert.show();
		}
	}

}
