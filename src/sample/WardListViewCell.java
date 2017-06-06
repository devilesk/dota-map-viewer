package sample;

import javafx.scene.control.ListCell;

public class WardListViewCell extends ListCell<Entry> {

    @Override
    protected void updateItem(Entry entry, boolean empty) {
        super.updateItem(entry, empty);

        if(empty || entry == null) {
            setText(null);
            setGraphic(null);
        }
        else {
            setText(entry.getTime() + " " + entry.getName());
            setGraphic(null);
        }
    }
}