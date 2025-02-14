package com.lazoft.forwarderplus.views.exportviews.shipmentAdvice;

import com.lazoft.forwarderplus.dto.TSReportDto;
import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.ClientType;
import com.lazoft.forwarderplus.enums.ContainerSize;
import com.lazoft.forwarderplus.enums.ContainerType;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.*;
import com.lazoft.forwarderplus.util.DateUtil;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.lazoft.forwarderplus.views.commonViews.ClientCreationDialogView;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ShipmentAdviceDialog extends Dialog {

    private final ShipmentService shipmentService;
    private final ScheduleService scheduleService;
    private final CarrierService carrierService;
    private final ClientService clientService;
    private final PortService portService;
    private final UserService userService;
    private final Shipment shipment;

    private final TextField mblNo = new TextField("Master B/L No:");
    private final TextField hblNo = new TextField("House B/L No:");
    private final TextArea goodsDescription = new TextArea("Goods Description");
    private final TextArea shipperMarks = new TextArea("Shipper Marks");
    private final TextField bookingNo = new TextField("Booking No");
    private final TextField clientInvoiceNo = new TextField("Shipper ShipmentInvoice No");
    private final ComboBox<ContainerType> containerType = new ComboBox<>("Container Type");
    private final ComboBox<ContainerSize> containerSize = new ComboBox<>("Container Size");
    private final IntegerField numOfContainers = new IntegerField("Number of Containers");
    private final ComboBox<Client> shipper = new ComboBox<>("Shipper");
    private final ComboBox<Client> consignee = new ComboBox<>("Consignee");
    private final ComboBox<Client> notifyParty = new ComboBox<>("Notify Party");
    private final TextField commodities = new TextField("Commodities");
    private final ComboBox<Carrier> carrierComboBox = new ComboBox<>("Carrier");
    private final Button generateHbl = new Button(LineAwesomeIcon.ATOM_SOLID.create());

    private final TextField schedule = new TextField("Schedule");
    private final DatePicker departureDate = new DatePicker("ETD Origin:");
    private final DatePicker arrivalDate = new DatePicker("ETA Destination:");
    private final TextField approxTime = new TextField("Transit Time");
    private final Button editSchedule = new Button(LineAwesomeIcon.PEN_SOLID.create());

    private final Checkbox useHbl = new Checkbox("Use HB/L instead MB/L?");
    private final Checkbox useConsignee = new Checkbox("Use Consignee instead of Notify ?");
    private final DatePicker adviceDate = new DatePicker("Advice Date");
    private final Checkbox showRespondentEmail = new Checkbox("Show email?");
    private final Checkbox hideRespondentPhone = new Checkbox("Hide contact no?");
    private final Checkbox showDesignation = new Checkbox("Show designation?");
    private final ComboBox<User> respondent = new ComboBox<>("Contact Details");

    private final IntegerField totalQuantity = new IntegerField("Total Quantity");
    private final TextField unit = new TextField("Unit");
    private final BigDecimalField totalGrossWeight = new BigDecimalField("Total Weight (KGs)");
    private final Button editCargo = new Button(LineAwesomeIcon.PEN_SOLID.create());

    private final Button saveButton = new Button("Save");
    private final Button downloadButton = new Button("Download as PDF");
    private final Button closeButton = new Button("Close");
    private final Button addClientButton = new Button(LineAwesomeIcon.USER_PLUS_SOLID.create());

    private boolean isSaved = false;
    private final AuthenticatedUser user;
    private final List<Client> clientList;


    public ShipmentAdviceDialog(ShipmentService shipmentService, ScheduleService scheduleService,
                                CarrierService carrierService, ClientService clientService, PortService portService,
                                UserService userService, Shipment shipment, AuthenticatedUser user) {

        this.user = user;
        this.shipment = shipment;
        this.userService = userService;
        this.portService = portService;
        this.clientService = clientService;
        this.carrierService = carrierService;
        this.scheduleService = scheduleService;
        this.shipmentService = shipmentService;
        this.clientList = clientService.getClientsByType(List.of(ClientType.ALL));

        this.setWidth("85%");
        this.setHeight("85%");
        this.setHeaderTitle("Edit Shipment Details");

        setUpFormLayout();
        setFieldAttributes();
        fillUpExistingValues();
        setListeners();

        getFooter().add(closeButton, downloadButton, saveButton);
    }

    private void setFieldAttributes() {
        bookingNo.setReadOnly(true);
        generateHbl.setTooltipText("Generate A Number");
        generateHbl.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        hblNo.setWidth("80%");

        containerType.setItems(ContainerType.values());
        containerType.setItemLabelGenerator(ContainerType::getContainerType);
        containerSize.setItems(ContainerSize.values());
        containerSize.setItemLabelGenerator(ContainerSize::getContainerSize);

        carrierComboBox.setItems(carrierService.getAllCarriers());
        carrierComboBox.setItemLabelGenerator(Carrier::getName);

        adviceDate.setValue(LocalDate.now());

        shipper.setItemLabelGenerator(Client::getName);
        shipper.setItems(clientList.stream().filter(client -> client.getType() == ClientType.SHIPPER
                || client.getType() == ClientType.ALL).collect(Collectors.toList()));

        consignee.setItemLabelGenerator(Client::getName);
        consignee.setItems(clientList.stream().filter(client -> client.getType() == ClientType.CONSIGNEE
                || client.getType() == ClientType.ALL).collect(Collectors.toList()));

        notifyParty.setItemLabelGenerator(Client::getName);
        notifyParty.setItems(clientList.stream().filter(client -> client.getType() == ClientType.NOTIFY_PARTY
                || client.getType() == ClientType.ALL).collect(Collectors.toList()));
        notifyParty.setWidth("80%");

        respondent.setItems(userService.getAll());
        respondent.setValue(user.get().orElse(null));
        respondent.setItemLabelGenerator(User::getName);

        schedule.setReadOnly(true);
        approxTime.setReadOnly(true);
        departureDate.setReadOnly(true);
        arrivalDate.setReadOnly(true);

        totalQuantity.setReadOnly(true);
        unit.setReadOnly(true);
        totalGrossWeight.setReadOnly(true);

        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        downloadButton.setIcon(LineAwesomeIcon.PRINT_SOLID.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
    }

    public void fillUpExistingValues() {
        bookingNo.setValue(shipment.getBooking().getBookingNo());
        clientInvoiceNo.setValue(StringUtils.defaultIfBlank(shipment.getClientInvoiceNo(), ""));
        mblNo.setValue(StringUtils.defaultIfBlank(shipment.getMblNo(), ""));
        hblNo.setValue(StringUtils.defaultIfBlank(shipment.getHblNo(), ""));
        containerType.setValue(shipment.getBooking().getContainerType());
        containerSize.setValue(shipment.getBooking().getContainerSize());
        numOfContainers.setValue(shipment.getNumOfContainers());
        carrierComboBox.setValue(shipment.getCarrier());
        commodities.setValue(shipment.getCommodity());
        shipper.setValue(shipment.getShipper());
        consignee.setValue(shipment.getConsignee());
        notifyParty.setValue(shipment.getNotifyParty());
        goodsDescription.setValue(StringUtils.defaultIfBlank(shipment.getGoodsDescription(), ""));
        shipperMarks.setValue(StringUtils.defaultIfBlank(shipment.getShipperMarks(), ""));

        fillUpScheduleValues();
        fillUpCargoValues();
    }

    private void fillUpScheduleValues() {
        Schedule scheduleData = shipment.getSchedule();
        if (scheduleData == null) {
            return;
        }
        schedule.setValue(scheduleData.getPortOfLoading().getPortCityAndCountry() + " To "
                + scheduleData.getPortOfDestination().getPortCityAndCountry());
        approxTime.setValue(ChronoUnit.DAYS.between(scheduleData.getPortOfLoadingETD(),
                scheduleData.getPortOfDestinationETA()) + " Days (Approx.)");
        departureDate.setValue(scheduleData.getPortOfLoadingETD());
        arrivalDate.setValue(scheduleData.getPortOfDestinationETA());
    }

    private void fillUpCargoValues() {
        if (shipment.getContainerDetails() == null || shipment.getContainerDetails().isEmpty()) {
            return;
        }
        List<ContainerDetails> containerDetailsList = shipment.getContainerDetails();
        BigDecimal totalWeightValue = BigDecimal.ZERO;
        int totalQuantityValue = 0;
        for (ContainerDetails container : containerDetailsList) {
            totalWeightValue = totalWeightValue.add(container.getGrossWeight());
            totalQuantityValue += container.getNoOfPackages();
        }

        totalQuantity.setValue(totalQuantityValue);
        unit.setValue(containerDetailsList.get(0).getPackageUnit().toString());
        totalGrossWeight.setValue(totalWeightValue);
    }

    public void setUpFormLayout() {
        Accordion shipmentPanel = new Accordion();
        shipmentPanel.add("Shipment Info", getShipmentInfoFormLayout());

        Accordion schedulePanel = new Accordion();
        schedulePanel.add("Schedule Info", getScheduleInfoFormLayout());

        Accordion containerDetailsPanel = new Accordion();
        containerDetailsPanel.add("Container Details", getContainerDetailsFormLayout());

        add(shipmentPanel, schedulePanel, containerDetailsPanel);
    }

    private FormLayout getContainerDetailsFormLayout() {
        FormLayout containerDetailsLayout = new FormLayout();
        editCargo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editCargo.setTooltipText("Edit Container Details");
        HorizontalLayout editContainerLayout = new HorizontalLayout(unit, editCargo);
        editContainerLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        editContainerLayout.setAlignItems(FlexComponent.Alignment.END);
        containerDetailsLayout.add(totalGrossWeight, totalQuantity, unit, editContainerLayout);
        containerDetailsLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 5));
        return containerDetailsLayout;
    }

    private FormLayout getScheduleInfoFormLayout() {
        FormLayout scheduleLayout = new FormLayout();
        editSchedule.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editSchedule.setTooltipText("Edit Schedule");
        HorizontalLayout editScheduleLayout = new HorizontalLayout(arrivalDate, editSchedule);
        editScheduleLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        editScheduleLayout.setAlignItems(FlexComponent.Alignment.END);
        scheduleLayout.add(schedule, approxTime, departureDate, editScheduleLayout);
        scheduleLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 5));
        scheduleLayout.setColspan(schedule, 2);
        return scheduleLayout;
    }

    private FormLayout getShipmentInfoFormLayout() {
        FormLayout shipmentInfoLayout = new FormLayout();

        HorizontalLayout hblComponent = new HorizontalLayout(hblNo, generateHbl);
        hblComponent.setVerticalComponentAlignment(FlexComponent.Alignment.END);

        HorizontalLayout notifyPartyLayout = new HorizontalLayout(notifyParty, addClientButton);
        notifyPartyLayout.setAlignItems(VerticalLayout.Alignment.END);

        hblComponent.setAlignItems(FlexComponent.Alignment.END);
        shipmentInfoLayout.add(bookingNo, clientInvoiceNo, mblNo, hblComponent,
                containerType, numOfContainers, containerSize, commodities,
                carrierComboBox, shipper, consignee, notifyPartyLayout,
                goodsDescription, shipperMarks);
        shipmentInfoLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 4));
        shipmentInfoLayout.setColspan(goodsDescription, 2);
        shipmentInfoLayout.setColspan(shipperMarks, 2);
        return shipmentInfoLayout;
    }

    private FormLayout getReportOptionsFormLayout() {
        FormLayout reportConfigLayout = new FormLayout();
        useConsignee.setEnabled(consignee.getValue() != null);
        useHbl.setEnabled(!StringUtils.isBlank(hblNo.getValue()));
        User user = this.user.get().get();
        showRespondentEmail.setEnabled(!StringUtils.isBlank(user.getEmail()));
        showDesignation.setEnabled(!StringUtils.isBlank(user.getDesignation()));
        reportConfigLayout.add(useHbl, useConsignee, showDesignation, showRespondentEmail, hideRespondentPhone, adviceDate, respondent);
        reportConfigLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 4));
        reportConfigLayout.setColspan(respondent, 2);
        return reportConfigLayout;
    }

    private void setListeners() {
        editCargo.addClickListener(event -> new EditContainerDetailsDialog(shipment, shipmentService, this).open());

        editSchedule.addClickListener(event -> new EditScheduleDialog(portService, shipmentService, scheduleService,shipment, this).open());

        generateHbl.addClickListener(event -> {});

        addClientButton.addClickListener(event -> new ClientCreationDialogView(clientService, clientList).open());

        saveButton.addClickListener(event -> {
            if (isInvalidEntriesForSave()) {
                return;
            }
            setValuesToShipmentForSaving();
            try {
                shipmentService.saveShipment(shipment);
                isSaved = true;
                NotificationUtil.getNotification("Saved Successfully!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 3000).open();
            } catch (Exception e) {
                log.error("Error is saving container details", e);
                NotificationUtil.getNotification("Unexpected Error! Could not save data.", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 5000).open();
            }
        });

        downloadButton.addClickListener(event -> {
            List<String> errors = findErrorsForReportData();
            Dialog dialog = new Dialog();
            dialog.getFooter().add(new Button("Close", e -> dialog.close()));
            if (!errors.isEmpty()) {
                dialog.setHeaderTitle("Errors in Data");
                ListBox<String> listBox = new ListBox<>();
                listBox.setItems(errors);
                dialog.add(new H4("Please fix the following before downloading Advice"), listBox);
                dialog.open();
                return;
            }
            dialog.setHeaderTitle("Advice is ready!");
            dialog.add(new Hr(), new H3("Report Options"), getReportOptionsFormLayout());

            Anchor downloadAdviceAnchor = getShipmentAdviceDownloadAnchor();
            dialog.getFooter().add(downloadAdviceAnchor);
            dialog.open();
        });

        closeButton.addClickListener(event -> {
            if (isSaved) {
                this.close();
                return;
            }
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Close Shipment Edit Window ?");
            confirmDialog.setText("Are you sure you want to close ? All unsaved changes will be lost.");
            confirmDialog.setCancelable(true);
            confirmDialog.setConfirmButton(new Button("Yes, I am Sure", confirmEvent -> this.close()));
            Button cancel = new Button("Close", confirmEvent -> confirmDialog.close());
            cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
            confirmDialog.setCancelButton(cancel);
            confirmDialog.open();
        });
    }

    private void setValuesToShipmentForSaving() {
        if (!StringUtils.isBlank(clientInvoiceNo.getValue())) {
            shipment.setClientInvoiceNo(clientInvoiceNo.getValue());
        }
        if (!StringUtils.isBlank(mblNo.getValue())) {
            shipment.setMblNo(mblNo.getValue());
        }
        if (!StringUtils.isBlank(hblNo.getValue())) {
            shipment.setHblNo(hblNo.getValue());
        }
        if (!StringUtils.isBlank(commodities.getValue())) {
            shipment.setCommodity(commodities.getValue());
        }
        if (carrierComboBox.getValue() != null) {
            shipment.setCarrier(carrierComboBox.getValue());
        }
        if (shipper.getValue() != null) {
            shipment.setShipper(shipper.getValue());
        }
        if (consignee.getValue() != null) {
            shipment.setConsignee(consignee.getValue());
        }
        if (notifyParty.getValue() != null) {
            shipment.setNotifyParty(notifyParty.getValue());
        }
        if (!StringUtils.isBlank(goodsDescription.getValue())) {
            shipment.setGoodsDescription(goodsDescription.getValue());
        }
        if (!StringUtils.isBlank(shipperMarks.getValue())) {
            shipment.setShipperMarks(shipperMarks.getValue());
        }
    }

    private boolean isInvalidEntriesForSave() {
        boolean isInvalid = false;
        if (containerType.getValue() == null) {
            containerType.setInvalid(true);
            containerType.setErrorMessage("Must provide container type");
            isInvalid = true;
        }
        if (containerSize.getValue() == null) {
            containerSize.setInvalid(true);
            containerSize.setErrorMessage("Must provide container size");
            isInvalid = true;
        }
        if (numOfContainers.getValue() == null || numOfContainers.getValue() < 1) {
            numOfContainers.setInvalid(true);
            numOfContainers.setErrorMessage("Cannot be empty or ZERO");
            isInvalid = true;
        }
        if (shipper.getValue() == null) {
            shipper.setInvalid(true);
            shipper.setErrorMessage("Must provide shipper");
            isInvalid = true;
        }
        if (consignee.getValue() == null && notifyParty.getValue() == null) {
            notifyParty.setInvalid(true);
            notifyParty.setErrorMessage("Must provide either Consignee or notify party");
            isInvalid = true;
        }
        if (carrierComboBox.getValue() == null) {
            carrierComboBox.setInvalid(true);
            carrierComboBox.setErrorMessage("Must provide Carrier");
            isInvalid = true;
        }
        return isInvalid;
    }

    private Anchor getShipmentAdviceDownloadAnchor() {
        Anchor anchor = new Anchor(new StreamResource("Shipment_Advice_" + shipment.getBooking().getBookingNo() +
                ".pdf", (InputStreamFactory) () -> {
            String report = "shipment_advice.jasper";
            Map<String, Object> parameters = prepareParamsForShipmentAdvice();

            try (InputStream stream = getClass().getResourceAsStream("/Reports/" + report)) {
                return new ByteArrayInputStream(JasperRunManager
                        .runReportToPdf(stream, parameters, new JREmptyDataSource(1)));
            } catch (JRException | IOException e) {
                throw new RuntimeException(e);
            }
        }), "");
        anchor.getElement().setAttribute("download", true);
        Button downloadButton = new Button("Download Advice");
        downloadButton.setIcon(LineAwesomeIcon.PRINT_SOLID.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        anchor.add(downloadButton);
        return anchor;
    }

    private Map<String, Object> prepareParamsForShipmentAdvice() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("LOGO_URL", "Images/logo_best.png");

        paramMap.put("ADVICE_DATE", StringUtils.defaultIfBlank(DateUtil.getDateAsString(adviceDate.getValue()),
                DateUtil.getCurrentDateAsString()));

        paramMap.put("MBL_NO", useHbl.getValue() ? hblNo.getValue() : mblNo.getValue());
//        paramMap.put("HBL_NO", hblNo.getValue());
        paramMap.put("BOOKING_NO", bookingNo.getValue());
        paramMap.put("SHIPPER_INVOICE_NO", clientInvoiceNo.getValue());

        StuffingDetails stuffingDetails = shipment.getStuffingDetails();
        if (stuffingDetails != null) {
            paramMap.put("STUFFING_DATE", stuffingDetails.getStuffingDate());
            paramMap.put("STUFFING_DEPOT", stuffingDetails.getStuffingDepot());
        }

        paramMap.put("SHIPPER_NAME", shipper.getValue().getName());
        paramMap.put("CONSIGNEE", useConsignee.getValue() ? consignee.getValue().getName()
                : notifyParty.getValue().getName());

        paramMap.put("NUM_OF_CONTAINER", numOfContainers.getValue() + "X" +
                containerSize.getValue().getContainerSize() + containerType.getValue().getContainerType());
        paramMap.put("COMMODITY", commodities.getValue());
        paramMap.put("QUANTITY", totalQuantity.getValue().toString());
        paramMap.put("GROSS_WEIGHT", totalGrossWeight.getValue().toString());

        Schedule shipmentSchedule = shipment.getSchedule();
        paramMap.put("PORT_OF_LOADING", shipmentSchedule.getPortOfLoading().getPortShortCode());
        paramMap.put("MV_CONNECT_PORT", shipmentSchedule.getMotherVesselPort().getPortShortCode());
        paramMap.put("FEEDER", shipmentSchedule.getPortOfLoadingVesselName());

        paramMap.put("POL_ETA", DateUtil.getDateAsString(shipmentSchedule.getPortOfLoadingETA()));
        paramMap.put("POL_ETD", DateUtil.getDateAsString(shipmentSchedule.getPortOfLoadingETD()));
        paramMap.put("MV_PORT_FEEDER_ETA", DateUtil.getDateAsString(shipmentSchedule.getMotherVesselETA()));

        List<ContainerDetails> containerDetails = shipment.getContainerDetails();
        paramMap.put("SEAL_NO", containerDetails.stream().map(ContainerDetails::getContainerNo)
                .reduce((container1, container2) -> container1 + ", " + container2).orElse(""));
        paramMap.put("CONTAINERS", containerDetails.stream().map(ContainerDetails::getSealNo)
                .reduce((seal1, seal2) -> seal1 + ", " + seal2).orElse(""));

        List<TSReportDto> tsReportDtoList = new LinkedList<>();
        List<Transshipment> tsList = shipmentSchedule.getTransshipments().stream().sorted(
                Comparator.comparing(Transshipment::getPortEta)).toList();

        for (int i = 0, count = 1; i < tsList.size(); i++) {
            Transshipment transshipment = tsList.get(i);
            if (transshipment.getVesselName() != null && !transshipment.getVesselName().isEmpty()) {
                tsReportDtoList.add(new TSReportDto("Vessel TS" + count++, transshipment.getVesselName()));
            }
            tsReportDtoList.add(new TSReportDto("ETA " + transshipment.getVesselPort().getPortName(),
                    DateUtil.getDateAsString(transshipment.getPortEta())));
        }

        tsReportDtoList.add(new TSReportDto("ETA Dest. " + System.lineSeparator() + "(" +
                shipmentSchedule.getPortOfDestination().getPortName() + ")",
                DateUtil.getDateAsString(shipmentSchedule.getPortOfDestinationETA())));

        JRDataSource dataSource = new JRBeanCollectionDataSource(tsReportDtoList);
        paramMap.put("COLLECTION_LIST", dataSource);

        assert this.user.get().isPresent();
        User user = this.user.get().get();
        paramMap.put("SIGNED_BY", user.getName());
        paramMap.put("SIGNED_BY_EMAIL", user.getEmail());
        paramMap.put("SIGNED_BY_CONTACT", user.getContactNo());

        return paramMap;
    }

    private List<String> findErrorsForReportData() {
        List<String> errorReasons = new LinkedList<>();
        if (user == null || user.get().isEmpty()) {
            NotificationUtil.getNotification("Session Expired. Please reload page and login again", "", false,
                    NotificationVariant.LUMO_ERROR, 4000);
            this.close();
            return errorReasons;
        }
        if (containerType.getValue() == null) {
            errorReasons.add("Must provide container type");
        }
        if (containerSize.getValue() == null) {
            errorReasons.add("Must provide container size");
        }
        if (numOfContainers.getValue() == null || numOfContainers.getValue() < 1) {
            errorReasons.add("Cannot be empty or ZERO");
        }
        if (shipper.getValue() == null) {
            errorReasons.add("Must provide shipper");
        }
        if (consignee.getValue() == null && notifyParty.getValue() == null) {
            errorReasons.add("Must provide either Consignee or notify party");
        }
        if (carrierComboBox.getValue() == null) {
            errorReasons.add("Must provide Carrier");
        }
        if (StringUtils.isBlank(commodities.getValue())) {
            errorReasons.add("Must provide Carrier");
        }
        if (shipment.getContainerDetails() == null || shipment.getContainerDetails().isEmpty()) {
            errorReasons.add("Container/Cargo details not provided");
        }
        errorReasons.addAll(validateScheduleForReport(shipment.getSchedule()));
        return errorReasons;
    }

    private List<String> validateScheduleForReport(Schedule schedule) {
        List<String> errorReasons = new LinkedList<>();
        if (schedule == null) {
            errorReasons.add("Schedule details not provided");
            return errorReasons;
        }
        if (schedule.getPortOfLoading() == null) {
            errorReasons.add("Port of loading not provided");
        }
        if (schedule.getPortOfLoadingETA() == null) {
            errorReasons.add("Port of Loading ETA not provided");
        }
        if (schedule.getPortOfLoadingETD() == null) {
            errorReasons.add("Port of Loading ETD not provided");
        }
        if (schedule.getPortOfDestination() == null) {
            errorReasons.add("Port of Destination not provided");
        }
        if (schedule.getPortOfDestinationETA() == null) {
            errorReasons.add("Port of Destination ETA not provided");
        }
        if (StringUtils.isBlank(schedule.getMotherVesselName()) && StringUtils.isBlank(schedule.getPortOfLoadingVesselName())) {
            errorReasons.add("Must specify either Mother Vessel or Feeder Vessel");
        }
        return errorReasons;
    }
}
