/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNull;
import static net.kyori.adventure.text.Component.empty;

@DefaultQualifier(NonNull.class)
public interface Pagination<T> {

    ComponentLike header(int page, int pages);

    ComponentLike footer(int page, int pages);

    ComponentLike pageOutOfRange(int page, int pages);

    ComponentLike item(T item, boolean lastOfPage);

    default List<Component> render(
        final Collection<T> content,
        final int page,
        final int itemsPerPage
    ) {
        if (content.isEmpty()) {
            throw new IllegalArgumentException("Cannot paginate an empty collection.");
        }

        final int pages = (int) Math.ceil(content.size() / (itemsPerPage * 1.00));
        if (page < 1 || page > pages) {
            return Collections.singletonList(this.pageOutOfRange(page, pages).asComponent());
        }

        final List<Component> renderedContent = new ArrayList<>();

        final Component header = this.header(page, pages).asComponent();
        if (header != empty()) {
            renderedContent.add(header);
        }

        final int start = itemsPerPage * (page - 1);
        final int maxIndex = start + itemsPerPage;

        if (content instanceof RandomAccess && content instanceof final List<T> contentList) {
            for (int i = start; i < maxIndex; i++) {
                if (i > content.size() - 1) {
                    break;
                }
                renderedContent.add(this.item(contentList.get(i), i == maxIndex - 1).asComponent());
            }
        } else {
            final Iterator<T> iterator = content.iterator();
            for (int i = 0; i < start && iterator.hasNext(); i++) {
                iterator.next();
            }
            for (int i = start; i < maxIndex && iterator.hasNext(); ++i) {
                renderedContent.add(this.item(iterator.next(), i == maxIndex - 1).asComponent());
            }
        }

        final Component footer = this.footer(page, pages).asComponent();
        if (footer != empty()) {
            renderedContent.add(footer);
        }

        return Collections.unmodifiableList(renderedContent);
    }

    static <T> Builder<T> builder() {
        return new Builder<>();
    }

    final class Builder<T> {
        private BiIntFunction<ComponentLike> headerRenderer = ($, $$) -> empty();
        private BiIntFunction<ComponentLike> footerRenderer = ($, $$) -> empty();
        private @MonotonicNonNull BiIntFunction<ComponentLike> pageOutOfRangeRenderer = null;
        private @MonotonicNonNull ItemRenderer<T> itemRenderer = null;

        private Builder() {
        }

        public Builder<T> header(final BiIntFunction<ComponentLike> headerRenderer) {
            this.headerRenderer = headerRenderer;
            return this;
        }

        public Builder<T> footer(final BiIntFunction<ComponentLike> footerRenderer) {
            this.footerRenderer = footerRenderer;
            return this;
        }

        public Builder<T> pageOutOfRange(final BiIntFunction<ComponentLike> pageOutOfRangeRenderer) {
            this.pageOutOfRangeRenderer = pageOutOfRangeRenderer;
            return this;
        }

        public Builder<T> item(final ItemRenderer<T> itemRenderer) {
            this.itemRenderer = itemRenderer;
            return this;
        }

        public Pagination<T> build() {
            return new DelegatingPaginationImpl<>(
                requireNonNull(this.headerRenderer, "Must provide a header renderer!"),
                requireNonNull(this.footerRenderer, "Must provide a footer renderer!"),
                requireNonNull(this.pageOutOfRangeRenderer, "Must provide a page out of range renderer!"),
                requireNonNull(this.itemRenderer, "Must provide an item renderer!")
            );
        }

        @FunctionalInterface
        public interface ItemRenderer<T> {
            ComponentLike render(T item, boolean lastOfPage);
        }

        private record DelegatingPaginationImpl<T>(
            BiIntFunction<ComponentLike> headerRenderer,
            BiIntFunction<ComponentLike> footerRenderer,
            BiIntFunction<ComponentLike> pageOutOfRangeRenderer,
            ItemRenderer<T> itemRenderer
        ) implements Pagination<T> {
            @Override
            public ComponentLike header(final int page, final int pages) {
                return this.headerRenderer.apply(page, pages);
            }

            @Override
            public ComponentLike footer(final int page, final int pages) {
                return this.footerRenderer.apply(page, pages);
            }

            @Override
            public ComponentLike pageOutOfRange(final int page, final int pages) {
                return this.pageOutOfRangeRenderer.apply(page, pages);
            }

            @Override
            public ComponentLike item(final T item, final boolean lastOfPage) {
                return this.itemRenderer.render(item, lastOfPage);
            }
        }
    }

    @FunctionalInterface
    interface BiIntFunction<T> {

        T apply(int i, int i1);

    }

}
