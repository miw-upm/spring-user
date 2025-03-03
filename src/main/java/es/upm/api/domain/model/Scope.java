package es.upm.api.domain.model;

import java.util.Arrays;
import java.util.List;

public enum Scope {
    ADMIN, MANAGER, OPERATOR, CUSTOMER, AUTHENTICATED;

    public static final String PREFIX = "SCOPE_";

    public static List<String> allValues() {
        return Arrays.stream(Scope.values())
                .map(Scope::value)
                .toList();
    }

    public static Scope of(String withPrefix) {
        return Scope.valueOf(withPrefix.replace(Scope.PREFIX, "").toUpperCase());
    }

    public String scopeValue(){
        return PREFIX+this.name().toLowerCase();
    }

    public String value() {
        return this.name().toLowerCase();
    }


}
