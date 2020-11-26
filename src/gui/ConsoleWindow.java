package gui;



import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import core.Core;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConsoleWindow extends Stage {
	private Core core;
	private MainWindow mw;
	private TextField input = new TextField();
	private TextArea output = new TextArea();
	private VBox layout = new VBox();
	private Scene scene;
	
	public ConsoleWindow(Core core, MainWindow mw) {			
		this.core = core;
		this.mw = mw;
		this.setTitle("Console");
		this.setHeight(300);
		this.setWidth(400);
		this.setMinWidth(100);
			
		output.setEditable(false);	
		layout.setSpacing(10);
		layout.setAlignment(Pos.CENTER);
		VBox.setVgrow(output, Priority.ALWAYS);
		layout.setPadding(new Insets(5));
		
		layout.getChildren().add(output);
		layout.getChildren().add(input);
		
		scene = new Scene(layout);
		this.setScene(scene);
		
		//把 System.out 的内容重定向到 output 区
		redirectSystemOut();
		//监听回车键
		input.setOnKeyPressed(e->{
			if(e.getCode() == KeyCode.ENTER) {
				execute(input.getText());
			}
		});
		
	}
		
	private void redirectSystemOut() {
		/*
		 * 这里要提供一个没有任何用处的OutputStream
		 * 原因请看 TextAreaPrintStream 类的解释
		 * 我知道这样写实在是太丑了, 但是我太蠢了所以想不到别的办法
		 */
		OutputStream ops = new OutputStream() {		
			@Override
			public void write(int b) throws IOException {
			}
		};	
		System.setOut(new TextAreaPrintStream(ops, output));
	}
	
	public void execute(String str) {
		output.appendText(">"+str+"\n");
		input.clear();
		core.execute(str);
		mw.update();
	}
	
	
	//getters & setters
	public TextField getInput() {
		return input;
	}

	public TextArea getOutput() {
		return output;
	}
	
	
	
	
	
}
