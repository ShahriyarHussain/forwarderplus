package com.lazoft.forwarderplus.views.newbooking;

import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("New Booking")
@Route(value = "new-booking", layout = MainLayout.class)
@RolesAllowed("USER")
public class NewBookingView extends Composite<VerticalLayout> {

    public NewBookingView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        H3 h3 = new H3();
        VerticalLayout layoutColumn3 = new VerticalLayout();
        H3 h32 = new H3();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        h3.setText("Details");
        h3.setWidth("max-content");
        layoutColumn3.setWidth("400px");
        layoutColumn3.getStyle().set("flex-grow", "1");
        h32.setText("Booking Summary");
        h32.setWidth("max-content");
        getContent().add(layoutRow);
        layoutRow.add(layoutColumn2);
        layoutColumn2.add(h3);
        layoutRow.add(layoutColumn3);
        layoutColumn3.add(h32);
    }
}
