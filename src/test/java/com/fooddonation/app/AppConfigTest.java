package com.fooddonation.app;

import com.fooddonation.notification.ChannelType;
import com.fooddonation.notification.Recipient;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppConfigTest {

    @Test
    void readsTheRealAppProperties() {
        AppConfig config = AppConfig.load();
        assertEquals(ZoneId.of("Africa/Cairo"), config.timezone());
        assertEquals(Duration.ofSeconds(60), config.expiryCheckPeriod());
        assertEquals(Duration.ofHours(48), config.expiryWarningWindow());
        assertEquals("coordinator@fooddonation.org", config.coordinatorEmail());

        List<Recipient> volunteers = config.volunteers();
        assertEquals(1, volunteers.size());
        assertEquals("volunteer-1", volunteers.get(0).getId());
        assertEquals(ChannelType.EMAIL, volunteers.get(0).getPreferredChannel());
    }

    @Test
    void missingSettingFailsClearly() {
        Properties props = new Properties();
        props.setProperty("volunteers.count", "1");
        AppConfig config = new AppConfig(props);
        IllegalStateException e = assertThrows(IllegalStateException.class, config::volunteers);
        assertEquals("Missing setting in app.properties: volunteer.1.name", e.getMessage());
    }
}
