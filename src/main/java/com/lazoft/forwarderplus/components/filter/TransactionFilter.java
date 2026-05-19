package com.lazoft.forwarderplus.components.filter;

import com.lazoft.forwarderplus.entity.finance.Account;
import com.lazoft.forwarderplus.entity.finance.Transaction;
import com.lazoft.forwarderplus.entity.finance.TransactionLeg;
import com.lazoft.forwarderplus.enums.AccountType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.Nonnull;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionFilter extends Div implements Specification<TransactionLeg> {

    private final ComboBox<Account> account = new ComboBox<>("Account");
    private final ComboBox<AccountType> accountType = new ComboBox<>("Account Type");
    private final TextField transactionRemarks = new TextField("Transaction Remarks");
    private final BigDecimalField fromAmount = new BigDecimalField("Amount");
    private final BigDecimalField toAmount = new BigDecimalField();
    private final DatePicker fromTransactionDate = new DatePicker("Transaction Date");
    private final DatePicker toTransactionDate = new DatePicker();

    public TransactionFilter(Runnable onSearch, List<Account> accountList) {

        setWidthFull();
        addClassName("filter-layout");
        addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                LumoUtility.BoxSizing.BORDER);

        fromTransactionDate.setLocale(Locale.UK);
        toTransactionDate.setLocale(Locale.UK);

        account.setItems(accountList);
        account.setItemLabelGenerator(acc -> acc.getName() + "- " + acc.getAccountType());

        accountType.setItems(AccountType.values());

        // Action buttons
        Button resetBtn = new Button("Reset");
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.addClickListener(e -> {
            account.setValue(account.getEmptyValue());
            fromTransactionDate.clear();
            toTransactionDate.clear();
            transactionRemarks.clear();
            onSearch.run();
        });

        Button searchBtn = new Button("Search");
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClickListener(e -> onSearch.run());

        Div actions = new Div(resetBtn, searchBtn);
        actions.addClassName(LumoUtility.Gap.SMALL);
        actions.addClassName("actions");

        add(account, accountType, createDateFilter(), createAmountFilter(), transactionRemarks, actions);
    }

    private Component createDateFilter() {
        fromTransactionDate.setPlaceholder("From");
        toTransactionDate.setPlaceholder("To");

        FlexLayout dateFilterComponent = new FlexLayout(fromTransactionDate, new Text(" – "), toTransactionDate);
        dateFilterComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
        dateFilterComponent.addClassName(LumoUtility.Gap.XSMALL);
        return dateFilterComponent;
    }

    private Component createAmountFilter() {
        fromAmount.setPlaceholder("From");
        toAmount.setPlaceholder("To");

        FlexLayout amountFilterComponent = new FlexLayout(fromAmount, new Text(" – "), toAmount);
        amountFilterComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
        amountFilterComponent.addClassName(LumoUtility.Gap.XSMALL);
        return amountFilterComponent;
    }

    @Override
    public Predicate toPredicate(@Nonnull Root<TransactionLeg> root,
                                 @Nonnull CriteriaQuery<?> query,
                                 @Nonnull CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();
        Join<TransactionLeg, Transaction> transactionJoin = root.join("transaction", JoinType.INNER);

        if (fromTransactionDate.getValue() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    transactionJoin.get("date"), fromTransactionDate.getValue())
            );
        }
        if (toTransactionDate.getValue() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    transactionJoin.get("date"), toTransactionDate.getValue())
            );
        }
        if (!account.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("account"), account.getValue()));
        }
        if (!accountType.isEmpty()) {
            Join<TransactionLeg, Account> accountJoin = root.join("account", JoinType.INNER);
            predicates.add(criteriaBuilder.equal(accountJoin.get("accountType"), accountType.getValue()));
        }
        if (fromAmount.getValue() != null || toAmount.getValue() != null) {
            Predicate hasDebit = criteriaBuilder.greaterThan(root.get("debitAmount"), BigDecimal.ZERO);
            Predicate debitInRange = criteriaBuilder.and(
                    hasDebit,
                    buildAmountRangePredicate(criteriaBuilder, root.get("debitAmount"))
            );

            Predicate hasCredit = criteriaBuilder.greaterThan(root.get("creditAmount"), BigDecimal.ZERO);
            Predicate creditInRange = criteriaBuilder.and(
                    hasCredit,
                    buildAmountRangePredicate(criteriaBuilder, root.get("creditAmount"))
            );
            predicates.add(criteriaBuilder.or(debitInRange, creditInRange));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate buildAmountRangePredicate(CriteriaBuilder cb, Path<BigDecimal> amountPath) {
        List<Predicate> bounds = new ArrayList<>();
        if (fromAmount.getValue() != null) {
            bounds.add(cb.greaterThanOrEqualTo(amountPath, fromAmount.getValue()));
        }
        if (toAmount.getValue() != null) {
            bounds.add(cb.lessThanOrEqualTo(amountPath, toAmount.getValue()));
        }
        return cb.and(bounds.toArray(new Predicate[0]));
    }
}
