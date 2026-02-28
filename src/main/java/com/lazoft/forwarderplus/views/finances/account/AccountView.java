package com.lazoft.forwarderplus.views.finances.account;

import com.lazoft.forwarderplus.components.dialog.AccountLedgerTagDialog;
import com.lazoft.forwarderplus.entity.Account;
import com.lazoft.forwarderplus.entity.LedgerTagInfo;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.LedgerService;
import com.lazoft.forwarderplus.services.finance.impl.AccountService;
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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@PageTitle("Accounts")
@Route(value = "accounts", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "FINANCE"})
@Slf4j
public class AccountView extends Composite<VerticalLayout> {

    private final TextField accountName = new TextField("Account Name");
    private final TextField accountNo = new TextField("Account No");

    private final BigDecimalField startingBalance = new BigDecimalField("Starting Balance");
    private final BigDecimalField currentBalance = new BigDecimalField("Current Balance");

    private final ComboBox<AmountCurrency> currency = new ComboBox<>("Currency");
    private final TextArea description = new TextArea("Description");
    private final Button tagLedger = new Button("Tag Ledger");
    private final Button createAccount = new Button("Create Account");

    private final Tabs accountManageTabs = new Tabs();

    private final Tab createAccountTab = new Tab("Create Account");
    private final VerticalLayout createAccountVerticalLayout = new VerticalLayout();

    private final Tab editAccountTab = new Tab("Manage Accounts");
    private final VerticalLayout editAccountVerticalLayout = new VerticalLayout();


    private final AccountService accountService;
    private final LedgerService ledgerService;
    private User user;
    private final List<LedgerTagInfo> taggedLedgers = new LinkedList<>();


    public AccountView(AccountService accountService, LedgerService ledgerService, AuthenticatedUser authenticatedUser) {
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
        Button reset = new Button("Reset");
        createAccountVerticalLayout.add(new VerticalLayout(new H3("Create Account"), formLayout,
                new HorizontalLayout(createAccount, reset)));
        editAccountVerticalLayout.add(new ManageAccountLayout(accountService, ledgerService));
        setFieldAttributes();
        setListeners();

        HorizontalLayout layoutRow = new HorizontalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(LumoUtility.Gap.MEDIUM);
        layoutRow.setWidth("80%");
        layoutRow.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        getContent().add(layoutRow);
        layoutRow.add(layoutColumn2);
        layoutColumn2.add(accountManageTabs, createAccountVerticalLayout, editAccountVerticalLayout);
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

        createAccount.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        tagLedger.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        accountManageTabs.add(createAccountTab, editAccountTab);
        accountManageTabs.setSelectedTab(createAccountTab);

        createAccountVerticalLayout.setVisible(true);
        editAccountVerticalLayout.setVisible(false);
    }

    private void setListeners() {
        createAccount.addClickListener(event -> {
            if (isInvalidData()) {
                return;
            }
            try {
                Account account = createAccountFromData();
                accountService.saveAccount(account);
                NotificationUtil.getNotification("Account Created Successfully", "", false,
                        NotificationVariant.LUMO_PRIMARY, 4000).open();
            } catch (Exception e) {
                log.error("Error while saving account", e);
                NotificationUtil.getNotification("Failed To Create Account.", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 6000).open();
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

        tagLedger.addClickListener(event -> new AccountLedgerTagDialog(taggedLedgers, ledgerService).open());

        accountManageTabs.addSelectedChangeListener(e -> {
            createAccountVerticalLayout.setVisible(createAccountTab.isSelected());
            editAccountVerticalLayout.setVisible(editAccountTab.isSelected());
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
        account.setAmountCurrency(currency.getValue());
        account.setCreatedAt(LocalDateTime.now());
        return account;
    }

    private boolean isInvalidData() {
        boolean isInvalid = false;
        if (StringUtils.isBlank(accountName.getValue())) {
            accountName.setInvalid(true);
            accountName.setErrorMessage("Account Name is required");
            isInvalid = true;
        }
        if (StringUtils.isBlank(accountNo.getValue())) {
            accountNo.setInvalid(true);
            accountNo.setErrorMessage("Account Code is required");
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
