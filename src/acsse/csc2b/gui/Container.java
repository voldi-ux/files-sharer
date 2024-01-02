package acsse.csc2b.gui;

import acsse.csc2b.models.MODE;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/**
 *  @author Voldi Muyumba (222031434)
 * */
public class Container extends VBox {
   private Stage primaryStage;
   private Scene seederScene;
   private Scene  leecherScene;
   private Button seederBtn = new Button("Seeder Mode");
   private Button leecherBtn = new Button("Leecher Mode");
   private Text heading = new Text("Select Mode");
   
   /**
    * constructs a container instance
    * @param stage the primary stage
    * */
   public Container(Stage stage) {
	   primaryStage = stage;
	
	   getChildren().addAll(heading, seederBtn, leecherBtn);
	   setAlignment(Pos.CENTER); // aligning the content to center
	   
	   setSpacing(20);
	   primaryStage.setScene(new Scene(this, 400,400)); // initial scene is the container
	   
	   primaryStage.setTitle("Peer to peer file sharing");
	   primaryStage.show();
	   
	   
	   //attach events to the btns
	   seederBtn.setOnAction( e -> SetMode(MODE.SEEDER));
	   leecherBtn.setOnAction( e -> SetMode(MODE.LEECHER));
   }
   
   
   /**
    * Changes the mode
    * @param mode the mode to change to
    * */
   private void SetMode(MODE mode) {
	    if (mode.equals(MODE.SEEDER)) {
	    	seederScene = new Scene(new Seeder(), 500,500);
	    	primaryStage.setScene(seederScene);
	    	primaryStage.setTitle("Seeding");
	    } else if (mode.equals(MODE.LEECHER)) {
	    	leecherScene = new Scene(new Leecher(), 500,500);
	    	primaryStage.setScene(leecherScene);
	    	primaryStage.setTitle("Leeching");
	    }
   }
   

}
