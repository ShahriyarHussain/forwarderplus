package com.lazoft.forwarderplus.views.finances.account;

import com.lazoft.forwarderplus.entity.Account;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.services.AccountService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
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
public class EditAccountLayout extends VerticalLayout {
    private final TextField accountName = new TextField("Account Name");
    private final TextField accountNo = new TextField("Account No");
    private final Button findAccount = new Button("Find Account");
    private final Grid<Account> accountGrid = new Grid<>(Account.class, false);

    private final AccountService accountService;

    public EditAccountLayout(AccountService accountService, User user) {
        this.accountService = accountService;
        setGridValues();
        setAttributes();
        setClickListeners();
        add(new H3("Edit Ledgers"), getFilterLayout(), accountGrid);
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
        accountGrid.addColumn("code").setHeader("Code").setAutoWidth(true);
        accountGrid.addColumn("name").setHeader("Name").setAutoWidth(true);
        accountGrid.addColumn("startingBalance").setHeader("Starting Balance").setAutoWidth(true);
        accountGrid.addColumn("currentBalance").setHeader("Current Balance").setAutoWidth(true).setSortable(true);
        accountGrid.addColumn("currency").setHeader("Currency").setAutoWidth(true);
        accountGrid.addColumn("createdOn").setHeader("Created On").setAutoWidth(true).setSortable(true);
        accountGrid.addComponentColumn(this::getEditLedgerButton).setHeader("Edit").setAutoWidth(true);
        accountGrid.setItems(query -> accountService.getAccountByFilter(getFilterSpecification(),
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))).stream());
    }

    private Button getEditLedgerButton(Account account) {
        Button edit = new Button(LineAwesomeIcon.PEN_SOLID.create());
        edit.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
//        edit.addClickListener(event -> new EditLedgerDialog().open());
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
