import acsse.csc2b.gui.Container;
import javafx.application.Application;
import javafx.stage.Stage;


/**
 *  @author Voldi Muyumba (222031434)
 * */
public class Main extends Application {

	public static void main(String[] args) {
		launch(args);

	}

	@Override
	public void start(Stage stage) throws Exception {
		new Container(stage);

	}

}
