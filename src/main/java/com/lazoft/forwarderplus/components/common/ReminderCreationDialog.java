package com.lazoft.forwarderplus.components.common;

import com.lazoft.forwarderplus.entity.Reminder;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.enums.ReminderVisibility;
import com.lazoft.forwarderplus.services.ReminderService;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ReminderCreationDialog extends Dialog {
    private final ReminderService reminderService;

    private final TextArea reminderMessage = new TextArea("Reminder Message");
    private final DatePicker reminderDate = new DatePicker("Reminder Date");
    private final ComboBox<ReminderVisibility> reminderVisibility = new ComboBox<>("Visibility (Who can see?)");

    private final Grid<Reminder> grid = new Grid<>(Reminder.class, false);
    private final Button saveButton = new Button("Add");
    private final Button close = new Button("Close");
    private final Button clearAll = new Button("Clear All");
    private final List<Reminder> reminderList = new LinkedList<>();
    private final User user;
    private final long shipmentId;

    public ReminderCreationDialog(ReminderService reminderService, User user, long shipmentId) {
        this.reminderService = reminderService;
        this.user = user;
        this.shipmentId = shipmentId;

        this.setHeaderTitle("Container Details");
        this.setWidth(900, Unit.PIXELS);
        this.getFooter().add(close, clearAll, saveButton);
        this.setCloseOnOutsideClick(false);

        setReminderDetailsGrid();
        loadReminderData();
        FormLayout formLayout = getReminderFormLayout();

        setAttributes();
        setListeners();
        fillUpExistingValues();
        add(formLayout, grid);
    }

    private void loadReminderData() {
        reminderList.clear();
        reminderList.addAll(reminderService.getRemindersByDateAndRoles(LocalDate.now(), user));
    }

    private void fillUpExistingValues() {
        grid.setItems(reminderList);
        reminderVisibility.setItems(ReminderVisibility.values());
    }

    private void setAttributes() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        close.addThemeVariants(ButtonVariant.LUMO_ERROR);
        clearAll.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
    }

    private void setListeners() {
        saveButton.addClickListener(event -> {
            try {
                if (isInvalidEntries()) {
                    NotificationUtil.getNotification("Please provide correct values",
                            "", false, NotificationVariant.LUMO_WARNING, 4000).open();
                    return;
                }
                Reminder reminder = new Reminder();
                reminder.setRemindOn(reminderDate.getValue());
                reminder.setCompleted(false);
                reminder.setMessage(reminderMessage.getValue());
                reminder.setVisibility(reminderVisibility.getValue());
                reminder.setCreatedBy(user);
                reminder.setReferenceId(shipmentId);
                Reminder savedReminder = reminderService.saveData(reminder);
                reminderList.add(savedReminder);
                grid.setItems(reminderList);
                NotificationUtil.getNotification("Saved Successfully!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 3000).open();
            } catch (Exception e) {
                log.error("Error is saving container details", e);
                NotificationUtil.getNotification("Unexpected Error! Could not save data.", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 5000).open();
            }
        });
    }

    private void setReminderDetailsGrid() {
        grid.addColumn("message").setHeader("Message").setAutoWidth(true).setSortable(false);
        grid.addColumn("visibility").setHeader("Visibility").setAutoWidth(true).setSortable(false);
        grid.addColumn("remindOn").setHeader("Remind On").setAutoWidth(true).setSortable(false);
        grid.addColumn("completed").setHeader("Completed").setAutoWidth(true).setSortable(false);
        grid.addComponentColumn(reminder -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(event -> {
                reminderService.remove(reminder);
                reminderList.remove(reminder);
                grid.setItems(reminderList);
            });
            return deleteButton;
        }).setHeader("Delete");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
    }

    private FormLayout getReminderFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(reminderMessage, reminderVisibility, reminderDate);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.setColspan(reminderMessage, 2);
        return formLayout;
    }

    private boolean isInvalidEntries() {
        boolean isInvalid = false;
        if (reminderVisibility.getValue() == null) {
            reminderVisibility.setInvalid(true);
            reminderVisibility.setErrorMessage("Must provide a value");
            isInvalid = true;
        }
        if (reminderDate.getValue() == null) {
            reminderDate.setInvalid(true);
            reminderDate.setErrorMessage("Cannot be null");
            isInvalid = true;
        }
        if (StringUtils.isBlank(reminderMessage.getValue())) {
            reminderMessage.setInvalid(true);
            reminderMessage.setErrorMessage("Must provide container no.");
            isInvalid = true;
        }
        return isInvalid;
    }
}
