package com.lazoft.forwarderplus.views.report;

import com.lazoft.forwarderplus.components.filter.TransactionFilter;
import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.entity.Transaction;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.enums.Role;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.*;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Set;

@PageTitle("View Transactions")
@Route(value = "view-transaction", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "FINANCE"})
@Slf4j
public class ViewTransactions extends Div {




    private Grid<Transaction> grid;

    private final Button deleteButton = new Button("Delete", VaadinIcon.EXCLAMATION_CIRCLE.create());

    private final User user;

    private final TransactionFilter filters;

    public ViewTransactions(TransactionService transactionService, AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.get().isEmpty()) {
            NotificationUtil.getNotification("Session Lost. Reload page or login again", "", false,
                    NotificationVariant.LUMO_WARNING, 2000).open();
            this.user = null;
        } else {
            this.user = authenticatedUser.get().get();
        }
//        this.shipmentService = shipmentService;
//        this.reminderService = reminderService;

        deleteButton.getStyle().set("margin", "10px");
        deleteButton.setVisible(authenticatedUser.get().get().getRoles().contains(Role.ADMIN));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        deleteButton.addClickListener(event -> deleteShipments());

        setSizeFull();
        addClassNames("view-shipments-view");
        filters = new TransactionFilter(this::refreshGrid, transactionService);
        VerticalLayout layout = new VerticalLayout(filters, createGrid(), deleteButton);
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
//        this.bookingService = bookingService;
    }

    private Component createGrid() {
        grid = new Grid<>(Transaction.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addColumn(transaction -> transaction.getTransactionAccount() == null
                ? transaction.getTransactionLedger().getCode() + "(" + transaction.getTransactionLedger().getName() + ")" :
                transaction.getTransactionAccount().getAccountNo() + "(" + transaction.getTransactionAccount().getName() + ")")
                .setHeader("Booking No").setAutoWidth(true);
        grid.addColumn("type").setHeader("Transaction Type").setAutoWidth(true).setSortable(false);
        grid.addColumn("transactionMethod").setHeader("Transaction Method").setAutoWidth(true).setSortable(false);
        grid.addColumn("currency").setHeader("Currency").setAutoWidth(true);
        grid.addColumn("businessDate").setHeader("Business Date").setAutoWidth(true);
        grid.addColumn("transactionDate").setHeader("Transaction Date").setAutoWidth(true);
        grid.addColumn("conversionRate").setHeader("Conversion Rate").setAutoWidth(true);
        grid.addColumn("totalAmount").setHeader("Amount").setAutoWidth(true);
        grid.addColumn("totalAmountBaseCurrency").setHeader("Amount (BDT)").setAutoWidth(true);

//        grid.setItems(query -> shipmentService.getShipmentsByFilter(
//                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
//                filters).stream());
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

//        List<Booking> bookings = selectedShipments.stream().map(Shipment::getBooking).distinct().toList();

        try {
//            shipmentService.deleteShipments(selectedShipments);
            NotificationUtil.getNotification("Deleted Successfully!", "", false, NotificationVariant.LUMO_PRIMARY, 2000).open();
        } catch (Exception e) {
            NotificationUtil.getNotification("Error while deleting", "", true, NotificationVariant.LUMO_ERROR, 4000).open();
        }
        refreshGrid();
    }

    private Button getReminderCreationButton(Shipment shipment) {
        Button create = new Button(VaadinIcon.EDIT.create());
        create.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
//        create.addClickListener(event -> new ReminderCreationDialog(reminderService, user, shipment.getShipmentId()).open());
        return create;
    }


    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }


}
