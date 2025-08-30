package com.lazoft.forwarderplus.views.export;

import com.lazoft.forwarderplus.components.TemplateBadge;
import com.lazoft.forwarderplus.components.dialog.ClientCreationDialog;
import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.ClientType;
import com.lazoft.forwarderplus.enums.ContainerSize;
import com.lazoft.forwarderplus.enums.ContainerType;
import com.lazoft.forwarderplus.model.xml.BookingTemplate;
import com.lazoft.forwarderplus.model.xml.CustomItem;
import com.lazoft.forwarderplus.model.xml.CustomItems;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.BookingService;
import com.lazoft.forwarderplus.services.CarrierService;
import com.lazoft.forwarderplus.services.ClientService;
import com.lazoft.forwarderplus.services.PortService;
import com.lazoft.forwarderplus.util.CustomItemUtil;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static com.lazoft.forwarderplus.util.Constants.COMMODITIES;
import static com.lazoft.forwarderplus.util.Constants.TEMPLATES;

@Slf4j
@PageTitle("New Booking")
@Route(value = "new-booking", layout = MainLayout.class)
@RolesAllowed({"EXPORT", "ADMIN"})
public class NewBookingView extends Composite<VerticalLayout> {

    private final TextField bookingNo = new TextField("Booking No");
    private final ComboBox<ContainerType> containerType = new ComboBox<>("Container Type");
    private final ComboBox<ContainerSize> containerSize = new ComboBox<>("Container Size");
    private final IntegerField numberOfContainers = new IntegerField("Number of Containers");
    private final ComboBox<String> commodity = new ComboBox<>("Commodity");
    private final ComboBox<Client> clients = new ComboBox<>("Shipper");
    private final ComboBox<Carrier> carrier = new ComboBox<>("Carrier");
    private final ComboBox<Port> loadingPort = new ComboBox<>("Port Of Loading");
    private final ComboBox<Port> destinationPort = new ComboBox<>("Port Of Destination");
    private final TextField remarks = new TextField("Remarks");
    private final IntegerField numOfShipments = new IntegerField("Number Of Shipments");

    private final Div templateLayout = new Div();

    private final Button createBooking = new Button("Create Booking");
    private final Button reset = new Button("Reset");

    private final List<Client> clientList = new LinkedList<>();
    private final List<Carrier> carrierList = new LinkedList<>();
    private final List<Port> portList = new LinkedList<>();
    private final List<CustomItem> commodityList;

    private final ClientService clientService;
    private final CarrierService carrierService;
    private final PortService portService;
    private final BookingService bookingService;

    private final List<CustomItem> templateList;

    private User user;


    public NewBookingView(ClientService clientService, CarrierService carrierService, PortService portService,
                          AuthenticatedUser authenticatedUser, BookingService bookingService) {
        this.clientService = clientService;
        this.carrierService = carrierService;
        this.portService = portService;
        this.bookingService = bookingService;
        this.commodityList = CustomItemUtil.getItemsListFromFile(COMMODITIES);
        this.templateList = CustomItemUtil.getItemsListFromFile(TEMPLATES);

        if (authenticatedUser.get().isPresent()) {
            user = authenticatedUser.get().get();
        } else {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setText("User session expired! Please login again");
            dialog.setCancelable(false);
            dialog.setConfirmButton(new Button("Logout", event -> authenticatedUser.logout()));
        }

        setAttributes();
        setListeners();
        setTemplateLayout();
        FormLayout formLayout = getEntryFormLayout();

        HorizontalLayout layoutRow = new HorizontalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        H3 h3 = new H3("New Booking (Export)");
        h3.setWidth("max-content");

        getContent().setWidth("80%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");

        getContent().add(layoutRow);
        layoutRow.add(layoutColumn2);
        layoutColumn2.add(h3, templateLayout, formLayout, new HorizontalLayout(createBooking, reset));
    }

