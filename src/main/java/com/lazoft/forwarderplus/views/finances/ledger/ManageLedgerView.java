package com.lazoft.forwarderplus.views.finances.ledger;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.LedgerService;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@PageTitle("Create Ledger")
@Route(value = "create-ledger", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN", "ACCOUNTS"})
public class ManageLedgerView extends Composite<VerticalLayout> {

    private final TextField ledgerName = new TextField("Ledger Name");
    private final TextField ledgerCode = new TextField("Ledger Code");

    private final BigDecimalField startingBalance = new BigDecimalField("Starting Balance");
    private final BigDecimalField currentBalance = new BigDecimalField("Current Balance");

    private final ComboBox<AmountCurrency> currency = new ComboBox<>("Currency");
    private final TextArea description = new TextArea("Description");

    private final Button createLedger = new Button("Create New Ledger");

    private final Tabs ledgerManageTabs = new Tabs();

    private final Tab createLedgerTab = new Tab("Create Ledger");
    private final VerticalLayout createLedgerVerticalLayout = new VerticalLayout();

    private final Tab editLedgerTab = new Tab("Edit Ledgers");
    private final VerticalLayout editLedgerVerticalLayout = new VerticalLayout();

    private final LedgerService ledgerService;

    private User user;


    public ManageLedgerView(LedgerService ledgerService, AuthenticatedUser authenticatedUser) {
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
        createLedgerVerticalLayout.add(new VerticalLayout(new H3("Create Ledger"), formLayout, new HorizontalLayout(createLedger, reset)));
        editLedgerVerticalLayout.add(new ManageLedgerLayout(ledgerService, user));
        setFieldAttributes();
        setListeners();

        HorizontalLayout layoutRow = new HorizontalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(LumoUtility.Gap.MEDIUM);
        layoutRow.setWidth("75%");
        layoutRow.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        getContent().add(layoutRow);
        layoutRow.add(layoutColumn2);
        layoutColumn2.add(ledgerManageTabs, createLedgerVerticalLayout, editLedgerVerticalLayout);

    }

    private FormLayout setUpFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.add(ledgerName, ledgerCode, startingBalance, currentBalance, currency, description);
        formLayout.setColspan(description, 2);
        formLayout.setMaxWidth("100%");
        return formLayout;
    }

    private void setFieldAttributes() {
        ledgerName.setRequired(true);
        ledgerCode.setRequired(true);

        startingBalance.setRequired(true);
        startingBalance.setValue(BigDecimal.ZERO);

        currentBalance.setRequired(true);
        currentBalance.setValue(BigDecimal.ZERO);

        currency.setRequired(true);
        currency.setItems(AmountCurrency.values());
        currency.setValue(AmountCurrency.BDT);

        createLedger.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        ledgerManageTabs.add(createLedgerTab, editLedgerTab);
        ledgerManageTabs.setSelectedTab(createLedgerTab);
        createLedgerVerticalLayout.setVisible(true);
        editLedgerVerticalLayout.setVisible(false);
    }

    private void setListeners() {
        createLedger.addClickListener((event) -> {
            if (isInvalidData()) {
                return;
            }
            try {
                Ledger ledger = createLedgerFromData();
                ledgerService.saveLedger(ledger);
                NotificationUtil.getNotification("Ledger Created Successfully", "", false,
                        NotificationVariant.LUMO_PRIMARY, 4000).open();
            } catch (Exception e) {
                NotificationUtil.getNotification("Failed To Create Ledger.", e.getMessage(), true,
                        NotificationVariant.LUMO_PRIMARY, 6000).open();
            }
        });

        ledgerCode.addBlurListener(e -> {
            if (StringUtils.isBlank(e.getSource().getValue())) {
                return;
            }
            if (ledgerService.ledgerAlreadyExistsByCode(ledgerCode.getValue())) {
                ledgerCode.setInvalid(true);
                ledgerCode.setErrorMessage("A Ledger with this ledger code already exists!");
            }
        });

        ledgerManageTabs.addSelectedChangeListener(e -> {
            createLedgerVerticalLayout.setVisible(createLedgerTab.isSelected());
            editLedgerVerticalLayout.setVisible(editLedgerTab.isSelected());
        });
    }

    private Ledger createLedgerFromData() {
        Ledger ledger = new Ledger();
        ledger.setName(ledgerName.getValue());
        ledger.setCode(ledgerCode.getValue());
        ledger.setStartingBalance(startingBalance.getValue());
        ledger.setCurrentBalance(currentBalance.getValue());
        ledger.setCurrency(currency.getValue());
        ledger.setDescription(description.getValue());
        ledger.setCreatedOn(LocalDateTime.now());
        return ledger;
    }

    private boolean isInvalidData() {
        boolean isInvalid = false;
        if (StringUtils.isBlank(ledgerName.getValue())) {
            ledgerName.setInvalid(true);
            ledgerName.setErrorMessage("Ledger Name is required");
            isInvalid = true;
        }
        if (StringUtils.isBlank(ledgerCode.getValue())) {
            ledgerCode.setInvalid(true);
            ledgerCode.setErrorMessage("Ledger Code is required");
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
