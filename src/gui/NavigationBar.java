package gui;


import core.Core;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class NavigationBar extends HBox {
	private Core core;
	private ConsoleWindow cw;
	private Button back = new Button("←");
	private TextField pathFiled = new TextField();
	
	public NavigationBar(Core core,ConsoleWindow cw) {
		this.core = core;
		
		pathFiled.setEditable(false);
		
		back.setOnAction(e->{
			if(core.getCurPath().equals("/")) {
				return;
			}
			int index = core.getCurPath().lastIndexOf("/");//找到当前路径的最后一个 "/" 的位置
			String superPath = core.getCurPath().substring(0, index);//字符串的开始到最后一个"/"之前就是父路径
			if(superPath.equals("")) {//但是上面这种做法会导致后退到根目录时路径变成""
				superPath = "/";
			}
			cw.execute("dir " + superPath);
		});
		this.getChildren().addAll(back, pathFiled);
		
		this.setSpacing(10);
		this.setPadding(new Insets(2,15,2,15));
		HBox.setHgrow(pathFiled, Priority.ALWAYS);//让路径栏
		
	}
	
	public void update() {
		pathFiled.setText(core.getCurPath());
	}
}
