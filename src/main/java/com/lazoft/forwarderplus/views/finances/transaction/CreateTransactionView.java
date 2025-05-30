package com.lazoft.forwarderplus.views.finances.transaction;

import com.lazoft.forwarderplus.components.dialog.CreateTransactionLegDialog;
import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.model.xml.CustomItem;
import com.lazoft.forwarderplus.model.xml.CustomItems;
import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.enums.TransactionMethod;
import com.lazoft.forwarderplus.enums.TransactionStatus;
import com.lazoft.forwarderplus.enums.TransactionType;
import com.lazoft.forwarderplus.services.AccountService;
import com.lazoft.forwarderplus.services.CurrencyDataService;
import com.lazoft.forwarderplus.services.LedgerService;
import com.lazoft.forwarderplus.services.TransactionService;
import com.lazoft.forwarderplus.util.CustomItemUtil;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.lazoft.forwarderplus.util.Constants.CATEGORIES;
import static com.lazoft.forwarderplus.util.Constants.UNITS;

@PageTitle("Create Transaction")
@Route(value = "create-transaction", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "FINANCE"})
@Slf4j
public class CreateTransactionView extends VerticalLayout {

    private final AccountService accountService;
    private final LedgerService ledgerService;
    private final TransactionService transactionService;
    private final CurrencyDataService currencyDataService;

    private final DatePicker transactionDate = new DatePicker("Transaction Date");
    private final ComboBox<TransactionMethod> transactionMethod = new ComboBox<>("Transaction Method");
    private final ComboBox<String> category = new ComboBox<>("Category");
    private final RadioButtonGroup<TransactionStatus> status = new RadioButtonGroup<>("Status");
    private final TextArea remarks = new TextArea("Remarks");

    private final RadioButtonGroup<TransactionType> transactionType = new RadioButtonGroup<>("Type");

    private final ComboBox<AccountLegerChoice> fromEntity = new ComboBox<>("From Account/Ledger");
    private final BigDecimalField fromEntityBalance = new BigDecimalField("Balance");

    private final ComboBox<AccountLegerChoice> toEntity = new ComboBox<>("To Account/Ledger");
    private final BigDecimalField toEntityBalance = new BigDecimalField("Balance");

    private final BigDecimalField foreignCurrencyAmount = new BigDecimalField("Amount");
    private final BigDecimalField localCurrencyAmount = new BigDecimalField("Amount In BDT");
    private final Button setLegs = new Button("Sub-Divide");
    private final ComboBox<AmountCurrency> currencyComboBox = new ComboBox<>("Currency");
    private final BigDecimalField conversionRate = new BigDecimalField("Conversion Rate");

    private final Button createTransaction = new Button("Create Transaction");
    private final Button clear = new Button("Clear All");

    private final TextField legRemarks = new TextField("Description");
    private final IntegerField quantity = new IntegerField("Quantity");
    private final ComboBox<String> unit = new ComboBox<>("Unit");
    private final BigDecimalField amount = new BigDecimalField("Amount");
    private final BigDecimalField totalAmount = new BigDecimalField("Total Amount");
    private final Button addLegButton = new Button(LineAwesomeIcon.PLUS_CIRCLE_SOLID.create());

    private final Grid<TransactionLeg> transactionLegGrid = new Grid<>(TransactionLeg.class, false);

    private int sl = 1;

    private final String categoriesFileName = CATEGORIES;
    private final List<CustomItem> categoryList;

    private final String unitsFileName = UNITS;
    private final List<CustomItem> unitList;

    private final List<AccountLegerChoice> accountLedgerList = new LinkedList<>();
    private final List<Account> accountsList;
    private final List<Ledger> ledgerList;
    private final List<TransactionLeg> transactionLegs = new LinkedList<>();


    public CreateTransactionView(AccountService accountService, LedgerService ledgerService,
                                 TransactionService transactionService, CurrencyDataService currencyDataService) {
        this.accountService = accountService;
        this.ledgerService = ledgerService;
        this.transactionService = transactionService;
        this.currencyDataService = currencyDataService;
        this.categoryList = CustomItemUtil.getItemsListFromFile(categoriesFileName);
        this.unitList = CustomItemUtil.getItemsListFromFile(unitsFileName);
        accountsList = accountService.getAccounts();
        ledgerList = ledgerService.getLedgers();
        setValues();
        setAttributes();
        setListeners();
        setFormLayout();
    }

