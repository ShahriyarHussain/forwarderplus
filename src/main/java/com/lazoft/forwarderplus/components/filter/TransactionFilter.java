package com.lazoft.forwarderplus.components.filter;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.enums.TransactionMethod;
import com.lazoft.forwarderplus.enums.TransactionType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.Nonnull;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionFilter extends Div implements Specification<Transaction> {

    private final TextField accountNo = new TextField("A/C No");
    private final TextField ledgerNo = new TextField("Ledger No");
    private final ComboBox<TransactionType> transactionType = new ComboBox<>("Type");
    private final ComboBox<TransactionMethod> transactionMethod = new ComboBox<>("Transaction Method");
    private final ComboBox<AmountCurrency> currency = new ComboBox<>("Currency");
    private final TextField remarks = new TextField("Remarks");
    private final BigDecimalField fromAmount = new BigDecimalField("Amount");
    private final BigDecimalField toAmount = new BigDecimalField();
    private final DatePicker fromTransactionDate = new DatePicker("Transaction Date");
    private final DatePicker toTransactionDate = new DatePicker();
    private final DatePicker fromBusinessDate = new DatePicker("Business Date");
    private final DatePicker toBusinessDate = new DatePicker();

    public TransactionFilter(Runnable onSearch) {

        setWidthFull();
        addClassName("filter-layout");
        addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                LumoUtility.BoxSizing.BORDER);

        fromTransactionDate.setLocale(Locale.UK);
        toTransactionDate.setLocale(Locale.UK);

        ledgerNo.setPlaceholder("Ledger No");
        ledgerNo.addKeyDownListener(keyDownEvent -> searchOnKeyDown(keyDownEvent, onSearch));

        accountNo.setPlaceholder("Account No");
        accountNo.addKeyDownListener(keyDownEvent -> searchOnKeyDown(keyDownEvent, onSearch));

        currency.setItems(AmountCurrency.values());

        transactionType.setItems(TransactionType.values());

        transactionMethod.setItems(TransactionMethod.values());
        transactionMethod.setItemLabelGenerator(TransactionMethod::getLabel);

        // Action buttons
        Button resetBtn = new Button("Reset");
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.addClickListener(e -> {
            ledgerNo.clear();
            accountNo.clear();
            fromTransactionDate.clear();
            toTransactionDate.clear();
            fromBusinessDate.clear();
            toBusinessDate.clear();
            transactionType.setValue(transactionType.getEmptyValue());
            transactionMethod.setValue(transactionMethod.getEmptyValue());
            remarks.clear();
            currency.clear();
            onSearch.run();
        });
        Button searchBtn = new Button("Search");
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClickListener(e -> onSearch.run());

        Div actions = new Div(resetBtn, searchBtn);
        actions.addClassName(LumoUtility.Gap.SMALL);
        actions.addClassName("actions");

        add(ledgerNo, accountNo, transactionType, transactionMethod, currency, remarks,
                createDateFilter(fromTransactionDate, toTransactionDate),
                createDateFilter(fromBusinessDate, toBusinessDate),
                createAmountFilter(), actions);
    }

    private HorizontalLayout getSummaryCard() {
        Card numberOfTransactions = new Card();
        numberOfTransactions.setTitle("INCOME");
        numberOfTransactions.setSubtitle(new H4("32,000"));
        Span pendingPrimary = new Span("BDT");
        pendingPrimary.getElement().getThemeList().add("badge Success");
        numberOfTransactions.setHeaderSuffix(pendingPrimary);
        return new HorizontalLayout(numberOfTransactions);
    }

    private void searchOnKeyDown(KeyDownEvent keyDownEvent, Runnable onSearch) {
        if (keyDownEvent.getKey() == Key.ENTER || keyDownEvent.getKey() == Key.NUMPAD_ENTER) {
            onSearch.run();
        }
    }

    private Component createDateFilter(DatePicker fromDate, DatePicker toDate) {
        fromDate.setPlaceholder("From");
        toDate.setPlaceholder("To");

        FlexLayout portSelectionComponent = new FlexLayout(fromDate, new Text(" – "), toDate);
        portSelectionComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
        portSelectionComponent.addClassName(LumoUtility.Gap.XSMALL);
        return portSelectionComponent;
    }

    private Component createAmountFilter() {
        fromAmount.setPlaceholder("From");
        toAmount.setPlaceholder("To");

        // For screen readers
        fromAmount.setAriaLabel("From Amount");
        toAmount.setAriaLabel("To Amount");

        FlexLayout portSelectionComponent = new FlexLayout(fromAmount, new Text(" – "), toAmount);
        portSelectionComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
        portSelectionComponent.addClassName(LumoUtility.Gap.XSMALL);
        return portSelectionComponent;
    }

    @Override
    public Predicate toPredicate(Root<Transaction> root, @Nonnull CriteriaQuery<?> query, @Nonnull CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
//        if (!ledgerNo.isEmpty()) {
//            String bookingNoLowerCase = ledgerNo.getValue().toLowerCase();
//            Join<Transaction, Ledger> ledgerJoin = root.join("transactionLedger");
//            Predicate bookingNoMatch = criteriaBuilder.like(criteriaBuilder.lower(
//                    ledgerJoin.get("bookingNo")), "%" + bookingNoLowerCase + "%");
//            predicates.add(bookingNoMatch);
//        }
//        if (!accountNo.isEmpty()) {
//            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("mblNo")), "%" + accountNo.getValue() + "%"));
//        }
        if (!transactionType.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("type"), transactionType.getValue()));
        }
        if (!transactionMethod.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("transactionMethod"), transactionMethod.getValue()));
        }
        if (!currency.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("currency"), currency.getValue()));
        }
        if (fromTransactionDate.getValue() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"),
                    criteriaBuilder.literal(fromTransactionDate.getValue())));
        }
        if (toTransactionDate.getValue() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"),
                    criteriaBuilder.literal(toTransactionDate.getValue())));
        }
        if (fromBusinessDate.getValue() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("businessDate"),
                    criteriaBuilder.literal(fromBusinessDate.getValue())));
        }
        if (toBusinessDate.getValue() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("businessDate"),
                    criteriaBuilder.literal(toBusinessDate.getValue())));
        }
        if (fromAmount.getValue() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"),
                    criteriaBuilder.literal(fromAmount.getValue())));
        }
        if (toAmount.getValue() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"),
                    criteriaBuilder.literal(toAmount.getValue())));
        }
        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }
}
