package de.yiluo.store.security;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// we need a set to store blacklist
// but regular set can not handle multiple people reading/changing it
// ConcurrentHashSet fix the problem
// but ConcurrentHashSet does not exist, only ConcurrentHashMap does
// so we use ConcurrentHashMap with key as set element and value as useless info
@Component
public class TokenBlacklist {
    // <> in new ConcurrentHashMap<>() is called diamond operator
    // it means new ConcurrentHashMap<String, boolean>(), where String is from Set<String>
    // just so less boilerplate is written
    private final Set<String> blacklistedTokens = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // final means the set is glued down, but we still can edit content inside it

    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}