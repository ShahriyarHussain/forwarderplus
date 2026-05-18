package com.lazoft.forwarderplus.views.accounting.transaction.view;

import com.lazoft.forwarderplus.components.filter.TransactionFilter;
import com.lazoft.forwarderplus.dto.TransactionSummary;
import com.lazoft.forwarderplus.entity.finance.Account;
import com.lazoft.forwarderplus.entity.finance.TransactionLeg;
import com.lazoft.forwarderplus.service.finance.AccountService;

import com.lazoft.forwarderplus.service.finance.TransactionService;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@PageTitle("View Transactions")
@Route(value = "transaction-view", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "FINANCE"})
@Slf4j
public class TransactionView extends Div {

    private final TransactionFilter transactionFilter;
    private final AccountService accountService;
    private final TransactionService transactionService;

    HorizontalLayout summaryLayout = new HorizontalLayout();
    private final Card totalCountCard = new Card();
    private final Card totalAmount = new Card();
    private final Card expenseCountCard = new Card();
    private final Card expenseAmountCard = new Card();
    private final Card incomeAmountCard = new Card();
    private final Card incomeCountCard = new Card();

    private final Grid<TransactionLeg> grid = new Grid<>(TransactionLeg.class, false);
    VerticalLayout pageLayout = new VerticalLayout();


    public TransactionView(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.transactionFilter = new TransactionFilter(this::refreshGrid, accountService.getAllAccounts());

        setGridLayout();
        setSummaryLayout();
        setPageLayout();
//        setValues();
        loadDataIntoGrid();

        setSizeFull();
        addClassNames("view-shipments-view");
        add(pageLayout);
    }

    private void loadDataIntoGrid() {
        grid.setItems(query -> {
            Sort groupedSort = Sort.by(Sort.Direction.ASC, "transaction.date")
                    .and(Sort.by(Sort.Direction.ASC, "transaction.id"));
            return transactionService.getShipmentsByFilter(
                    PageRequest.of(query.getPage(), query.getPageSize(), groupedSort),
                    transactionFilter).stream();
        });
    }

    private void setPageLayout() {
        pageLayout.setSizeFull();
        pageLayout.setPadding(false);
        pageLayout.setSpacing(false);
        pageLayout.add(transactionFilter, new Hr(), grid);
    }

    private void setSummaryLayout() {
        summaryLayout.setWidthFull();
        summaryLayout.setSpacing(true);
        summaryLayout.setPadding(true);
        summaryLayout.getStyle().set("flex-wrap", "wrap");

        summaryLayout.add(totalCountCard, totalAmount, incomeCountCard, incomeAmountCard,
                expenseCountCard, expenseAmountCard);
        summaryLayout.setFlexGrow(1, totalCountCard, totalAmount, incomeCountCard,
                incomeAmountCard, expenseCountCard);
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private void setGridLayout() {
        grid.addColumn(leg -> leg.getTransaction().getId()).setHeader("Transaction Id")
                .setAutoWidth(true).setSortable(true);
        grid.addColumn(leg -> leg.getAccount().getName() + "- " + leg.getAccount().getAccountType())
                .setHeader("Account").setAutoWidth(true);
        grid.addColumn(TransactionLeg::getCreditAmount).setHeader("Credit Amount").setAutoWidth(true).setSortable(true);
        grid.addColumn(TransactionLeg::getDebitAmount).setHeader("Debit Amount").setAutoWidth(true).setSortable(true);
        grid.addComponentColumn(this::getPrintInvoiceButton).setHeader("Print Invoice").setAutoWidth(true);

        grid.setWidth("100%");
        grid.setMinHeight("50%");
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.Border.RIGHT, LumoUtility.BorderColor.CONTRAST_10);
    }

    private Button getPrintInvoiceButton(TransactionLeg transactionLeg) {
        return null;
    }

    private void setValues(TransactionSummary transactionSummary) {
        setCardAttributes(totalCountCard, "Total Transactions",
                String.valueOf(transactionSummary.getTotalCount()), null, null);
        setCardAttributes(incomeAmountCard, "Total Revenue",
                transactionSummary.getIncomeAmount().toPlainString(),
                LineAwesomeIcon.ARROW_UP_SOLID, "primary");
        setCardAttributes(expenseCountCard, "Total Expense",
                String.valueOf(transactionSummary.getExpenseCount()),
                LineAwesomeIcon.ARROW_DOWN_SOLID, "error");
        setCardAttributes(expenseAmountCard, "Total Asset",
                transactionSummary.getExpenseAmount().toPlainString(),
                LineAwesomeIcon.ARROW_DOWN_SOLID, "error");
        setCardAttributes(expenseAmountCard, "Total Liability",
                transactionSummary.getExpenseAmount().toPlainString(),
                LineAwesomeIcon.ARROW_DOWN_SOLID, "error");

        BigDecimal netAmount = transactionSummary.getIncomeAmount().subtract(transactionSummary.getExpenseAmount());
        boolean isProfit = transactionSummary.getIncomeAmount()
                .compareTo(transactionSummary.getExpenseAmount()) >= 0;
        setCardAttributes(totalAmount, "Net Amount", netAmount.toPlainString(),
                isProfit ? LineAwesomeIcon.ARROW_UP_SOLID : LineAwesomeIcon.ARROW_DOWN_SOLID,
                isProfit ? "primary" : "error");
    }

    private void setCardAttributes(Card card,
                                   String title,
                                   String cardValue,
                                   LineAwesomeIcon cardIcon,
                                   String badgeType) {
        card.setTitle(title);
        card.setSubtitle(new H5(cardValue));
        if (cardIcon == null) {
            return;
        }
        Span pendingPrimary = new Span(cardIcon.create());
        pendingPrimary.getElement().getThemeList().add("badge " + badgeType + " primary");
        card.setHeaderSuffix(pendingPrimary);
    }


}
