package com.lazoft.forwarderplus.views.commonViews;

import com.lazoft.forwarderplus.entity.Carrier;
import com.lazoft.forwarderplus.services.CarrierService;
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

public class CarrierCreationDialog extends Dialog {

    public CarrierCreationDialog(CarrierService carrierService) {
        H3 h3 = new H3("Add Carrier");
        this.getHeader().add(h3);

        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("fit-content");

        TextField carrierName = new TextField("Port Name");
        carrierName.setRequired(true);
        TextField carrierCountry = new TextField("Country");
        

        Button addButton = new Button("Add", event -> {
            if (StringUtils.isBlank(carrierName.getValue())) {
                carrierName.setInvalid(true);
                NotificationUtil.getNotification("Must provide Carrier Name", "", false,
                        NotificationVariant.LUMO_WARNING, 4000).open();
                return;
            }
            Carrier carrier = new Carrier();
            carrier.setName(carrierName.getValue());
            carrier.setCountry(carrierCountry.getValue());


            try {
                carrierService.savePort(carrier);
                NotificationUtil.getNotification("Carrier Created Successfully!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 4000).open();
                this.close();
            } catch (Exception e) {
                NotificationUtil.getNotification("Unexpected Error while saving Carrier!", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 5000).open();
            }
        });
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button closeButton = new Button("Close", event -> this.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        formLayout.add(carrierName, carrierCountry);

        this.add(formLayout);
        this.setWidth(700, Unit.PIXELS);
        this.getFooter().add(addButton, closeButton);
    }
}