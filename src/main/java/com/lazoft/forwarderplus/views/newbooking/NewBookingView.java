package com.lazoft.forwarderplus.views.newbooking;

import com.lazoft.forwarderplus.entity.Client;
import com.lazoft.forwarderplus.enums.ClientType;
import com.lazoft.forwarderplus.enums.ContainerSize;
import com.lazoft.forwarderplus.enums.ContainerType;
import com.lazoft.forwarderplus.services.ClientService;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.LinkedList;
import java.util.List;

@PageTitle("New Booking")
@Route(value = "new-booking", layout = MainLayout.class)
@RolesAllowed("USER")
public class NewBookingView extends Composite<VerticalLayout> {

    private TextField bookingNo = new TextField("Booking No");
    private ComboBox<ContainerType> containerType = new ComboBox<>("Container Type");
    private ComboBox<ContainerSize> containerSize = new ComboBox<>("Container Size");
    private IntegerField numberOfContainers = new IntegerField("Number of Containers");
    private TextField commodity = new TextField("Commodity");
    private ComboBox<Client> clients = new ComboBox<>("Shipper");

    private List<Client> clientList = new LinkedList<>();


    private final ClientService clientService;


    public NewBookingView(ClientService clientService) {
        this.clientService = clientService;

        HorizontalLayout clientsLayout = getClientLayout();

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.add(bookingNo, numberOfContainers, containerType, containerSize, commodity, clientsLayout);
        formLayout.setColspan(clientsLayout, 2);
        formLayout.setMaxWidth("70%");

        HorizontalLayout layoutRow = new HorizontalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        H3 h3 = new H3();
        VerticalLayout layoutColumn3 = new VerticalLayout();
        H3 h32 = new H3();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        h3.setText("Details");
        h3.setWidth("max-content");
        layoutColumn3.setWidth("400px");
        layoutColumn3.getStyle().set("flex-grow", "1");
        h32.setText("Booking Summary");
        h32.setWidth("max-content");
        getContent().add(layoutRow);
        layoutRow.add(layoutColumn2);
        layoutColumn2.add(h3, formLayout);
        layoutRow.add(layoutColumn3);
        layoutColumn3.add(h32);


    }

    private HorizontalLayout getClientLayout() {
        HorizontalLayout clientLayout = new HorizontalLayout();
        clientLayout.setAlignItems(VerticalLayout.Alignment.END);
        Button addButton = new Button();
        addButton.setTooltipText("Add New Client");
        addButton.setIcon(LineAwesomeIcon.USER_PLUS_SOLID.create());
        addButton.setWidth(4, Unit.REM);
        addButton.addClickListener(event -> {
           createClientDialog().open();
        });

        clients.setWidth("60%");
        clients.setAllowCustomValue(true);
        clients.setItemLabelGenerator(Client::getName);
        clients.setItems(clientList);
        clients.addFocusListener(event -> {
            if (clientList.isEmpty()) {
                clientList.addAll(clientService.getAllClients());
            }
            clients.setItems(clientList);
        });
        clientLayout.add(clients, addButton);

        return clientLayout;
    }

    private Dialog createClientDialog() {
        Dialog dialog = new Dialog();

        H3 h3 = new H3("Add Shipper");
        dialog.getHeader().add(h3);

        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("fit-content");

        TextField partyName = new TextField("Name (as printed on B/L)");
        partyName.setValue(clients.getValue() != null ? clients.getValue().getName() : "");
        partyName.setRequired(true);
        partyName.addBlurListener(event -> validateTextFieldForEmpty(event.getSource()));

        TextField email = new TextField("Email");
        //email.setRequired(true);
        //partyName.addBlurListener(event -> validateTextFieldForEmpty(event.getSource());

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
            notification.setDuration(3000);
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
                dialog.close();
            } catch (Exception e) {
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setText("Error: " + e.getMessage());
                notification.open();
            }
            notification.open();
        });
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button closeButton = new Button("Close", event -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        formLayout.add(partyName, partyType, address, email, city, country, taxId, postCode, accountNum, accountBank);
        formLayout.setColspan(address, 2);

        dialog.add(formLayout);
        dialog.setWidth("fit-content");
        dialog.getFooter().add(addButton, closeButton);

        return dialog;
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
