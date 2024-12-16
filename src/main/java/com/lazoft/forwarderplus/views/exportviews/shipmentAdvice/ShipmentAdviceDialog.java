package com.lazoft.forwarderplus.views.exportviews.shipmentAdvice;

import com.lazoft.forwarderplus.entity.Carrier;
import com.lazoft.forwarderplus.entity.Client;
import com.lazoft.forwarderplus.entity.Commodity;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.enums.ContainerSize;
import com.lazoft.forwarderplus.enums.ContainerType;
import com.lazoft.forwarderplus.services.CarrierService;
import com.lazoft.forwarderplus.services.ClientService;
import com.lazoft.forwarderplus.services.ScheduleService;
import com.lazoft.forwarderplus.services.ShipmentService;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.vaadin.lineawesome.LineAwesomeIcon;

public class ShipmentAdviceDialog extends Dialog {

    private final ShipmentService shipmentService;
    private final ScheduleService scheduleService;
    private final CarrierService carrierService;
    private final ClientService clientService;
    private final Shipment shipment;


    private final TextField mblNo = new TextField("Master B/L No:");
    private final TextField hblNo = new TextField("House B/L No:");
    private final TextArea goodsDescription = new TextArea("Goods Description");
    private final TextArea shipperMarks = new TextArea("Shipper Marks");
    private final TextField bookingNo = new TextField("Booking No");
    private final TextField shipperInvoiceNo = new TextField("Shipper Invoice No");
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

    private final TextField quantity = new TextField("Quantity");
    private final TextField unit = new TextField("Unit");
    private final TextField grossWeight = new TextField("Weight");
    private final Button editCargo = new Button(LineAwesomeIcon.PEN_SOLID.create());

    public ShipmentAdviceDialog(ShipmentService shipmentService, ScheduleService scheduleService,
                                CarrierService carrierService, ClientService clientService, Shipment shipment) {
        this.shipmentService = shipmentService;
        this.scheduleService = scheduleService;
        this.carrierService = carrierService;
        this.clientService = clientService;
        this.shipment = shipment;
        this.setWidth("85%");
        this.setHeight("85%");
        this.setHeaderTitle("Edit Shipment");

        setUpFormLayout();
        fillUpExistingValues();

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

        Button downloadButton = new Button("Download");
        downloadButton.setIcon(LineAwesomeIcon.PRINT_SOLID.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        getFooter().add(closeButton, downloadButton, saveButton);
    }

    private void fillUpExistingValues() {

    }

    public void setUpFormLayout() {
        FormLayout shipmentInfoLayout = getShipmentInfoFormLayout();
        FormLayout scheduleLayout = getScheduleInfoFormLayout();
        FormLayout cargoDetailsLayout = getCargoDetailsFormLayout();

        Accordion shipmentPanel = new Accordion();
        shipmentPanel.add("Shipment Info", shipmentInfoLayout);

        Accordion schedulePanel = new Accordion();
        schedulePanel.add("Schedule Info", scheduleLayout);

        Accordion cargoDetailsPanel = new Accordion();
        cargoDetailsPanel.add("Cargo Details", cargoDetailsLayout);
        add(shipmentPanel, schedulePanel, cargoDetailsPanel);
    }

    private FormLayout getCargoDetailsFormLayout() {
        FormLayout cargoDetailsLayout = new FormLayout();
        editCargo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editCargo.setTooltipText("Edit Cargo Details");
        HorizontalLayout editCargoLayout = new HorizontalLayout(grossWeight, editCargo);
        editCargoLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        editCargoLayout.setAlignItems(FlexComponent.Alignment.END);
        cargoDetailsLayout.add(quantity, unit, editCargoLayout);
        cargoDetailsLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 5));
        return cargoDetailsLayout;
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
        bookingNo.setReadOnly(true);
        generateHbl.setTooltipText("Generate A Number");
        generateHbl.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout hblComponent = new HorizontalLayout(hblNo, generateHbl);
        hblComponent.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        hblComponent.setAlignItems(FlexComponent.Alignment.END);
        shipmentInfoLayout.add(bookingNo, shipperInvoiceNo, mblNo, hblComponent,
                containerType, numOfContainers, containerSize, commodities,
                carrierComboBox, shipper, consignee, notifyParty,
                goodsDescription, shipperMarks);
        shipmentInfoLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 4));
        shipmentInfoLayout.setColspan(goodsDescription,2);
        shipmentInfoLayout.setColspan(shipperMarks,2);
        return shipmentInfoLayout;
    }
}
