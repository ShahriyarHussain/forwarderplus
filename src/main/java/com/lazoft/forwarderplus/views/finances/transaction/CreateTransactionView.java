package com.lazoft.forwarderplus.views.finances.transaction;

import com.lazoft.forwarderplus.dto.xml.Categories;
import com.lazoft.forwarderplus.dto.xml.Category;
import com.lazoft.forwarderplus.entity.Account;
import com.lazoft.forwarderplus.entity.Ledger;
import com.lazoft.forwarderplus.entity.Transaction;
import com.lazoft.forwarderplus.entity.TransactionLeg;
import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.enums.TransactionMethod;
import com.lazoft.forwarderplus.enums.TransactionStatus;
import com.lazoft.forwarderplus.enums.TransactionType;
import com.lazoft.forwarderplus.services.AccountService;
import com.lazoft.forwarderplus.services.LedgerService;
import com.lazoft.forwarderplus.services.TransactionService;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@PageTitle("Create Transaction")
@Route(value = "create-transaction", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "ACCOUNTS"})
@Slf4j
public class CreateTransactionView extends VerticalLayout {

    private final AccountService accountService;
    private final LedgerService ledgerService;
    private final TransactionService transactionService;

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

    private final BigDecimalField amount = new BigDecimalField("Amount");
    private final Button setLegs = new Button("Sub-Divide");
    private final ComboBox<AmountCurrency> currencyComboBox = new ComboBox<>("Currency");

    private final Button createTransaction = new Button("Create Transaction");
    private final Button clear = new Button("Clear All");

    private final File categoriesFile = new File("./categories.xml");

    private final List<Category> categoryList;
    List<AccountLegerChoice> accountLedgerList = new LinkedList<>();
    private final JAXBContext context;

    private final List<Account> accountsList;
    private final List<Ledger> ledgerList;

    private final List<TransactionLeg> transactionLegs = new LinkedList<>();

    public CreateTransactionView(AccountService accountService, LedgerService ledgerService,
                                 TransactionService transactionService) {
        this.accountService = accountService;
        this.ledgerService = ledgerService;
        this.transactionService = transactionService;
        this.context = getContext();
        this.categoryList = getCategoryListFromFile();
        accountsList = accountService.getAccounts();
        ledgerList = ledgerService.getLedgers();
        setValues();
        setAttributes();
        setListeners();
        setFormLayout();
    }

    private JAXBContext getContext() {
        try {
            return JAXBContext.newInstance(Categories.class);
        } catch (JAXBException e) {
            log.error("Error while getting context", e);
        }
        return null;
    }

    private List<Category> getCategoryListFromFile() {
        if (context == null || !categoriesFile.exists()) {
            return new LinkedList<>();
        }
        try {
            Categories categories = (Categories) context.createUnmarshaller().unmarshal(categoriesFile);
            return categories.getCategories();
        } catch (JAXBException e) {
            log.error(e.getMessage(), e);
        }
        return new LinkedList<>();
    }

    enum EntityType {
        ACCOUNT, LEDGER
    }

    record AccountLegerChoice(String title, EntityType type, Account account, Ledger ledger) {};

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

        category.setItems(categoryList.stream().map(Category::getName).toList());
        currencyComboBox.setItems(AmountCurrency.values());
    }

    private void setAttributes() {
        remarks.setMaxHeight(8, Unit.REM);
        setLegs.setIcon(VaadinIcon.SPLIT.create());
        category.setAllowCustomValue(true);
        category.setWidth("20%");
        transactionType.setWidth("40%");
        transactionDate.setWidth("30%");

        fromEntity.setWidth("40%");
        fromEntityBalance.setWidth("30%");
        fromEntityBalance.setReadOnly(true);

        toEntity.setWidth("40%");
        toEntityBalance.setWidth("30%");
        toEntityBalance.setReadOnly(true);

        currencyComboBox.setWidth("20%");
        amount.setWidth("30%");
        setLegs.setWidth("15%");
        setLegs.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        createTransaction.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        clear.addThemeVariants(ButtonVariant.LUMO_ERROR);
    }

    private void setListeners() {
        category.addCustomValueSetListener(event -> {
            String customValue = event.getDetail();
            if (customValue == null) {
                return;
            }
            categoryList.add(new Category(customValue, event.getDetail().trim().toLowerCase().hashCode()));
            category.setItems(categoryList.stream().map(Category::getName).toList());
            category.setValue(customValue);
        });
        createTransaction.addClickListener(event -> {
            Categories newCategory = new Categories(categoryList);
            saveCategories(newCategory);
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

    private void saveCategories(Categories categories) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(categories, categoriesFile);
        } catch (JAXBException e) {
            log.error("Error while saving categories", e);
        }
    }

    public void updateAmountByTransactionLegs() {
        if (transactionLegs.isEmpty()) {
            return;
        }
        amount.setValue(transactionLegs.stream().map(TransactionLeg::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private void setFormLayout() {
        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.add(category, transactionMethod, transactionDate);

        FormLayout formLayout = new FormLayout();
        HorizontalLayout fromLayout = new HorizontalLayout();
        fromLayout.add(fromEntity, fromEntityBalance);
        fromLayout.setWidth("100%");

        HorizontalLayout toLayout = new HorizontalLayout();
        toLayout.add(toEntity, toEntityBalance);
        toLayout.setWidth("100%");

        HorizontalLayout amountLayout = new HorizontalLayout();
        amountLayout.setAlignItems(FlexComponent.Alignment.END);
        amountLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        amountLayout.add(currencyComboBox, amount, setLegs);
        amountLayout.setWidth("100%");

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setAlignItems(FlexComponent.Alignment.END);
        buttonLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        buttonLayout.add(createTransaction, clear);
        buttonLayout.setWidth("100%");

        formLayout.add(transactionType, topLayout, fromLayout, toLayout, amountLayout, remarks, buttonLayout);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));
        formLayout.setColspan(transactionType, 3);
        formLayout.setColspan(topLayout, 3);
        formLayout.setColspan(fromLayout, 3);
        formLayout.setColspan(toLayout, 3);
        formLayout.setColspan(amountLayout, 3);
        formLayout.setColspan(buttonLayout, 3);
        formLayout.setWidth("70%");

        add(formLayout);
    }
}
