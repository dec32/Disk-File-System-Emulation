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
	private Button back = new Button("��");
	private TextField pathFiled = new TextField();
	
	public NavigationBar(Core core,ConsoleWindow cw) {
		this.core = core;
		
		pathFiled.setEditable(false);
		
		back.setOnAction(e->{
			if(core.getCurPath().equals("/")) {
				return;
			}
			int index = core.getCurPath().lastIndexOf("/");//�ҵ���ǰ·�������һ�� "/" ��λ��
			String superPath = core.getCurPath().substring(0, index);//�ַ����Ŀ�ʼ�����һ��"/"֮ǰ���Ǹ�·��
			if(superPath.equals("")) {//�����������������ᵼ�º��˵���Ŀ¼ʱ·�����""
				superPath = "/";
			}
			cw.execute("dir " + superPath);
		});
		this.getChildren().addAll(back, pathFiled);
		
		this.setSpacing(10);
		this.setPadding(new Insets(2,15,2,15));
		HBox.setHgrow(pathFiled, Priority.ALWAYS);//��·����
		
	}
	
	public void update() {
		pathFiled.setText(core.getCurPath());
	}
}
