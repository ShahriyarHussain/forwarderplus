package com.lazoft.forwarderplus.components;

import com.vaadin.flow.component.html.Span;

public class Divider extends Span {

    public Divider() {
        getStyle().set("background-color", "grey");
        getStyle().set("flex", "0 0 2px");
        getStyle().set("align-self", "stretch");
    }
}