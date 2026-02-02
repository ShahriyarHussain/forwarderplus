package com.lazoft.forwarderplus.views.export.advice;

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
import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.time.LocalDate;
import java.util.*;

@Slf4j
public class EditScheduleDialog extends Dialog {

    private final ScheduleService scheduleService;
    private final ShipmentService shipmentService;
    private final PortService portService;

    private final ComboBox<Schedule> existingSchedule = new ComboBox<>("Choose Existing Schedule");

    private final TextField feederVessel = new TextField("Feeder Vessel");
    private final ComboBox<Port> portOfLoading = new ComboBox<>("Port of Loading");
    private final DatePicker polFeederEta = new DatePicker("Feeder ETA (PoL)");
    private final DatePicker polFeederEtd = new DatePicker("Feeder ETD (PoL)");
    private final DatePicker mvPortFeederEta = new DatePicker("Feeder ETA (Connect)");

    private final TextField motherVessel = new TextField("Mother Vessel");
    private final ComboBox<Port> motherVesselPort = new ComboBox<>("Mother Vessel/Connect Port");
    private final DatePicker motherVesselPortETA = new DatePicker("Mother Vessel/ Connect Port ETA");

    private final ComboBox<Port> portOfDestination = new ComboBox<>("Port of Destination");
    private final DatePicker etaPortOfDestination = new DatePicker("Destination ETA");

    private final TextField transshipmentVessel = new TextField("Transshipment Vessel");
    private final ComboBox<Port> transshipmentPort = new ComboBox<>("Transshipment Port");
    private final DatePicker transshipmentETA = new DatePicker("Transshipment ETA");

    private final Grid<Transshipment> grid = new Grid<>(Transshipment.class, false);

    private final Button addTransshipmentButton = new Button(LineAwesomeIcon.PLUS_CIRCLE_SOLID.create());
    private final Button clearAllBtn = new Button("Clear All");
    private final Button closeBtn = new Button("Close");
    private final Button saveButton = new Button("Save");
    private final Button selectSchedule = new Button(VaadinIcon.CHECK_CIRCLE.create());

    private final List<Transshipment> transshipmentList = new LinkedList<>();
    private final List<Transshipment> deletedTransshipmentList = new LinkedList<>();

    private final List<Port> portList = new LinkedList<>();
    private Schedule schedule;
    private final Shipment shipment;
    private final ShipmentAdviceDialog shipmentAdviceDialog;

    private int sl = 0;

    public EditScheduleDialog(PortService portService, ShipmentService shipmentService,
                              ScheduleService scheduleService, Shipment shipment,
                              ShipmentAdviceDialog shipmentAdviceDialog) {

        this.shipmentAdviceDialog = shipmentAdviceDialog;
        this.scheduleService = scheduleService;
        this.shipmentService = shipmentService;
        this.portService = portService;
        this.shipment = shipment;
        this.schedule = shipment.getSchedule();
        this.setHeaderTitle("Schedule Details");
        this.setWidth(1000, Unit.PIXELS);
        this.getFooter().add(closeBtn, clearAllBtn, saveButton);
        this.setCloseOnOutsideClick(false);

        sl = transshipmentList.size();
        prepareTransshipmentGrid();
        setAttributes();
        setExistingValues(schedule);
        setClickListeners();

        FormLayout formLayout = getScheduleEditForm();
        add(formLayout, new Hr(), grid);
    }

    private void setExistingValues(Schedule schedule) {
        if (schedule == null) {
            portOfLoading.setValue(shipment.getBooking().getLoadingPort());
            portOfDestination.setValue(shipment.getBooking().getDestinationPort());
            if (shipment.getStuffingDetails() == null) {
                return;
            }
            feederVessel.setValue(StringUtils.defaultString(shipment.getStuffingDetails().getVessel()));
            return;
        }

        feederVessel.setValue(StringUtils.defaultString(schedule.getPortOfLoadingVesselName()));

        portOfLoading.setValue(schedule.getPortOfLoading());
        polFeederEta.setValue(schedule.getPortOfLoadingETA());
        polFeederEtd.setValue(schedule.getPortOfLoadingETD());

        portOfDestination.setValue(schedule.getPortOfDestination());
        etaPortOfDestination.setValue(schedule.getPortOfDestinationETA());

        motherVessel.setValue(schedule.getMotherVesselName());
        motherVesselPort.setValue(schedule.getMotherVesselPort());
        motherVesselPortETA.setValue(schedule.getMotherVesselETA());

        transshipmentList.clear();
        transshipmentList.addAll(schedule.getTransshipments()
                .stream()
                .sorted(Comparator.comparing(Transshipment::getTransshipmentId))
                .toList()
        );
        updateTransshipmentSerial();

        grid.setItems(transshipmentList);
    }

