package com.lazoft.forwarderplus.views.exportviews.shipmentAdvice;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.services.PortService;
import com.lazoft.forwarderplus.services.ScheduleService;
import com.lazoft.forwarderplus.services.ShipmentService;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
public class EditScheduleDialog extends Dialog {

    private final ScheduleService scheduleService;
    private final ShipmentService shipmentService;
    private final PortService portService;

    private final ComboBox<Schedule> existingSchedule = new ComboBox<>("Choose Existing Schedule");

    private final TextField feederVessel = new TextField("Feeder Vessel");
    private final ComboBox<Port> portOfLoading = new ComboBox<>("Port of Loading");
    private final DatePicker etaPortOfLoading = new DatePicker("Port of Loading ETA");
    private final DatePicker etdPortOfLoading = new DatePicker("Port of Loading ETD");

    private final TextField motherVessel = new TextField("Mother Vessel");
    private final ComboBox<Port> motherVesselPort = new ComboBox<>("Mother Vessel Port");
    private final DatePicker motherVesselPortETA = new DatePicker("Mother Vessel Connect Port ETA");

    private final ComboBox<Port> portOfDestination = new ComboBox<>("Port of Destination");
    private final DatePicker etaPortOfDestination = new DatePicker("Port of Destination ETA");

    private final TextField transshipmentVessel = new TextField("Transshipment Vessel");
    private final ComboBox<Port> transshipmentPort = new ComboBox<>("Transshipment Port");
    private final DatePicker transshipmentETA = new DatePicker("Transshipment ETA");

    private final Grid<Transshipment> grid = new Grid<>(Transshipment.class, false);

    private final Button addTransshipmentButton = new Button(LineAwesomeIcon.PLUS_CIRCLE_SOLID.create());
    private final Button clearAllBtn = new Button("Clear All");
    private final Button saveButton = new Button("Save");
    private final Button selectSchedule = new Button(VaadinIcon.CHECK_CIRCLE.create());

    private final Set<Transshipment> transshipmentSet = new HashSet<>();
    private final List<Port> portList = new LinkedList<>();
    private Schedule schedule;
    private final Shipment shipment;

    public EditScheduleDialog(PortService portService, ShipmentService shipmentService,
                              ScheduleService scheduleService, Shipment shipment) {

        this.scheduleService = scheduleService;
        this.shipmentService = shipmentService;
        this.portService = portService;
        this.shipment = shipment;
        this.schedule = shipment.getSchedule();
        this.setHeaderTitle("Schedule Details");
        this.setWidth(800, Unit.PIXELS);
        this.getFooter().add(clearAllBtn, new Button("Close"), saveButton);

        prepareContainerDetailsGrid();

        setExistingValues();
        setAttributes();
        setClickListeners();

        FormLayout formLayout = getScheduleEditForm();
        add(formLayout, new Hr(), grid);
    }

    private void setExistingValues() {
        if (schedule == null) {
            return;
        }
        existingSchedule.setValue(schedule);

        feederVessel.setValue(schedule.getFeederVesselName());
        etaPortOfLoading.setValue(schedule.getPortOfLoadingETA());
        etdPortOfLoading.setValue(schedule.getPortOfLoadingETD());

        portOfLoading.setValue(schedule.getPortOfLoading());
        portOfDestination.setValue(schedule.getPortOfDestination());
        etaPortOfDestination.setValue(schedule.getPortOfDestinationETA());

        motherVessel.setValue(schedule.getMotherVesselName());
        motherVesselPort.setValue(schedule.getMotherVesselPort());
        motherVesselPortETA.setValue(schedule.getMotherVesselETA());

        grid.setItems(schedule.getTransshipments());
    }

    private void setExistingScheduleValues() {
        Booking shipmentBooking = shipment.getBooking();
        List<Schedule> schedules = scheduleService.getScheduleByPolAndPodAndDate(shipmentBooking.getLoadingPort(),
                shipmentBooking.getDestinationPort(), LocalDate.now().minusMonths(2));

        if (schedule != null) {
            schedules.add(schedule);
        }
        existingSchedule.setItems(schedules);
        existingSchedule.setValue(schedule);
    }

