package com.fooddonation.model.user;

import com.fooddonation.notification.ChannelType;
import com.fooddonation.notification.Recipient;

public abstract class User {
    private long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Role role;
    private ChannelType preferredChannel;

    protected User(Role role) {
        this.role = role;
    }

    protected User(String name, String email, String phone, String address, Role role,
                   ChannelType preferredChannel) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.preferredChannel = preferredChannel;
    }

    public Recipient toRecipient() {
        return new Recipient(String.valueOf(id), name, email, phone, preferredChannel);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public ChannelType getPreferredChannel() {
        return preferredChannel;
    }

    public void setPreferredChannel(ChannelType preferredChannel) {
        this.preferredChannel = preferredChannel;
    }
}
