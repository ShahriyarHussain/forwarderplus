package com.lazoft.forwarderplus.views.configviews;

import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.enums.Role;
import com.lazoft.forwarderplus.services.UserService;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;


public class EditUserDialog extends Dialog {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final User selectedUser;
    private final UserConfigurationView userConfigurationView;

    private final TextField username = new TextField("Username");
    private final TextField fullname = new TextField("Full Name");
    private final TextField email = new TextField("Email Field");
    private final TextField designation = new TextField("Designation");
    private final PasswordField password = new PasswordField("Temporary Password For Reset");
    private final CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
    private final MultiSelectComboBox<Role> roles = new MultiSelectComboBox<>("Roles");
    private final Button save = new Button("Save");


    public EditUserDialog(UserService userService, PasswordEncoder passwordEncoder, User selectedUser,
                          UserConfigurationView userConfigurationView) {
        this.passwordEncoder = passwordEncoder;
        this.selectedUser = selectedUser;
        this.userService = userService;
        this.userConfigurationView = userConfigurationView;
        this.setWidth(800, Unit.PIXELS);

        setExistingValues();
        setSaveButtonListener();
        setHeaderTitle("Edit User");

        FormLayout formLayout = new FormLayout();
        formLayout.add(username, email, fullname, designation, checkboxGroup, password, roles);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        add(formLayout);
        getFooter().add(save, new Button("Cancel", event -> {
            userConfigurationView.refreshGrid();
            this.close();
        }));
    }

    private void setExistingValues() {
        username.setReadOnly(true);
        username.setValue(selectedUser.getUsername());
        fullname.setValue(selectedUser.getName());
        email.setValue(selectedUser.getEmail());
        designation.setValue(selectedUser.getDesignation());
        password.setRevealButtonVisible(true);

        checkboxGroup.setLabel("Actions");
        checkboxGroup.setItems("Lock User?", "Disable User?");
        if (!selectedUser.isUserNotLocked()) {
            checkboxGroup.select("Lock User?");
        }
        if (!selectedUser.isNotTerminated()) {
            checkboxGroup.select("Disable User?");
        }
        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        roles.setItems(Role.values());
        roles.setValue(selectedUser.getRoles());
    }

    private void setSaveButtonListener() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(event -> {
            selectedUser.setName(fullname.getValue());
            selectedUser.setEmail(email.getValue());
            selectedUser.setDesignation(designation.getValue());
            selectedUser.setUserNotLocked(!checkboxGroup.getSelectedItems().contains("Lock User?"));
            selectedUser.setNotTerminated(!checkboxGroup.getSelectedItems().contains("Disable User?"));
            selectedUser.setRoles(roles.getSelectedItems());

            if (StringUtils.isNotBlank(password.getValue())) {
                selectedUser.setHashedPassword(passwordEncoder.encode(password.getValue()));
            }
            selectedUser.setModifiedOn(LocalDateTime.now());
            userService.update(selectedUser);

            NotificationUtil.getNotification("User updated successfully!", "", false,
                    NotificationVariant.LUMO_PRIMARY, 4000).open();
        });
    }
}
