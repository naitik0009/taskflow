package com.taskflow.security;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/** Convenience accessor for the authenticated principal. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UserPrincipal principal() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("No authenticated user in context");
        }
        return principal;
    }

    public static UUID id() {
        return principal().getId();
    }
}
