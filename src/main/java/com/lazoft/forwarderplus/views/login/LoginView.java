package com.lazoft.forwarderplus.views.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

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
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }
}
