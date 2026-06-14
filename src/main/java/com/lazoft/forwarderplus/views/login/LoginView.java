package com.lazoft.forwarderplus.views.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.*;
import org.springframework.security.web.WebAttributes;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        addClassName("login-view");
        addClassName("lumo-base-color");
        setSizeFull();

        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        login.setAction("login");
        Button registerButton = new Button("Create New Account");
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        registerButton.addClickListener(e ->
                registerButton.getUI().ifPresent(ui ->
                        ui.navigate("register"))
        );
        login.setForgotPasswordButtonVisible(false);
        login.addForgotPasswordListener(e -> registerButton.getUI().ifPresent(ui -> ui.navigate("register")));

        add(new H1("Forwarder+"), login, registerButton);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            LoginI18n i18n = LoginI18n.createDefault();
            LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();

            VaadinServletRequest request = VaadinServletRequest.getCurrent();
            if (request != null) {
                HttpSession session = request.getHttpServletRequest().getSession(false);
                if (session != null) {
                    Exception exception = (Exception) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

                    if (exception != null) {
                        switch (exception) {
                            case BadCredentialsException badCredentialsException -> {
                                errorMessage.setTitle("Incorrect Credentials");
                                errorMessage.setMessage("Please check your username and password and try again.");
                            }
                            case AccountExpiredException accountExpiredException -> {
                                errorMessage.setTitle("Account Terminated");
                                errorMessage.setMessage("Your account has been terminated.");
                            }
                            case LockedException lockedException -> {
                                errorMessage.setTitle("Account is Locked");
                                errorMessage.setMessage("Your account has been locked due to too many failed attempts.");
                            }
                            case DisabledException disabledException -> {
                                errorMessage.setTitle("Account Not Active");
                                errorMessage.setMessage("This account is not activated yet. Please contact administrator.");
                            }
                            case CredentialsExpiredException credentialsExpiredException -> {
                                errorMessage.setTitle("Password Expired");
                                errorMessage.setMessage("Your password has expired. Please contact administrator to reset.");
                            }
                            default -> {
                                errorMessage.setTitle("Login Failed");
                                errorMessage.setMessage("An unexpected error occurred: " + exception.getMessage());
                            }
                        }
                    }
                }
            }

            i18n.setErrorMessage(errorMessage);
            login.setI18n(i18n);
            login.setError(true);
        }
    }
}
