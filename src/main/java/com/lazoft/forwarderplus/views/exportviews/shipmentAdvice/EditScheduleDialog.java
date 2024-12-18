package com.lazoft.forwarderplus.views.exportviews.shipmentAdvice;

import com.lazoft.forwarderplus.entity.ContainerDetails;
import com.lazoft.forwarderplus.entity.Port;
import com.lazoft.forwarderplus.entity.Schedule;
import com.lazoft.forwarderplus.entity.Transshipment;
import com.lazoft.forwarderplus.enums.PackageUnit;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.vaadin.flow.component.Text;
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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class EditScheduleDialog extends Dialog {

    private final Set<Transshipment> transshipmentSet = new HashSet<>();

    private final ComboBox<Schedule> existingSchedule = new ComboBox<>("Choose Existing Schedule");

    private final TextField feederVessel = new TextField("Feeder Vessel");

    private final ComboBox<Port> portOfLoading = new ComboBox<>("Port of Loading");
    private final DatePicker etaPortOfLoading = new DatePicker("Port of Loading ETA");
    private final DatePicker etdPortOfLoading = new DatePicker("Port of Loading ETD");

    private final TextField motherVessel = new TextField("Mother Vessel");
    private final ComboBox<Port> motherVesselPort = new ComboBox<>("Mother Vessel Port");
    private final DatePicker motherETAoConnect = new DatePicker("Mother Vessel Connect Port ETA");

    private final ComboBox<Port> portOfDestination = new ComboBox<>("Port of Destination");
    private final DatePicker etaPortOfDestination = new DatePicker("Port of Destination ETA");

    private final Button addPortButton = new Button(VaadinIcon.PLUS_CIRCLE_O.create());

    private final TextField transshipmentVessel = new TextField("Transshipment Vessel");
    private final ComboBox<Port> transshipmentPort = new ComboBox<>("Transshipment Port");
    private final DatePicker transshipmentETA = new DatePicker("Transshipment ETA");

    private final Button addTransshipmentButton = new Button(VaadinIcon.CHECK_CIRCLE.create());
    private final Button clearAllBtn = new Button("Clear All");

    private final Button saveButton = new Button("Save");

    public EditScheduleDialog() {
        this.setHeaderTitle("Schedule Details");
        this.setWidth(800, Unit.PIXELS);
        this.getFooter().add(clearAllBtn, new Button("Close"), saveButton);

        setAttributes();

        Grid<Transshipment> grid = getContainerDetailsGrid();
        FormLayout formLayout = getScheduleEditForm();

        add(formLayout, new Hr(), grid);
    }

    private void setAttributes() {
        existingSchedule.setHelperText("Autofill with existing schedule");
        addTransshipmentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        clearAllBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
    }

    private Grid<Transshipment> getContainerDetailsGrid() {
        Grid<Transshipment> grid = new Grid<>(Transshipment.class, false);
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
            //transshipment.setVesselPort(transshipmentPort.getValue());
            transshipment.setPortEta(transshipmentETA.getValue());
            transshipment.setVesselName(transshipmentVessel.getValue());
            transshipmentSet.add(transshipment);
            grid.setItems(transshipmentSet);
        });

            return grid;
    }

    private FormLayout getScheduleEditForm() {
        FormLayout formLayout = new FormLayout();

        HorizontalLayout transhipmentEtaAddLayout = new HorizontalLayout(transshipmentETA, addTransshipmentButton);
        transhipmentEtaAddLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        transhipmentEtaAddLayout.setAlignItems(FlexComponent.Alignment.END);

        Hr line1 = new Hr(), line2 = new Hr(), line3 = new Hr();
        formLayout.add(existingSchedule,
                line1,
                feederVessel, etaPortOfLoading, etdPortOfLoading,
                portOfLoading, portOfDestination, etaPortOfDestination,
                line2,
                motherVessel, motherVesselPort, motherETAoConnect,
                line3,
                transshipmentVessel, transshipmentPort, transhipmentEtaAddLayout);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));
        formLayout.setColspan(line1, 3);
        formLayout.setColspan(line2, 3);
        formLayout.setColspan(line3, 3);
        formLayout.setColspan(existingSchedule, 2);
        return formLayout;
    }

    private boolean isInvalidEntries() {
//        if (grossWeight.getValue() == null || noOfPackages.getValue() == 0 || packageUnitComboBox.getValue() == null) {
//            NotificationUtil.getNotification("Please provide correct weight, quantity and unit",
//                    "", false, NotificationVariant.LUMO_WARNING, 4000).open();
//            return true;
//        }
//        if (StringUtils.isBlank(containerNo.getValue()) || StringUtils.isBlank(sealNo.getValue())) {
//            NotificationUtil.getNotification("Container No or Seal No is empty!",
//                    "", false, NotificationVariant.LUMO_WARNING, 4000).open();
//            return true;
//        }
        return false;
    }

}
