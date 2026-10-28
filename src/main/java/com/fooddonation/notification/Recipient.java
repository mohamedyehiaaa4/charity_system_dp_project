package com.fooddonation.notification;

public class Recipient {
    private String id;
    private String name;
    private String email;
    private String phone;
    private ChannelType preferredChannel;

    public Recipient() {
    }

    public Recipient(String id, String name, String email, String phone, ChannelType preferredChannel) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.preferredChannel = preferredChannel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public ChannelType getPreferredChannel() {
        return preferredChannel;
    }

    public void setPreferredChannel(ChannelType preferredChannel) {
        this.preferredChannel = preferredChannel;
    }
}
