package com.fooddonation.ui;

import com.fooddonation.model.user.User;

/** Who is signed in. Shared by every controller, so all screens act as the same user. */
public class Session {
    private User user;

    public User getUser() {
        if (user == null) {
            throw new IllegalStateException("Nobody is signed in");
        }
        return user;
    }

    public void signIn(User user) {
        this.user = user;
    }

    public void signOut() {
        user = null;
    }
}
