package com.finflow.admin.context;


public class IdentityContext {

    private static final ThreadLocal<String> userEmail = new ThreadLocal<>();
    private static final ThreadLocal<String> userRole = new ThreadLocal<>();

    public static void set(String email, String role) {
        userEmail.set(email);
        userRole.set(role);
    }

    public static String getEmail() {
        return userEmail.get();
    }

    public static String getRole() {
        return userRole.get();
    }

    public static void clear() {
        userEmail.remove();
        userRole.remove();
    }
}
