package com.lazoft.forwarderplus.views.report;

import com.lazoft.forwarderplus.components.filter.TransactionFilter;
import com.lazoft.forwarderplus.dto.TransactionResult;
import com.lazoft.forwarderplus.dto.TransactionSummary;
import com.lazoft.forwarderplus.entity.Transaction;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.*;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

@PageTitle("View Transactions")
@Route(value = "view-transaction", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "FINANCE"})
@Slf4j
public class ViewTransactions extends Div {

    private final TransactionService transactionService;
    private Grid<Transaction> grid;
    private final User user;
    private final TransactionFilter filters;
    private final Card totalCountCard = new Card();
    private final Card totalAmount = new Card();
    private final Card expenseCountCard = new Card();
    private final Card expenseAmountCard = new Card();
    private final Card incomeAmountCard = new Card();
    private final Card incomeCountCard = new Card();


    public ViewTransactions(TransactionService transactionService, AuthenticatedUser authenticatedUser) {
        this.transactionService = transactionService;
        if (authenticatedUser.get().isEmpty()) {
            NotificationUtil.getNotification("Session Lost. Reload page or login again", "", false,
                    NotificationVariant.LUMO_WARNING, 2000).open();
            this.user = null;
        } else {
            this.user = authenticatedUser.get().get();
        }

        setSizeFull();
        addClassNames("view-shipments-view");
        filters = new TransactionFilter(this::refreshGrid);

        HorizontalLayout summaryLayout = new HorizontalLayout();
        summaryLayout.setWidthFull();
        summaryLayout.setSpacing(true);
        summaryLayout.setPadding(true);
        summaryLayout.getStyle().set("flex-wrap", "wrap");

        summaryLayout.add(totalCountCard, totalAmount, incomeCountCard, incomeAmountCard, expenseCountCard, expenseAmountCard);
        summaryLayout.setFlexGrow(1, totalCountCard, totalAmount, incomeCountCard, incomeAmountCard, expenseCountCard);

        VerticalLayout layout = new VerticalLayout(filters, new Hr(), summaryLayout, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private Component createGrid() {
        grid = new Grid<>(Transaction.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addColumn(transaction -> transaction.getTransactionAccount() == null
                        ? transaction.getTransactionLedger().getCode() + "(" + transaction.getTransactionLedger().getName() + ")" :
                        transaction.getTransactionAccount().getAccountNo() + "(" + transaction.getTransactionAccount().getName() + ")")
                .setHeader("A/C No./Ledger").setAutoWidth(true);
        grid.addColumn("type").setHeader("Transaction Type").setAutoWidth(true).setSortable(false);
        grid.addColumn("transactionMethod").setHeader("Transaction Method").setAutoWidth(true).setSortable(false);
        grid.addColumn("businessDate").setHeader("Business Date").setAutoWidth(true);
        grid.addColumn("transactionDate").setHeader("Transaction Date").setAutoWidth(true);
        grid.addColumn("conversionRate").setHeader("Conversion Rate").setAutoWidth(true);
        grid.addColumn("totalAmount").setHeader("Amount").setAutoWidth(true);
        grid.addColumn("currency").setHeader("Currency").setAutoWidth(true);
        grid.addColumn("totalAmountBaseCurrency").setHeader("Amount (BDT)").setAutoWidth(true);
        grid.addComponentColumn(this::viewTransactionDetailsButton).setHeader("Details").setAutoWidth(true);
        grid.setItems(query -> {
                    TransactionResult result = transactionService.getTransactionsAndSummaryByFilter(
                            PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                            filters);
                    setValues(result.getSummary());
                    return result.getTransactions().stream();
                }
        );
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private Button viewTransactionDetailsButton(Transaction transaction) {
        Button button = new Button(VaadinIcon.FILE_TREE_SMALL.create());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(e -> {
        });
        button.setEnabled(!transaction.getTransactionLegs().isEmpty());
        return button;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private void setValues(TransactionSummary transactionSummary) {
        setCardAttributes(totalCountCard, "Total Transactions",
                String.valueOf(transactionSummary.getTotalCount()), null, null);
        setCardAttributes(incomeCountCard, "Income Transactions",
                String.valueOf(transactionSummary.getIncomeCount()), LumoIcon.ANGLE_UP.create(), "primary");
        setCardAttributes(incomeAmountCard, "Total Income",
                transactionSummary.getIncomeAmount().toPlainString(), LumoIcon.ANGLE_UP.create(), "primary");
        setCardAttributes(expenseCountCard, "Expense Transactions",
                String.valueOf(transactionSummary.getExpenseCount()), LumoIcon.ANGLE_DOWN.create(), "error");
        setCardAttributes(expenseAmountCard, "Total Expense",
                transactionSummary.getExpenseAmount().toPlainString(), LumoIcon.ANGLE_DOWN.create(), "error");

        BigDecimal netAmount = transactionSummary.getIncomeAmount().subtract(transactionSummary.getExpenseAmount());
        boolean isProfit = transactionSummary.getIncomeAmount().compareTo(transactionSummary.getExpenseAmount()) >= 0;
        setCardAttributes(totalAmount, "Net Amount", netAmount.toPlainString(),
                isProfit ? LumoIcon.ANGLE_UP.create() : LumoIcon.ANGLE_DOWN.create(),
                isProfit ? "primary" : "error");
    }

    private void setCardAttributes(Card card, String title, String cardValue, Icon badgeTitle, String badgeType) {
        card.setTitle(title);
        card.setSubtitle(new H5(cardValue));
        if (badgeTitle == null) {
            return;
        }
        Span pendingPrimary = new Span(badgeTitle);
        pendingPrimary.getElement().getThemeList().add("badge " + badgeType + " primary");
        card.setHeaderSuffix(pendingPrimary);
    }
}
