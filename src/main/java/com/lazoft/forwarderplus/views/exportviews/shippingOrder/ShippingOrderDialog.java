package com.lazoft.forwarderplus.views.exportviews.shippingOrder;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.ClientType;
import com.lazoft.forwarderplus.enums.PackageUnit;
import com.lazoft.forwarderplus.enums.ShipmentStatus;
import com.lazoft.forwarderplus.services.ClientService;
import com.lazoft.forwarderplus.services.ShipmentService;
import com.lazoft.forwarderplus.util.DateUtil;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.lazoft.forwarderplus.views.commonViews.ClientCreationDialogView;
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
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ShippingOrderDialog extends Dialog {
    private final DatePicker documentDate = new DatePicker("Order Date");
    private final TextField bookingNo = new TextField("Booking No");
    private final TextField cnfAgentName = new TextField("CnF Agent Name");
    private final TextField cnfAgentContact = new TextField("CnF Agent Contact");
    private final TextField shipper = new TextField("Shipper");
    private final TextField portOfLoading = new TextField("Port Of Loading");
    private final TextField portOfDischarge = new TextField("Port Of Discharge");
    private final TextField vessel = new TextField("Departure Vessel");
    private final ComboBox<Client> notifyParty = new ComboBox<>("Notify Party");
    private final IntegerField quantity = new IntegerField("Quantity");
    private final ComboBox<PackageUnit> units = new ComboBox<>("Units");

    private final Button addClientButton = new Button(LineAwesomeIcon.USER_PLUS_SOLID.create());
    private final Button saveButton = new Button("Save");
    private final Button closeButton = new Button("Close");
    private final Button printButton = new Button("Download As PDF");

    private StuffingDetails stuffingDetails;
    private final Booking booking;
    private final User user;
    private final List<Client> clientList;
    private final ClientService clientService;
    private final Shipment shipment;
    private final ShippingOrderView shippingOrderView;
    private final ShipmentService shipmentService;

    public ShippingOrderDialog(Shipment shipment, User user, ClientService clientService,
                               ShipmentService shipmentService, ShippingOrderView shippingOrderView) {
        this.clientService = clientService;
        this.user = user;
        this.clientList = clientService.getClientsByType(List.of(ClientType.NOTIFY_PARTY, ClientType.ALL));
        this.stuffingDetails = shipment.getStuffingDetails();
        this.booking = shipment.getBooking();
        this.shipment = shipment;
        this.shippingOrderView = shippingOrderView;
        this.shipmentService = shipmentService;

        setWidth("40%");
        setHeight("70%");

        setUpMainLayout();
        setFieldAttributes();
        setExistingValues(shipment);
        addListeners();

        Anchor printButtonAnchor = getReportDownloadButtonAnchor(shipment, booking);
        this.getFooter().add(saveButton, printButtonAnchor, closeButton);
    }


    private void setUpMainLayout() {
        FormLayout formLayout = new FormLayout();
        HorizontalLayout notifyPartyLayout = new HorizontalLayout(notifyParty, addClientButton);
        notifyPartyLayout.setAlignItems(VerticalLayout.Alignment.END);

        formLayout.add(documentDate, new Hr(),
                bookingNo, vessel,
                portOfLoading, portOfDischarge,
                shipper, notifyPartyLayout,
                cnfAgentName, cnfAgentContact,
                quantity, units);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        add(new H3("Shipping Order"), new Hr(), formLayout);
    }

    private void setFieldAttributes() {
        bookingNo.setReadOnly(true);
        portOfLoading.setReadOnly(true);
        portOfDischarge.setReadOnly(true);

        saveButton.setIcon(LineAwesomeIcon.SAVE_SOLID.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        printButton.setIcon(LineAwesomeIcon.PRINT_SOLID.create());
        printButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        notifyParty.setWidth("90%");
        notifyParty.setRequired(true);
        notifyParty.setItems(clientService.getAllClients());
        notifyParty.setItemLabelGenerator(Client::getName);
    }

    private void setExistingValues(Shipment shipment) {
        documentDate.setValue(LocalDate.now());
        units.setItems(PackageUnit.values());
        bookingNo.setValue(booking.getBookingNo());
        bookingNo.setReadOnly(true);
        portOfLoading.setValue(booking.getLoadingPort().getPortLabel());
        portOfDischarge.setValue(booking.getDestinationPort().getPortLabel());
        shipper.setValue(shipment.getShipper().getName());
        shipper.setReadOnly(true);
        notifyParty.setValue(shipment.getNotifyParty());
//        Client notifyParty = shipment.getNotifyParty();
//        if (notifyParty != null) {
//            this.notifyParty.setValue(notifyParty);
//        }

        if (stuffingDetails == null) {
            return;
        }
        cnfAgentName.setValue(StringUtils.defaultString(stuffingDetails.getCnfAgentName()));
        cnfAgentContact.setValue(StringUtils.defaultString(stuffingDetails.getCnfAgentContactNo()));
        units.setValue(stuffingDetails.getPackageUnit());
        quantity.setValue(stuffingDetails.getQuantity());
        vessel.setValue(StringUtils.defaultString(stuffingDetails.getVessel()));

    }

    private void addListeners() {
        saveButton.addClickListener(event -> {
            if (isInvalidDataForSaveAndReport()) {
                NotificationUtil.getNotification("Please provide correct values", "", false,
                        NotificationVariant.LUMO_WARNING, 3000).open();
                return;
            }
            prepareDataForSaving();
            try {
                shipmentService.addStuffingDetailsToShipment(shipment, stuffingDetails);
                NotificationUtil.getNotification("Saved Successfully!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 4000).open();
            } catch (Exception e) {
                log.error("Error in save shipment in shipping order page", e);
                NotificationUtil.getNotification("Failed to save Data Due To Unexpected Error",
                        e.getMessage(), true, NotificationVariant.LUMO_ERROR, 6000).open();
            }
        });

        addClientButton.addClickListener(event -> new ClientCreationDialogView(clientService, clientList).open());

        notifyParty.addFocusListener(event -> notifyParty.setItems(clientList));

        closeButton.addClickListener(event -> {
            shippingOrderView.refreshGrid();
            this.close();
        });
    }

    private boolean isInvalidDataForSaveAndReport() {
        boolean isInvalid = false;
        if (StringUtils.isBlank(vessel.getValue())) {
            vessel.setInvalid(true);
            vessel.setErrorMessage("Must provide vessel name");
            isInvalid = true;
        }
        if (units.getValue() == null) {
            units.setInvalid(true);
            units.setErrorMessage("Must provide a value");
            isInvalid = true;
        }
        if (quantity.getValue() == null || quantity.getValue() <= 0) {
            quantity.setInvalid(true);
            quantity.setErrorMessage("Must be greater than 0");
            isInvalid = true;
        }
        if (notifyParty.getValue() == null) {
            notifyParty.setInvalid(true);
            notifyParty.setErrorMessage("Must provide a notify party");
            isInvalid = true;
        }
        return isInvalid;
    }

    private void prepareDataForSaving() {
        if (stuffingDetails == null) {
            stuffingDetails = new StuffingDetails();
        }
        stuffingDetails.setStuffingId(shipment.getShipmentId());
        stuffingDetails.setPackageUnit(units.getValue());
        stuffingDetails.setCnfAgentName(cnfAgentName.getValue());
        stuffingDetails.setCnfAgentContactNo(cnfAgentContact.getValue());
        stuffingDetails.setQuantity(quantity.getValue());
        stuffingDetails.setVessel(vessel.getValue());
        shipment.setNotifyParty(notifyParty.getValue());
    }

    private Anchor getReportDownloadButtonAnchor(Shipment shipment, Booking booking) {
        Anchor anchor = new Anchor(new StreamResource("Shipping_order_" + booking.getBookingNo() + ".pdf",
                (InputStreamFactory) () -> {
                    Map<String, Object> parameters = prepareParamsForShippingOrder(shipment, user);
                    String report = "shipping_order.jasper";

                    ByteArrayInputStream inputStream = null;
                    try (InputStream stream = getClass().getResourceAsStream("/Reports/" + report)) {
                        inputStream = new ByteArrayInputStream(JasperRunManager
                                .runReportToPdf(stream, parameters, new JREmptyDataSource(1)));
                        shipment.setStatus(ShipmentStatus.SHIPPING_ORDER_CREATED);
                        shipmentService.saveShipment(shipment);
                        return inputStream;
                    } catch (JRException | IOException e) {
                        log.error("Error in shipping order report creation", e);
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        log.error("Error in shipping order anchor", e);
                        if (inputStream != null) {
                            return inputStream;
                        } else {
                            throw new RuntimeException(e);
                        }
                    }
                }), "");

        anchor.getElement().setAttribute("download", true);
        anchor.add(printButton);
        return anchor;
    }

    private Map<String, Object> prepareParamsForShippingOrder(Shipment shipment, User user) {
        Map<String, Object> paramMap = new HashMap<>();
        Booking booking = shipment.getBooking();

        paramMap.put("LOGO_URL", "Images/logo_best.png");

        paramMap.put("DATE", DateUtil.getDateAsString(documentDate.getValue()));
        paramMap.put("BOOKING_NO", booking.getBookingNo());

        paramMap.put("CNF_AGENT", StringUtils.defaultString(cnfAgentName.getValue()));
        paramMap.put("CONTACT", StringUtils.defaultString(cnfAgentContact.getValue()));
        paramMap.put("SHIPPER_NAME", shipment.getShipper().getName());
        paramMap.put("NOTIFY_PARTY", shipment.getNotifyParty().getName());

        paramMap.put("CONTAINERS", shipment.getNumOfContainers() + " X " + booking.getContainerSize().getContainerSize());
        paramMap.put("GOODS_DESC", booking.getCommodity());
        paramMap.put("QUANTITY", quantity.getValue() + " " + units.getValue().toString());

        paramMap.put("PORT_OF_LOADING", booking.getLoadingPort().getPortCityAndCountry());
        paramMap.put("VESSEL", StringUtils.defaultString(vessel.getValue()));
        paramMap.put("PORT_OF_DELIVERY", booking.getDestinationPort().getPortCityAndCountry());

        paramMap.put("SHIPPING_LINE", shipment.getCarrier().getName());

        paramMap.put("SIGNED_BY", user.getName());
        paramMap.put("SIGNED_BY_EMAIL", user.getEmail());
        paramMap.put("SIGNED_BY_CONTACT", user.getContactNo());

        return paramMap;
    }
}
