package com.lazoft.forwarderplus.views.common;


import com.lazoft.forwarderplus.entity.Client;
import com.lazoft.forwarderplus.enums.ClientType;
import com.lazoft.forwarderplus.services.ClientService;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;

public class ClientCreationDialog extends Dialog {

    public ClientCreationDialog(ClientService clientService, List<Client> clientList) {
        H3 h3 = new H3("Add Client");
        this.getHeader().add(h3);

        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("fit-content");

        TextField partyName = new TextField("Name (as printed on B/L)");
        partyName.setRequired(true);
        partyName.addBlurListener(event -> validateTextFieldForEmpty(event.getSource()));

        TextField email = new TextField("Email");

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
            if (partyName.isInvalid() || partyType.isInvalid() || address.isInvalid()) {
                NotificationUtil.getNotification("Please fill up all required fields", "", false,
                        NotificationVariant.LUMO_WARNING, 4000).open();
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
                NotificationUtil.getNotification("Client Saved Successfully!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 4000).open();
                clientList.add(savedClient);
                this.close();
            } catch (Exception e) {
                NotificationUtil.getNotification("Unexpected Error while saving client!", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 5000).open();
            }
        });
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button closeButton = new Button("Close", event -> this.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

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
