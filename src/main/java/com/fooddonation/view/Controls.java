package com.fooddonation.view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.util.List;
import java.util.Optional;
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

    /** Fills a table column with a node (badge, tags) built from the row object. */
    public static <S> void nodeColumn(TableColumn<S, String> column, Function<S, Node> node) {
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(""));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                S row = empty || getTableRow() == null ? null : getTableRow().getItem();
                setText(null);
                setGraphic(row == null ? null : node.apply(row));
            }
        });
    }

    /** A label with the given style classes, e.g. a status badge or an add-on tag. */
    public static Label styled(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        label.setMinWidth(Region.USE_PREF_SIZE); // badges and tags never shrink to "Sub..."
        return label;
    }

    /** A row of small tags, e.g. a donation's add-ons. */
    public static HBox tags(List<String> names) {
        HBox row = new HBox(4, names.stream().map(name -> styled(name, "tag")).toArray(Label[]::new));
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** Shows a document (receipt, summary) in a pop-up over the given node's window. */
    public static void showDocument(Node owner, String title, String text) {
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setPrefSize(560, 360);
        area.getStyleClass().add("document");
        Alert alert = new Alert(Alert.AlertType.NONE, "", ButtonType.CLOSE);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.getDialogPane().setContent(area);
        styleLike(alert, owner);
        alert.showAndWait();
    }

    /** Asks for one line of text; empty when the user cancels. */
    public static Optional<String> ask(Node owner, String title, String question, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(question);
        dialog.getEditor().setPromptText(prompt);
        dialog.setGraphic(null);
        styleLike(dialog, owner);
        return dialog.showAndWait();
    }

    private static void styleLike(Dialog<?> dialog, Node owner) {
        if (owner.getScene() != null) {
            dialog.initOwner(owner.getScene().getWindow());
            dialog.getDialogPane().getStylesheets().setAll(owner.getScene().getStylesheets());
        }
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
