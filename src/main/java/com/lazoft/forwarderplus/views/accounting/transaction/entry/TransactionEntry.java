package com.lazoft.forwarderplus.views.accounting.transaction.entry;

import com.lazoft.forwarderplus.builder.PopUpMessageBuilder;
import com.lazoft.forwarderplus.components.Divider;
import com.lazoft.forwarderplus.entity.finance.Account;
import com.lazoft.forwarderplus.entity.finance.Transaction;
import com.lazoft.forwarderplus.entity.finance.TransactionLeg;
import com.lazoft.forwarderplus.exception.ValidationException;
import com.lazoft.forwarderplus.service.finance.AccountService;
import com.lazoft.forwarderplus.service.finance.TransactionService;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@PageTitle("Transaction Entry")
@Route(value = "transaction-entry", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "FINANCE"})
@Slf4j
public class TransactionEntry extends VerticalLayout {

    private final AccountService accountService;
    private final TransactionService transactionService;

    private final VerticalLayout cardLayout = new VerticalLayout();
    private final Card totalDebitCard = new Card();
    private final Card totalCreditCard = new Card();

    private final HorizontalLayout entryLayout = new HorizontalLayout();
    private final FormLayout entryFormLayout = new FormLayout();
    private final ComboBox<Account> account = new ComboBox<>("Transaction Account");
    private final BigDecimalField debitAmount = new BigDecimalField("Debit Amount");
    private final BigDecimalField creditAmount = new BigDecimalField("Credit Amount");
    private final TextArea legRemarks = new TextArea("Leg Remarks");
    private final Button addLegButton = new Button("Add Transaction Leg");

    private final HorizontalLayout postLayout = new HorizontalLayout();
    private final DatePicker transactionDate = new DatePicker("Transaction Date");
    private final TextArea transactionRemarks = new TextArea("Transaction Remarks");
    private final Button postTransactionButton = new Button("Post Transaction");

    private final Grid<TransactionLeg> debitLegsGrid = new Grid<>(TransactionLeg.class, false);
    private final Grid<TransactionLeg> creditLegsGrid = new Grid<>(TransactionLeg.class, false);

    private final HorizontalLayout gridLayout = new HorizontalLayout();
    private final List<TransactionLeg> debitLegs = new ArrayList<>();
    private final List<TransactionLeg> creditLegs = new ArrayList<>();

    public TransactionEntry(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;

        setCardLayout();
        setEntryLayout();
        setGridLayout();
        setPostLayout();
        setCardProperties();
        setButtonProperties();

        loadData();
        setAddLegButtonActions();
        setAddTransactionButtonActions();

        add(entryLayout, new Hr(),
                gridLayout, new Hr(),
                postLayout,
                postTransactionButton
        );
    }

