package com.lazoft.forwarderplus.views.accounting.chart;

import com.lazoft.forwarderplus.builder.PopUpMessageBuilder;
import com.lazoft.forwarderplus.entity.finance.Account;
import com.lazoft.forwarderplus.enums.AccountType;
import com.lazoft.forwarderplus.exception.ValidationException;
import com.lazoft.forwarderplus.service.finance.AccountService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChartOfAccountsCreationDialog extends Dialog {

    private final ChartOfAccountsView chartOfAccountsView;
    private final AccountService accountService;

    private final TextField nameTextField = new TextField("Account name");
    private final ComboBox<AccountType> typeComboBox = new ComboBox<>("Account type");
    private final TextField remarks = new TextField("Remarks");
    private final Button saveButton = new Button("Save");
    private final Button closeButton = new Button("close");
    private final FormLayout formLayout = new FormLayout();

    public ChartOfAccountsCreationDialog(ChartOfAccountsView chartOfAccountsView,
                                         AccountService accountService) {
        this.chartOfAccountsView = chartOfAccountsView;
        this.accountService = accountService;

        setFormLayout();
        setComponentActions();
        setComponentProperties();

        loadData();
        add(formLayout);
    }

    private void loadData() {
        typeComboBox.setItems(AccountType.values());
    }

    private void setComponentProperties() {
        this.setHeaderTitle("Create Account");
        this.getFooter().add(saveButton, closeButton);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
    }

    private void setFormLayout() {
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.add(nameTextField, typeComboBox, remarks);
        formLayout.setMaxWidth("100%");
    }

    private void setComponentActions() {
        saveButton.addClickListener(event -> {
            try {
                addAccount();
                chartOfAccountsView.refreshGrid();
                PopUpMessageBuilder.builder()
                        .message("Account created successfully")
                        .variant(NotificationVariant.LUMO_PRIMARY)
                        .duration(2000)
                        .build()
                        .generatePopUp().open();
            } catch (ValidationException ex) {
                PopUpMessageBuilder.builder().message(ex.getMessage())
                        .duration(3000)
                        .variant(NotificationVariant.LUMO_WARNING)
                        .build()
                        .generatePopUp().open();
            } catch (Exception ex) {
                PopUpMessageBuilder.builder().message("Unexpected Error Occurred!")
                        .isExpandable(true)
                        .expandedMessage(ex.getMessage())
                        .duration(5000)
                        .variant(NotificationVariant.LUMO_ERROR)
                        .build()
                        .generatePopUp().open();
                log.error(ex.getMessage(), ex);
            }
        });
        closeButton.addClickListener(e -> this.close());
    }

    private void addAccount() {
        Account account = new Account();
        account.setName(nameTextField.getValue());
        account.setAccountType(typeComboBox.getValue());
        account.setRemarks(remarks.getValue());
        accountService.addAccount(account);
        chartOfAccountsView.refreshGrid();
    }
}
