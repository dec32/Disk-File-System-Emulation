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
	//���½���
	public void update() {
		ArrayList<DirItem> dirDirItems = new ArrayList<DirItem>();
		ArrayList<DirItem> fileDirItems = new ArrayList<DirItem>();
		
		this.getItems().clear();
		for (int i = 0; i < 8; i++) {
			//�ѵ�ǰĿ¼��8��Ŀ¼��ȫ��ȡ�������һ��
			//Ϊ�յĶ���, �����ļ���Ŀ¼���Ŀ¼��Ŀ¼��ֿ����
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
		
		//Ϊÿһ��Ŀ¼��Ŀ¼���½�һ��DirViewItem������Ϊ���������
		for(DirItem di:dirDirItems) {
			this.getItems().add(new DirViewItem(di));
		}
		//Ϊÿһ���ļ���Ŀ¼���½�һ��DirViewItem������Ϊ���������
		for(DirItem di:fileDirItems) {
			this.getItems().add(new DirViewItem(di));
		}	
		
		
		this.setOnMouseClicked(e->{
			//������Ŀ��˫������
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
				//�����Ҽ��˵��ļ���
				this.setContextMenu(new DirViewItemMenu(dvi.getDi()));
			}
			
			
		});
		
		
		
		this.refresh();
	}
}
