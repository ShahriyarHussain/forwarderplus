package com.lazoft.forwarderplus.views.newbooking;

import com.lazoft.forwarderplus.entity.Carrier;
import com.lazoft.forwarderplus.entity.Client;
import com.lazoft.forwarderplus.entity.Port;
import com.lazoft.forwarderplus.enums.ClientType;
import com.lazoft.forwarderplus.enums.ContainerSize;
import com.lazoft.forwarderplus.enums.ContainerType;
import com.lazoft.forwarderplus.services.CarrierService;
import com.lazoft.forwarderplus.services.ClientService;
import com.lazoft.forwarderplus.services.PortService;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H6;
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
import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.LinkedList;
import java.util.List;

@PageTitle("New Booking")
@Route(value = "new-booking", layout = MainLayout.class)
@RolesAllowed("USER")
public class NewBookingView extends Composite<VerticalLayout> {

    private final TextField bookingNo = new TextField("Booking No");
    private final ComboBox<ContainerType> containerType = new ComboBox<>("Container Type");
    private final ComboBox<ContainerSize> containerSize = new ComboBox<>("Container Size");
    private final IntegerField numberOfContainers = new IntegerField("Number of Containers");
    private final TextField commodity = new TextField("Commodity");
    private final ComboBox<Client> clients = new ComboBox<>("Shipper");
    private final ComboBox<Carrier> carrier = new ComboBox<>("Carrier");
    private final ComboBox<Port> loadingPort = new ComboBox<>("Port Of Loading");
    private final ComboBox<Port> destinationPort = new ComboBox<>("Port Of Loading");
    private final TextField inLandDestination = new TextField("In Land Location (ie. Origin: Dhaka ICD, Dest: Istanbul Depot)");

    private final List<Client> clientList = new LinkedList<>();
    private final List<Carrier> carrierList = new LinkedList<>();
    private final List<Port> portList = new LinkedList<>();

    private final Button createBooking = new Button("Create Booking");
    private final Button reset = new Button("Reset");
    VerticalLayout bookingSummary = new VerticalLayout();


    private final ClientService clientService;
    private final CarrierService carrierService;
    private final PortService portService;


    public NewBookingView(ClientService clientService, CarrierService carrierService, PortService portService) {
        this.clientService = clientService;
        this.carrierService = carrierService;
        this.portService = portService;

        setComponentAttributes();
        prepareBookingSummary();

        HorizontalLayout clientsLayout = getClientLayout();
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.add(bookingNo, numberOfContainers, containerType, containerSize, commodity, carrier, loadingPort,
                destinationPort, inLandDestination, clientsLayout );
//        formLayout.setColspan(clientsLayout, 2);
        formLayout.setMaxWidth("75%");

        HorizontalLayout layoutRow = new HorizontalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        VerticalLayout layoutColumn3 = new VerticalLayout();
        H3 h3 = new H3();
        H2 h32 = new H2();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        h3.setText("New Booking (Export)");
        h3.setWidth("max-content");
        layoutColumn3.setWidth("450px");
        layoutColumn3.getStyle().set("flex-grow", "1");
        h32.setText("Booking Summary");
        h32.setWidth("max-content");
        getContent().add(layoutRow);
        layoutRow.add(layoutColumn2);
        layoutColumn2.add(h3, formLayout, new HorizontalLayout(createBooking, reset));
        layoutRow.add(layoutColumn3);
        layoutColumn3.add(h32, bookingSummary);


    }

    private void setComponentAttributes() {
        setCarrierAttributes();
        setButtonAttributes();
        setPortAttributes();

        containerSize.setRequired(true);
        containerSize.setItems(ContainerSize.values());
        containerSize.setItemLabelGenerator(ContainerSize::getContainerSize);

        containerType.setRequired(true);
        containerType.setItems(ContainerType.values());
        containerType.setItemLabelGenerator(ContainerType::getContainerType);
    }

    private void setCarrierAttributes() {
        carrier.setRequired(true);
        carrier.addFocusListener(event -> {
            if (carrierList.isEmpty()) {
                carrierList.addAll(carrierService.getAllCarriers());
            }
            carrier.setItems(carrierList);
        });
        carrier.setItemLabelGenerator(Carrier::getName);
    }

    private void setButtonAttributes() {
        setCreateBookingButtonAttributes();
        setResetButtonAttributes();
    }

