package es.upm.api.resources;

public final class Security {
    public static final String ADMIN_MANAGER_OPERATOR = "hasAnyAuthority('SCOPE_admin','SCOPE_manager','SCOPE_operator')";
    public static final String CUSTOMER_OWNER = "hasAuthority('SCOPE_customer') and #mobile == authentication.name";
    public static final String ALL = "permitAll()";
    public static final String OR = " or ";
    public static final String AND = " and ";

    private Security() {
        // Forbidden
    }
}