    private void setAttributes() {
        bookingNo.setRequired(true);
        commodity.setRequired(true);

        carrier.setRequired(true);
        carrier.setItemLabelGenerator(Carrier::getName);

        loadingPort.setRequired(true);
        destinationPort.setRequired(true);

        numberOfContainers.setRequired(true);
        numberOfContainers.setMin(1);
        numberOfContainers.setMax(10000);

        numOfShipments.setRequired(true);
        numOfShipments.setMin(0);
        numOfShipments.setMax(100);

        containerSize.setRequired(true);
        containerSize.setItems(ContainerSize.values());
        containerSize.setItemLabelGenerator(ContainerSize::getContainerSize);

        containerType.setRequired(true);
        containerType.setItems(ContainerType.values());
        containerType.setItemLabelGenerator(ContainerType::getType);

        createBooking.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBooking.setIcon(LineAwesomeIcon.PLUS_SOLID.create());

        commodity.setItems(commodityList.stream().map(CustomItem::getName).toList());
        commodity.setAllowCustomValue(true);

        templateLayout.setMaxWidth("80%");
        templateLayout.setWhiteSpace(HasText.WhiteSpace.NOWRAP);
        templateLayout.getStyle().setOverflow(Style.Overflow.AUTO);
    }

    private void setTemplateLayout() {
        templateLayout.removeAll();
        if (templateList.isEmpty()) {
            templateLayout.setVisible(false);
            return;
        }
        templateLayout.setVisible(true);
        templateLayout.add(new Text("Saved Templates: "));
        for (CustomItem item : templateList) {
            TemplateBadge badge = new TemplateBadge(item.getName(),
                    () -> this.setTemplateValues(item.getBookingTemplate()), () -> deleteFromTemplate(item));
            templateLayout.add(badge);
        }
    }

