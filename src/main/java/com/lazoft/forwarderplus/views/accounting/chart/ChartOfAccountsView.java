package com.lazoft.forwarderplus.views.accounting.chart;

import com.lazoft.forwarderplus.builder.PopUpMessageBuilder;
import com.lazoft.forwarderplus.entity.finance.Account;
import com.lazoft.forwarderplus.service.finance.AccountService;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Chart of Account")
@Route(value = "chart-of-accounts", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "FINANCE"})
@Slf4j
public class ChartOfAccountsView extends VerticalLayout {

    private final H2 header =  new H2("Chart of Accounts");
    private final Grid<Account> accountGrid = new Grid<>(Account.class, false)  ;
    private final Button addAccountButton =  new Button("Add Account");
    private final List<Account> accounts = new ArrayList<>();

    private final AccountService accountService;

    public ChartOfAccountsView(AccountService accountService) {
        this.accountService = accountService;
        setComponentActions();
        setComponentProperties();
        loadDataToGrid();
        setHeight("100%");
        add(header, addAccountButton, accountGrid);
    }

    private void setComponentProperties() {
        accountGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        accountGrid.setSizeFull();
        accountGrid.addColumn(Account::getId).setHeader("Id").setAutoWidth(true).setSortable(true);
        accountGrid.addColumn(Account::getName).setHeader("Name").setAutoWidth(true).setSortable(true);
        accountGrid.addColumn(Account::getAccountType).setHeader("Type").setAutoWidth(true).setSortable(true);
        accountGrid.addColumn(Account::getRemarks).setHeader("Remarks").setAutoWidth(true);
        accountGrid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        accountGrid.addComponentColumn(this::getDeleteButton).setHeader("Delete").setAutoWidth(true);

        accountGrid.setItems(accounts);
        addAccountButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addAccountButton.setIcon(VaadinIcon.ARROW_RIGHT.create());
    }

    private Button getDeleteButton(Account account) {
        Button deleteButton = new Button(LineAwesomeIcon.TRASH_ALT.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(event -> {
            accountService.removeAccount(account);
            refreshGrid();
            PopUpMessageBuilder.builder()
                    .message("Account deleted!")
                    .variant(NotificationVariant.LUMO_PRIMARY)
                    .duration(2000).build().generatePopUp().open();
        });
        return deleteButton;
    }

    private void setComponentActions() {
        addAccountButton.addClickListener(e -> openChartOfAccountCreationDialog());
    }

    private void loadDataToGrid() {
        accounts.clear();
        accounts.addAll(accountService.getAllAccounts());
    }

    public void refreshGrid() {
        loadDataToGrid();
        accountGrid.getDataProvider().refreshAll();
    }

    private void openChartOfAccountCreationDialog() {
        new ChartOfAccountsCreationDialog(this, accountService).open();
    }

}
