package com.lazoft.forwarderplus.views.exportviews.shippingOrder;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.ClientType;
import com.lazoft.forwarderplus.enums.PackageUnit;
import com.lazoft.forwarderplus.services.ClientService;
import com.lazoft.forwarderplus.services.ScheduleService;
import com.lazoft.forwarderplus.services.ShipmentService;
import com.lazoft.forwarderplus.services.StuffingDetailsService;
import com.lazoft.forwarderplus.util.DateUtil;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.lazoft.forwarderplus.views.commonViews.ClientCreationDialogView;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShippingOrderDialog extends Dialog {
    private final DatePicker documentDate = new DatePicker("Report Date");
    private final TextField bookingNo = new TextField("Booking No");
    private final TextField cnfAgentName = new TextField("CnF Agent Name");
    private final TextField cnfAgentContact = new TextField("CnF Agent Contact");
    private final TextField shipper = new TextField("Shipper");
    private final TextField portOfLoading = new TextField("Port Of Loading");
    private final TextField portOfDischarge = new TextField("Port Of Discharge");
    private final TextField vessel = new TextField("Vessel");
    private final ComboBox<Client> notifyParty = new ComboBox<>("Notify Party");
    private final IntegerField quantity = new IntegerField("Quantity");
    private final ComboBox<PackageUnit> units = new ComboBox<>("Units");
    private final IntegerField numOfContainers = new IntegerField("Number of Containers");
    private StuffingDetails stuffingDetails;

    private final Booking booking;
    private final User user;
    private final List<Client> clientList;
    private final ClientService clientService;
    private final ShippingOrderView shippingOrderView;

    public ShippingOrderDialog(Shipment shipment, User user, ClientService clientService,
                               StuffingDetailsService stuffingDetailsService, ScheduleService scheduleService,
                               ShipmentService shipmentService, ShippingOrderView shippingOrderView) {
        this.clientService = clientService;
        this.shippingOrderView = shippingOrderView;
        this.user = user;
        this.setWidth(800, Unit.PIXELS);

        clientList = clientService.getClientsByType(List.of(ClientType.NOTIFY_PARTY, ClientType.ALL));
        notifyParty.setItems(clientService.getAllClients());
        notifyParty.setItemLabelGenerator(Client::getName);
        bookingNo.setReadOnly(true);
        portOfLoading.setReadOnly(true);
        portOfDischarge.setReadOnly(true);
        stuffingDetails = shipment.getStuffingDetails();
        booking = shipment.getBooking();

        FormLayout formLayout = new FormLayout();
        formLayout.add(documentDate, new Hr(), bookingNo, vessel, portOfLoading, portOfDischarge, shipper, getClientLayout(),
                cnfAgentName, cnfAgentContact, quantity, units);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        setExistingValues(shipment);

        Button saveButton = new Button("Save");
        saveButton.setIcon(LineAwesomeIcon.SAVE_SOLID.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            try {
                saveShipment(shipment, stuffingDetailsService, shipmentService);
            } catch (Exception e) {

            }
        });

        this.add(new H3("Shipping Order"), new Hr(), formLayout);

        Anchor printButtonAnchor = getReportDownloadButtonAnchor(shipment, booking);
        Button closeButton = new Button("Close", event -> {
            shippingOrderView.refreshGrid();
            this.close();
        });
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        this.getFooter().add(saveButton, printButtonAnchor, closeButton);
    }

    private void saveShipment(Shipment shipment, StuffingDetailsService stuffingDetailsService,
                              ShipmentService shipmentService) {
        if (stuffingDetails == null) {
            stuffingDetails = new StuffingDetails();
            stuffingDetails.setQuantity(quantity.getValue());
        }

        stuffingDetails.setStuffingId(shipment.getShipmentId());
        stuffingDetails.setPackageUnit(units.getValue());
        stuffingDetails.setCnfAgentName(cnfAgentName.getValue());
        stuffingDetails.setCnfAgentContactNo(cnfAgentContact.getValue());
        stuffingDetails.setQuantity(quantity.getValue());
        StuffingDetails savedStuffingDetails = stuffingDetailsService.saveStuffingDetails(stuffingDetails);

        shipment.setNumOfContainers(numOfContainers.getValue());
        shipment.setStuffingDetails(savedStuffingDetails);
        shipment.setNotifyParty(notifyParty.getValue());
        shipmentService.saveShipment(shipment);

        NotificationUtil.getNotification("Saved Successfully!", "", false,
                NotificationVariant.LUMO_PRIMARY, 4000).open();
    }

    private HorizontalLayout getClientLayout() {
        HorizontalLayout clientLayout = new HorizontalLayout();
        clientLayout.setAlignItems(VerticalLayout.Alignment.END);
        Button addButton = new Button();
        addButton.setTooltipText("Add New Notify Party");
        addButton.setIcon(LineAwesomeIcon.USER_PLUS_SOLID.create());
        addButton.setWidth("10%");
        addButton.addClickListener(event -> new ClientCreationDialogView(clientService, clientList).open());

        notifyParty.setWidth("90%");
        notifyParty.setRequired(true);
        notifyParty.setAllowCustomValue(true);
        notifyParty.setItemLabelGenerator(Client::getName);
        notifyParty.setItems(clientList);
        notifyParty.addFocusListener(event -> {
            if (clientList.isEmpty()) {
                clientList.addAll(clientService.getAllClients());
            }
            notifyParty.setItems(clientList);
        });
        clientLayout.add(notifyParty, addButton);
        return clientLayout;
    }

    protected void setExistingValues(Shipment shipment) {
        documentDate.setValue(LocalDate.now());
        units.setItems(PackageUnit.values());
        bookingNo.setValue(booking.getBookingNo());
        bookingNo.setReadOnly(true);
        portOfLoading.setValue(booking.getLoadingPort().getPortLabel());
        portOfDischarge.setValue(booking.getDestinationPort().getPortLabel());
        shipper.setValue(shipment.getShipper().getName());
        shipper.setReadOnly(true);
        numOfContainers.setValue(shipment.getNumOfContainers());
        Client notifyParty = shipment.getNotifyParty();
        if (notifyParty != null) {
            this.notifyParty.setValue(notifyParty);
        }

        if (stuffingDetails != null) {
            cnfAgentName.setValue(stuffingDetails.getCnfAgentName());
            cnfAgentContact.setValue(stuffingDetails.getCnfAgentContactNo());
            units.setValue(stuffingDetails.getPackageUnit());
            quantity.setValue(stuffingDetails.getQuantity());
        }
    }

    private Anchor getReportDownloadButtonAnchor(Shipment shipment, Booking booking) {
        Anchor anchor = new Anchor(new StreamResource("Shipping_order_" + booking.getBookingNo() + ".pdf",
                (InputStreamFactory) () -> {
                    Map<String, Object> parameters;
                    String report = "shipping_order.jasper";
                    parameters = prepareParamsForShippingOrder(shipment, user);

                    try (InputStream stream = getClass().getResourceAsStream("/Reports/" + report)) {
                        return new ByteArrayInputStream(JasperRunManager.runReportToPdf(stream, parameters,
                                new JREmptyDataSource(1)));
                    } catch (JRException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }), "");

        Button printButton = new Button("Download As PDF");
        printButton.setIcon(LineAwesomeIcon.PRINT_SOLID.create());
        printButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        anchor.getElement().setAttribute("download", true);
        anchor.add(printButton);
        return anchor;
    }

    private Map<String, Object> prepareParamsForShippingOrder(Shipment shipment, User user) {
        Map<String, Object> paramMap = new HashMap<>();
        StuffingDetails stuffingDetails = shipment.getStuffingDetails();
        Booking booking = shipment.getBooking();
        Schedule schedule = shipment.getSchedule();

        paramMap.put("LOGO_URL", "Images/logo_best.png");

        paramMap.put("DATE", DateUtil.getCurrentDateAsString());
        paramMap.put("BOOKING_NO", booking.getBookingNo());

        paramMap.put("CNF_AGENT", stuffingDetails.getCnfAgentName());
        paramMap.put("CONTACT", stuffingDetails.getCnfAgentContactNo());
        paramMap.put("SHIPPER_NAME", shipment.getShipper().getName());
        paramMap.put("NOTIFY_PARTY", shipment.getNotifyParty().getName());

        paramMap.put("CONTAINERS", shipment.getNumOfContainers() + " X " + booking.getContainerSize());
        paramMap.put("GOODS_DESC", booking.getCommodity());
        paramMap.put("QUANTITY", stuffingDetails.getQuantity() + " "
                + stuffingDetails.getPackageUnit().toString());

        paramMap.put("PORT_OF_LOADING", schedule.getPortOfLoading().getPortCityAndCountry());
        paramMap.put("VESSEL", schedule.getFeederVesselName());
        paramMap.put("PORT_OF_DELIVERY", schedule.getPortOfDestination().getPortCityAndCountry());

        paramMap.put("SHIPPING_LINE", shipment.getCarrier().getName());

        paramMap.put("SIGNED_BY", user.getName());
        paramMap.put("SIGNED_BY_EMAIL", user.getEmail());
        paramMap.put("SIGNED_BY_CONTACT", user.getContactNo());

        return paramMap;
    }
}
