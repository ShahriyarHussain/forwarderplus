package com.lazoft.forwarderplus.views.finances.account;

import com.lazoft.forwarderplus.entity.Ledger;
import com.lazoft.forwarderplus.entity.LedgerTagInfo;
import com.lazoft.forwarderplus.enums.LedgerTransactionType;
import com.lazoft.forwarderplus.services.LedgerService;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import lombok.extern.slf4j.Slf4j;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.List;

@Slf4j
public class AccountLedgerTagDialog extends Dialog {

    private final List<LedgerTagInfo> taggedLedgers;
    private final LedgerService ledgerService;


    private final ComboBox<Ledger> findLedger = new ComboBox<>("Find Ledger");
    private final Button findLedgerButton = new Button(LineAwesomeIcon.SEARCH_SOLID.create());
    private final TextField ledgerName = new TextField("Ledger Name");
    private final TextField ledgerCode = new TextField("Ledger Code");

    private final BigDecimalField startingBalance = new BigDecimalField("Starting Balance");
    private final BigDecimalField currentBalance = new BigDecimalField("Current Balance");

    private final TextField currency = new TextField("Currency");
    private final ComboBox<LedgerTransactionType> ledgerTransactionType = new ComboBox<>("On Account Transaction");
    private final Button tagLedgerButton = new Button("Tag");

    private final Grid<LedgerTagInfo> ledgerGrid = new Grid<>(LedgerTagInfo.class, false);

    private final Button saveButton = new Button("Save");
    private final Button close = new Button("Close");

    private Ledger selectedLedger;

    public AccountLedgerTagDialog(List<LedgerTagInfo> taggedLedgers, LedgerService ledgerService) {

        this.taggedLedgers = taggedLedgers;
        this.ledgerService = ledgerService;
        this.setHeaderTitle("Tag Ledger for Account: ");
        this.setWidth(800, Unit.PIXELS);
        this.getFooter().add(close, saveButton);
        this.setCloseOnOutsideClick(false);

        setTaggedLedgerGridProperties();
        setAttributes();
        setListeners();
        fillUpExistingValues();

        HorizontalLayout findLedgerLayout = new HorizontalLayout(findLedger, findLedgerButton);
        findLedgerLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        findLedgerLayout.setAlignItems(FlexComponent.Alignment.END);

        FormLayout formLayout = getLedgerDetailsLayout();
        add(findLedgerLayout, formLayout, ledgerGrid);
    }

    private void fillUpExistingValues() {
        ledgerGrid.setItems(taggedLedgers);
    }

    private void setAttributes() {
        ledgerName.setReadOnly(true);
        ledgerCode.setReadOnly(true);
        startingBalance.setReadOnly(true);
        currency.setReadOnly(true);
        currentBalance.setReadOnly(true);
        tagLedgerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        findLedger.setItemLabelGenerator(ledger -> ledger.getName() + " (" + ledger.getCode() + ")");
        findLedger.setItems(ledgerService.getAllLedgers());

        findLedgerButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        findLedgerButton.setTooltipText("Find Ledger");

        ledgerTransactionType.setItems(LedgerTransactionType.values());
        ledgerTransactionType.setItemLabelGenerator(LedgerTransactionType::getTitle);
        ledgerTransactionType.setWidth("90%");

        close.addThemeVariants(ButtonVariant.LUMO_ERROR);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    private void setListeners() {
        saveButton.addClickListener(event -> {
            try {
                NotificationUtil.getNotification(taggedLedgers.size() + " Ledgers Tagged Successfully!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 3000).open();
            } catch (Exception e) {
                log.error("Error is saving container details", e);
                NotificationUtil.getNotification("Unexpected Error! Could not save data.", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 5000).open();
            }
        });

        findLedgerButton.addClickListener(event -> setLedgerValuesAfterFinding());
        findLedger.addBlurListener(event -> setLedgerValuesAfterFinding());

        tagLedgerButton.addClickListener(event -> {
            if (isInvalidEntries()) {
                return;
            }
            LedgerTagInfo info = new LedgerTagInfo();
            info.setLedger(selectedLedger);
            info.setTransactionType(ledgerTransactionType.getValue());
            taggedLedgers.add(info);
            ledgerGrid.setItems(taggedLedgers);
        });

        close.addClickListener(event -> close());
    }

    private void setLedgerValuesAfterFinding() {
        if (findLedger.getValue() == null) {
            clearLedgerDetailsFields();
            NotificationUtil.getNotification("Please provide a valid ledger code", "", false,
                    NotificationVariant.LUMO_WARNING, 4000).open();
            return;
        }
//        Optional<Ledger> ledger = ledgerService.getLedgerByCode(findLedger.getValue());
//        if (ledger.isPresent()) {

            selectedLedger = findLedger.getValue();
            setLedgerValuesOnSelection();
//            return;
//        }
//        NotificationUtil.getNotification("No ledger found with code: " + findLedger.getValue(), "", false,
//                NotificationVariant.LUMO_WARNING, 4000).open();
    }

    private void setLedgerValuesOnSelection() {
        ledgerName.setValue(selectedLedger.getName());
        ledgerCode.setValue(selectedLedger.getCode());
        currency.setValue(selectedLedger.getCurrency().getCurrencyName());
        startingBalance.setValue(selectedLedger.getStartingBalance());
        currentBalance.setValue(selectedLedger.getCurrentBalance());
    }

    private void clearLedgerDetailsFields() {
        ledgerName.clear();
        ledgerCode.clear();
        currency.clear();
        startingBalance.clear();
        currentBalance.clear();
    }

    private void setTaggedLedgerGridProperties() {
        ledgerGrid.addColumn(info -> info.getLedger().getName() + "(" + info.getLedger().getCode() + ")")
                .setHeader("Ledger").setAutoWidth(true).setSortable(false);
        ledgerGrid.addColumn(info -> info.getTransactionType().getTitle()).setHeader("Transaction Behavior").setAutoWidth(true).setSortable(false);
        ledgerGrid.addComponentColumn(info -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(event -> {
                taggedLedgers.remove(info);
                ledgerGrid.setItems(taggedLedgers);
            });
            return deleteButton;
        }).setHeader("Delete");

        ledgerGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        ledgerGrid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
    }

    private FormLayout getLedgerDetailsLayout() {
        FormLayout formLayout = new FormLayout();
        HorizontalLayout tranTypeAndAddLedgerButton = new HorizontalLayout(ledgerTransactionType, tagLedgerButton);
        tranTypeAndAddLedgerButton.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        tranTypeAndAddLedgerButton.setAlignItems(FlexComponent.Alignment.END);

        formLayout.add(ledgerName, ledgerCode, startingBalance, currentBalance,
                currency, tranTypeAndAddLedgerButton);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        return formLayout;
    }

    private boolean isInvalidEntries() {
        if (selectedLedger == null) {
            NotificationUtil.getNotification("Please select a ledger", "", false,
                    NotificationVariant.LUMO_WARNING, 4000).open();
            return true;
        }
        if (taggedLedgers.stream().anyMatch(info -> info.getLedger().getCode().equalsIgnoreCase(selectedLedger.getCode()))) {
            NotificationUtil.getNotification("Ledger is already added", "", false,
                    NotificationVariant.LUMO_WARNING, 4000).open();
            return true;
        }
        if (ledgerTransactionType.getValue() == null) {
            ledgerTransactionType.setInvalid(true);
            ledgerTransactionType.setErrorMessage("Must select a value");
            return true;
        }
        return false;
    }
}