    enum EntityType {
        ACCOUNT, LEDGER
    }

    record AccountLegerChoice(String title, EntityType type, Account account, Ledger ledger) {}

    private void setValues() {
        transactionType.setItems(TransactionType.values());
        transactionMethod.setItems(TransactionMethod.values());
        transactionMethod.setItemLabelGenerator(TransactionMethod::getLabel);
        transactionDate.setValue(LocalDate.now());

        accountLedgerList.addAll(accountsList.stream().map(account -> new AccountLegerChoice(
                "(Account) " + account.getName() + " (" + account.getAccountNo() + ")", EntityType.ACCOUNT, account, null)).toList());
        accountLedgerList.addAll(ledgerList.stream().map(ledger -> new AccountLegerChoice(
                "(Ledger) " + ledger.getName() + " (" + ledger.getCode() + ")", EntityType.LEDGER, null, ledger)).toList());


        fromEntity.setItems(accountLedgerList);
        fromEntity.setItemLabelGenerator(AccountLegerChoice::title);
        toEntity.setItems(accountLedgerList);
        toEntity.setItemLabelGenerator(AccountLegerChoice::title);

        category.setItems(categoryList.stream().map(CustomItem::getName).toList());
        unit.setItems(unitList.stream().map(CustomItem::getName).toList());
        currencyComboBox.setItems(AmountCurrency.values());
    }

    private void setAttributes() {
        transactionType.setWidth("40%");

        transactionDate.setWidth("20%");
        transactionDate.setLocale(Locale.UK);
        category.setWidth("25%");
        category.setAllowCustomValue(true);
        transactionMethod.setWidth("30%");

        fromEntity.setWidth("40%");
        fromEntityBalance.setWidth("30%");
        fromEntityBalance.setReadOnly(true);

        toEntity.setWidth("40%");
        toEntity.setVisible(false);
        toEntityBalance.setWidth("30%");
        toEntityBalance.setReadOnly(true);
        toEntityBalance.setVisible(false);

        currencyComboBox.setWidth("20%");
        currencyComboBox.setValue(AmountCurrency.BDT);
        conversionRate.setWidth("30%");
        conversionRate.setValue(BigDecimal.ONE);

        foreignCurrencyAmount.setWidth("20%");
        localCurrencyAmount.setWidth("30%");
        localCurrencyAmount.setReadOnly(true);
        setLegs.setIcon(VaadinIcon.SPLIT.create());
        setLegs.setWidth("10%");
        setLegs.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        remarks.setMaxHeight(8, Unit.REM);
        createTransaction.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        clear.addThemeVariants(ButtonVariant.LUMO_ERROR);

        totalAmount.setReadOnly(true);
        transactionLegGrid.setMaxHeight("150px");
    }

    private void setListeners() {
        setPrimaryListeners();
        setMiscListeners();
    }