    private void setExistingScheduleValues() {
        Booking shipmentBooking = shipment.getBooking();
        List<Schedule> schedules = scheduleService.getScheduleByPolAndPodAndDate(shipmentBooking.getLoadingPort(),
                shipmentBooking.getDestinationPort(), LocalDate.now().minusMonths(6));

        if (schedule != null && schedules.stream().noneMatch(item ->
                Objects.equals(item.getScheduleId(), schedule.getScheduleId()))) {
            schedules.add(schedule);
        }
        existingSchedule.setItems(schedules);
        existingSchedule.setValue(schedule);
    }

    private void setAttributes() {
        existingSchedule.setPlaceholder("Autofill with existing schedule");
        existingSchedule.setWidth("90%");
        existingSchedule.setItemLabelGenerator(Schedule::getScheduleSummary);

        transshipmentETA.setWidth("90%");

        selectSchedule.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTransshipmentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        clearAllBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
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

        polFeederEta.setLocale(Locale.UK);
        polFeederEtd.setLocale(Locale.UK);
        motherVesselPortETA.setLocale(Locale.UK);
        etaPortOfDestination.setLocale(Locale.UK);
        transshipmentETA.setLocale(Locale.UK);
        mvPortFeederEta.setLocale(Locale.UK);
    }

    private void setClickListeners() {
        addTransshipmentButton.addClickListener(event -> {
            Transshipment transshipment = new Transshipment();
            transshipment.setVesselPort(transshipmentPort.getValue());
            transshipment.setSl(sl++);
            transshipment.setPortEta(transshipmentETA.getValue());
            transshipment.setVesselName(transshipmentVessel.getValue());
            transshipmentList.add(transshipment);
            grid.setItems(transshipmentList);
        });

        selectSchedule.addClickListener(event -> {
            if (existingSchedule.getValue() == null) {
                NotificationUtil.getNotification("Please select a value first", "", false,
                        NotificationVariant.LUMO_WARNING, 4000).open();
            }
            Schedule selectedSchedule = existingSchedule.getValue();
            setExistingValues(selectedSchedule);
        });

        saveButton.addClickListener(event -> {
            try {
                if (isInvalidEntries()) {
                    NotificationUtil.getNotification("Please provide correct entries in required fields", "",
                            false, NotificationVariant.LUMO_WARNING, 3000).open();
                    return;
                }
                if (!deletedTransshipmentList.isEmpty() && deleteTransshipmentsFromPersistence()) return;

                setValuesToSchedule();
                shipmentService.addScheduleToShipment(shipment, schedule, transshipmentList);
                NotificationUtil.getNotification("Schedule Saved Successfully!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 3000).open();
            } catch (Exception e) {
                log.error("Error is saving container details", e);
                NotificationUtil.getNotification("Unexpected Error! Could not save data.", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 5000).open();
            }
        });

        closeBtn.addClickListener(event -> {
            shipmentAdviceDialog.fillUpExistingValues();
            close();
        });
    }

    private boolean deleteTransshipmentsFromPersistence() {
        try {
//            shipmentService.deleteTransshipments(deletedTransshipmentList);
            schedule.setTransshipments(transshipmentList);
            scheduleService.saveSchedule(schedule);
            shipmentService.deleteTransshipments(deletedTransshipmentList);
        } catch (Exception e) {
            NotificationUtil.getNotification("Failed to delete trans-shipments", e.getMessage(), true, NotificationVariant.LUMO_ERROR, 5000);
            transshipmentList.addAll(deletedTransshipmentList);
            updateTransshipmentSerial();
            return true;
        }
        return false;
    }

    private void updateTransshipmentSerial() {
        sl = 1;
        transshipmentList.forEach(ts -> ts.setSl(sl++));
    }


    private void setValuesToSchedule() {
        if (schedule == null) {
            schedule = new Schedule();
        }

        schedule.setPortOfLoadingVesselName(feederVessel.getValue());
        schedule.setPortOfLoadingETA(polFeederEta.getValue());
        schedule.setPortOfLoadingETD(polFeederEtd.getValue());
        schedule.setMvPortFeederEta(mvPortFeederEta.getValue());

        schedule.setPortOfLoading(portOfLoading.getValue());
        schedule.setPortOfDestination(portOfDestination.getValue());
        schedule.setPortOfDestinationETA(etaPortOfDestination.getValue());

        schedule.setMotherVesselName(motherVessel.getValue());
        schedule.setMotherVesselPort(motherVesselPort.getValue());
        schedule.setMotherVesselETA(motherVesselPortETA.getValue());

        schedule.setTransshipments(new LinkedList<>());
        updateTransshipmentSerial();
        schedule.getTransshipments().addAll(transshipmentList);
    }

    private void prepareTransshipmentGrid() {
        grid.addColumn("sl").setHeader("Sl No.").setAutoWidth(true).setSortable(false);
        grid.addColumn(transshipment -> transshipment.getVesselPort().getPortLabel()).setHeader("TS Port")
                .setAutoWidth(true).setSortable(false);
        grid.addColumn("vesselName").setHeader("Vessel").setAutoWidth(true).setSortable(false);
        grid.addColumn("portEta").setHeader("ETA To Port").setAutoWidth(true).setSortable(false);
        grid.addComponentColumn(transshipment -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(event -> {
                sl--;
                deletedTransshipmentList.add(transshipment);
                transshipmentList.remove(transshipment);
                updateTransshipmentSerial();
                grid.setItems(transshipmentList);
            });
            return deleteButton;
        }).setHeader("Delete");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        grid.setItems(transshipmentList);
    }

