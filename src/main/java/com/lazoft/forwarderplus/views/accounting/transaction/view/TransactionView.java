package com.lazoft.forwarderplus.views.accounting.transaction.view;

import com.lazoft.forwarderplus.components.filter.TransactionFilter;
import com.lazoft.forwarderplus.dto.AccountTypeSummaryDto;
import com.lazoft.forwarderplus.entity.finance.TransactionLeg;
import com.lazoft.forwarderplus.enums.AccountType;
import com.lazoft.forwarderplus.service.finance.AccountService;
import com.lazoft.forwarderplus.service.finance.TransactionService;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@PageTitle("View Transactions")
@Route(value = "transaction-view", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "FINANCE"})
@Slf4j
public class TransactionView extends Div {

    private final TransactionFilter transactionFilter;
    private final TransactionService transactionService;

    HorizontalLayout summaryLayout = new HorizontalLayout();
    private final Card netAssetCard = new Card();
    private final Card netLiabilityCard = new Card();
    private final Card netRevenueCard = new Card();
    private final Card netExpenseCard = new Card();
    private final Card netEquityCard = new Card();
    private final Map<AccountType, Card> cardMap = new EnumMap<>(AccountType.class);


    private final List<AccountTypeSummaryDto> dto = new LinkedList<>();

    private final Grid<TransactionLeg> grid = new Grid<>(TransactionLeg.class, false);
    VerticalLayout pageLayout = new VerticalLayout();


    public TransactionView(AccountService accountService, TransactionService transactionService) {
        this.transactionService = transactionService;
        this.transactionFilter = new TransactionFilter(this::refreshGrid, accountService.getAllAccounts());

        setCardMap();
        setGridLayout();
        setSummaryLayout();
        setPageLayout();
        setSummaryValues();
        loadDataIntoGrid();

        setSizeFull();
        addClassNames("view-shipments-view");
        add(pageLayout);
    }

    private void setCardMap() {
        cardMap.put(AccountType.ASSET, netAssetCard);
        cardMap.put(AccountType.LIABILITY, netLiabilityCard);
        cardMap.put(AccountType.REVENUE, netRevenueCard);
        cardMap.put(AccountType.EXPENSE, netExpenseCard);
        cardMap.put(AccountType.EQUITY, netEquityCard);
    }

    private void loadDataIntoGrid() {
        grid.setItems(query -> {
            Sort groupedSort = Sort.by(Sort.Direction.ASC, "transaction.date")
                    .and(Sort.by(Sort.Direction.ASC, "transaction.id"));
            return transactionService.getShipmentsByFilter(
                    PageRequest.of(query.getPage(), query.getPageSize(), groupedSort),
                    transactionFilter).stream();
        });
        dto.addAll(transactionService.getSummaryHeaderData(transactionFilter));
        setSummaryValues();
    }

    private void setPageLayout() {
        pageLayout.setSizeFull();
        pageLayout.setPadding(false);
        pageLayout.setSpacing(false);
        pageLayout.add(transactionFilter, new Hr(), summaryLayout, grid);
    }

    private void setSummaryLayout() {
        summaryLayout.setWidthFull();
        summaryLayout.setSpacing(true);
        summaryLayout.setPadding(true);
        summaryLayout.getStyle().set("flex-wrap", "wrap");
    }

    private void refreshGrid() {
        dto.clear();
        dto.addAll(transactionService.getSummaryHeaderData(transactionFilter));
        setSummaryValues();
        grid.getDataProvider().refreshAll();
    }

    private void setGridLayout() {
        grid.addColumn(leg -> leg.getTransaction().getId()).setHeader("Transaction Id")
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER).setSortable(true).setResizable(true);
        grid.addColumn(leg -> leg.getAccount().getName() + "- " + leg.getAccount().getAccountType())
                .setHeader("Account").setAutoWidth(true).setResizable(true);
        grid.addColumn(TransactionLeg::getCreditAmount).setHeader("Credit Amount")
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END).setSortable(true);
        grid.addColumn(TransactionLeg::getDebitAmount).setHeader("Debit Amount")
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END).setSortable(true);
        grid.addColumn(TransactionLeg::getLegRemarks).setHeader("Remarks").setResizable(true)
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setSortable(true);
        grid.addComponentColumn(this::getPrintInvoiceButton).setHeader("Print Invoice")
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);
        grid.addComponentColumn(this::getViewDetailsButton).setHeader("View Details")
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

        grid.setWidth("100%");
        grid.setPartNameGenerator(leg -> {
            if (leg.getTransaction() == null || leg.getTransaction().getId() == null) {
                return null;
            }
            return (leg.getTransaction().getId() % 2 == 0) ? "tx-even" : "tx-odd";
        });
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.Border.RIGHT, LumoUtility.BorderColor.CONTRAST_10);
    }

    private Button getPrintInvoiceButton(TransactionLeg transactionLeg) {
        Button button = new Button(LineAwesomeIcon.PRINT_SOLID.create());
        button.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
       return button;
    }

    private Button getViewDetailsButton(TransactionLeg transactionLeg) {
        Button button = new Button(LineAwesomeIcon.COMPASS.create());
        button.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    private void setSummaryValues() {
        summaryLayout.removeAll();
        dto.forEach(summary -> {
            Card card = cardMap.get(summary.accountType());
            setCardAttributes(card,
                    "Net " + summary.accountType(),
                    String.valueOf(summary.getNetBalance()));
            summaryLayout.add(card);
            summaryLayout.setFlexGrow(1, card);
        });
        setSummaryLayout();
    }

    private void setCardAttributes(Card card,
                                   String title,
                                   String cardValue) {
        card.setTitle(title);
        card.setSubtitle(new H5(cardValue));
        Span pendingPrimary = new Span(LineAwesomeIcon.WALLET_SOLID.create());
        pendingPrimary.getElement().getThemeList().add("badge " + "primary" + " primary");
        card.setHeaderSuffix(pendingPrimary);
    }
}
