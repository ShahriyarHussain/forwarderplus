package com.lazoft.forwarderplus.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

public class TemplateBadge extends Span {

    public TemplateBadge(String text, Runnable applyTemplate, Runnable deleteTemplate) {
        super(text);

        Button clearButton = new Button(VaadinIcon.CLOSE_SMALL.create());
        clearButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_TERTIARY_INLINE);
        clearButton.getStyle().set("cursor", "pointer");
        clearButton.setTooltipText("Delete Template");

        add(clearButton);

        getElement().getThemeList().add("badge pill");
        getStyle().set("background-color", "rgb(144, 173, 198, 0.5)");
        getStyle().set("color", "rgb(5, 5, 51)");
        getStyle().set("margin-left", "10px");
        getStyle().set("cursor", "pointer");
        addClickListener(event -> applyTemplate.run());
        clearButton.addClickListener(event -> deleteTemplate.run());
    }
}
