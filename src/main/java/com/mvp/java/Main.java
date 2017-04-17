package com.mvp.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Main extends Application {

    private ConfigurableApplicationContext springContext;
    private Parent rootNode;

    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override
    public void init() throws Exception {
        springContext = bootstrapSpringApplicationContext();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);
        rootNode = fxmlLoader.load();
    }

    /**
     * Bootstraps the Spring container via Spring Boot entry point. Registers
     * all the Spring Beans via either @Configuration, @Component during
     * scanning. Gets any parameters past into the application from command line
     * (if any).
     */
    private ConfigurableApplicationContext bootstrapSpringApplicationContext() {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
        String[] args = getParameters().getRaw().stream().toArray(String[]::new);
        builder.headless(false); //needed for TestFX integration testing or eles will get a java.awt.HeadlessException during tests
        return builder.run(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(rootNode));
        stage.setTitle("NASA Missions Database");
        stage.centerOnScreen();
        stage.getScene().getStylesheets().add("style.css");
        stage.show();

    }

    @Override
    public void stop() throws Exception {
        springContext.close();
    }

}

//Options ...   
//gets called for each Controller that you want DI. If you put a println
//in before the return, you can see which Controller gets DI by spring
//via good old anonymous class
//        loader.setControllerFactory(new Callback<Class<?>, Object>() {
//            @Override
//            public Object call(Class<?> clazz) {
//                return springContext.getBean(clazz);
//            }
//        });
// via lambda
//        loader.setControllerFactory((clazz) -> springContext.getBean(clazz));
//  via method reference       
// loader.setControllerFactory(springContext::getBean);