    private void setPrimaryListeners() {
        transactionType.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                fromEntity.setVisible(false);
                fromEntityBalance.setVisible(false);
                toEntity.setVisible(false);
                toEntityBalance.setVisible(false);
                return;
            }
            if (event.getValue() == TransactionType.TRANSFER) {
                fromEntity.setVisible(true);
                fromEntityBalance.setVisible(true);
                toEntity.setVisible(true);
                toEntityBalance.setVisible(true);
            } else {
                fromEntity.setVisible(true);
                fromEntityBalance.setVisible(true);
                toEntity.setVisible(false);
                toEntityBalance.setVisible(false);
            }
        });

        fromEntity.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                return;
            }
            fromEntityBalance.setValue(getBalanceFromChoice(event.getValue()));
        });

        toEntity.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                return;
            }
            toEntityBalance.setValue(getBalanceFromChoice(event.getValue()));
        });

        setLegs.addClickListener(event -> new CreateTransactionLegDialog(transactionLegs, this).open());

        createTransaction.addClickListener(event -> {
            CustomItems newCategory = new CustomItems(categoryList);
            CustomItemUtil.saveCustomItems(newCategory, categoriesFileName);

            CustomItems newUnits = new CustomItems(unitList);
            CustomItemUtil.saveCustomItems(newUnits, unitsFileName);

            if (isInvalidEntries()) {
                NotificationUtil.getNotification("Please provide valid data in the marked fields", "", false,
                        NotificationVariant.LUMO_WARNING, 3000).open();
                return;
            }
            createNewTransaction();
        });

        addLegButton.addClickListener(event -> {
            TransactionLeg transactionLeg = new TransactionLeg();
            transactionLeg.setQuantity(quantity.getValue());
            transactionLeg.setUnit(unit.getValue());
            transactionLeg.setAmount(amount.getValue());
            transactionLeg.setRemarks(remarks.getValue());
            transactionLeg.setSlNo(sl);
            transactionLeg.setId((long) sl);
            transactionLegs.add(transactionLeg);
            transactionLegGrid.setItems(transactionLegs);
            sl++;
        });
    }

    private void setMiscListeners() {
        category.addCustomValueSetListener(event -> {
            String customValue = event.getDetail();
            if (customValue == null) {
                return;
            }
            categoryList.add(new CustomItem(customValue, event.getDetail().trim().toLowerCase().hashCode(), null));
            category.setItems(categoryList.stream().map(CustomItem::getName).toList());
            category.setValue(customValue);
        });

        unit.addCustomValueSetListener(event -> {
            String customValue = event.getDetail();
            if (customValue == null) {
                return;
            }
            unitList.add(new CustomItem(customValue, event.getDetail().trim().toLowerCase().hashCode(), null));
            unit.setItems(unitList.stream().map(CustomItem::getName).toList());
            unit.setValue(customValue);
        });

        currencyComboBox.addValueChangeListener(event -> {
            BigDecimal rate = BigDecimal.ONE;
            try {
                rate = currencyDataService.getConversionRateByCurrency(event.getValue(), AmountCurrency.BDT);
            } catch (Exception e) {
                log.error("Error occurred while fetching data from web.", e);
            }
            conversionRate.setValue(rate);
            updateLocalCurrencyAmount();
        });

        foreignCurrencyAmount.addValueChangeListener(event -> updateLocalCurrencyAmount());

        amount.addValueChangeListener(event ->
                totalAmount.setValue(amount.getValue().multiply(new BigDecimal(quantity.getValue()))));
    }

    private void updateLocalCurrencyAmount() {
        if (foreignCurrencyAmount.getValue() == null) {
            return;
        }
        BigDecimal rate = conversionRate.getValue() == null ? BigDecimal.ONE : conversionRate.getValue();
        localCurrencyAmount.setValue(foreignCurrencyAmount.getValue().multiply(rate));
    }

    private boolean isInvalidEntries() {
        if (transactionType.getValue() == null) {
            transactionType.setInvalid(true);
            transactionType.setErrorMessage("Transaction type is required");
            return true;
        }
        if (transactionMethod.getValue() == null) {
            transactionMethod.setInvalid(true);
            transactionMethod.setErrorMessage("Transaction method is required");
            return true;
        }
        if (transactionDate.getValue() == null) {
            transactionDate.setInvalid(true);
            transactionDate.setErrorMessage("Transaction date is required");
            return true;
        }
        if (fromEntity.getValue() == null) {
            fromEntity.setInvalid(true);
            fromEntity.setErrorMessage("From Account/Ledger is required");
            return true;
        }
        if (transactionType.getValue() == TransactionType.TRANSFER && toEntity.getValue() == null) {
            toEntity.setInvalid(true);
            toEntity.setErrorMessage("To Account/Ledger is required for Transfer Type Transaction");
            return true;
        }
        if (currencyComboBox.getValue() == null) {
            currencyComboBox.setInvalid(true);
            currencyComboBox.setErrorMessage("Currency cannot be empty");
            return true;
        }
        if (conversionRate.getValue() == null || conversionRate.getValue().equals(BigDecimal.ZERO)) {
            conversionRate.setValue(BigDecimal.ONE);
        }
        if (foreignCurrencyAmount.getValue() == null || foreignCurrencyAmount.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            foreignCurrencyAmount.setInvalid(true);
            foreignCurrencyAmount.setErrorMessage("Amount cannot be Negative/Zero/Empty");
            return true;
        }
        if (transactionType.getValue() != TransactionType.INCOME &&
                localCurrencyAmount.getValue().compareTo(fromEntityBalance.getValue()) > 0) {
            NotificationUtil.getNotification("Entered amount is more than available balance!", "", false,
                    NotificationVariant.LUMO_WARNING, 3000).open();
        }
        return false;
    }

    private void createNewTransaction() {
        List<Transaction> transactionList = new ArrayList<>(4);
        Transaction transaction = createTransactionFromEnteredData();
        transaction.setSlNo(1);
        if (fromEntity.getValue().type.equals(EntityType.ACCOUNT)) {
            transaction.setTransactionAccount(fromEntity.getValue().account);
        } else {
            transaction.setTransactionLedger(fromEntity.getValue().ledger);
        }
        transactionList.add(transaction);
        if (transactionType.getValue().equals(TransactionType.TRANSFER)) {
            Transaction transferTransaction = createTransactionFromEnteredData();
            transferTransaction.setSlNo(2);
            if (toEntity.getValue().type.equals(EntityType.ACCOUNT)) {
                transferTransaction.setTransactionAccount(toEntity.getValue().account);
            }
            if (toEntity.getValue().type.equals(EntityType.LEDGER)) {
                transferTransaction.setTransactionLedger(toEntity.getValue().ledger);
            }
            transactionList.add(transferTransaction);
        }

        try {
            int batchNo = transactionService.postTransaction(transactionList);
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setText("Transaction Posted Successfully! Batch No: " + batchNo);
            confirmDialog.setHeader("Transaction Posting");
            confirmDialog.setConfirmButton("Ok", event -> confirmDialog.close());
            confirmDialog.open();
            clearAll();
        } catch (OptimisticLockingFailureException e) {
            NotificationUtil.getNotification("Transaction details were updated. Please try again", e.getMessage(), true,
                    NotificationVariant.LUMO_ERROR, 5000).open();
        } catch (Exception e) {
            NotificationUtil.getNotification("Unexpected Error", e.getMessage(), true,
                    NotificationVariant.LUMO_ERROR, 5000).open();
        }
    }

    private Transaction createTransactionFromEnteredData() {
        Transaction transaction = new Transaction();
        if (transactionLegs.isEmpty()) {
            TransactionLeg leg = new TransactionLeg();
            leg.setSlNo(1);
            leg.setAmount(localCurrencyAmount.getValue());
            leg.setRemarks(remarks.getValue());
            transactionLegs.add(leg);
        }
        transaction.setTransactionLegs(transactionLegs);
        transaction.setTransactionMethod(transactionMethod.getValue());
        transaction.setType(transactionType.getValue());
        transaction.setBusinessDate(LocalDate.now());
        transaction.setTransactionDate(transactionDate.getValue());
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setTotalAmountBaseCurrency(localCurrencyAmount.getValue());
        transaction.setTotalAmount(foreignCurrencyAmount.getValue());
        transaction.setRemarks(remarks.getValue());
        transaction.setCurrency(currencyComboBox.getValue());
        transaction.setConversionRate(conversionRate.getValue());
        return transaction;
    }

    private BigDecimal getBalanceFromChoice(AccountLegerChoice choice) {
        if (choice.type == EntityType.ACCOUNT) {
            return accountsList.stream().filter(acc -> acc.getAccountId() == choice.account.getAccountId())
                    .findFirst().map(Account::getCurrentBalance).orElse(BigDecimal.ZERO);
        }
        if (choice.type == EntityType.LEDGER) {
            return ledgerList.stream().filter(ledger -> ledger.getLedgerId() == choice.ledger.getLedgerId())
                    .findFirst().map(Ledger::getCurrentBalance).orElse(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }

    public void updateAmountByTransactionLegs() {
        if (transactionLegs.isEmpty()) {
            return;
        }
        foreignCurrencyAmount.setValue(transactionLegs.stream().map(TransactionLeg::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        updateLocalCurrencyAmount();
    }

    private void setFormLayout() {
        Hr topDivider = new Hr(), middleDivider = new Hr(), bottomDivider = new Hr();
        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.add(transactionDate, transactionMethod, category);

        FormLayout formLayout = new FormLayout();
        HorizontalLayout fromLayout = new HorizontalLayout();
        fromLayout.add(fromEntity, fromEntityBalance);
        fromLayout.setWidth("100%");

        HorizontalLayout toLayout = new HorizontalLayout();
        toLayout.add(toEntity, toEntityBalance);
        toLayout.setWidth("100%");

        VerticalLayout middleLayout = new VerticalLayout();
        setupTransactionLegGrid();
        FormLayout transactionLegLayout = getTransactionLegForm();
        middleLayout.add(transactionLegLayout, transactionLegGrid);
        middleLayout.setWidth("100%");

        HorizontalLayout currencyAmountLayout = new HorizontalLayout();
        currencyAmountLayout.add(currencyComboBox, conversionRate, foreignCurrencyAmount, localCurrencyAmount);
        currencyAmountLayout.setWidth("100%");

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setAlignItems(FlexComponent.Alignment.END);
        buttonLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        buttonLayout.add(createTransaction, clear);
        buttonLayout.setWidth("100%");

        formLayout.add(transactionType, topLayout, topDivider, fromLayout, toLayout, middleDivider, currencyAmountLayout,
                middleLayout, bottomDivider, remarks, buttonLayout);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));
        formLayout.setColspan(topDivider, 3);
        formLayout.setColspan(middleDivider, 3);
        formLayout.setColspan(bottomDivider, 3);
        formLayout.setColspan(transactionType, 3);
        formLayout.setColspan(topLayout, 3);
        formLayout.setColspan(fromLayout, 3);
        formLayout.setColspan(toLayout, 3);
        formLayout.setColspan(currencyAmountLayout, 3);
        formLayout.setColspan(middleLayout, 3);
        formLayout.setColspan(buttonLayout, 3);
        formLayout.setColspan(remarks, 2);
        formLayout.setWidth("70%");

        add(formLayout);
    }

    private FormLayout getTransactionLegForm() {
        FormLayout formLayout = new FormLayout();
        HorizontalLayout amountLayout = new HorizontalLayout();
        amountLayout.add(legRemarks, quantity, unit, amount, totalAmount , addLegButton);
        amountLayout.setAlignItems(FlexComponent.Alignment.END);
        amountLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        formLayout.add(amountLayout);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 4));
        formLayout.setColspan(amountLayout, 3);
        return formLayout;
    }

    public void setupTransactionLegGrid() {
        transactionLegGrid.addColumn(TransactionLeg::getSlNo).setHeader("Sl").setWidth("2%");
        transactionLegGrid.addColumn(TransactionLeg::getRemarks).setHeader("Description");
        transactionLegGrid.addColumn(TransactionLeg::getAmount).setHeader("Price/Unit").setAutoWidth(true);
        transactionLegGrid.addColumn(TransactionLeg::getQuantity).setHeader("Quantity").setAutoWidth(true);
        transactionLegGrid.addColumn(TransactionLeg::getUnit).setHeader("Unit").setAutoWidth(true);
        transactionLegGrid.addComponentColumn(leg -> {
            Button deleteButton = new Button(LineAwesomeIcon.MINUS_CIRCLE_SOLID.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(event -> {
                transactionLegs.remove(leg);
                transactionLegGrid.setItems(transactionLegs);
                refreshGrandTotals();
                sl--;
            });
            return deleteButton;
        }).setHeader("Delete");
        transactionLegGrid.setItems(transactionLegs);
    }

    private void refreshGrandTotals() {
        foreignCurrencyAmount.setValue(transactionLegs.stream().map(TransactionLeg::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        localCurrencyAmount.setValue(foreignCurrencyAmount.getValue().multiply(conversionRate.getValue()));
    }

    private void clearAll() {
        transactionMethod.clear();
        category.clear();
        status.clear();
        remarks.clear();
        transactionType.clear();
        fromEntity.clear();
        fromEntityBalance.clear();
        toEntity.clear();
        toEntityBalance.clear();
        foreignCurrencyAmount.clear();
        localCurrencyAmount.clear();
        currencyComboBox.clear();
        conversionRate.clear();
        accountLedgerList.clear();
        accountsList.clear();
        accountsList.addAll(accountService.getAccounts());
        ledgerList.clear();
        ledgerList.addAll(ledgerService.getLedgers());
        transactionLegs.clear();
        setValues();
    }
}
