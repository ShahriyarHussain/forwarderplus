package com.lazoft.forwarderplus.views.finances.ledger;

import com.lazoft.forwarderplus.entity.Ledger;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.services.LedgerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ManageLedgerLayout extends VerticalLayout {
    private final TextField ledgerName = new TextField("Ledger Name");
    private final TextField ledgerCode = new TextField("Ledger Code");
    private final Button findLedger = new Button("Find Ledger");
    private final Grid<Ledger> ledgerGrid = new Grid<>(Ledger.class, false);

    private final LedgerService ledgerService;

    public ManageLedgerLayout(LedgerService ledgerService, User user) {
        this.ledgerService = ledgerService;
        setGridValues();
        setAttributes();
        setClickListeners();
        add(new H3("Edit Ledgers"), getFilterLayout(), ledgerGrid);
    }

    private void setAttributes() {
        setWidth("100%");
        //getStyle().set("flex-grow", "1");
        findLedger.setIcon(LineAwesomeIcon.SEARCH_SOLID.create());
        ledgerGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        ledgerGrid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
    }

    private void setClickListeners() {
        findLedger.addClickListener(event -> refreshGrid());
    }

    private HorizontalLayout getFilterLayout() {
        HorizontalLayout horizontalLayout = new HorizontalLayout(ledgerName, ledgerCode, findLedger);
        horizontalLayout.setAlignItems(Alignment.END);
        horizontalLayout.setVerticalComponentAlignment(Alignment.END);
        return horizontalLayout;
    }

    public void setGridValues() {
        ledgerGrid.addColumn("code").setHeader("Code").setAutoWidth(true);
        ledgerGrid.addColumn("name").setHeader("Name").setAutoWidth(true);
        ledgerGrid.addColumn("startingBalance").setHeader("Starting Balance").setAutoWidth(true);
        ledgerGrid.addColumn("currentBalance").setHeader("Current Balance").setAutoWidth(true).setSortable(true);
        ledgerGrid.addColumn("currency").setHeader("Currency").setAutoWidth(true);
        ledgerGrid.addColumn("createdOn").setHeader("Created On").setAutoWidth(true).setSortable(true);
        ledgerGrid.addComponentColumn(this::getEditLedgerButton).setHeader("Edit").setAutoWidth(true);
        ledgerGrid.setItems(query -> ledgerService.getLedgersByFilter(getFilterSpecification(),
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))).stream());
    }

    private Button getEditLedgerButton(Ledger ledger) {
        Button edit = new Button(LineAwesomeIcon.PEN_SOLID.create());
        edit.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
//        edit.addClickListener(event -> new EditLedgerDialog().open());
        return edit;
    }

    private void refreshGrid() {
        ledgerGrid.getDataProvider().refreshAll();
    }

    public Specification<Ledger> getFilterSpecification() {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (!StringUtils.isBlank(ledgerName.getValue())) {
                predicateList.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + ledgerName.getValue().toLowerCase() + "%"));
            }
            if (!StringUtils.isBlank(ledgerCode.getValue())) {
                predicateList.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), "%" + ledgerCode.getValue().toLowerCase() + "%"));
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[]{}));
        };

    }

}
