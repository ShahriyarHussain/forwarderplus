package com.lazoft.forwarderplus.views.export.advice;

import com.lazoft.forwarderplus.components.dialog.ClientCreationDialog;
import com.lazoft.forwarderplus.components.dialog.ReportOptionsDialog;
import com.lazoft.forwarderplus.dto.ReportOptionsDto;
import com.lazoft.forwarderplus.dto.TSReportDto;
import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.*;
import com.lazoft.forwarderplus.model.xml.CustomItem;
import com.lazoft.forwarderplus.model.xml.CustomItems;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.*;
import com.lazoft.forwarderplus.util.*;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.lazoft.forwarderplus.util.Constants.DEPOT;

@Slf4j
public class ShipmentAdviceDialog extends Dialog {

    private final ShipmentService shipmentService;
    private final ScheduleService scheduleService;
    private final CarrierService carrierService;
    private final ClientService clientService;
    private final PortService portService;
    private final UserService userService;
    private final IdGenerationService idGenerationService;
    private final Shipment shipment;

    private final TextField mblNo = new TextField("Master B/L No:");
    private final TextField hblNo = new TextField("House B/L No:");
    private final TextArea goodsDescription = new TextArea("Goods Description");
    private final TextArea shipperMarks = new TextArea("Shipper Marks");
    private final TextField bookingNo = new TextField("Booking No");
    private final TextField clientInvoiceNo = new TextField("Shipper Invoice No");
    private final ComboBox<ContainerType> containerType = new ComboBox<>("Container Type");
    private final ComboBox<ContainerSize> containerSize = new ComboBox<>("Container Size");
    private final IntegerField numOfContainers = new IntegerField("Number of Containers");
    private final ComboBox<Client> shipper = new ComboBox<>("Shipper");
    private final ComboBox<Client> consignee = new ComboBox<>("Consignee");
    private final ComboBox<Client> notifyParty = new ComboBox<>("Notify Party");
    private final TextField commodities = new TextField("Commodities");
    private final ComboBox<Carrier> carrierComboBox = new ComboBox<>("Carrier");
    private final Button generateHbl = new Button(LineAwesomeIcon.ATOM_SOLID.create());
    private final RadioButtonGroup<ShippingTerm> freightTerm = new RadioButtonGroup<>("Freight Term");

    private final DatePicker stuffingDate = new DatePicker("Stuffing Date");
    private final ComboBox<String> stuffingDepot = new ComboBox<>("Stuffing Depot");

    private final TextField schedule = new TextField("Schedule");
    private final DatePicker departureDate = new DatePicker("ETD Origin:");
    private final DatePicker arrivalDate = new DatePicker("ETA Destination:");
    private final TextField approxTime = new TextField("Transit Time");
    private final Button editSchedule = new Button(LineAwesomeIcon.PEN_SOLID.create());

    private final IntegerField totalQuantity = new IntegerField("Total Quantity");
    private final TextField unit = new TextField("Unit");
    private final BigDecimalField totalGrossWeight = new BigDecimalField("Total Weight (KGs)");
    private final Button editCargo = new Button(LineAwesomeIcon.PEN_SOLID.create());

    private final Button saveButton = new Button("Save");
    private final Button downloadButton = new Button("Download as PDF");
    private final Button closeButton = new Button("Close");
    private final Button addClientButton = new Button(LineAwesomeIcon.USER_PLUS_SOLID.create());

    private static final int TEXT_AREA_CHAR_LIMIT = 4000;

    private boolean isSaved = false;
    private final User user;
    private final List<Client> clientList;
    private final List<CustomItem> depotList;

    private final StuffingDetails stuffingDetails;


