package com.lazoft.forwarderplus.views.login;


import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.enums.CountryCodes;
import com.lazoft.forwarderplus.enums.Role;
import com.lazoft.forwarderplus.services.UserService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@AnonymousAllowed
@PageTitle("Register")
@Route("register")
public class RegisterView extends VerticalLayout {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    private final EmailField email = new EmailField("Email");
    private final TextField userName = new TextField("Username");
    private final TextField fullName = new TextField("Full Name");
    private final TextField designation = new TextField("Designation");
    private final ComboBox<CountryCodes> country = new ComboBox<>("Country Code:");
    private final TextField contactNumber = new TextField("Contact Number");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirmPassword = new PasswordField("Confirm Password");
    private final Button registerButton = new Button("Register");
    private final Button loginButton = new Button("Back To Login");

    public RegisterView(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;

        addClassName("lumo-base-color");
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        H1 title = new H1("Forwarder+");
        title.getStyle().setMarginBottom("20px");
        title.addClassName("lumo-title");

        setComponentAttributes();
        setEventListeners();

        H2 formTitle = new H2("Create New Account");
        formTitle.getStyle().setMarginBottom("5px");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.setColspan(formTitle, 2);
        formLayout.add(formTitle, email, userName, fullName, designation, country, contactNumber, new Text(""), password, confirmPassword);
        formLayout.setMaxWidth("40%");
        formLayout.getStyle().setPadding("20px");
        formLayout.getStyle().setBackgroundColor("#F6F5EF");
        formLayout.getStyle().setBorderRadius("10px");

        setAlignSelf(Alignment.CENTER, formLayout);
        add(title, formLayout, new HorizontalLayout(registerButton, loginButton));
    }

    private void setComponentAttributes() {
        country.setItems(CountryCodes.values());
        country.setItemLabelGenerator(countryCodes ->
                countryCodes.getCountryName() + " (" + countryCodes.getCountryCode() + ")");
        country.setWidth("40%");
        contactNumber.setPattern("[0-9]+");
        password.setRevealButtonVisible(true);
        confirmPassword.setRevealButtonVisible(true);
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        userName.setRequired(true);
        email.setRequired(true);
        fullName.setRequired(true);
        password.setRequired(true);
        confirmPassword.setRequired(true);
    }

    private void setEventListeners() {
        country.addValueChangeListener(event ->
                contactNumber.setPrefixComponent(new H5(event.getValue().getCountryCode())));

        registerButton.addClickListener(event -> {
            if (!isAllFieldsValid()) {
                return;
            }
            User user = new User();
            user.setUsername(userName.getValue());
            user.setName(fullName.getValue());
            user.setEmail(email.getValue());
            user.setHashedPassword(passwordEncoder.encode(password.getValue()));
            user.setContactNo(country.getValue().getCountryCode() + contactNumber.getValue());
            user.setDesignation(designation.getValue());
            user.setRoles(Set.of(Role.USER));
            userService.create(user);
            Notification notification = new Notification();
            notification.setText("User Registered!");
            notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            notification.setDuration(4000);
                notification.setPosition(Notification.Position.TOP_START);
            notification.open();
            registerButton.getUI().ifPresent(ui ->
                    ui.navigate("login"));
        });

        loginButton.addClickListener(event -> registerButton.getUI().ifPresent(ui -> ui.navigate("login")));

        userName.addBlurListener(event -> {
            if (userName.isInvalid()) {
                return;
            }
            if (userService.get(event.getSource().getValue()).isPresent()) {
                userName.setInvalid(true);
                userName.setErrorMessage("username already in use");
                return;
            }
            email.setInvalid(false);
        });

        email.addBlurListener(event -> {
            if (email.isInvalid()) {
                return;
            }
            if (userService.existsByEmail(event.getSource().getValue())) {
                email.setInvalid(true);
                email.setErrorMessage("Email already in use");
                return;
            }
            email.setInvalid(false);
        });

        fullName.addBlurListener(event -> {
            if (StringUtils.isBlank(fullName.getValue())) {
                fullName.setInvalid(true);
                fullName.setErrorMessage("Invalid Name");
            } else {
                fullName.setInvalid(false);
            }
        });

        confirmPassword.addBlurListener(event -> validatePassword());
    }

    private boolean isAllFieldsValid() {
        if (email.isInvalid()) {
            email.setErrorMessage("Invalid email");
            return false;
        }
        if (userName.isInvalid()) {
            userName.setErrorMessage("Invalid username");
            return false;
        }
        if (fullName.isInvalid()) {
            fullName.setErrorMessage("Invalid name");
            return false;
        }
        return !password.isInvalid() && !confirmPassword.isInvalid();
    }

    private void validatePassword() {
        if (StringUtils.isBlank(password.getValue())) {
            password.setInvalid(true);
            confirmPassword.setInvalid(true);
            confirmPassword.setErrorMessage("Passwords do not match");
        } else if (!password.getValue().equals(confirmPassword.getValue())) {
            confirmPassword.setErrorMessage("Passwords do not match");
            confirmPassword.setInvalid(true);
        } else if (password.getValue().matches("")) {
            password.setInvalid(true);
        } else if (!isStrongPassword()) {
            password.setInvalid(true);
        } else {
            confirmPassword.setInvalid(false);
        }
    }

    private boolean isStrongPassword() {
        String pass = password.getValue();
        if (!email.isInvalid() && !userName.isInvalid() && !fullName.isInvalid()
                && StringUtils.containsAnyIgnoreCase(pass, userName.getValue(), fullName.getValue(), email.getValue(),
                email.getValue().substring(0, email.getValue().indexOf('@')))) {
            password.setInvalid(true);
            password.setErrorMessage("Password cannot contain parts of name, username, or email");
            return false;
        }
        if (pass.length() < 6) {
            password.setInvalid(true);
            password.setErrorMessage("Password length must be at least 6 characters");
            return false;
        }

        boolean lower = false, upper = false, numeric = false, special = false;
        for (char c : pass.toCharArray()) {
            if (CharUtils.isAsciiAlphaLower(c)) {
                lower = true;
                continue;
            }
            if (CharUtils.isAsciiNumeric(c)) {
                upper = true;
                continue;
            }
            if (CharUtils.isAsciiAlphaUpper(c)) {
                numeric = true;
                continue;
            }
            if (CharUtils.isAscii(c)) {
                special = true;
            }
        }

        if (!(lower && upper && numeric && special)) {
            password.setInvalid(true);
            password.setErrorMessage("Password must have 1 uppercase, 1 lowercase, 1 number and 1 special character");
            return false;
        }

        return true;
    }
}
