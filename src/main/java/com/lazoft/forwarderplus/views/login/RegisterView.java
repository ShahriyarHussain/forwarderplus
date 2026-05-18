package com.lazoft.forwarderplus.views.login;


import com.lazoft.forwarderplus.builder.PopUpMessageBuilder;
import com.lazoft.forwarderplus.dto.RegisterUserDto;
import com.lazoft.forwarderplus.enums.CountryCode;
import com.lazoft.forwarderplus.service.RegisterService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

@AnonymousAllowed
@PageTitle("Register")
@Route("register")
public class RegisterView extends VerticalLayout {

    private final RegisterService registerService;

    private final EmailField emailField = new EmailField("Email");
    private final TextField userNameField = new TextField("Username");
    private final TextField fullNameField = new TextField("Full Name");
    private final TextField designationField = new TextField("Designation");
    private final ComboBox<CountryCode> countryCodeComboBox = new ComboBox<>("Country Code:");
    private final TextField contactNo = new TextField("Contact Number");
    private final PasswordField passwordField = new PasswordField("Password");
    private final PasswordField confirmPasswordField = new PasswordField("Confirm Password");
    private final Button registerButton = new Button("Register");
    private final Button loginButton = new Button("Back To Login");


    @Value("${dev.env}")
    private boolean devEnvironment;

    public RegisterView(RegisterService registerService) {
        this.registerService = registerService;

        addClassName("lumo-base-color");
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        H1 title = new H1("Forwarder+");
        title.getStyle().setMarginBottom("20px");
        title.addClassName("lumo-title");

        H2 formTitle = new H2("Create New Account");
        formTitle.getStyle().setMarginBottom("5px");

        setCountryCodeBehaviour();
        setRegisterButtonBehaviour();
        setLoginButtonBehaviour();
        setUserNameBehaviour();
        setPasswordFieldsBehaviour();
        setEmailBehaviour();
        setFullNameBehaviour();
        contactNo.setPattern("[0-9]+");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.setColspan(formTitle, 2);
        formLayout.setMaxWidth("40%");
        formLayout.getStyle().setPadding("20px");
        formLayout.getStyle().setBackgroundColor("#F6F5EF");
        formLayout.getStyle().setBorderRadius("10px");
        formLayout.add(
                formTitle,
                emailField, userNameField,
                fullNameField, designationField,
                countryCodeComboBox, contactNo,
                passwordField, confirmPasswordField
        );

        setAlignSelf(Alignment.CENTER, formLayout);
        add(title, formLayout, new HorizontalLayout(registerButton, loginButton));
    }


    //------------ field behaviours ------//
    private void setCountryCodeBehaviour() {
        countryCodeComboBox.setItems(CountryCode.values());
        countryCodeComboBox.setItemLabelGenerator(countryCodes ->
                countryCodes.getCountryName() + " (" + countryCodes.getCountryCode() + ")");
        countryCodeComboBox.setWidth("40%");
        countryCodeComboBox.addValueChangeListener(event ->
                contactNo.setPrefixComponent(new H5(event.getValue().getCountryCode()))
        );
    }

    private void setUserNameBehaviour() {
        userNameField.setRequired(true);
        userNameField.addBlurListener(event ->
                validateUserNameField(event.getSource()));
    }

    private void setPasswordFieldsBehaviour() {
        passwordField.setRevealButtonVisible(true);
        confirmPasswordField.setRevealButtonVisible(true);
        confirmPasswordField.addBlurListener(event ->
                validatePassword(passwordField, confirmPasswordField));
    }

    private void setEmailBehaviour() {
        emailField.setRequired(true);
        emailField.setPattern("^[A-Za-z0-9._-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        emailField.addBlurListener(event ->
                validateEmailField(event.getSource())
        );
    }

    private void setFullNameBehaviour() {
        fullNameField.setRequired(true);
        fullNameField.addBlurListener(
                event -> validateFullName(event.getSource())
        );
    }