    private FormLayout getEntryFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.add(bookingNo, numberOfContainers, containerType, containerSize, commodity, carrier, loadingPort,
                destinationPort, numOfShipments, getClientLayout(), remarks, new Hr());
        formLayout.setMaxWidth("75%");
        return formLayout;
    }

    private HorizontalLayout getClientLayout() {
        HorizontalLayout clientLayout = new HorizontalLayout();
        clientLayout.setAlignItems(FlexComponent.Alignment.END);
        Button addButton = new Button();
        addButton.setTooltipText("Add New Shipper");
        addButton.setIcon(LineAwesomeIcon.USER_PLUS_SOLID.create());
        addButton.setWidth("10%");
        addButton.addClickListener(event -> new ClientCreationDialog(clientService, clientList).open());

        clients.setWidth("90%");
        clients.setRequired(true);
        clients.setAllowCustomValue(true);
        clients.setItemLabelGenerator(Client::getName);
        clients.setItems(clientList);
        clients.addFocusListener(event -> loadClientPropertiesOnDemand());
        clientLayout.add(clients, addButton);
        return clientLayout;
    }

    private void setListeners() {
        carrier.addFocusListener(event -> loadCarrierPropertiesOnDemand());

        commodity.addCustomValueSetListener(event -> {
            String customValue = event.getDetail();
            if (customValue == null) {
                return;
            }
            commodityList.add(new CustomItem(customValue, event.getDetail().trim().toLowerCase().hashCode(), null));
            commodity.setItems(commodityList.stream().map(CustomItem::getName).toList());
            commodity.setValue(customValue);
        });

        loadingPort.addFocusListener(event -> loadPortComponentPropertiesOnDemand(loadingPort));

        destinationPort.addFocusListener(event -> loadPortComponentPropertiesOnDemand(destinationPort));

        createBooking.addClickListener(event -> {
            if (!isAllFieldsValid()) {
                NotificationUtil.getNotification("Please provide correct data in the marked fields!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 4000).open();
                return;
            }
            CustomItems newCommodity = new CustomItems(commodityList);
            CustomItemUtil.saveCustomItems(newCommodity, COMMODITIES);
            try {
                Booking booking = createNewBooking();
                showBookingConfirmationDialog(booking);
            } catch (Exception e) {
                NotificationUtil.getNotification("Unexpected error! " + e.getMessage(), e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 5000).open();
            }
        });

        reset.addClickListener(event -> {
            bookingNo.clear();
            numberOfContainers.clear();
            containerType.clear();
            containerSize.clear();
            commodity.clear();
            carrier.clear();
            clients.clear();
        });
    }

    private boolean isAllFieldsValid() {
        boolean isValid = true;
        if (StringUtils.isAllBlank(bookingNo.getValue())) {
            bookingNo.setInvalid(true);
            bookingNo.setErrorMessage("Booking No Cannot be Empty!");
            bookingNo.focus();
            isValid = false;
        }
        if (bookingService.bookingExists(bookingNo.getValue())) {
            bookingNo.setInvalid(true);
            bookingNo.setErrorMessage("Booking with the same booking no already exists!");
            bookingNo.focus();
            isValid = false;
        }
        if (numOfShipments.getValue() == null || numOfShipments.getValue() < 1 || numOfShipments.getValue() > 100) {
            numOfShipments.setInvalid(true);
            numOfShipments.setErrorMessage("Value must be between 1 and 100");
            numOfShipments.focus();
            isValid = false;
        }
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
        if (StringUtils.isBlank(commodity.getValue())) {
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

        if (isValid) {
            bookingNo.setInvalid(false);
            numberOfContainers.setInvalid(false);
            containerType.setInvalid(false);
            containerSize.setInvalid(false);
            commodity.setInvalid(false);
            carrier.setInvalid(false);
            clients.setInvalid(false);
            numOfShipments.setInvalid(false);
        }

        return isValid;
    }

    private Booking createNewBooking() {
        Booking booking = new Booking();
        booking.setBookingNo(bookingNo.getValue());
        booking.setContainerType(containerType.getValue());
        booking.setContainerSize(containerSize.getValue());
        booking.setNumOfContainers(numberOfContainers.getValue());
        booking.setRemarks(remarks.getValue());
        booking.setCreatedBy(user);
        booking.setCreatedOn(LocalDateTime.now());
        booking.setLoadingPort(loadingPort.getValue());
        booking.setDestinationPort(destinationPort.getValue());
        booking.setCommodity(commodity.getValue());
        booking.setShipper(clients.getValue());
        booking.setNumOfShipments(numOfShipments.getValue());
        booking.setCarrier(carrier.getValue());
        return bookingService.createBooking(booking);
    }

    private void showBookingConfirmationDialog(Booking booking) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Booking Created!");
        FormLayout bookingConfirmationLayout = new FormLayout();

        bookingConfirmationLayout.addFormItem(new Text(booking.getBookingNo()), "Booking No:");
        bookingConfirmationLayout.addFormItem(new Text(booking.getNumOfContainers() + " X " +
                booking.getContainerSize().getContainerSize() + " " + booking.getContainerType().getType()),
                "Containers:");
        bookingConfirmationLayout.addFormItem(new Text(booking.getShipments().get(0).getCommodity()),  "Commodity:");
        bookingConfirmationLayout.addFormItem(new Text(booking.getCarrier().getName()),  "Carrier:");
        bookingConfirmationLayout.addFormItem(new Text(booking.getShipper().getName()),  "Shipper:");

        Button okButton = new Button("OK");
        okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        okButton.addClickListener(event -> dialog.close());

        Button saveAsTemplate = new Button("Save as Template");
        saveAsTemplate.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        saveAsTemplate.addClickListener(event -> saveToTemplate(booking));

        dialog.add(bookingConfirmationLayout);
        dialog.setWidth(400, Unit.PIXELS);
        dialog.getFooter().add(okButton, saveAsTemplate);
        dialog.open();
    }

    public void setTemplateValues(BookingTemplate template) {
        if (template.getNumOfContainer() != null && template.getNumOfContainer() > 0) {
            numberOfContainers.setValue(template.getNumOfContainer());
        }
        if (StringUtils.isNotBlank(template.getContainerType())) {
            containerType.setValue(ContainerType.valueOf(template.getContainerType()));
        }
        if (StringUtils.isNotBlank(template.getContainerSize())) {
            containerSize.setValue(ContainerSize.valueOf(template.getContainerSize()));
        }
        if (StringUtils.isNotBlank(template.getCommodity())) {
            commodity.setValue(template.getCommodity());
        }
        if (template.getCarrier() != null && template.getCarrier() != 0) {
            loadCarrierPropertiesOnDemand();
            carrier.setValue(carrierList.stream()
                    .filter(line -> line.getName().toLowerCase().hashCode() == template.getCarrier())
                    .findFirst().orElse(null));
        }
        if (template.getPortOfLoading() != null && template.getPortOfLoading() > 0) {
            loadPortComponentPropertiesOnDemand(loadingPort);
            loadingPort.setValue(portList.stream()
                    .filter(port -> port.getPortShortCode().toLowerCase().hashCode() == template.getPortOfLoading())
                    .findFirst().orElse(null));
        }
        if (template.getPortOfDestination() != null && template.getPortOfDestination() > 0) {
            loadPortComponentPropertiesOnDemand(destinationPort);
            destinationPort.setValue(portList.stream()
                    .filter(port -> port.getPortShortCode().toLowerCase().hashCode() == template.getPortOfDestination())
                    .findFirst().orElse(null));
        }
        if (template.getShipper() != null && template.getShipper() != 0) {
            loadClientPropertiesOnDemand();
            clients.setValue(clientList.stream()
                    .filter(shipper -> shipper.getName().toLowerCase().hashCode() == template.getShipper())
                    .findFirst().orElse(null));
        }
    }

    private void saveToTemplate(Booking booking) {
        BookingTemplate bookingTemplate = new BookingTemplate();
        bookingTemplate.setNumOfContainer(booking.getNumOfContainers());
        bookingTemplate.setContainerType(booking.getContainerType().toString());
        bookingTemplate.setContainerSize(booking.getContainerSize().toString());
        bookingTemplate.setCommodity(booking.getCommodity());
        bookingTemplate.setCarrier(booking.getCarrier().getName().toLowerCase().hashCode());
        bookingTemplate.setPortOfLoading(booking.getLoadingPort().getPortShortCode().toLowerCase().hashCode());
        bookingTemplate.setPortOfDestination(booking.getDestinationPort().getPortShortCode().toLowerCase().hashCode());
        bookingTemplate.setShipper(booking.getShipper().getName().toLowerCase().hashCode());
        String name = booking.getContainerSize().getContainerSize() + "-" + StringUtils.truncate(
                booking.getCommodity() + "-" + booking.getDestinationPort().getPortShortCode(), 25);
        templateList.add(new CustomItem(name, name.hashCode(), bookingTemplate));
        CustomItems newTemplate = new CustomItems(templateList);
        CustomItemUtil.saveCustomItems(newTemplate, TEMPLATES);
        NotificationUtil.getNotification("Template Saved!", "", false, NotificationVariant.LUMO_PRIMARY, 2000).open();
        setTemplateLayout();

    }

    private void deleteFromTemplate(CustomItem item) {
        templateList.remove(item);
        CustomItems newTemplate = new CustomItems(templateList);
        CustomItemUtil.saveCustomItems(newTemplate, TEMPLATES);
        setTemplateLayout();
    }

    private void loadPortComponentPropertiesOnDemand(ComboBox<Port> portComboBox) {
        if (portList.isEmpty()) {
            portList.addAll(portService.getAllPorts());
        }
        portComboBox.setItems(portList);
        portComboBox.setItemLabelGenerator(Port::getPortLabel);
    }

    private void loadCarrierPropertiesOnDemand() {
        if (carrierList.isEmpty()) {
            carrierList.addAll(carrierService.getAllCarriers());
        }
        carrier.setItems(carrierList);
    }

    private void loadClientPropertiesOnDemand() {
        if (clientList.isEmpty()) {
            clientList.addAll(clientService.getClientsByType(List.of(ClientType.SHIPPER, ClientType.ALL)));
        }
        clients.setItems(clientList);
    }
}
