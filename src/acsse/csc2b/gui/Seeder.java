package acsse.csc2b.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import acsse.csc2b.models.Server;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

/**
 * The Class Seeder.
 *  @author Voldi Muyumba (222031434)
 *
 */
public class Seeder extends VBox {

	/** The file list container. */
	private ScrollPane fileListContainer = new ScrollPane();

	/** The seed btn. */
	private Button seedBtn = new Button("Start seeding");

	/** The add file. */
	private Button addFile = new Button("Add file for seeding");

	/** The heading. */
	private Text heading = new Text("Files seeding ");

	/** The files. */
	private VBox files = new VBox();

	/** The btn container. */
	private HBox btnContainer = new HBox();

	/** The paths. */
	private List<Path> paths; // a list containing the paths of the files that the seeder can seed

	/**
	 * Instantiates a new seeder.
	 */
	public Seeder() {
		listFiles();
		setupUI();
		connect();

		// attaching events
		addFile.setOnAction(e -> {
			addFile();
		});
	}

	/**
	 * Setup UI.
	 */
	private void setupUI() {
		setPadding(new Insets(10, 10, 10, 10));

		setAlignment(Pos.TOP_CENTER);
		setSpacing(10);
		fileListContainer.setPrefHeight(200);
		fileListContainer.setPrefWidth(600);

		fileListContainer.setContent(files);
		btnContainer.setSpacing(10);
		btnContainer.getChildren().addAll(addFile, seedBtn);
		btnContainer.setAlignment(Pos.BOTTOM_RIGHT);
		getChildren().addAll(heading, fileListContainer, btnContainer);
	}

	/**
	 * Adds the file.
	 */
	private void addFile() {
		FileChooser chooser = new FileChooser();
		File file = chooser.showOpenDialog(null);

		if (file != null && file.isFile() && file.exists()) {
			Date date = new Date();
			String time = "" + date.getTime();
			String ext = getExtension(file.getName());
			String fileName = time + "." + ext;
			File fileToSave = new File("data/seeding/" + fileName);

			try {
				FileOutputStream fout = new FileOutputStream(fileToSave);
				FileInputStream fin = new FileInputStream(file);

				byte[] buffer = new byte[1024];
				int byteRead = 0;

				while ((byteRead = fin.read(buffer)) != -1) {
					fout.write(buffer, 0, byteRead);
					fout.flush();
				}

				listFiles(); // reloading the files

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("file name is :" + fileName);

		}
	}

	/**
	 * List files.
	 */
	private void listFiles() {
		// should read the list of files available in the seeder folder and then display
		// them
		files.getChildren().clear();
		Path seeding = Paths.get("data/seeding");
		paths = getFilePaths(seeding);
		paths.forEach(p -> files.getChildren().add(new Text(p.getFileName().toString())));

	}

	/**
	 * Gets the extension.
	 *
	 * @param fileName the file name
	 * @return the extension
	 */
	private String getExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1);
	}

	/**
	 * Gets the file paths.
	 *
	 * @param path the path
	 * @return the file paths
	 */
	private List<Path> getFilePaths(Path path) {
		List<Path> lists = null;

		try (Stream<Path> walk = Files.walk(path, 1)) {
			// we reading all the available files paths in the specified path and putting
			// them in a list
			// Files::isRegular is a method reference
			lists = walk.filter(Files::isRegularFile).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lists;

	}

	// only start seeding when this method is called
	/**
	 * Connect.
	 */
	// the assumption here is that they can only be one leecher to whom we are
	// seeding to at a time
	private void connect() {
		Server server = new Server(paths);
		new Thread(server).start();

	}

}
