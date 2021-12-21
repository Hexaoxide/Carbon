package net.draycia.carbon.common.users.db;

import com.google.common.base.Splitter;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.locator.ClasspathSqlLocator;

public final class QueriesLocator {

    private static final Splitter SPLITTER = Splitter.on(';');
    private final ClasspathSqlLocator locator = ClasspathSqlLocator.create();

    public @NonNull String query(final @NonNull String name) {
        return this.locator.locate("queries/" + name);
    }

    public @NonNull List<@NonNull String> queries(final @NonNull String name) {
        return SPLITTER.splitToList(this.locator.locate("queries/" + name));
    }

}
