package gui;



import core.DirItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class DirViewItemMenu extends ContextMenu{
	private MenuItem property = new MenuItem("Property");
	
	public DirViewItemMenu(DirItem di) {
		this.getItems().addAll(property);
		property.setOnAction(e->{
			PropertyWindow pw = new PropertyWindow(di);
			pw.show();
		});
	}
	
}
