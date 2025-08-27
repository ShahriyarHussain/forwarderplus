package com.lazoft.forwarderplus.views.viewshipments;

import com.lazoft.forwarderplus.components.dialog.ReminderCreationDialog;
import com.lazoft.forwarderplus.components.filter.ShipmentFilter;
import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.enums.Role;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.*;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.weaver.ast.Not;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.format.DateTimeFormatter;
import java.util.Set;

@PageTitle("Search & View Shipments")
@Route(value = "view-shipments", layout = MainLayout.class)
@RolesAllowed("USER")
@Uses(Icon.class)
public class ViewShipmentsView extends Div {

    private final ShipmentService shipmentService;
    private final ReminderService reminderService;
    private Grid<Shipment> grid;

    private final Button deleteButton = new Button("Delete", VaadinIcon.EXCLAMATION_CIRCLE.create());

    private final User user;

    private final ShipmentFilter filters;

    public ViewShipmentsView(PortService portService, ShipmentService shipmentService, ReminderService reminderService,
                             CarrierService carrierService, AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.get().isEmpty()) {
            NotificationUtil.getNotification("Session Lost. Reload page or login again", "", false,
                    NotificationVariant.LUMO_WARNING, 2000).open();
            this.user = null;
        } else {
            this.user = authenticatedUser.get().orElseThrow(() -> new NotFoundException("User not found"));
        }
        this.shipmentService = shipmentService;
        this.reminderService = reminderService;

        deleteButton.getStyle().set("margin", "10px");
        deleteButton.setVisible(authenticatedUser.get().orElseThrow(() -> new NotFoundException("Username not found")).getRoles().contains(Role.ADMIN));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        deleteButton.addClickListener(event -> deleteShipments());

        setSizeFull();
        addClassNames("view-shipments-view");
        filters = new ShipmentFilter(this::refreshGrid, portService, carrierService);
        VerticalLayout layout = new VerticalLayout(filters, createGrid(), deleteButton);
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private Component createGrid() {
        grid = new Grid<>(Shipment.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addColumn(shipment -> shipment.getBooking().getBookingNo()).setHeader("Booking No").setAutoWidth(true);
        grid.addColumn("hblNo").setHeader("House B/L No").setAutoWidth(true).setSortable(false);
        grid.addColumn("mblNo").setHeader("Master B/L No").setAutoWidth(true).setSortable(false);
        grid.addColumn("clientInvoiceNo").setAutoWidth(true);
        grid.addColumn(shipment -> StringUtils.truncate(shipment.getShipper().getName(), 35)).setHeader("Shipper").setAutoWidth(true);
        grid.addColumn(shipment -> {
            Booking booking = shipment.getBooking();
            return booking.getLoadingPort().getPortCityAndCountry() + " -> " + booking.getDestinationPort().getPortCityAndCountry();
        }).setHeader("Route").setAutoWidth(true).setSortable(false);
        grid.addComponentColumn(shipment -> {
            Span statusBadge = new Span(shipment.getStatus().getStatus());
            statusBadge.getElement().getThemeList().add("badge");
            statusBadge.getStyle().setBackgroundColor(shipment.getStatus().getColor());
            statusBadge.getStyle().setColor("beige");
            statusBadge.getStyle().set("font-weight", "bold");
            return statusBadge;
        }).setHeader("Status").setAutoWidth(true).setSortable(true);
        grid.addColumn(shipment -> shipment.getCreatedBy().getUsername()).setHeader("Created By").setAutoWidth(true);
        grid.addColumn(shipment -> shipment.getCreatedOn().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy 'T' hh:mm:ss")))
                .setHeader("Created On").setAutoWidth(true).setSortable(true);
        grid.addComponentColumn(this::getReminderCreationButton).setTextAlign(ColumnTextAlign.CENTER)
                .setHeader("Edit Advice").setAutoWidth(true);

        grid.setItems(query -> shipmentService.getShipmentsByFilter(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void deleteShipments() {
        Set<Shipment> selectedShipments = grid.getSelectedItems();
        if (selectedShipments.isEmpty()) {
            NotificationUtil.getNotification("No Items Selected!", "", false, NotificationVariant.LUMO_WARNING, 2000).open();
            return;
        }

        try {
            shipmentService.deleteShipments(selectedShipments);
            NotificationUtil.getNotification("Deleted Successfully!", "", false, NotificationVariant.LUMO_PRIMARY, 2000).open();
        } catch (Exception e) {
            NotificationUtil.getNotification("Error while deleting", "", true, NotificationVariant.LUMO_ERROR, 4000).open();
        }
        refreshGrid();
    }

    private Button getReminderCreationButton(Shipment shipment) {
        Button create = new Button(VaadinIcon.EDIT.create());
        create.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        create.addClickListener(event -> new ReminderCreationDialog(reminderService, user, shipment.getShipmentId()).open());
        return create;
    }


    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}
