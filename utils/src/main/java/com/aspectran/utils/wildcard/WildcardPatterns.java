package com.aspectran.utils.wildcard;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created: 2024-12-13</p>
 */
public class WildcardPatterns {

    private final WildcardPattern[] patterns;

    private WildcardPatterns(@NonNull WildcardPattern[] patterns) {
        this.patterns = patterns;
    }

    @NonNull
    public WildcardPattern[] getPatterns() {
        return patterns;
    }

    public boolean hasPatterns() {
        return (patterns.length > 0);
    }

    public boolean matches(CharSequence input) {
        for (WildcardPattern pattern : patterns) {
            if (pattern.matches(input)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    static WildcardPatterns of(WildcardPattern[] patterns) {
        if (patterns == null || patterns.length == 0) {
            return null;
        }
        return new WildcardPatterns(patterns);
    }

    @Nullable
    public static WildcardPatterns of(String[] patterns) {
        return of(patterns, Character.MIN_VALUE);
    }

    @Nullable
    public static WildcardPatterns of(String[] patterns, char separator) {
        if (patterns == null || patterns.length == 0) {
            return null;
        }
        List<WildcardPattern> list = new ArrayList<>(patterns.length);
        for (String pattern : patterns) {
            if (pattern != null && !pattern.isEmpty()) {
                WildcardPattern wildcardPattern = new WildcardPattern(pattern, separator);
                list.add(wildcardPattern);
            }
        }
        if (!list.isEmpty()) {
            return of(list.toArray(new WildcardPattern[0]));
        } else {
            return null;
        }
    }

}
