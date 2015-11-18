/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bms;

import java.net.URL;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 *
 * @author wpawgasa
 */
public class DTIBMS extends Application {
    private Scene scene;
    @Override
    public void start(Stage stage) {
        
        
        
        
        
//        GridPane grid = new GridPane();
//        grid.setAlignment(Pos.CENTER);
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.setPadding(new Insets(25, 25, 25, 25));
//        
//        Text scenetitle = new Text("Spider Link");
//        scenetitle.setId("app-text");
//        grid.add(scenetitle, 0, 0);
//        
//        Button connBtn = new Button("CONNECT");
//        connBtn.getStyleClass().add("boxbutton");
//        
//        Button configBtn = new Button("SETTINGS");
//        configBtn.getStyleClass().add("boxbutton");
//        
//        HBox hbBtn = new HBox(10);
//        hbBtn.setAlignment(Pos.CENTER);
//        hbBtn.getChildren().add(connBtn);
//        hbBtn.getChildren().add(configBtn);
//        grid.add(hbBtn, 0, 1);
//        
////        btn.setOnAction(new EventHandler<ActionEvent>() {
////            
////            @Override
////            public void handle(ActionEvent event) {
////                System.out.println("Hello World!");
////            }
////        });
//        
//        Scene scene = new Scene(grid, 300, 250);
//        primaryStage.setTitle("Spider Data Link");
//        primaryStage.setScene(scene);
//        primaryStage.setFullScreen(true);
//        scene.getStylesheets().add
// (SpiderDataLink.class.getResource("main_style.css").toExternalForm());
//        primaryStage.show();
        // create the scene
        stage.setTitle("Web View");
        scene = new Scene(new Browser(),750,500, Color.web("#666970"));
        stage.setScene(scene);
        stage.show();
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    
    
}

class Browser extends Region {
    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();
     
    public Browser() {
        
        try {  
        URL url = getClass().getResource("views/index.html");
        //apply the styles
        getStyleClass().add("browser");
        // load the web page
        webEngine.load(url.toExternalForm());
        //add the web view to the scene
        getChildren().add(browser);
              
        } catch(Exception e) {  
            e.printStackTrace();  
        }  
 
    }    
}
