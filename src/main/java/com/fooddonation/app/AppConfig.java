package com.fooddonation.app;

import com.fooddonation.notification.ChannelType;
import com.fooddonation.notification.Recipient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Settings read from app.properties. A missing setting fails at startup with a clear message. */
public class AppConfig {
    private final Properties props;

    public AppConfig(Properties props) {
        this.props = props;
    }

    /** Loads app.properties from the classpath. */
    public static AppConfig load() {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream("/app.properties")) {
            if (in == null) {
                throw new IllegalStateException("Missing app.properties");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read app.properties", e);
        }
        return new AppConfig(props);
    }

    public ZoneId timezone() {
        return ZoneId.of(get("timezone"));
    }

    public Path emailLog() {
        return Path.of(get("log.email"));
    }

    public Path smsLog() {
        return Path.of(get("log.sms"));
    }

    public Duration expiryCheckPeriod() {
        return Duration.ofSeconds(Long.parseLong(get("expiry.check.seconds")));
    }

    public Duration expiryWarningWindow() {
        return Duration.ofHours(Long.parseLong(get("expiry.warning.hours")));
    }

    public String coordinatorEmail() {
        return get("coordinator.email");
    }

    /** Volunteers are listed as volunteer.1.name, volunteer.1.email, ... up to volunteers.count. */
    public List<Recipient> volunteers() {
        int count = Integer.parseInt(props.getProperty("volunteers.count", "0").trim());
        List<Recipient> volunteers = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String key = "volunteer." + i + ".";
            volunteers.add(new Recipient("volunteer-" + i, get(key + "name"), get(key + "email"),
                    get(key + "phone"), ChannelType.valueOf(get(key + "channel"))));
        }
        return volunteers;
    }

    private String get(String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing setting in app.properties: " + key);
        }
        return value.trim();
    }
}
