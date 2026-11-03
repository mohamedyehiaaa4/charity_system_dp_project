package com.fooddonation.controller;

import com.fooddonation.notification.InAppInbox;
import com.fooddonation.notification.InboxMessage;
import com.fooddonation.ui.Controls;
import com.fooddonation.ui.Formats;
import com.fooddonation.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.util.Collections;
import java.util.List;

/** COM-1: in-app messages sent to the signed-in user by the observers. Used by both panels. */
public class InboxController implements Refreshable {
    private final InAppInbox inbox;
    private final Session session;

    @FXML private TableView<InboxMessage> table;
    @FXML private TableColumn<InboxMessage, String> timeColumn;
    @FXML private TableColumn<InboxMessage, String> subjectColumn;
    @FXML private TextArea body;
    @FXML private Label summary;

    public InboxController(InAppInbox inbox, Session session) {
        this.inbox = inbox;
        this.session = session;
    }

    @FXML
    private void initialize() {
        Controls.column(timeColumn, m -> Formats.dateTime(m.getSentAt()));
        Controls.column(subjectColumn, m -> (m.isRead() ? "" : "● ") + m.getSubject());
        table.setPlaceholder(new Label("No in-app messages. Messages sent by email or SMS are in the log files."));
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            body.setText(selected == null ? "" : selected.getMessage());
            if (selected != null && !selected.isRead()) {
                selected.setRead(true);
                table.refresh();
                updateSummary();
            }
        });
    }

    @Override
    @FXML
    public void refresh() {
        List<InboxMessage> messages = inbox.getMessages(session.getUser().toRecipient().getId());
        Collections.reverse(messages); // newest first
        table.getItems().setAll(messages);
        updateSummary();
    }

    /** Unread messages, shown next to "Inbox" in the sidebar. */
    @Override
    public String badge() {
        long unread = inbox.getMessages(session.getUser().toRecipient().getId()).stream()
                .filter(m -> !m.isRead()).count();
        return unread == 0 ? "" : String.valueOf(unread);
    }

    private void updateSummary() {
        long unread = table.getItems().stream().filter(m -> !m.isRead()).count();
        summary.setText(table.getItems().size() + " messages, " + unread + " unread");
    }
}
