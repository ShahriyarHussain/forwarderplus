package com.lazoft.forwarderplus.views.blmanager;

import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("B/L Manager")
@Route(value = "bl-manager", layout = MainLayout.class)
@RolesAllowed("USER")
public class BLManagerView extends Composite<VerticalLayout> {

    public BLManagerView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
