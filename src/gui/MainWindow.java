package gui;

import core.*;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainWindow extends Stage{
	private Core core =new Core();//程序内核
	private VBox layout = new VBox();
	private Scene scene;
	private ConsoleWindow cw = new ConsoleWindow(core,this);
	private TopMenuBar tmb = new TopMenuBar();
	private NavigationBar nb = new NavigationBar(core,cw);
	private DirView dv = new DirView(core,this,cw);
	
	
	public MainWindow() {
		this.setTitle("Disk");
		
		//设置顶部菜单的监听
		tmb.getShowConsole().setOnAction(e->{
			cw.show();
		});
		
		//更新文件列表
		dv.update();
		
		//设置控制台的监听
//		cw.getInput().setOnKeyPressed(e->{ 
//			if(e.getCode() == KeyCode.ENTER) {
//				cw.getOutput().appendText(">"+cw.getInput().getText()+"\n");
//				core.execute(cw.getInput().getText());
//				cw.getInput().clear();
//				update();
//			}
//		});
				
		layout.getChildren().add(tmb);
		layout.getChildren().add(nb);
		layout.getChildren().add(dv);
//		layout.setSpacing(10);
//		layout.setPadding(new Insets(5));
		
		scene = new Scene(layout);
		this.setScene(scene);
		this.setResizable(false);
		this.setWidth(500);
		
		update();
	}
	
	public void update() {
		nb.update();
		dv.update();
	}

	public ConsoleWindow getCw() {
		return cw;
	}
	
	

	

}
