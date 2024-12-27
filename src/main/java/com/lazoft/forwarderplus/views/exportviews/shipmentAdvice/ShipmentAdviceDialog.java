package com.lazoft.forwarderplus.views.exportviews.shipmentAdvice;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.ClientType;
import com.lazoft.forwarderplus.enums.ContainerSize;
import com.lazoft.forwarderplus.enums.ContainerType;
import com.lazoft.forwarderplus.services.*;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class ShipmentAdviceDialog extends Dialog {

    private final ShipmentService shipmentService;
    private final ScheduleService scheduleService;
    private final CarrierService carrierService;
    private final ClientService clientService;
    private final PortService portService;
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
    private final ComboBox<Commodity> commodities = new ComboBox<>("Commodities");
    private final ComboBox<Carrier> carrierComboBox = new ComboBox<>("Carrier");
    private final Button generateHbl = new Button(LineAwesomeIcon.ATOM_SOLID.create());

    private final TextField schedule = new TextField("Schedule");
    private final DatePicker departureDate = new DatePicker("ETD Origin:");
    private final DatePicker arrivalDate = new DatePicker("ETA Destination:");
    private final TextField approxTime = new TextField("Approx. Transit");
    private final Button editSchedule = new Button(LineAwesomeIcon.PEN_SOLID.create());

    private final IntegerField totalQuantity = new IntegerField("Total Quantity");
    private final TextField unit = new TextField("Unit");
    private final BigDecimalField totalGrossWeight = new BigDecimalField("Total Weight");
    private final Button editCargo = new Button(LineAwesomeIcon.PEN_SOLID.create());

    public ShipmentAdviceDialog(ShipmentService shipmentService, ScheduleService scheduleService,
                                CarrierService carrierService, ClientService clientService, PortService portService,
                                Shipment shipment) {

        this.shipmentService = shipmentService;
        this.scheduleService = scheduleService;
        this.carrierService = carrierService;
        this.clientService = clientService;
        this.portService = portService;
        this.shipment = shipment;

        this.setWidth("85%");
        this.setHeight("85%");
        this.setHeaderTitle("Edit Shipment");

        setUpFormLayout();
        setFieldAttributes();
        fillUpExistingValues();
        setListeners();

        Button closeButton = new Button("Close");
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        closeButton.addClickListener(event -> {
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

        Button saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button downloadButton = new Button("Download as PDF");
        downloadButton.setIcon(LineAwesomeIcon.PRINT_SOLID.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        getFooter().add(closeButton, downloadButton, saveButton);
    }

    private void setListeners() {
        editCargo.addClickListener(event -> new EditContainerDetailsLayout(shipment, shipmentService, this).open());
        editSchedule.addClickListener(event -> new EditScheduleDialog(portService, shipmentService, scheduleService,
                shipment, this).open());
        generateHbl.addClickListener(event -> {});
    }

    private void setFieldAttributes() {
        bookingNo.setReadOnly(true);
        generateHbl.setTooltipText("Generate A Number");
        generateHbl.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        containerType.setItems(ContainerType.values());
        containerType.setItemLabelGenerator(ContainerType::getContainerType);
        containerSize.setItems(ContainerSize.values());
        containerSize.setItemLabelGenerator(ContainerSize::getContainerSize);

        carrierComboBox.setItems(carrierService.getAllCarriers());
        carrierComboBox.setItemLabelGenerator(Carrier::getName);

        List<Client> clients = clientService.getAllClients();
        shipper.setItemLabelGenerator(Client::getName);
        shipper.setItems(clients.stream().filter(client -> client.getType() == ClientType.SHIPPER
                || client.getType() == ClientType.ALL).collect(Collectors.toList()));
        consignee.setItemLabelGenerator(Client::getName);
        consignee.setItems(clients.stream().filter(client -> client.getType() == ClientType.CONSIGNEE
                || client.getType() == ClientType.ALL).collect(Collectors.toList()));
        notifyParty.setItemLabelGenerator(Client::getName);
        notifyParty.setItems(clients.stream().filter(client -> client.getType() == ClientType.NOTIFY_PARTY
                || client.getType() == ClientType.ALL).collect(Collectors.toList()));

        schedule.setReadOnly(true);
        approxTime.setReadOnly(true);
        departureDate.setReadOnly(true);
        arrivalDate.setReadOnly(true);

        totalQuantity.setReadOnly(true);
        unit.setReadOnly(true);
        totalGrossWeight.setReadOnly(true);
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
        departureDate.setValue(scheduleData.getPortOfDestinationETA());
        arrivalDate.setValue(scheduleData.getPortOfLoadingETD());
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
        FormLayout shipmentInfoLayout = getShipmentInfoFormLayout();
        FormLayout scheduleLayout = getScheduleInfoFormLayout();
        FormLayout containerDetailsLayout = getContainerDetailsFormLayout();

        Accordion shipmentPanel = new Accordion();
        shipmentPanel.add("Shipment Info", shipmentInfoLayout);

        Accordion schedulePanel = new Accordion();
        schedulePanel.add("Schedule Info", scheduleLayout);

        Accordion containerDetailsPanel = new Accordion();
        containerDetailsPanel.add("Container Details", containerDetailsLayout);
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
        scheduleLayout.setColspan(schedule,2);
        return scheduleLayout;
    }

    private FormLayout getShipmentInfoFormLayout() {
        FormLayout shipmentInfoLayout = new FormLayout();
        HorizontalLayout hblComponent = new HorizontalLayout(hblNo, generateHbl);
        hblComponent.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        hblComponent.setAlignItems(FlexComponent.Alignment.END);
        shipmentInfoLayout.add(bookingNo, clientInvoiceNo, mblNo, hblComponent,
                containerType, numOfContainers, containerSize, commodities,
                carrierComboBox, shipper, consignee, notifyParty,
                goodsDescription, shipperMarks);
        shipmentInfoLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 4));
        shipmentInfoLayout.setColspan(goodsDescription,2);
        shipmentInfoLayout.setColspan(shipperMarks,2);
        return shipmentInfoLayout;
    }
}
