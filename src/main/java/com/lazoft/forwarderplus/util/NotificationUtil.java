package com.lazoft.forwarderplus.util;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class NotificationUtil {

    public static Notification getNotification(String message, String expandedMessage, boolean isExpandable,
                                       NotificationVariant variant, int duration) {
        Notification notification = new Notification();
        Icon icon =  variant == NotificationVariant.LUMO_PRIMARY ? VaadinIcon.CHECK_CIRCLE.create()
                : variant == NotificationVariant.LUMO_ERROR ? VaadinIcon.CLOSE_CIRCLE.create()
                : VaadinIcon.EXCLAMATION_CIRCLE.create();

        Button closeBtn = new Button(VaadinIcon.CLOSE_SMALL.create(), clickEvent -> notification.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        HorizontalLayout layout;
        Text text = new Text(message);
        if (isExpandable) {
            Button expandBtn = new Button("View", clickEvent -> {
                clickEvent.getSource().setVisible(false);
                text.setText(message + ": " + System.lineSeparator() + expandedMessage);
            });
            layout = new HorizontalLayout(icon, text, expandBtn, closeBtn);
        } else {
            layout = new HorizontalLayout(icon, text, closeBtn);
        }
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        notification.add(layout);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.setDuration(duration);
        notification.addThemeVariants(variant);
        return notification;
    }
}
