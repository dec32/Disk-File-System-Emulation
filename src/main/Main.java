package main;

import java.util.Scanner;

import core.*;
import gui.*;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application{

	public static void main(String[] args) {
		launch();		
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		MainWindow mw = new MainWindow();
		mw.show();
		mw.getCw().show();
	}

}
