package com.lazoft.forwarderplus.views.finances.account;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.AccountService;
import com.lazoft.forwarderplus.services.LedgerService;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

@PageTitle("Create Account")
@Route(value = "create-account", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN", "ACCOUNTS"})
public class CreateAccountView extends Composite<VerticalLayout> {

    private final TextField accountName = new TextField("Account Name");
    private final TextField accountNo = new TextField("Ledger Code");

    private final BigDecimalField startingBalance = new BigDecimalField("Starting Balance");
    private final BigDecimalField currentBalance = new BigDecimalField("Current Balance");

    private final ComboBox<AmountCurrency> currency = new ComboBox<>("Currency");
    private final TextArea description = new TextArea("Description");
    private final Button tagLedger = new Button("Tag Ledger");
    private final Button createLedger = new Button("Create Ledger");


    private final AccountService accountService;
    private final LedgerService ledgerService;
    private User user;
    private Account account;
    private final List<LedgerTagInfo> taggedLedgers = new LinkedList<>();


    public CreateAccountView(AccountService accountService, LedgerService ledgerService, AuthenticatedUser authenticatedUser) {
        this.accountService = accountService;
        this.ledgerService = ledgerService;

        if (authenticatedUser.get().isPresent()) {
            user = authenticatedUser.get().get();
        } else {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setText("User session expired! Please login again");
            dialog.setCancelable(false);
            dialog.setConfirmButton(new Button("Logout", event -> authenticatedUser.logout()));
        }

        FormLayout formLayout = setUpFormLayout();
        setFieldAttributes();
        setListeners();

        HorizontalLayout layoutRow = new HorizontalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        VerticalLayout layoutColumn3 = new VerticalLayout();
        H3 h3 = new H3();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(LumoUtility.Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        h3.setText("Create Account");
        h3.setWidth("max-content");
        layoutColumn3.setWidth("450px");
        layoutColumn3.getStyle().set("flex-grow", "1");
        getContent().add(layoutRow);
        layoutRow.add(layoutColumn2);
        Button reset = new Button("Reset");
        layoutColumn2.add(h3, formLayout, new HorizontalLayout(createLedger, reset));
        layoutRow.add(layoutColumn3);
    }

    private FormLayout setUpFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        HorizontalLayout currencyAndTagBtnLayout = new HorizontalLayout(currency, tagLedger);
        currencyAndTagBtnLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        currencyAndTagBtnLayout.setAlignItems(FlexComponent.Alignment.END);

        formLayout.add(accountName, accountNo, startingBalance, currentBalance, currencyAndTagBtnLayout, description);
        formLayout.setColspan(accountName, 2);
        formLayout.setColspan(description, 2);
        formLayout.setMaxWidth("75%");
        return formLayout;
    }

    private void setFieldAttributes() {
        accountName.setRequired(true);
        accountNo.setRequired(true);

        startingBalance.setRequired(true);
        startingBalance.setValue(BigDecimal.ZERO);

        currentBalance.setRequired(true);
        currentBalance.setValue(BigDecimal.ZERO);

        currency.setRequired(true);
        currency.setItems(AmountCurrency.values());
        currency.setValue(AmountCurrency.BDT);
        currency.setWidth("80%");

        createLedger.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        tagLedger.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
    }

    private void setListeners() {
        createLedger.addClickListener((event) -> {
            if (isInvalidData()) {
                return;
            }
            try {
                Account account = createAccountFromData();
                accountService.save(account);
                NotificationUtil.getNotification("Account Created Successfully", "", false,
                        NotificationVariant.LUMO_PRIMARY, 4000).open();
            } catch (Exception e) {
                NotificationUtil.getNotification("Failed To Create Account.", e.getMessage(), true,
                        NotificationVariant.LUMO_PRIMARY, 6000).open();
            }
        });

        accountNo.addBlurListener(e -> {
            if (StringUtils.isBlank(e.getSource().getValue())) {
                return;
            }
            if (accountService.accountAlreadyExists(accountNo.getValue())) {
                accountNo.setInvalid(true);
                accountNo.setErrorMessage("A Ledger with this ledger code already exists!");
            }
        });

        tagLedger.addClickListener(event -> {
            account = createAccountFromData();
            new AccountLedgerTagDialog(taggedLedgers, ledgerService).open();
        });
    }

    private Account createAccountFromData() {
        Account account = new Account();
        account.setAccountNo(accountNo.getValue());
        account.setName(accountName.getValue());
        account.setCurrentBalance(currentBalance.getValue());
        account.setStartingBalance(startingBalance.getValue());
        account.setFinancialDetails(description.getValue());
        account.setTaggedLedgers(taggedLedgers);
        return account;
    }

    private boolean isInvalidData() {
        boolean isInvalid = false;
        if (StringUtils.isBlank(accountName.getValue())) {
            accountName.setInvalid(true);
            accountName.setErrorMessage("Ledger Name is required");
            isInvalid = true;
        }
        if (StringUtils.isBlank(accountNo.getValue())) {
            accountNo.setInvalid(true);
            accountNo.setErrorMessage("Ledger Code is required");
            isInvalid = true;
        }
        if (startingBalance.getValue() == null) {
            startingBalance.setInvalid(true);
            startingBalance.setErrorMessage("Starting Balance is required");
            isInvalid = true;
        }
        if (currentBalance.getValue() == null) {
            currentBalance.setInvalid(true);
            currentBalance.setErrorMessage("Current Balance is required");
            isInvalid = true;
        }
        if (currency.getValue() == null) {
            currency.setInvalid(true);
            currency.setErrorMessage("currency is required");
            isInvalid = true;
        }
        return isInvalid;
    }


}