    private void setCreateBookingButtonAttributes() {
        createBooking.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBooking.setIcon(LineAwesomeIcon.PLUS_SOLID.create());
        createBooking.addClickListener(event -> {
            Notification notification = new Notification();
            notification.setDuration(4000);
            notification.setPosition(Notification.Position.TOP_END);
            if (isAllFieldsValid()) {
                prepareBookingSummary();
                notification.setText("New Booking Successfully Created. Booking Id: " + bookingNo.getValue());
                notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            } else {
                notification.setText("Please provide correct data in the marked fields!");
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
            notification.open();
        });
    }

    private void setResetButtonAttributes() {
        bookingNo.clear();
        numberOfContainers.clear();
        containerType.clear();
        containerSize.clear();
        commodity.clear();
        carrier.clear();
        clients.clear();
    }

    private boolean isAllFieldsValid() {
        bookingNo.setInvalid(false);
        numberOfContainers.setInvalid(false);
        containerType.setInvalid(false);
        containerSize.setInvalid(false);
        commodity.setInvalid(false);
        carrier.setInvalid(false);
        clients.setInvalid(false);

        boolean isValid = true;
        if (destinationPort.getValue() == null) {
            destinationPort.setInvalid(true);
            destinationPort.setErrorMessage("Must Select Destination Port");
            destinationPort.focus();
            isValid = false;
        }
        if (loadingPort.getValue() == null) {
            loadingPort.setInvalid(true);
            loadingPort.setErrorMessage("Must Select Loading Port");
            loadingPort.focus();
            isValid = false;
        }
        if (clients.getValue() == null) {
            clients.setInvalid(true);
            clients.setErrorMessage("Please choose a shipper or create a new one.");
            clients.focus();
            isValid = false;
        }
        if (carrier.getValue() == null) {
            carrier.setInvalid(true);
            carrier.setErrorMessage("Please choose a carrier!");
            carrier.focus();
            isValid = false;
        }
        if (StringUtils.isAllBlank(commodity.getValue())) {
            commodity.setInvalid(true);
            commodity.setErrorMessage("Commodity Name Cannot be Empty!");
            commodity.focus();
            isValid = false;
        }
        if (containerSize.getValue() == null) {
            containerSize.setInvalid(true);
            containerSize.setErrorMessage("Please choose a container size!");
            containerSize.focus();
            isValid = false;
        }
        if (containerType.getValue() == null) {
            containerType.setInvalid(true);
            containerType.setErrorMessage("Please choose a container type!");
            containerType.focus();
            isValid = false;
        }
        if (numberOfContainers.getValue() == null || numberOfContainers.getValue() <= 0) {
            numberOfContainers.setInvalid(true);
            numberOfContainers.setErrorMessage("Please provide a valid number between 1 and 10,000!");
            numberOfContainers.focus();
            isValid = false;
        }
        if (StringUtils.isAllBlank(bookingNo.getValue())) {
            bookingNo.setInvalid(true);
            bookingNo.setErrorMessage("Booking No Cannot be Empty!");
            bookingNo.focus();
            isValid = false;
        }

        return isValid;
    }

    private HorizontalLayout getClientLayout() {
        HorizontalLayout clientLayout = new HorizontalLayout();
        clientLayout.setAlignItems(VerticalLayout.Alignment.END);
        Button addButton = new Button();
        addButton.setTooltipText("Add New Client");
        addButton.setIcon(LineAwesomeIcon.USER_PLUS_SOLID.create());
        addButton.setWidth("10%");
        addButton.addClickListener(event -> createClientDialog().open());

        clients.setWidth("90%");
        clients.setRequired(true);
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
        dialog.setWidth(700, Unit.PIXELS);
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

    private void prepareBookingSummary() {
        H4 exportBooking = new H4("Export Booking");

        HorizontalLayout bookingLayout = new HorizontalLayout(new H6("Booking No: "), new Text(bookingNo.getValue()));
        bookingLayout.setAlignItems(FlexComponent.Alignment.END);

        HorizontalLayout containerLayout = new HorizontalLayout(new H6("Container: "),
                new Text(numberOfContainers.getValue() + " X " +
                        (containerSize.getValue() == null ? "" : containerSize.getValue().getContainerSize()) + " " +
                        (containerType.getValue() == null ? "" : containerType.getValue().getContainerType())));
        containerLayout.setAlignItems(FlexComponent.Alignment.END);

        HorizontalLayout commodityLayout = new HorizontalLayout(new H6("Commodity: "),
                new Text(commodity.getValue()));
        commodityLayout.setAlignItems(FlexComponent.Alignment.END);

        HorizontalLayout carrierLayout = new HorizontalLayout(new H6("Carrier: "),
                new Text(carrier.getValue() == null ? "" : carrier.getValue().getName()));
        carrierLayout.setAlignItems(FlexComponent.Alignment.END);

        HorizontalLayout shipperLayout = new HorizontalLayout(new H6("Shipper: "),
                new Text(clients.getValue() == null ? "" : clients.getValue().getName()));
        shipperLayout.setAlignItems(FlexComponent.Alignment.END);

        bookingSummary.removeAll();
        bookingSummary.add(exportBooking, shipperLayout, containerLayout, commodityLayout, carrierLayout, bookingLayout);
        bookingSummary.setVisible(true);
    }

    private void setPortAttributes() {
        loadingPort.setRequired(true);
        loadingPort.addFocusListener(event -> loadPortComponentPropertiesOnDemand(loadingPort));

        destinationPort.setRequired(true);
        destinationPort.addFocusListener(event -> loadPortComponentPropertiesOnDemand(destinationPort));
    }

    private void loadPortComponentPropertiesOnDemand(ComboBox<Port> portComboBox) {
        if (portList.isEmpty()) {
            portList.addAll(portService.getAllPorts());
        }
        portComboBox.setItems(portList);
        portComboBox.setItemLabelGenerator(Port::getPortLabel);
    }
}
