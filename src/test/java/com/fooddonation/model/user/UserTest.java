package com.fooddonation.model.user;

import com.fooddonation.notification.ChannelType;
import com.fooddonation.notification.Recipient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {

    @Test
    void toRecipientCopiesContactDetailsAndUsesIdAsRecipientId() {
        Donor donor = new Donor("Sara", "sara@mail.com", "0100", "Cairo", DonorType.INDIVIDUAL, ChannelType.EMAIL);
        donor.setId(7);

        Recipient recipient = donor.toRecipient();

        assertEquals("7", recipient.getId());
        assertEquals("Sara", recipient.getName());
        assertEquals("sara@mail.com", recipient.getEmail());
        assertEquals("0100", recipient.getPhone());
        assertEquals(ChannelType.EMAIL, recipient.getPreferredChannel());
    }
}
