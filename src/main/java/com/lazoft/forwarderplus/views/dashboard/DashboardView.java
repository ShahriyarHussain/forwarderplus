package com.lazoft.forwarderplus.views.dashboard;

import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.PermitAll;

@PageTitle("Dashboard")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class DashboardView extends Composite<VerticalLayout> {

    public DashboardView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        H3 h3 = new H3();
        VerticalLayout layoutColumn3 = new VerticalLayout();
        VerticalLayout layoutColumn4 = new VerticalLayout();
        H5 h5 = new H5();
        ProgressBar progressBar = new ProgressBar();
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        VerticalLayout layoutColumn5 = new VerticalLayout();
        H5 h52 = new H5();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("flex-grow", "1");
        layoutColumn2.setWidth("100%");
        layoutColumn2.getStyle().set("flex-grow", "1");
        h3.setText("Summary");
        h3.setWidth("max-content");
        layoutColumn3.setWidth("450px");
        layoutColumn3.getStyle().set("flex-grow", "1");
        layoutColumn4.setWidthFull();
        layoutColumn3.setFlexGrow(1.0, layoutColumn4);
        layoutColumn4.setWidth("100%");
        layoutColumn4.setHeight("min-content");
        h5.setText("Today's Progress");
        h5.setWidth("max-content");
        progressBar.setValue(0.5);
        checkboxGroup.setLabel("Checkbox Group");
        checkboxGroup.setWidth("100%");
        checkboxGroup.setItems("Order ID", "Product Name", "Customer", "Status");
        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        layoutColumn5.setWidthFull();
        layoutColumn3.setFlexGrow(1.0, layoutColumn5);
        layoutColumn5.setWidth("100%");
        layoutColumn5.getStyle().set("flex-grow", "1");
        h52.setText("Notifications");
        h52.setWidth("max-content");
        getContent().add(layoutRow);
        layoutRow.add(layoutColumn2);
        layoutColumn2.add(h3);
        layoutRow.add(layoutColumn3);
        layoutColumn3.add(layoutColumn4);
        layoutColumn4.add(h5);
        layoutColumn4.add(progressBar);
        layoutColumn4.add(checkboxGroup);
        layoutColumn3.add(layoutColumn5);
        layoutColumn5.add(h52);
    }
}
