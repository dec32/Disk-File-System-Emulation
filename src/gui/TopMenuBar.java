package gui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class TopMenuBar extends MenuBar{
	Menu file = new Menu("File");
	MenuItem nu = new MenuItem("New");
	MenuItem openFile = new MenuItem("Open File");
	
	Menu console = new Menu("Console");
	MenuItem showConsole = new MenuItem("Show Console");
	public TopMenuBar() {
		file.getItems().addAll(nu,openFile);
		console.getItems().addAll(showConsole);
		this.getMenus().addAll(file,console);
	}
	
	
	
	public MenuItem getShowConsole() {
		return showConsole;
	}
	
	
}