    public ShipmentAdviceDialog(ShipmentService shipmentService, ScheduleService scheduleService,
                                CarrierService carrierService, ClientService clientService, PortService portService,
                                UserService userService, IdGenerationService idGenerationService,
                                Shipment shipment, AuthenticatedUser authenticatedUser) {

        if (authenticatedUser.get().isEmpty()) {
            NotificationUtil.getNotification("User not logged in! Reload page and try again", "", false, NotificationVariant.LUMO_ERROR, 3000).open();
            this.user = null;
            close();
        } else {
            this.user = authenticatedUser.get().get();
        }

        this.shipment = shipment;
        this.userService = userService;
        this.portService = portService;
        this.clientService = clientService;
        this.carrierService = carrierService;
        this.scheduleService = scheduleService;
        this.shipmentService = shipmentService;
        this.idGenerationService = idGenerationService;
        this.clientList = clientService.getAllClients();this.depotList = CustomItemUtil.getItemsListFromFile(Constants.DEPOT);

        if (shipment.getStuffingDetails() == null) {
            this.stuffingDetails = new StuffingDetails();
        } else {
            this.stuffingDetails = shipment.getStuffingDetails();
        }

        this.setWidth("85%");
        this.setHeight("85%");
        this.setHeaderTitle("Edit Shipment Details");
        this.setCloseOnOutsideClick(false);

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
        containerType.setItemLabelGenerator(ContainerType::getType);
        containerSize.setItems(ContainerSize.values());
        containerSize.setItemLabelGenerator(ContainerSize::getContainerSize);

        carrierComboBox.setItems(carrierService.getAllCarriers());
        carrierComboBox.setItemLabelGenerator(Carrier::getName);

        shipper.setItemLabelGenerator(Client::getName);
        shipper.setItems(new LinkedList<>());
        shipper.addFocusListener(event ->
                loadClientPropertiesOnDemand(shipper, ClientType.SHIPPER));

        consignee.setItemLabelGenerator(Client::getName);
        consignee.setItems(new LinkedList<>());
        consignee.addFocusListener(event ->
                loadClientPropertiesOnDemand(consignee, ClientType.CONSIGNEE));

        notifyParty.setWidth("80%");
        notifyParty.setItemLabelGenerator(Client::getName);
        notifyParty.setItems(new LinkedList<>());
        notifyParty.addFocusListener(event ->
                loadClientPropertiesOnDemand(notifyParty, ClientType.NOTIFY_PARTY));


        schedule.setReadOnly(true);
        approxTime.setReadOnly(true);
        departureDate.setReadOnly(true);
        departureDate.setLocale(Locale.UK);
        arrivalDate.setReadOnly(true);
        arrivalDate.setLocale(Locale.UK);

        totalQuantity.setReadOnly(true);
        unit.setReadOnly(true);
        totalGrossWeight.setReadOnly(true);

        freightTerm.setItems(ShippingTerm.values());
        freightTerm.setWidth("20%");

        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        downloadButton.setIcon(LineAwesomeIcon.PRINT_SOLID.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        addClientButton.setTooltipText("Add Client");

        stuffingDepot.setItems(depotList.stream().map(CustomItem::getName).toList());
        stuffingDepot.setAllowCustomValue(true);
        stuffingDate.setLocale(Locale.UK);

        editSchedule.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editSchedule.setTooltipText("Edit Schedule");
        editSchedule.setMaxWidth("15%");
        editSchedule.setText("Edit Schedule");

        editCargo.setTooltipText("Edit Container Details");
        editCargo.setMaxWidth("20%");
        editCargo.setText("Edit Container Details");
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
        freightTerm.setValue(shipment.getShippingTerm());
        fillUpScheduleValues();
        fillUpCargoValues();

        StuffingDetails stuffingDetails = shipment.getStuffingDetails();
        if (stuffingDetails == null) {
            return;
        }
        stuffingDepot.setValue(stuffingDetails.getStuffingDepot());
        stuffingDate.setValue(stuffingDetails.getStuffingDate());
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
        if (shipment.getContainerDetails().isEmpty()) {
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

    private void loadClientPropertiesOnDemand(ComboBox<Client> clientComboBox, ClientType clientType) {
        if (clientList.isEmpty()) {
            clientList.addAll(clientService.getClientsByType(List.of(clientType, ClientType.ALL)));
        }
        clientComboBox.setItems(clientList);
    }

    public void setUpFormLayout() {
        Accordion shipmentPanel = new Accordion();
        shipmentPanel.add("Shipment Info (Click to Collapse/Expand)", getShipmentInfoFormLayout());

        Accordion schedulePanel = new Accordion();
        schedulePanel.add("Schedule Info (Click to Collapse/Expand)", getScheduleInfoFormLayout());

        Accordion containerDetailsPanel = new Accordion();
        containerDetailsPanel.add("Container Details (Click to Collapse/Expand)", getContainerDetailsFormLayout());

        add(shipmentPanel, schedulePanel, containerDetailsPanel);
    }

    private FormLayout getContainerDetailsFormLayout() {
        FormLayout containerDetailsLayout = new FormLayout();
        editCargo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editCargo.setTooltipText("Edit Container Details");
        containerDetailsLayout.add(totalGrossWeight, totalQuantity, unit, editCargo);
        containerDetailsLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 5));
        return containerDetailsLayout;
    }

    private FormLayout getScheduleInfoFormLayout() {
        FormLayout scheduleLayout = new FormLayout();
        scheduleLayout.add(schedule, approxTime, departureDate, arrivalDate, editSchedule);
        scheduleLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 5));
        scheduleLayout.setColspan(schedule, 2);
        return scheduleLayout;
    }

    private FormLayout getShipmentInfoFormLayout() {
        FormLayout shipmentInfoLayout = new FormLayout();

        HorizontalLayout hblComponent = new HorizontalLayout(hblNo, generateHbl);
        hblComponent.setVerticalComponentAlignment(FlexComponent.Alignment.END);

        HorizontalLayout notifyPartyLayout = new HorizontalLayout(notifyParty, addClientButton);
        notifyPartyLayout.setAlignItems(FlexComponent.Alignment.END);

        hblComponent.setAlignItems(FlexComponent.Alignment.END);
        shipmentInfoLayout.add(bookingNo, clientInvoiceNo, mblNo, hblComponent,
                containerType, numOfContainers, containerSize, commodities,
                carrierComboBox, shipper, consignee, notifyPartyLayout,
                stuffingDate, stuffingDepot, freightTerm,
                goodsDescription, shipperMarks);
        shipmentInfoLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 4));
