package com.lazoft.forwarderplus.views.commonViews;


import com.lazoft.forwarderplus.entity.Client;
import com.lazoft.forwarderplus.enums.ClientType;
import com.lazoft.forwarderplus.services.ClientService;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;

public class ClientCreationDialogView extends Dialog {

    public ClientCreationDialogView(ClientService clientService, List<Client> clientList) {
        H3 h3 = new H3("Add Shipper");
        this.getHeader().add(h3);

        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("fit-content");

        TextField partyName = new TextField("Name (as printed on B/L)");
        partyName.setRequired(true);
        partyName.addBlurListener(event -> validateTextFieldForEmpty(event.getSource()));

        TextField email = new TextField("Email");
        //email.setRequired(true);
        //partyName.addBlurListener(event -> validateTextFieldForEmpty(event.getSource());A

        TextArea address = new TextArea("Address (as printed on B/L");
        address.addBlurListener(event -> {
            TextArea area = event.getSource();
            if (area.getValue() == null || area.getValue().trim().isEmpty()) {
                area.setInvalid(true);
                area.setErrorMessage("Cannot be empty");
            } else {
                area.setInvalid(false);
            }
        });

        TextField city = new TextField("City");
        TextField country = new TextField("Country");
        TextField taxId = new TextField("TaxId");
        TextField postCode = new TextField("Post/Zip Code");
        TextField accountNum = new TextField("Bank Account");
        TextField accountBank = new TextField("Bank Name");


        ComboBox<ClientType> partyType = new ComboBox<>("Party Type");
        partyType.setItems(ClientType.values());
        partyType.setItemLabelGenerator(ClientType::name);
        partyType.setRequired(true);
        partyType.setRequiredIndicatorVisible(true);
        partyType.setValue(ClientType.SHIPPER);

        Button addButton = new Button("Add", event -> {
            Notification notification = new Notification();
            notification.setDuration(4000);
            notification.setPosition(Notification.Position.TOP_END);
            if (partyName.isInvalid() || partyType.isInvalid() || address.isInvalid()) {
                notification.setText("Please fill up all required fields");
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                notification.open();
                return;
            }
            Client client = new Client();
            client.setName(partyName.getValue());
            client.setType(partyType.getValue());
            client.setCity(city.getValue());
            client.setAddress(address.getValue());
            client.setCountry(country.getValue());
            client.setPostCode(postCode.getValue());
            client.setTaxId(taxId.getValue());
            client.setEmail(email.getValue());
            client.setAccountBank(accountBank.getValue());
            client.setAccountNumber(accountNum.getValue());

            try {
                Client savedClient = clientService.saveClient(client);
                notification.setText("Shipper Added Successfully!");
                notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                clientList.add(savedClient);
                this.close();
            } catch (Exception e) {
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setText("Error: " + e.getMessage());
                notification.open();
            }
            notification.open();
        });
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button closeButton = new Button("Close", event -> this.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        formLayout.add(partyName, partyType, address, email, city, country, taxId, postCode, accountNum, accountBank);
        formLayout.setColspan(address, 2);

        this.add(formLayout);
        this.setWidth(700, Unit.PIXELS);
        this.getFooter().add(addButton, closeButton);
    }

    private void validateTextFieldForEmpty(TextField textField) {
        if (textField.getValue() == null || textField.getValue().trim().isEmpty()) {
            textField.setInvalid(true);
            textField.setErrorMessage("Cannot be empty");
        } else {
            textField.setInvalid(false);
        }
    }
}