    private void setRegisterButtonBehaviour() {
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(event -> performUserRegistrationOperations());
    }

    private void setLoginButtonBehaviour() {
        loginButton.addClickListener(event ->
                event.getSource().getUI().ifPresent(ui -> ui.navigate("login"))
        );
    }

    //------------ Ops -------------------//
    private void performUserRegistrationOperations() {
        if (!isAllFieldsValid()) {
            return;
        }
        registerService.registerUser(new RegisterUserDto(
                userNameField.getValue(),
                fullNameField.getValue(),
                emailField.getValue(),
                passwordField.getValue(),
                countryCodeComboBox.getValue(),
                contactNo.getValue(),
                designationField.getValue()
        ));
        showSuccessDialog().open();
    }


    //------------- validations ---------------//
    private boolean isAllFieldsValid() {
        if (emailField.isInvalid() || userNameField.isInvalid() || fullNameField.isInvalid() || passwordField.isInvalid() ||  confirmPasswordField.isInvalid()) {
            PopUpMessageBuilder.builder()
                    .message("Please correct invalid fields")
                    .variant(NotificationVariant.LUMO_ERROR)
                    .isExpandable(false)
                    .duration(3000).build()
                    .generatePopUp()
                    .open();
            return false;
        }
        return true;
    }

    private void validatePassword(PasswordField passwordField, PasswordField confirmPasswordField) {
        if (StringUtils.isBlank(passwordField.getValue())) {
            passwordField.setInvalid(true);
            confirmPasswordField.setInvalid(true);
            confirmPasswordField.setErrorMessage("password cannot be blank");
            return;
        }
        if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            confirmPasswordField.setErrorMessage("Passwords do not match");
            confirmPasswordField.setInvalid(true);
            return;
        }
        if (!registerService.isStrongPassword(passwordField.getValue())) {
            passwordField.setInvalid(true);
            passwordField.setErrorMessage("Password must have minimum 6 characters, 1 upper case character & 1 number");
            return;
        }
        confirmPasswordField.setInvalid(false);
    }

    private void validateUserNameField(TextField userNameField) {
        if (StringUtils.containsWhitespace(userNameField.getValue())) {
            userNameField.setInvalid(true);
            userNameField.setErrorMessage(userNameField.getLabel() + " must not contain spaces");
            return;
        }
        if (registerService.isUserNameExists(userNameField.getValue())) {
            userNameField.setInvalid(true);
            userNameField.setErrorMessage(userNameField.getLabel() + " already in use");
            return;
        }
        userNameField.setInvalid(false);
    }

    private void validateEmailField(EmailField emailField) {
        if (StringUtils.containsWhitespace(emailField.getValue())) {
            emailField.setInvalid(true);
            emailField.setErrorMessage(emailField.getLabel() + " invalid email address");
            return;
        }
        if (registerService.isUserNameExists(emailField.getValue())) {
            emailField.setInvalid(true);
            emailField.setErrorMessage(emailField.getLabel() + " already in use");
            return;
        }
        emailField.setInvalid(false);
    }

    private void validateFullName(TextField fullNameField) {
        if (StringUtils.isBlank(fullNameField.getValue())) {
            fullNameField.setInvalid(true);
            fullNameField.setErrorMessage("Invalid Name");
            return;
        }
        fullNameField.setInvalid(false);
    }

    //------------ Popup Dialogs---------------//
    private ConfirmDialog showSuccessDialog() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("User Created Successfully!");

        confirmDialog.setText(new H5("You can now login. You may need approval from ADMIN to unlock account"));
        confirmDialog.setCancelable(false);
        confirmDialog.setConfirmButton(new Button("OK", confirmEvent -> confirmEvent.getSource().getUI()
                .ifPresent(ui -> ui.navigate("login"))));
        return confirmDialog;
    }
}
