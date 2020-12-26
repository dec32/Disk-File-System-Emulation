package gui;

import com.sun.javafx.scene.control.skin.ContextMenuContent;

import core.DirItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DirViewItem extends Label{
	private DirItem di;
	public DirViewItem(DirItem di) {
		//设置名字和图标
		this.di = di;
		this.setText(di.getFullName());
		if(di.isDir()) {
			this.setGraphic(new ImageView(new Image("file:folder icon.png")));
		}else {
			this.setGraphic(new ImageView(new Image("file:file icon.png")));
		}
	}
	
	//getters & setters
	public DirItem getDi() {
		return di;
	}
	
	
	
	
}
