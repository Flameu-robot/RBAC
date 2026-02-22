package org.example.sorter;

import org.example.entity.User;
import java.util.Comparator;

public class UserSorters {

    private UserSorters() {}

    public static Comparator<User> byUsername() {
        return Comparator.comparing(User::username);
    }

    public static Comparator<User> byFullName() {
        return Comparator.comparing(User::fullname);
    }

    public static Comparator<User> byEmail() {
        return Comparator.comparing(User::email);
    }
}