package com.fooddonation.ui;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.util.Callback;

import java.util.function.Function;

/** Small helpers for the repeated bits of JavaFX setup, so controllers stay about behaviour. */
public final class Controls {

    private Controls() {
    }

    /** Shows each combo box item with the given text, both in the list and in the closed box. */
    public static <T> void showAs(ComboBox<T> box, Function<T, String> text) {
        Callback<javafx.scene.control.ListView<T>, ListCell<T>> cells = list -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : text.apply(item));
            }
        };
        box.setCellFactory(cells);
        box.setButtonCell(cells.call(null));
    }

    /** Fills a table column from the row object with a read-only text value. */
    public static <S> void column(TableColumn<S, String> column, Function<S, String> text) {
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(text.apply(cell.getValue())));
    }

    /** Shows a success line under a form. */
    public static void ok(Label label, String text) {
        label.getStyleClass().removeAll("error", "ok");
        label.getStyleClass().add("ok");
        label.setText(text);
    }

    /** Shows what went wrong, using the message the model gave. */
    public static void error(Label label, RuntimeException e) {
        label.getStyleClass().removeAll("error", "ok");
        label.getStyleClass().add("error");
        label.setText(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
    }

    public static void clear(Label label) {
        label.getStyleClass().removeAll("error", "ok");
        label.setText("");
    }
}
