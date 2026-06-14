package com.lazoft.forwarderplus.builder;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PopUpMessageBuilder {
    private String message;
    private String expandedMessage;
    private boolean isExpandable;
    private NotificationVariant variant;
    private int duration;

    public Notification generatePopUp() {
        Notification notification = new Notification();
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.setDuration(duration);
        notification.addThemeVariants(variant);
        Button closeBtn = new Button(VaadinIcon.CLOSE_SMALL.create(), clickEvent -> notification.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        Text text = new Text(message);
        Icon icon = getIconByVariant(variant);

        HorizontalLayout layout = new HorizontalLayout(icon, text, closeBtn);
        if (isExpandable) {
            Button expandBtn = new Button("View", clickEvent -> {
                clickEvent.getSource().setVisible(false);
                text.setText(message + ": " + System.lineSeparator() + expandedMessage);
            });
            layout = new HorizontalLayout(icon, text, expandBtn, closeBtn);
        }

        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        notification.add(layout);
        return notification;
    }

    private static Icon getIconByVariant(NotificationVariant variant) {
        return switch (variant) {
            case LUMO_PRIMARY -> VaadinIcon.CHECK_CIRCLE.create();
            case LUMO_ERROR -> VaadinIcon.ARROWS_CROSS.create();
            case LUMO_WARNING -> VaadinIcon.EXCLAMATION.create();
            default -> VaadinIcon.AIRPLANE.create();
        };
    }
}