//        shipmentInfoLayout.setColspan(freightTerm, 2);
        shipmentInfoLayout.setColspan(goodsDescription, 2);
        shipmentInfoLayout.setColspan(shipperMarks, 2);
        return shipmentInfoLayout;
    }

    private void setListeners() {
        editCargo.addClickListener(event -> new EditContainerDetailsDialog(shipment, shipmentService, this).open());

        editSchedule.addClickListener(event -> new EditScheduleDialog(portService, shipmentService, scheduleService,shipment, this).open());

        generateHbl.addClickListener(event -> {
            try {
                IdGeneration idGeneration = idGenerationService.getIncrementedId(IdTypes.HOUSE_BL);
                hblNo.setValue(idGeneration.getPrefix() + idGeneration.getIncrementNum() + idGeneration.getSuffix());
            } catch (IllegalArgumentException e) {
                NotificationUtil.getNotification("Id generation not set for this field", "", false, NotificationVariant.LUMO_WARNING, 3000).open();
            } catch (Exception e) {
                NotificationUtil.getNotification("Error while generating Id", e.getMessage(), true, NotificationVariant.LUMO_ERROR, 3000).open();
            }
        });

        addClientButton.addClickListener(event -> new ClientCreationDialog(clientService, clientList).open());

        saveButton.addClickListener(event -> {
            CustomItems newDepots = new CustomItems(depotList);
            CustomItemUtil.saveCustomItems(newDepots, DEPOT);

            if (isInvalidEntriesForSave()) {
                return;
            }

            prepareDataForSaving();
            setValuesToShipmentForSaving();
            try {
                shipmentService.updateShipmentWithStuffingDetails(shipment, stuffingDetails);
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

            ReportOptionsDto dto = new ReportOptionsDto();
            dto.setUser(user);
            dto.setConsignee(consignee.getValue() == null ? null : consignee.getValue().getName());
            dto.setHblNo(hblNo.getValue());
            dto.setView(View.SHIPMENT_ADVICE);
            dto.setUsers(userService.getAll());
            dto.setParameters(prepareParamsForShipmentAdvice());
            dto.setFileName("Shipment-Advice-" + shipment.getMblNo());
            dto.setReportSourceFileName("shipment_advice.jasper");
            dto.setReportDate(LocalDate.now());

            Dialog reportDialog = new ReportOptionsDialog(dto);
            reportDialog.open();

            if (shipment.getStatus() == ShipmentStatus.SHIPPING_ORDER_OK) {
                shipment.setStatus(ShipmentStatus.SHIPMENT_ADVICE_OK);
                shipmentService.saveShipment(shipment);
            }
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

        stuffingDepot.addCustomValueSetListener(event -> {
            String customValue = event.getDetail();
            if (customValue == null) {
                return;
            }
            depotList.add(new CustomItem(customValue, event.getDetail().trim().toLowerCase().hashCode(), null));
            stuffingDepot.setItems(depotList.stream().map(CustomItem::getName).toList());
            stuffingDepot.setValue(customValue);
        });
    }

    private void prepareDataForSaving() {
        stuffingDetails.setStuffingId(shipment.getShipmentId());
        if (shipment.getContainerDetails() != null && !shipment.getContainerDetails().isEmpty()) {
            stuffingDetails.setPackageUnit(shipment.getContainerDetails().get(0).getPackageUnit());
        }
        stuffingDetails.setQuantity(totalQuantity.getValue());
        if (shipment.getSchedule() != null) {
            stuffingDetails.setVessel(shipment.getSchedule().getPortOfLoadingVesselName());
        }
        stuffingDetails.setStuffingDepot(stuffingDepot.getValue());
        stuffingDetails.setStuffingDate(stuffingDate.getValue());
        if (notifyParty.getValue() != null) {
            shipment.setNotifyParty(notifyParty.getValue());
        }
    }

    private void setValuesToShipmentForSaving() {
        if (freightTerm.getValue() != null) {
            shipment.setShippingTerm(freightTerm.getValue());
        }
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
            shipment.setGoodsDescription(StringUtils.truncate(goodsDescription.getValue(), TEXT_AREA_CHAR_LIMIT));
        }
        if (!StringUtils.isBlank(shipperMarks.getValue())) {
            shipment.setShipperMarks(StringUtils.truncate(shipperMarks.getValue(), TEXT_AREA_CHAR_LIMIT));
        }
        if (!StringUtils.isBlank(stuffingDepot.getValue())) {
            stuffingDetails.setStuffingDepot(stuffingDepot.getValue());
        }
        if (stuffingDate != null) {
            stuffingDetails.setStuffingDate(stuffingDate.getValue());
        }
        if (numOfContainers.getValue() != 0) {
            shipment.getBooking().setNumOfContainers(numOfContainers.getValue());
            shipment.setNumOfContainers(numOfContainers.getValue());
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
        if (goodsDescription.getValue() != null && goodsDescription.getValue().length() > TEXT_AREA_CHAR_LIMIT) {
            goodsDescription.setInvalid(true);
            goodsDescription.setErrorMessage("Cannot exceed " + TEXT_AREA_CHAR_LIMIT + " Characters");
            isInvalid = true;
        }
        if (shipperMarks.getValue() != null && shipperMarks.getValue().length() > TEXT_AREA_CHAR_LIMIT) {
            shipperMarks.setInvalid(true);
            shipperMarks.setErrorMessage("Cannot exceed " + TEXT_AREA_CHAR_LIMIT + " Characters");
            isInvalid = true;
        }
//        if (stuffingDate.getValue() != null) {
//            stuffingDate.setInvalid(true);
//            stuffingDate.setErrorMessage("Please provide a date");
//            isInvalid = true;
//        }
        return isInvalid;
    }

    private Map<String, Object> prepareParamsForShipmentAdvice() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("LOGO_URL", "Images/logo_best.png");
        paramMap.put("MBL_NO", mblNo.getValue());
        paramMap.put("HBL_NO", hblNo.getValue());

        paramMap.put("BOOKING_NO", bookingNo.getValue());
        paramMap.put("SHIPPER_INVOICE_NO", clientInvoiceNo.getValue());

        StuffingDetails stuffingDetails = shipment.getStuffingDetails();
        if (stuffingDetails != null) {
            paramMap.put("STUFFING_DATE", DateUtil.getDateAsString(stuffingDetails.getStuffingDate()));
            paramMap.put("STUFFING_DEPOT", stuffingDetails.getStuffingDepot());
        }

        paramMap.put("SHIPPER_NAME", shipper.getValue().getName());
        paramMap.put("NOTIFY_PARTY", notifyParty.getValue().getName());
        if (consignee.getValue() != null) {
            paramMap.put("CONSIGNEE", consignee.getValue().getName());
        }

        paramMap.put("NUM_OF_CONTAINER", numOfContainers.getValue() + " X " +
                containerSize.getValue().getContainerSize() + " " +  containerType.getValue().getType());
        paramMap.put("COMMODITY", commodities.getValue());
        paramMap.put("QUANTITY", totalQuantity.getValue().toString() + " " +unit.getValue());
        paramMap.put("GROSS_WEIGHT", AmountFormatter.getFormattedAmount(totalGrossWeight.getValue(), AmountCurrency.BDT) + " KGs");

        Schedule shipmentSchedule = shipment.getSchedule();
        paramMap.put("PORT_OF_LOADING", shipmentSchedule.getPortOfLoading().getPortShortCode());
        paramMap.put("MV_CONNECT_PORT", shipmentSchedule.getMotherVesselPort().getPortShortCode());
        paramMap.put("FEEDER", shipmentSchedule.getPortOfLoadingVesselName());

        paramMap.put("POL_ETA", DateUtil.getDateAsString(shipmentSchedule.getPortOfLoadingETA()));
        paramMap.put("POL_ETD", DateUtil.getDateAsString(shipmentSchedule.getPortOfLoadingETD()));
        paramMap.put("MV_PORT_FEEDER_ETA", DateUtil.getDateAsString(shipmentSchedule.getMvPortFeederEta()));

        List<ContainerDetails> containerDetails = shipment.getContainerDetails();
        paramMap.put("SEAL_NO", containerDetails.stream().map(ContainerDetails::getContainerNo)
                .reduce((container1, container2) -> container1 + ", " + container2).orElse(""));
        paramMap.put("CONTAINERS", containerDetails.stream().map(ContainerDetails::getSealNo)
                .reduce((seal1, seal2) -> seal1 + ", " + seal2).orElse(""));

        List<TSReportDto> tsReportDtoList = new LinkedList<>();
        List<Transshipment> tsList = shipmentSchedule.getTransshipments().stream().sorted(
                Comparator.comparing(Transshipment::getTransshipmentId)).toList();

        tsReportDtoList.add(new TSReportDto("Mother Vessel", shipmentSchedule.getMotherVesselName()));
        tsReportDtoList.add(new TSReportDto("ETA " + shipmentSchedule.getMotherVesselPort().getPortName(),
                DateUtil.getDateAsString(shipmentSchedule.getMotherVesselETA())));

        for (int i = 0; i < tsList.size(); i++) {
            Transshipment transshipment = tsList.get(i);
            if (!StringUtils.isBlank(transshipment.getVesselName())) {
                tsReportDtoList.add(new TSReportDto("Vessel TS" + (i+1), transshipment.getVesselName()));
            }
            tsReportDtoList.add(new TSReportDto("ETA " + transshipment.getVesselPort().getPortName(),
                    DateUtil.getDateAsString(transshipment.getPortEta())));
        }

        tsReportDtoList.add(new TSReportDto("ETA Dest. " + System.lineSeparator() + "(" +
                shipmentSchedule.getPortOfDestination().getPortName() + ")",
                DateUtil.getDateAsString(shipmentSchedule.getPortOfDestinationETA())));

        JRDataSource dataSource = new JRBeanCollectionDataSource(tsReportDtoList);
        paramMap.put("COLLECTION_LIST", dataSource);
        return paramMap;
    }

    private List<String> findErrorsForReportData() {
        List<String> errorReasons = new LinkedList<>();
        if (containerType.getValue() == null) {
            errorReasons.add("Provide container type");
        }
        if (containerSize.getValue() == null) {
            errorReasons.add("Provide container size");
        }
        if (numOfContainers.getValue() == null || numOfContainers.getValue() < 1) {
            errorReasons.add("Number of Containers cannot be empty or ZERO");
        }
        if (shipper.getValue() == null) {
            errorReasons.add("Provide shipper");
        }
        if (consignee.getValue() == null && notifyParty.getValue() == null) {
            errorReasons.add("Provide either Consignee or notify party");
        }
        if (carrierComboBox.getValue() == null) {
            errorReasons.add("Provide a valid carrier");
        }
        if (StringUtils.isBlank(commodities.getValue())) {
            errorReasons.add("Commodities cannot be empty");
        }
        if (shipment.getContainerDetails() == null || shipment.getContainerDetails().isEmpty()) {
            errorReasons.add("Provide Container/Cargo details");
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
