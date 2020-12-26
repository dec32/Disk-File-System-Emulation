package gui;

import java.util.ArrayList;

import core.Core;
import core.DirItem;
import core.Util;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;

public class DirView extends ListView<DirViewItem>{
	
	private Core core;
	private MainWindow mw;
	private ConsoleWindow cw;
	public DirView(Core core, MainWindow mw, ConsoleWindow cw) {
		this.core = core;
		this.mw = mw;
		this.cw = cw;
	}
	//更新界面
	public void update() {
		ArrayList<DirItem> dirDirItems = new ArrayList<DirItem>();
		ArrayList<DirItem> fileDirItems = new ArrayList<DirItem>();
		
		this.getItems().clear();
		for (int i = 0; i < 8; i++) {
			//把当前目录的8个目录项全部取出来检查一遍
			//为空的丢弃, 并把文件的目录项和目录的目录项分开存放
			DirItem di = Util.getDirItemAt(core.getCurDir(), i);
			if(di.getFullName().equals(".")) {
				continue;
			}
			if(di.isDir()) {
				dirDirItems.add(di);
			}else {
				fileDirItems.add(di);
			}
		}
		
		//为每一个目录的目录项新建一个DirViewItem，并作为子组件加入
		for(DirItem di:dirDirItems) {
			this.getItems().add(new DirViewItem(di));
		}
		//为每一个文件的目录项新建一个DirViewItem，并作为子组件加入
		for(DirItem di:fileDirItems) {
			this.getItems().add(new DirViewItem(di));
		}	
		
		
		this.setOnMouseClicked(e->{
			//设置项目的双击监听
			DirViewItem dvi = this.getSelectionModel().getSelectedItem();
			if(e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {			
				if(dvi.getDi().isDir()) {
					cw.execute("dir "+dvi.getDi().getFullName());
					mw.update();
				}else {
					cw.execute("type "+dvi.getDi().getFullName());
					mw.update();
				}
			}else if(e.getButton() == MouseButton.SECONDARY && e.getClickCount() ==1) {
				//设置右键菜单的监听
				this.setContextMenu(new DirViewItemMenu(dvi.getDi()));
			}
			
			
		});
		
		
		
		this.refresh();
	}
}