    private void setAttributes() {
        existingSchedule.setHelperText("Autofill with existing schedule");
        existingSchedule.setItemLabelGenerator(Schedule::getScheduleSummary);

        selectSchedule.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTransshipmentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        clearAllBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        portList.clear();
        portList.addAll(portService.getAllPorts());

        portOfLoading.setItems(portList);
        portOfDestination.setItems(portList);
        motherVesselPort.setItems(portList);
        transshipmentPort.setItems(portList);
        setExistingScheduleValues();

        portOfLoading.setItemLabelGenerator(Port::getPortLabel);
        portOfDestination.setItemLabelGenerator(Port::getPortLabel);
        motherVesselPort.setItemLabelGenerator(Port::getPortLabel);
        transshipmentPort.setItemLabelGenerator(Port::getPortLabel);
    }

    private void setClickListeners() {
        saveButton.addClickListener(event -> {
            try {
                if (schedule == null) {
                    setValuesToNewSchedule();
                }
                shipmentService.addScheduleToShipment(shipment, schedule);
                NotificationUtil.getNotification("Schedule Saved Successfully!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 3000).open();
            } catch (Exception e) {
                log.error("Error is saving container details", e);
                NotificationUtil.getNotification("Unexpected Error! Could not save data.", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 5000).open();
            }
        });

        selectSchedule.addClickListener(event -> {
            if (existingSchedule.getValue() == null) {
                NotificationUtil.getNotification("Please select a value first", "", false,
                        NotificationVariant.LUMO_WARNING, 4000).open();
            }
            Schedule selectedSchedule = existingSchedule.getValue();
            if (selectedSchedule != null) {
                feederVessel.setValue(selectedSchedule.getFeederVesselName());
                etaPortOfLoading.setValue(selectedSchedule.getPortOfLoadingETA());
                etdPortOfLoading.setValue(selectedSchedule.getPortOfLoadingETD());

                portOfLoading.setValue(selectedSchedule.getPortOfLoading());
                portOfDestination.setValue(selectedSchedule.getPortOfDestination());
                etaPortOfDestination.setValue(selectedSchedule.getPortOfDestinationETA());

                motherVessel.setValue(selectedSchedule.getMotherVesselName());
                motherVesselPort.setValue(selectedSchedule.getMotherVesselPort());
                motherVesselPortETA.setValue(selectedSchedule.getMotherVesselETA());
            }
        });
    }

    private void setValuesToNewSchedule() {

    }

    private void prepareContainerDetailsGrid() {
        grid.addColumn("sl").setHeader("Sl No.").setAutoWidth(true).setSortable(false);
        grid.addColumn(transshipment -> "Test").setHeader("TS Port").setAutoWidth(true).setSortable(false);
        grid.addColumn("vesselName").setHeader("Vessel").setAutoWidth(true).setSortable(false);
        grid.addColumn("portEta").setHeader("ETA To Port").setAutoWidth(true).setSortable(false);
        grid.addComponentColumn(transshipment -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(event -> {
                transshipmentSet.remove(transshipment);
                grid.setItems(transshipmentSet);
            });
            return deleteButton;
        }).setHeader("Delete");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        grid.setItems(transshipmentSet);

        addTransshipmentButton.addClickListener(event -> {
            Transshipment transshipment = new Transshipment();
            transshipment.setSl(transshipmentSet.size() + 1);
            transshipment.setPortEta(transshipmentETA.getValue());
            transshipment.setVesselName(transshipmentVessel.getValue());
            transshipmentSet.add(transshipment);
            grid.setItems(transshipmentSet);
        });

    }

    private FormLayout getScheduleEditForm() {
        FormLayout formLayout = new FormLayout();

        HorizontalLayout chooseExistingShipmentLayout = new HorizontalLayout(existingSchedule, addTransshipmentButton);
        chooseExistingShipmentLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        chooseExistingShipmentLayout.setAlignItems(FlexComponent.Alignment.END);

        HorizontalLayout transhipmentEtaAddLayout = new HorizontalLayout(transshipmentETA, addTransshipmentButton);
        transhipmentEtaAddLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        transhipmentEtaAddLayout.setAlignItems(FlexComponent.Alignment.END);

        Hr line1 = new Hr(), line2 = new Hr(), line3 = new Hr();
        formLayout.add(existingSchedule,
                line1,
                feederVessel, etaPortOfLoading, etdPortOfLoading,
                portOfLoading, portOfDestination, etaPortOfDestination,
                line2,
                motherVessel, motherVesselPort, motherVesselPortETA,
                line3,
                transshipmentVessel, transshipmentPort, transhipmentEtaAddLayout);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));
        formLayout.setColspan(line1, 3);
        formLayout.setColspan(line2, 3);
        formLayout.setColspan(line3, 3);
        formLayout.setColspan(chooseExistingShipmentLayout, 3);
        return formLayout;
    }

    private boolean isInvalidEntries() {
        return false;
    }
}
