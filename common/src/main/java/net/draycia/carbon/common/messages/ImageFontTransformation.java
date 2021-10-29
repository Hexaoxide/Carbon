package net.draycia.carbon.common.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.parser.node.ElementNode;
import net.kyori.adventure.text.minimessage.parser.node.TagPart;
import net.kyori.adventure.text.minimessage.transformation.Modifying;
import net.kyori.adventure.text.minimessage.transformation.Transformation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ImageFontTransformation extends Transformation implements Modifying {

    final Component nbsp = Component.translatable("space.-1");
    final JoinConfiguration joinConfiguration = JoinConfiguration.separator(nbsp);
    final List<TagPart> args;

    public ImageFontTransformation(List<TagPart> args) {
        this.args = args;
    }

    @Override
    public void visit(final ElementNode curr) {

    }

    @Override
    public Component apply(final Component curr, final int depth) {
        final var siblings = new ArrayList<ComponentLike>();

        curr.replaceText(TextReplacementConfig.builder()
                .match(Pattern.compile("[^\\uF801]"))
                .replacement(builder -> {
                    // We do this instead of just in place replacing
                    // Returning the result of the replacement results in "grouped" components of the builder and translatable
                    // This results in render errors
                    // The goal here is to make ALL characters and translatable components siblings
                    siblings.add(builder);

                    return builder;
                })
                .build());

        return Component.join(this.joinConfiguration, siblings);
    }

    @Override
    public Component apply() {
        return Component.empty();
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

}
