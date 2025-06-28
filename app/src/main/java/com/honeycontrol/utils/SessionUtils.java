package com.honeycontrol.utils;

import com.honeycontrol.UserSession;
import com.honeycontrol.models.User;

public class SessionUtils {

    public static boolean isUserLoggedIn() {
        return UserSession.getInstance().isUserLoggedIn();
    }

    public static User getCurrentUser() {
        return UserSession.getInstance().getCurrentUser();
    }

    public static String getCurrentUserCompanyId() {
        return UserSession.getInstance().getCurrentUserCompanyId();
    }

    public static String getCurrentUserName() {
        return UserSession.getInstance().getCurrentUserName();
    }

}
