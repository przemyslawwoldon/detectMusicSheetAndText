package mainApp;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import mainApp.view.MainAppController;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;

public class App extends Application {

	private Stage primaryStage;
    private AnchorPane rootLayout;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			this.primaryStage = primaryStage;
	        this.primaryStage.setTitle("ImgProcessingAndDetectText");
	        initRootLayout();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getResource("view/mainApp.fxml"));
            rootLayout = (AnchorPane) loader.load();
            MainAppController controller = loader.getController();
            controller.setMainApp(this);
            Scene scene = new Scene(rootLayout);
            primaryStage.setResizable(false);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    public Stage getPrimaryStage() {
        return primaryStage;
    }
	
	public static void main(String[] args) {
		launch(args);
	}
}
