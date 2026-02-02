package com.lazoft.forwarderplus.components.filter;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.ClientType;
import com.lazoft.forwarderplus.enums.ContainerSize;
import com.lazoft.forwarderplus.enums.ShipmentStatus;
import com.lazoft.forwarderplus.model.xml.CustomItem;
import com.lazoft.forwarderplus.services.CarrierService;
import com.lazoft.forwarderplus.services.ClientService;
import com.lazoft.forwarderplus.services.PortService;
import com.lazoft.forwarderplus.util.CustomItemUtil;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.Nonnull;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ShipmentFilter extends Div implements Specification<Shipment> {

    private final TextField blNo = new TextField("HBL/MBL No:");
    private final TextField bookingNo = new TextField("Booking No");
    private final ComboBox<Port> portOfLoading = new ComboBox<>("Port");
    private final ComboBox<Port> portOfDestination = new ComboBox<>();
    private final ComboBox<Client> clientComboBox = new ComboBox<>("Shipper");
    private final ComboBox<Carrier> carrier = new ComboBox<>("Carrier");
    private final ComboBox<String> commodity = new ComboBox<>("Commodity");
    private final ComboBox<ShipmentStatus> status = new ComboBox<>("Status");
    private final ComboBox<ContainerSize> containerSize = new ComboBox<>("Container Size");
    private final DatePicker createFromDate = new DatePicker("Created Date");
    private final DatePicker createdToDate = new DatePicker();

    public ShipmentFilter(Runnable onSearch,
                          PortService portService,
                          CarrierService carrierService,
                          ClientService clientService) {

        List<Port> ports = portService.getAllPorts();
        List<Carrier> carriers = carrierService.getAllCarriers();
        List<Client> clientList = clientService.getClientsByType(List.of(ClientType.SHIPPER, ClientType.ALL));

        setWidthFull();
        addClassName("filter-layout");
        addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                LumoUtility.BoxSizing.BORDER);

        createFromDate.setLocale(Locale.UK);
        createdToDate.setLocale(Locale.UK);

        bookingNo.setPlaceholder("Booking No");
        bookingNo.addKeyDownListener(keyDownEvent -> searchOnKeyDown(keyDownEvent, onSearch));

        blNo.setPlaceholder("HBL/MBL No");
        blNo.addKeyDownListener(keyDownEvent -> searchOnKeyDown(keyDownEvent, onSearch));

        containerSize.setItems(ContainerSize.values());
        containerSize.setItemLabelGenerator(ContainerSize::getContainerSize);

        clientComboBox.setItems(clientList);
        clientComboBox.setItemLabelGenerator(Client::getName);

        commodity.setItems(CustomItemUtil.getItemsListFromFile("commodities").stream().map(CustomItem::getName).toList());

        status.setItems(ShipmentStatus.values());
        status.setItemLabelGenerator(ShipmentStatus::getStatus);

        carrier.setItems(carriers);
        carrier.setItemLabelGenerator(Carrier::getName);

        portOfLoading.setItems(ports);
        portOfLoading.setItemLabelGenerator(Port::getPortName);
        portOfDestination.setItems(ports);
        portOfDestination.setItemLabelGenerator(Port::getPortName);

        // Action buttons
        Button resetBtn = new Button("Reset");
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.addClickListener(e -> {
            bookingNo.clear();
            blNo.clear();
            createFromDate.clear();
            createdToDate.clear();
            portOfLoading.setValue(portOfLoading.getEmptyValue());
            portOfDestination.setValue(portOfDestination.getEmptyValue());
            containerSize.setValue(containerSize.getEmptyValue());
            carrier.clear();
            commodity.clear();
            status.clear();
            clientComboBox.clear();
            onSearch.run();
        });
        Button searchBtn = new Button("Search");
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClickListener(e -> onSearch.run());

        Div actions = new Div(resetBtn, searchBtn);
        actions.addClassName(LumoUtility.Gap.SMALL);
        actions.addClassName("actions");

        add(bookingNo, blNo, createPortFilter(), clientComboBox, commodity, containerSize, carrier,
                createDateFilter(), status, actions);
    }

    private void searchOnKeyDown(KeyDownEvent keyDownEvent, Runnable onSearch) {
        if (keyDownEvent.getKey() == Key.ENTER || keyDownEvent.getKey() == Key.NUMPAD_ENTER) {
            onSearch.run();
        }
    }

    private Component createDateFilter() {
        createFromDate.setPlaceholder("From");
        createdToDate.setPlaceholder("To");

        // For screen readers
        createFromDate.setAriaLabel("Created From");
        createdToDate.setAriaLabel("Created To");

        FlexLayout portSelectionComponent = new FlexLayout(createFromDate, new Text(" – "), createdToDate);
        portSelectionComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
        portSelectionComponent.addClassName(LumoUtility.Gap.XSMALL);
        return portSelectionComponent;
    }

    private Component createPortFilter() {
        portOfLoading.setPlaceholder("Origin");
        portOfDestination.setPlaceholder("Destination");

        // For screen readers
        portOfLoading.setAriaLabel("Origin Port");
        portOfDestination.setAriaLabel("Destination Port");

        FlexLayout portSelectionComponent = new FlexLayout(portOfLoading, new Text(" – "), portOfDestination);
        portSelectionComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
        portSelectionComponent.addClassName(LumoUtility.Gap.XSMALL);
        return portSelectionComponent;
    }

    private Component createCommodityStatusFilter() {
        FlexLayout commodityStatusComponent = new FlexLayout(commodity, status);
        commodityStatusComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
        commodityStatusComponent.addClassName(LumoUtility.Gap.XSMALL);
        return commodityStatusComponent;
    }

    @Override
    public Predicate toPredicate(Root<Shipment> root, @Nonnull CriteriaQuery<?> query, @Nonnull CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        root.fetch("schedule", JoinType.LEFT);

        if (!bookingNo.isEmpty()) {
            String bookingNoLowerCase = bookingNo.getValue().toLowerCase();
            Join<Shipment, Booking> bookingJoin = root.join("booking");
            Predicate bookingNoMatch = criteriaBuilder.like(criteriaBuilder.lower(
                    bookingJoin.get("bookingNo")), "%" + bookingNoLowerCase + "%");
            predicates.add(bookingNoMatch);
        }
        if (!status.isEmpty()) {
            ShipmentStatus shipmentStatus = status.getValue();
            Predicate statusMatch = criteriaBuilder.equal(root.get("status"), shipmentStatus);
            predicates.add(statusMatch);
        }
        if (!blNo.isEmpty()) {
            String blNoValue = blNo.getValue().toLowerCase();
            Predicate mblMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("mblNo")), "%" + blNoValue + "%");
            Predicate hblMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("hblNo")), "%" + blNoValue + "%");
            Predicate blMatch = criteriaBuilder.or(mblMatch, hblMatch);
            predicates.add(blMatch);
        }
        if (!portOfLoading.isEmpty()) {
            long portId = portOfLoading.getValue().getId();
            Join<Shipment, Booking> bookingJoin = root.join("booking");
            Join<Booking, Port> portJoin = bookingJoin.join("loadingPort");
            Predicate portMatch = criteriaBuilder.equal(portJoin.get("id"), portId);
            predicates.add(portMatch);
        }
        if (!portOfDestination.isEmpty()) {
            long portId = portOfDestination.getValue().getId();
            Join<Shipment, Booking> bookingJoin = root.join("booking");
            Join<Booking, Port> portJoin = bookingJoin.join("destinationPort");
            Predicate portMatch = criteriaBuilder.equal(portJoin.get("id"), portId);
            predicates.add(portMatch);
        }
        if (!carrier.isEmpty()) {
            long carrierId = carrier.getValue().getId();
            Join<Shipment, Carrier> carrierJoin = root.join("carrier");
            Predicate carrierMatch = criteriaBuilder.equal(carrierJoin.get("id"),  carrierId);
            predicates.add(carrierMatch);
        }
        if (!commodity.isEmpty()) {
            String lowerCaseFilter = commodity.getValue().toLowerCase();
            Predicate commodityMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("commodity")), "%" + lowerCaseFilter + "%");
            predicates.add(commodityMatch);
        }
        if (createFromDate.getValue() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdOn"),
                    criteriaBuilder.literal(createFromDate.getValue())));
        }
        if (createdToDate.getValue() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdOn"),
                    criteriaBuilder.literal(createdToDate.getValue())));
        }
        if (!containerSize.isEmpty()) {
            Join<Shipment, Booking> bookingJoin = root.join("booking");
            Predicate containerSizeMatch = criteriaBuilder.equal(bookingJoin.get("containerSize"), containerSize.getValue());
            predicates.add(containerSizeMatch);
        }
        if (!clientComboBox.isEmpty()) {
            Client clientFilter = clientComboBox.getValue();
            Join<Shipment, Client> clientJoin = root.join("shipper");
            Predicate clientNoMatch = criteriaBuilder.like(criteriaBuilder.lower(
                    clientJoin.get("id")), String.valueOf(clientFilter.getId()));
            predicates.add(clientNoMatch);
        }
        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }
}