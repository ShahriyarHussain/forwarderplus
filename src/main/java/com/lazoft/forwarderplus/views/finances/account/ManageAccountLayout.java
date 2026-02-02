package com.lazoft.forwarderplus.views.finances.account;

import com.lazoft.forwarderplus.entity.Account;
import com.lazoft.forwarderplus.services.finance.impl.AccountService;
import com.lazoft.forwarderplus.services.LedgerService;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ManageAccountLayout extends VerticalLayout {
    private final TextField accountName = new TextField("Account Name");
    private final TextField accountNo = new TextField("Account No");
    private final Button findAccount = new Button("Find Account");
    private final Grid<Account> accountGrid = new Grid<>(Account.class, false);

    private final AccountService accountService;
    private final LedgerService ledgerService;

    public ManageAccountLayout(AccountService accountService, LedgerService ledgerService) {
        this.accountService = accountService;
        this.ledgerService = ledgerService;
        setGridValues();
        setAttributes();
        setClickListeners();
        add(getFilterLayout(), accountGrid);
    }

    private void setAttributes() {
        setWidth("100%");
        findAccount.setIcon(LineAwesomeIcon.SEARCH_SOLID.create());
        accountGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        accountGrid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
    }

    private void setClickListeners() {
        findAccount.addClickListener(event -> refreshGrid());
    }

    private HorizontalLayout getFilterLayout() {
        HorizontalLayout horizontalLayout = new HorizontalLayout(accountName, accountNo, findAccount);
        horizontalLayout.setAlignItems(FlexComponent.Alignment.END);
        horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        return horizontalLayout;
    }

    public void setGridValues() {
        accountGrid.addColumn("accountNo").setHeader("Account No").setAutoWidth(true);
        accountGrid.addColumn("name").setHeader("Name").setAutoWidth(true);
        accountGrid.addColumn("startingBalance").setHeader("Starting Balance").setAutoWidth(true);
        accountGrid.addColumn("currentBalance").setHeader("Current Balance").setAutoWidth(true).setSortable(true);
        accountGrid.addColumn("amountCurrency").setHeader("Currency").setAutoWidth(true);
        accountGrid.addColumn("createdAt").setHeader("Created On").setAutoWidth(true).setSortable(true);
        accountGrid.addComponentColumn(this::getEditAccountButton).setHeader("Edit").setAutoWidth(true);
        accountGrid.addComponentColumn(this::getDeleteAccountButton).setHeader("Delete").setAutoWidth(true);

        accountGrid.setItems(query -> accountService.getAccountByFilter(getFilterSpecification(),
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))).stream());
    }

    private Button getEditAccountButton(Account account) {
        Button edit = new Button(LineAwesomeIcon.PEN_SOLID.create());
        edit.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        edit.addClickListener(event -> new EditAccountDialog(accountService, ledgerService, account).open());
        return edit;
    }

    private Button getDeleteAccountButton(Account account) {
        Button edit = new Button(LineAwesomeIcon.TRASH_ALT_SOLID.create());
        edit.addThemeVariants(ButtonVariant.LUMO_ERROR);
        edit.addClickListener(event -> accountService.deleteAccount(account));
        edit.addClickListener(event -> {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Delete Account");
            confirmDialog.setText("Are you sure you want to delete this account ?");
            confirmDialog.setCancelable(true);
            confirmDialog.setConfirmButton(new Button("Yes, I am Sure", confirmEvent -> {
                try {
                    accountService.deleteAccount(account);
                    NotificationUtil.getNotification("Deleted Successfully", "",
                            false, NotificationVariant.LUMO_PRIMARY, 2000);
                } catch (Exception e) {
                    log.error("Error while deleting account: {}", e.getMessage(), e);
                    NotificationUtil.getNotification("Failed to delete entry", "Reason: " + e.getMessage(),
                            true, NotificationVariant.LUMO_ERROR, 5000);
                }
            }));
            Button cancel = new Button("Close", confirmEvent -> confirmDialog.close());
            cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
            confirmDialog.setCancelButton(cancel);
            confirmDialog.open();
        });
        return edit;
    }

    private void refreshGrid() {
        accountGrid.getDataProvider().refreshAll();
    }

    public Specification<Account> getFilterSpecification() {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (!StringUtils.isBlank(accountName.getValue())) {
                predicateList.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + accountName.getValue().toLowerCase() + "%"));
            }
            if (!StringUtils.isBlank(accountNo.getValue())) {
                predicateList.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("accountNo")), "%" + accountNo.getValue().toLowerCase() + "%"));
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[]{}));
        };

    }
}
