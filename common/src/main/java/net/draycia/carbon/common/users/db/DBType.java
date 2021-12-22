package net.draycia.carbon.common.users.db;

public final class DBType {

    private final String basePath;

    private DBType(final String basePath) {
        this.basePath = basePath;
    }

    public String basePath() {
        return this.basePath;
    }

    public static final DBType MYSQL = new DBType("queries/sql/");
    //public static final DBType POSTGRESQL = new DBType("queries/postgresql/");

}
