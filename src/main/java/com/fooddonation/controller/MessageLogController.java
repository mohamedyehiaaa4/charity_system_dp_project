package com.fooddonation.controller;

import com.fooddonation.model.user.User;
import com.fooddonation.ui.Controls;
import com.fooddonation.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * COM-1: email and SMS are simulated by log files. This page shows the signed-in user only the
 * messages sent to their own email address or phone number, for both the donor and the coordinator.
 */
public class MessageLogController implements Refreshable {
    private static final int LAST_MESSAGES = 200;
    private static final Pattern MESSAGE_START = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T");

    /** One "sent" message, read from a log line: "time | to: x | subject: y | text". */
    record SentMessage(String channel, String time, String to, String subject, String text) {
    }

    private final Path emailLog;
    private final Path smsLog;
    private final Session session;

    @FXML private TableView<SentMessage> table;
    @FXML private TableColumn<SentMessage, String> channelColumn;
    @FXML private TableColumn<SentMessage, String> timeColumn;
    @FXML private TableColumn<SentMessage, String> subjectColumn;
    @FXML private TextArea body;
    @FXML private Label summary;

    public MessageLogController(Path emailLog, Path smsLog, Session session) {
        this.emailLog = emailLog;
        this.smsLog = smsLog;
        this.session = session;
    }

    @FXML
    private void initialize() {
        Controls.nodeColumn(channelColumn, m -> Controls.styled(m.channel(), "badge",
                "Email".equals(m.channel()) ? "ch-email" : "ch-sms"));
        Controls.column(timeColumn, m -> m.time().replace('T', ' ').substring(0, Math.min(16, m.time().length())));
        Controls.column(subjectColumn, SentMessage::subject);
        Label empty = new Label("Nothing sent to you by email or SMS. "
                + "If your channel is \"In app\", your notifications are in your Inbox.");
        empty.setWrapText(true);
        empty.setMaxWidth(320);
        empty.setTextAlignment(TextAlignment.CENTER);
        empty.getStyleClass().add("muted");
        table.setPlaceholder(empty);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) ->
                body.setText(selected == null ? "" : "To: " + selected.to() + "\n"
                        + "Subject: " + selected.subject() + "\n\n" + selected.text()));
    }

    @Override
    @FXML
    public void refresh() {
        User user = session.getUser();
        List<SentMessage> mine = new ArrayList<>();
        mine.addAll(read(emailLog, "Email", user.getEmail()));
        mine.addAll(read(smsLog, "SMS", user.getPhone()));
        mine.sort(Comparator.comparing(SentMessage::time).reversed()); // newest first
        table.getItems().setAll(mine);
        body.clear();
        summary.setText(mine.size() + " messages sent to " + user.getEmail()
                + (user.getPhone() == null || user.getPhone().isBlank() ? "" : " or " + user.getPhone()));
    }

    /** The last lines of one log that were sent to the given address (email) or number (SMS). */
    static List<SentMessage> read(Path file, String channel, String address) {
        if (address == null || address.isBlank() || !Files.exists(file)) {
            return List.of();
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
        // a message starts with its date; a long message (like a receipt) continues on the next lines
        List<String> entries = new ArrayList<>();
        for (String line : lines) {
            if (MESSAGE_START.matcher(line).lookingAt() || entries.isEmpty()) {
                entries.add(line);
            } else {
                entries.set(entries.size() - 1, entries.get(entries.size() - 1) + "\n" + line);
            }
        }
        List<SentMessage> messages = new ArrayList<>();
        for (String entry : entries.subList(Math.max(0, entries.size() - LAST_MESSAGES), entries.size())) {
            String[] parts = entry.split(" \\| ", 4);
            if (parts.length == 4 && parts[1].startsWith("to: ") && parts[2].startsWith("subject: ")) {
                String to = parts[1].substring(4).trim();
                if (to.equalsIgnoreCase(address.trim())) {
                    messages.add(new SentMessage(channel, parts[0], to, parts[2].substring(9), parts[3]));
                }
            }
        }
        return messages;
    }
}
