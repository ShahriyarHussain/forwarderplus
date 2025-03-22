package com.lazoft.forwarderplus.components.dialog;

import com.lazoft.forwarderplus.entity.Port;
import com.lazoft.forwarderplus.services.PortService;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

public class PortCreationDialog extends Dialog {

    public PortCreationDialog(PortService portService) {
        H3 h3 = new H3("Add Port");
        this.getHeader().add(h3);

        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("fit-content");

        TextField portName = new TextField("Port Name");
        portName.setRequired(true);
        TextField portCountry = new TextField("Country");
        TextField portCity = new TextField("City");
        TextField portShortCode = new TextField("Port Short Code");
        portShortCode.setRequired(true);


        Button addButton = new Button("Add", event -> {
            if (StringUtils.isBlank(portName.getValue()) || StringUtils.isBlank(portShortCode.getValue())) {
                portName.setInvalid(true);
                portShortCode.setInvalid(true);
                NotificationUtil.getNotification("Please fill up all required fields", "", false,
                        NotificationVariant.LUMO_WARNING, 4000).open();
                return;
            }
            Port port = new Port();
            port.setPortName(portName.getValue());
            port.setPortCountry(portCountry.getValue());
            port.setPortCity(portCity.getValue());
            port.setPortShortCode(portShortCode.getValue());

            try {
                portService.savePort(port);
                NotificationUtil.getNotification("Port Created Successfully!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 4000).open();
                this.close();
            } catch (Exception e) {
                NotificationUtil.getNotification("Unexpected Error while saving Port!", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 5000).open();
            }
        });
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button closeButton = new Button("Close", event -> this.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        formLayout.add(portName, portShortCode, portCity, portCountry);

        this.add(formLayout);
        this.setWidth(700, Unit.PIXELS);
        this.getFooter().add(addButton, closeButton);
    }
}