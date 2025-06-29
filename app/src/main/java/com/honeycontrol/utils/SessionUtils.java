package com.honeycontrol.utils;

public class SessionUtils {

    public static boolean isUserLoggedIn() {
        return UserSession.getInstance().isUserLoggedIn();
    }

    public static String getCurrentUserId() {
        return UserSession.getInstance().getCurrentUser().getId();
    }

    public static String getCurrentUserCompanyId() {
        return UserSession.getInstance().getCurrentUserCompanyId();
    }

    public static String getCurrentUserName() {
        return UserSession.getInstance().getCurrentUserName();
    }

}
