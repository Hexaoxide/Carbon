package net.draycia.simplechat

import net.kyori.text.Component
import net.kyori.text.TextComponent
import net.kyori.text.event.ClickEvent
import net.kyori.text.event.HoverEvent
import net.kyori.text.event.HoverEvent.Action.SHOW_TEXT

fun Component.removeEscape(escape: Char = '\\'): Component {
    if (this is TextComponent) {
        return content(content().filterNot { it == escape })
                .children(children().map { it.removeEscape(escape) })
                .clickEvent(clickEvent()?.run {
                    ClickEvent.of(action(), value().filterNot { it == escape })
                })
                .hoverEvent(hoverEvent()?.run {
                    if (action() == SHOW_TEXT) {
                        HoverEvent.of(action(), value().removeEscape(escape))
                    } else {
                        this
                    }
                })
    }

    return this
}