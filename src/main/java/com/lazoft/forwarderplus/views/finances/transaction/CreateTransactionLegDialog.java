package com.lazoft.forwarderplus.views.finances.transaction;

import com.lazoft.forwarderplus.entity.Transaction;
import com.lazoft.forwarderplus.entity.TransactionLeg;
import com.lazoft.forwarderplus.services.TransactionService;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class CreateTransactionLegDialog extends Dialog {

    private final List<TransactionLeg> transactionLegs;
    private final CreateTransactionView createTransactionView;
    private int serialNo = 1;

    private final TextField remarks = new TextField("Remarks");
    private final BigDecimalField amount = new BigDecimalField("Amount");
    private final Button addLegButton = new Button(LineAwesomeIcon.PLUS_CIRCLE_SOLID.create());

    Grid<TransactionLeg> grid = new Grid<>(TransactionLeg.class);

    private final Button saveButton = new Button("Save");
    private final Button close = new Button("Close");


    public CreateTransactionLegDialog(List<TransactionLeg> transactionLegs, CreateTransactionView createTransactionView) {
        this.transactionLegs = transactionLegs;
        this.createTransactionView = createTransactionView;
        this.setHeaderTitle("Add Transaction Legs");
        this.setWidth(500, Unit.PIXELS);
        this.setCloseOnOutsideClick(false);
        this.serialNo = transactionLegs.size() + 1;

        grid = getTransactionLegGrid();
        FormLayout formLayout = getTransactionLegForm();
        setAttributes();
        fillUpExistingValues();
        setListeners();

        this.getFooter().add(close, saveButton);
        add(formLayout, grid);
    }

    private void fillUpExistingValues() {
        if (transactionLegs == null || transactionLegs.isEmpty()) {
            return;
        }
        grid.setItems(transactionLegs);
    }

    private void setAttributes() {
        addLegButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        close.addThemeVariants(ButtonVariant.LUMO_ERROR);
        amount.setWidth("40%");
        remarks.setWidth("50%");
        addLegButton.setWidth("10%");
    }

    private FormLayout getTransactionLegForm() {
        FormLayout formLayout = new FormLayout();
        HorizontalLayout amountLayout = new HorizontalLayout();
        amountLayout.add(amount, remarks, addLegButton);
        amountLayout.setAlignItems(FlexComponent.Alignment.END);
        amountLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        formLayout.add(amountLayout);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));
        formLayout.setColspan(amountLayout, 3);
        return formLayout;
    }

    private void setListeners() {
        addLegButton.addClickListener(event -> {
           if (amount.getValue() == null || amount.getValue().equals(BigDecimal.ZERO)) {
               NotificationUtil.getNotification("Amount cannot be Zero or Empty", "", false,
                       NotificationVariant.LUMO_WARNING, 3000).open();
               return;
           }
           TransactionLeg leg = new TransactionLeg();
           leg.setSlNo(serialNo++);
           leg.setAmount(amount.getValue());
           leg.setRemarks(remarks.getValue());
           transactionLegs.add(leg);
           grid.setItems(transactionLegs);
        });
        saveButton.addClickListener(event -> {
            createTransactionView.updateAmountByTransactionLegs();
            close();
        });
    }

    private Grid<TransactionLeg> getTransactionLegGrid() {
        Grid<TransactionLeg> grid = new Grid<>(TransactionLeg.class, false);
        grid.addColumn("slNo").setHeader("Serial No").setAutoWidth(true).setSortable(false);
        grid.addColumn("amount").setHeader("Amount").setAutoWidth(true).setSortable(false);
        grid.addColumn("remarks").setHeader("Remarks").setAutoWidth(true).setSortable(false);
        grid.addComponentColumn(containerDetails -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(event -> {
                transactionLegs.remove(containerDetails);
                grid.setItems(transactionLegs);
                serialNo--;
            });
            return deleteButton;
        }).setHeader("Delete");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        return grid;
    }
}