    private FormLayout getScheduleEditForm() {
        FormLayout formLayout = new FormLayout();

        HorizontalLayout chooseExistingShipmentLayout = new HorizontalLayout(existingSchedule, selectSchedule);
        chooseExistingShipmentLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        chooseExistingShipmentLayout.setAlignItems(FlexComponent.Alignment.END);

        HorizontalLayout transhipmentEtaAddLayout = new HorizontalLayout(transshipmentETA, addTransshipmentButton);
        transhipmentEtaAddLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        transhipmentEtaAddLayout.setAlignItems(FlexComponent.Alignment.END);

        Hr line1 = new Hr();
        Hr line2 = new Hr();
        Hr line3 = new Hr();
        Hr line4 = new Hr();

        formLayout.add(chooseExistingShipmentLayout,
                line1,
                portOfLoading, portOfDestination, etaPortOfDestination,
                line2,
                feederVessel, polFeederEta, polFeederEtd, mvPortFeederEta,
                line3,
                motherVessel, motherVesselPort, motherVesselPortETA,
                line4,
                transshipmentVessel, transshipmentPort, transhipmentEtaAddLayout);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));
        formLayout.setColspan(line1, 3);
        formLayout.setColspan(line2, 3);
        formLayout.setColspan(line3, 3);
        formLayout.setColspan(line4, 3);
        formLayout.setColspan(chooseExistingShipmentLayout, 3);
        return formLayout;
    }

    private boolean isInvalidEntries() {
        boolean isInvalid = false;

        if (polFeederEta.getValue() == null) {
            polFeederEta.setInvalid(true);
            polFeederEta.setErrorMessage("Please select a date");
            isInvalid = true;
        }
        if (polFeederEtd.getValue() == null) {
            polFeederEtd.setInvalid(true);
            polFeederEtd.setErrorMessage("Please select a date");
            return true;
        }
        if (polFeederEtd.getValue().isBefore(polFeederEta.getValue())) {
            polFeederEtd.setInvalid(true);
            polFeederEtd.setErrorMessage("Origin departure cannot be earlier than arrival");
            isInvalid = true;
        }
        if (portOfLoading.getValue() == null) {
            portOfLoading.setInvalid(true);
            portOfLoading.setErrorMessage("Please provide correct value");
            isInvalid = true;
        }
        if (portOfDestination.getValue() == null) {
            portOfDestination.setInvalid(true);
            portOfDestination.setErrorMessage("Please provide correct value");
            isInvalid = true;
        }
        if (etaPortOfDestination.getValue() == null) {
            etaPortOfDestination.setInvalid(true);
            etaPortOfDestination.setErrorMessage("Please provide correct value");
            return true;
        }
        if (etaPortOfDestination.getValue().isBefore(polFeederEta.getValue()) ||
                etaPortOfDestination.getValue().isBefore(etaPortOfDestination.getValue())) {
            etaPortOfDestination.setInvalid(true);
            etaPortOfDestination.setErrorMessage("Destination arrival cannot be earlier than origin arrival/departure");
            isInvalid = true;
        }
        if (StringUtils.isBlank(motherVessel.getValue())) {
            motherVessel.setInvalid(true);
            motherVessel.setErrorMessage("Must provide mother vessel name");
            isInvalid = true;
        }
        if (motherVesselPort.getValue() == null) {
            motherVesselPort.setInvalid(true);
            motherVesselPort.setErrorMessage("Must provide mother vessel port");
            isInvalid = true;
        }
        if (motherVesselPortETA.getValue() == null) {
            motherVesselPortETA.setInvalid(true);
            motherVesselPortETA.setErrorMessage("Please provide mother vessel ETA");
            isInvalid = true;
        }
        return isInvalid;
    }
}
