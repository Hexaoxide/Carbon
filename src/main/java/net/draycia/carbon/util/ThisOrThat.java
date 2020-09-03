package net.draycia.carbon.util;

import org.jetbrains.annotations.Nullable;

public class ThisOrThat<A, B> {

    @Nullable A objectA;
    @Nullable B objectB;

    public ThisOrThat(@Nullable A objectA, @Nullable B objectB) {
        this.objectA = objectA;
        this.objectB = objectB;
    }

    public boolean isThis() {
        return objectA != null;
    }

    public boolean isThat() {
        return objectB != null;
    }

    public @Nullable A getThis() {
        return objectA;
    }

    public @Nullable B getThat() {
        return objectB;
    }

}
