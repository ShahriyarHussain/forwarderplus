package com.lazoft.forwarderplus.views.finances.account;

import com.lazoft.forwarderplus.components.dialog.AccountLedgerTagDialog;
import com.lazoft.forwarderplus.entity.Account;
import com.lazoft.forwarderplus.entity.LedgerTagInfo;
import com.lazoft.forwarderplus.services.finance.impl.AccountService;
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
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class EditAccountDialog extends Dialog {

    public EditAccountDialog(AccountService accountService, LedgerService ledgerService, Account account) {
        List<LedgerTagInfo> taggedLedgers = new LinkedList<>(account.getTaggedLedgers());

        H3 h3 = new H3("Edit Account");
        TextField accountName = new TextField("Account Name");
        accountName.setValue(account.getName());
        TextField accountNo = new TextField("Account No");
        accountNo.setValue(account.getAccountNo());
        accountNo.setReadOnly(true);
        TextArea description = new TextArea("Description");
        description.setValue(account.getFinancialDetails());
        TextField currency = new TextField("Currency");
        currency.setValue(account.getAmountCurrency().getCurrencyAndSymbol());
        currency.setReadOnly(true);
        BigDecimalField currentBalance = new BigDecimalField("Current Balance", account.getCurrentBalance(), "0.00");
        currentBalance.setReadOnly(true);
        BigDecimalField startingBalance = new BigDecimalField("Starting Balance", account.getStartingBalance(), "0.00");
        startingBalance.setReadOnly(true);
        Button tagLedger = new Button("Tag Ledger");
        Button save = new Button("Save");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.add(accountName, accountNo, currency, currentBalance, startingBalance, tagLedger, description);
        formLayout.setColspan(accountName, 2);
        formLayout.setColspan(description, 2);
        formLayout.setWidth("fit-content");

        save.addClickListener(event -> {
            account.setName(accountName.getValue());
            account.setFinancialDetails(description.getValue());
            account.setTaggedLedgers(taggedLedgers);
            account.setUpdatedAt(LocalDateTime.now());

            try {
                accountService.saveAccount(account);
                NotificationUtil.getNotification("Saved Successfully", "", false,
                        NotificationVariant.LUMO_PRIMARY, 4000).open();
            } catch (Exception e) {
                log.error("Error while saving account", e);
                NotificationUtil.getNotification("Failed To Save", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 6000).open();
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        tagLedger.addClickListener(event -> new AccountLedgerTagDialog(taggedLedgers, ledgerService).open());
        tagLedger.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button closeButton = new Button("Close", event -> this.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        this.getHeader().add(h3);
        this.add(formLayout);
        this.setWidth(700, Unit.PIXELS);
        this.getFooter().add(save, closeButton);
    }
}
