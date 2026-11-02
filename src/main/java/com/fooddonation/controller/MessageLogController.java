package com.fooddonation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** COM-1: email and SMS are simulated by log files, so this tab shows what was "sent". */
public class MessageLogController implements Refreshable {
    private static final int LAST_LINES = 200;

    private final Path emailLog;
    private final Path smsLog;

    @FXML private TextArea emailArea;
    @FXML private TextArea smsArea;
    @FXML private Label emailPath;
    @FXML private Label smsPath;

    public MessageLogController(Path emailLog, Path smsLog) {
        this.emailLog = emailLog;
        this.smsLog = smsLog;
    }

    @FXML
    private void initialize() {
        emailPath.setText(emailLog.toAbsolutePath().toString());
        smsPath.setText(smsLog.toAbsolutePath().toString());
    }

    @Override
    @FXML
    public void refresh() {
        emailArea.setText(lastLines(emailLog));
        smsArea.setText(lastLines(smsLog));
        emailArea.positionCaret(emailArea.getLength());
        smsArea.positionCaret(smsArea.getLength());
    }

    private static String lastLines(Path file) {
        if (!Files.exists(file)) {
            return "Nothing sent on this channel yet.";
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            return String.join("\n", lines.subList(Math.max(0, lines.size() - LAST_LINES), lines.size()));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
    }
}
