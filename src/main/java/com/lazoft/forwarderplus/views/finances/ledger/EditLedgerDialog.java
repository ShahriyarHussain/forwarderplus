package com.lazoft.forwarderplus.views.finances.ledger;

import com.lazoft.forwarderplus.entity.Ledger;
import com.lazoft.forwarderplus.services.LedgerService;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class EditLedgerDialog extends Dialog {

    public EditLedgerDialog(LedgerService ledgerService, Ledger ledger) {

        H3 h3 = new H3("Edit Ledger");
        TextField ledgerName = new TextField("Ledger Name");
        ledgerName.setValue(ledger.getName());
        TextField ledgerCode = new TextField("Ledger Code");
        ledgerCode.setValue(ledger.getCode());
        ledgerCode.setReadOnly(true);
        TextArea description = new TextArea("Description");
        description.setValue(ledger.getDescription());
        TextField currency = new TextField("Currency");
        currency.setValue(ledger.getCurrency().getCurrencyAndSymbol());
        BigDecimalField currentBalance = new BigDecimalField("Current Balance", ledger.getCurrentBalance(), "0.00");
        currentBalance.setReadOnly(true);
        BigDecimalField startingBalance = new BigDecimalField("Starting Balance", ledger.getStartingBalance(), "0.00");
        startingBalance.setReadOnly(true);
        currency.setReadOnly(true);
        Button save = new Button("Save");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.add(ledgerName, ledgerCode, currency, currentBalance, startingBalance, description);
        formLayout.setColspan(ledgerName, 2);
        formLayout.setColspan(description, 2);
        formLayout.setWidth("fit-content");

        save.addClickListener(event -> {
            ledger.setName(ledgerName.getValue());
            ledger.setDescription(description.getValue());
            ledger.setUpdatedOn(LocalDateTime.now());

            try {
                ledgerService.saveLedger(ledger);
                NotificationUtil.getNotification("Saved Successfully", "", false,
                        NotificationVariant.LUMO_PRIMARY, 4000).open();
            } catch (Exception e) {
                log.error("Error while saving account", e);
                NotificationUtil.getNotification("Failed To Save", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 6000).open();
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button closeButton = new Button("Close", event -> this.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        this.getHeader().add(h3);
        this.add(formLayout);
        this.setWidth(700, Unit.PIXELS);
        this.getFooter().add(save, closeButton);
    }
}
