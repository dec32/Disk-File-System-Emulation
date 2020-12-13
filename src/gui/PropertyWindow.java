package gui;

import core.DirItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PropertyWindow extends Stage{
	private Image icon;
	private TextField name = new TextField("abc.ef");
	private Label location = new Label("Loacation:");
	private Label locationValue = new Label("/123/456/abc.ef");
	private Label size = new Label("Size:");
	private Label sizeValue = new Label("14 bytes");
	private Label blockNumber = new Label("Block Number:");
	private Label blockNumberValue = new Label("2, 3, 5");
	private CheckBox ro = new CheckBox("Read Only");
	private CheckBox sys = new CheckBox("System");
	private VBox layout = new VBox();
	
	
	public PropertyWindow(DirItem di) {
		
		//先根据传来的目录项的属性设置各组件的内容
		if(di.isDir()) {
			icon = new Image("file:folder icon.png");
		}else {
			icon = new Image("file:file icon.png");
		}
		
		name.setText(di.getFullName());
		sizeValue.setText(String.valueOf(di.getSize()));
		blockNumberValue.setText(String.valueOf(di.getBlockNum()));
		
		if(di.isRo()) {
			ro.setSelected(true);
		}
		if(di.isSys()) {
			sys.setSelected(true);
		}
		
		//改变checkbock的选中状态时, 立刻把状态取反, 相当于禁止了选中状态的改变(默认的效果有点难看)
		ro.setOnAction(e->{
			ro.setSelected(!ro.isSelected());
		});
		
		sys.setOnAction(e->{
			sys.setSelected(!sys.isSelected());
		});
		
		
		//设置窗体，安排布局
		this.setTitle("Property");
		this.setWidth(300);
		this.setHeight(400);
		this.setResizable(false);
		
		HBox topPane = new HBox();
		topPane.getChildren().addAll(new ImageView(icon), name);
		topPane.setSpacing(20);
		topPane.setAlignment(Pos.BOTTOM_LEFT);
		name.setEditable(false);
		
		
		GridPane infoPane = new GridPane();
		
		infoPane.add(size, 0, 0);
		infoPane.add(blockNumber, 0, 1);
		
		infoPane.add(sizeValue, 1, 0);
		infoPane.add(blockNumberValue, 1, 1);
		infoPane.setHgap(30);
		infoPane.setVgap(15);
		
			
		HBox checkBoxPane = new HBox();
		checkBoxPane.getChildren().addAll(ro, sys);
		checkBoxPane.setSpacing(30);
//		ro.setDisable(true);
//		sys.setDisable(true);

		layout.getChildren().addAll(topPane, new Separator(), infoPane, new Separator(), checkBoxPane);
		layout.setSpacing(30);
		layout.setPadding(new Insets(40,30,40,30));
		this.setScene(new Scene(layout));
		
	}
	
	
	public PropertyWindow(){
		this.setTitle("Property");
		this.setWidth(300);
		this.setHeight(400);
		this.setResizable(false);
		
		HBox topPane = new HBox();
		topPane.getChildren().addAll(name);
		name.setEditable(false);
		
		
		GridPane infoPane = new GridPane();
		
		infoPane.add(location, 0, 0);
		infoPane.add(size, 0, 1);
		infoPane.add(blockNumber, 0, 2);
		
		infoPane.add(locationValue, 1, 0);
		infoPane.add(sizeValue, 1, 1);
		infoPane.add(blockNumberValue, 1, 2);
		infoPane.setHgap(30);
		infoPane.setVgap(15);
		
			
		HBox checkBoxPane = new HBox();
		checkBoxPane.getChildren().addAll(ro, sys);
		checkBoxPane.setSpacing(30);
//		ro.setDisable(true);
//		sys.setDisable(true);

		layout.getChildren().addAll(topPane, new Separator(), infoPane, new Separator(), checkBoxPane);
		layout.setSpacing(30);
		layout.setPadding(new Insets(40,30,40,30));
		this.setScene(new Scene(layout));
	}
}