    //----- Actions & Logic --//
    private void setAddLegButtonActions() {
        addLegButton.addClickListener(e -> {
            try {
                addTransactionLeg();
                refreshData();
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
    }

    private void setAddTransactionButtonActions() {
        postTransactionButton.addClickListener(e -> {
            try {
                createTransaction();
                showTransactionCreatedDialog();
                resetAll();
                refreshData();
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
    }

    private void showTransactionCreatedDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Transaction Posting");
        dialog.setText("Transaction posted successfully!");
        Button okButton = new  Button("OK", LineAwesomeIcon.CHECK_SOLID.create());
        okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        okButton.addClickListener(e -> dialog.close());
        dialog.setConfirmButton(okButton);
        dialog.open();
    }

    private void resetAll() {
        account.clear();
        transactionDate.clear();
        debitAmount.clear();
        creditAmount.clear();
        legRemarks.clear();

        transactionRemarks.clear();

        debitLegs.clear();
        creditLegs.clear();

        transactionDate.setValue(LocalDate.now());
        totalDebitCard.setSubtitle(new H4("0.00"));
        totalDebitCard.setSubtitle(new H4("0.00"));
    }

    private void createTransaction() {
        Transaction transaction = new Transaction();
        transaction.setDate(transactionDate.getValue());
        transaction.addLegs(debitLegs);
        transaction.addLegs(creditLegs);
        transaction.setDescription(legRemarks.getValue());
        transactionService.saveTransaction(transaction);
    }

    private void addTransactionLeg() {
        TransactionLeg leg = createTransactionLegFromData();
        if (isBigDecimalNullOrZero(leg.getCreditAmount())) {
            debitLegs.add(leg);
        } else  {
            creditLegs.add(leg);
        }
    }

    private TransactionLeg createTransactionLegFromData() {
        TransactionLeg leg = new TransactionLeg();
        validateLegData();
        leg.setAccount(account.getValue());
        leg.setLegRemarks(legRemarks.getValue());
        if (isBigDecimalNullOrZero(debitAmount.getValue())) {
            leg.setCreditAmount(creditAmount.getValue());
        } else {
            leg.setDebitAmount(debitAmount.getValue());
        }
        return leg;
    }

    private void validateLegData() {
        if (account.getValue() == null) {
            throw new ValidationException("Must provide account");
        }
        if (transactionDate.getValue() == null) {
            throw new ValidationException("Must provide date");
        }
        if (isBigDecimalNullOrZero(debitAmount.getValue()) && isBigDecimalNullOrZero(creditAmount.getValue())) {
            throw new ValidationException("Must provide either credit or debit amount");
        }
        if (!isBigDecimalNullOrZero(debitAmount.getValue()) && !isBigDecimalNullOrZero(creditAmount.getValue())) {
            throw new ValidationException("Cannot accept both Debit & Credit amount");
        }
    }

    private boolean isBigDecimalNullOrZero(BigDecimal decimal) {
        return Objects.isNull(decimal) || decimal.equals(BigDecimal.ZERO);
    }

    public void refreshData() {
        debitLegsGrid.getDataProvider().refreshAll();
        creditLegsGrid.getDataProvider().refreshAll();

        BigDecimal totalDebit = debitLegs.stream().map(TransactionLeg::getDebitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = creditLegs.stream().map(TransactionLeg::getCreditAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!isBigDecimalNullOrZero(totalDebit) && !totalDebit.equals(totalCredit)) {
            setCardStyle(totalCreditCard, "Total Credit", LumoIcon.CROSS.create(), "error");
            setCardStyle(totalDebitCard, "Total Debit", LumoIcon.CROSS.create(), "error");
        } else {
            setCardStyle(totalCreditCard, "Total Credit", LumoIcon.CHECKMARK.create(), "primary");
            setCardStyle(totalDebitCard, "Total Debit", LumoIcon.CHECKMARK.create(), "primary");
        }
        totalDebitCard.setSubtitle(new H4(totalDebit.toPlainString()));
        totalCreditCard.setSubtitle(new H4(totalCredit.toPlainString()));
    }

    private void loadData() {
        transactionDate.setValue(LocalDate.now());
        creditAmount.setValue(BigDecimal.ZERO);
        debitAmount.setValue(BigDecimal.ZERO);

        List<Account> accountList = accountService.getAllAccounts();
        account.setItems(accountList);
        account.setItemLabelGenerator(acc -> acc.getName() + "- " + acc.getAccountType());

        debitLegsGrid.setItems(debitLegs);
        creditLegsGrid.setItems(creditLegs);
    }

    private Button getDeleteButton(TransactionLeg transactionLeg, boolean isCredit) {
        Button deleteButton = new Button(LineAwesomeIcon.TRASH_SOLID.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(event -> {
            if (isCredit) {
                creditLegs.remove(transactionLeg);
            } else  {
                debitLegs.remove(transactionLeg);
            }
            refreshData();
        });
        return deleteButton;
    }

    //----- UI Properties --//

    private void setButtonProperties() {
        addLegButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addLegButton.setIcon(LineAwesomeIcon.PLUS_CIRCLE_SOLID.create());
        addLegButton.getStyle().setHeight("40px");

        postTransactionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        postTransactionButton.setIcon(LineAwesomeIcon.CHECK_CIRCLE_SOLID.create());
        postTransactionButton.getStyle().setHeight("50px");
    }

    private void setPostLayout() {
        transactionDate.setLocale(Locale.UK);
        postLayout.setAlignItems(Alignment.START);
        postLayout.setWidth("100%");
        transactionRemarks.setWidth("20%");

        postLayout.add(transactionRemarks, transactionDate);
    }

    private void setCardLayout() {
        cardLayout.setWidth("20%");
        cardLayout.setAlignItems(Alignment.CENTER);
        cardLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        cardLayout.add(totalCreditCard, totalDebitCard);
    }

    private void setEntryLayout() {
        entryFormLayout.setWidth("90%");
        entryFormLayout.add(account, debitAmount, creditAmount, legRemarks, addLegButton);
        entryFormLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 5));
        entryFormLayout.setColspan(legRemarks, 2);

        entryLayout.setWidth("100%");
        entryLayout.setAlignItems(Alignment.CENTER);
        entryLayout.add(entryFormLayout);
    }

    private void setGridLayout() {
        setDebitGridProperties();
        VerticalLayout debitLegsLayout = new VerticalLayout();
        debitLegsLayout.setWidth("50%");
        debitLegsLayout.add(new H3("Debit Legs"), new H4("(Asset/Expense)"), totalDebitCard, debitLegsGrid);

        setCreditLegProperties();
        VerticalLayout creditLegsLayout = new VerticalLayout();
        creditLegsLayout.setWidth("50%");
        creditLegsLayout.add(new H3("Credit Legs"), new H4("(Liability/Revenue/Equity)"), totalCreditCard, creditLegsGrid);

        gridLayout.setSizeFull();
        gridLayout.add(debitLegsLayout, new Divider(), creditLegsLayout);
    }

    private void setCreditLegProperties() {
        creditLegsGrid.addColumn(leg -> creditLegs.indexOf(leg) + 1).setHeader("SL").setAutoWidth(true).setSortable(true);
        creditLegsGrid.addColumn(leg -> leg.getAccount().getName() + "- " + leg.getAccount().getAccountType())
                .setHeader("Credit Account").setAutoWidth(true);
        creditLegsGrid.addColumn(TransactionLeg::getCreditAmount).setHeader("Amount").setAutoWidth(true).setSortable(true);
        creditLegsGrid.addComponentColumn(transactionLeg -> getDeleteButton(transactionLeg, true))
                .setHeader("Delete").setAutoWidth(true);

        creditLegsGrid.setWidth("100%");
        creditLegsGrid.setAllRowsVisible(true);
        creditLegsGrid.addClassNames(LumoUtility.Border.TOP, LumoUtility.Border.RIGHT, LumoUtility.BorderColor.CONTRAST_10);
    }

    private void setDebitGridProperties() {
        debitLegsGrid.addColumn(leg -> debitLegs.indexOf(leg) + 1).setHeader("SL").setAutoWidth(true).setSortable(true);
        debitLegsGrid.addColumn(leg -> leg.getAccount().getName() + "- " + leg.getAccount().getAccountType())
                .setHeader("Debit Account").setAutoWidth(true);
        debitLegsGrid.addColumn(TransactionLeg::getDebitAmount).setHeader("Amount").setAutoWidth(true).setSortable(true);
        debitLegsGrid.addComponentColumn(transactionLeg -> getDeleteButton(transactionLeg, false))
                .setHeader("Delete").setAutoWidth(true);

        debitLegsGrid.setWidth("100%");
        debitLegsGrid.setAllRowsVisible(true);
        debitLegsGrid.addClassNames(LumoUtility.Border.TOP, LumoUtility.Border.RIGHT, LumoUtility.BorderColor.CONTRAST_10);
    }

    private void setCardProperties() {
        setCardStyle(totalCreditCard, "Total Credit", LumoIcon.CHECKMARK.create(), "primary");
        setCardStyle(totalDebitCard, "Total Debit", LumoIcon.CHECKMARK.create(), "primary");
    }

    private void setCardStyle(Card card, String title, Icon icon, String badgeType) {
        card.setTitle(title);
        card.setSubtitle(new H4("0.00"));
        Span pendingPrimary = new Span(icon);
        pendingPrimary.getElement().getThemeList().add("badge " + badgeType + " primary");
        card.setHeaderSuffix(pendingPrimary);
        card.setWidth("100%");
    }

    private Anchor getExportAsExcelAnchor() {
        return null;
    }
}
