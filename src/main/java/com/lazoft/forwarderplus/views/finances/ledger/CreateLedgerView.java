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
import com.vaadin.flow.component.notification.NotificationVariant;
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
import java.time.LocalDateTime;
import java.util.Random;

@PageTitle("Create Ledger")
@Route(value = "create-ledger", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN", "ACCOUNTS"})
public class CreateLedgerView extends Composite<VerticalLayout> {

    private final TextField ledgerName = new TextField("Ledger Name");
    private final TextField ledgerCode = new TextField("Ledger Code");

    private final BigDecimalField startingBalance = new BigDecimalField("Starting Balance");
    private final BigDecimalField currentBalance = new BigDecimalField("Current Balance");

    private final ComboBox<AmountCurrency> currency = new ComboBox<>("Currency");
    private final TextArea description = new TextArea("Description");

    private final Button createLedger = new Button("Create Ledger");

    private final LedgerService ledgerService;

    private User user;


    public CreateLedgerView(LedgerService ledgerService, AuthenticatedUser authenticatedUser) {
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
        H2 h32 = new H2();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(LumoUtility.Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        layoutColumn2.getStyle().set("flex-grow", "1");
        h3.setText("Create New Ledger");
        h3.setWidth("max-content");
        layoutColumn3.setWidth("450px");
        layoutColumn3.getStyle().set("flex-grow", "1");
        h32.setText("Booking Summary");
        h32.setWidth("max-content");
        getContent().add(layoutRow);
        layoutRow.add(layoutColumn2);
        Button reset = new Button("Reset");
        layoutColumn2.add(h3, formLayout, new HorizontalLayout(createLedger, reset));
        layoutRow.add(layoutColumn3);
    }

    private FormLayout setUpFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.add(ledgerName, ledgerCode, startingBalance, currentBalance, currency, description);
        formLayout.setColspan(description, 2);
        formLayout.setMaxWidth("75%");
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
