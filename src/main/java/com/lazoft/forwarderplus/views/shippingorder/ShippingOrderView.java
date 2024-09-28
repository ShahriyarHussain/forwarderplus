package com.lazoft.forwarderplus.views.shippingorder;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.ContainerSize;
import com.lazoft.forwarderplus.services.*;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@PageTitle("Shipping Order")
@Route(value = "shipping-order", layout = MainLayout.class)
@RolesAllowed("USER")
@Uses(Icon.class)
public class ShippingOrderView extends Div {

//    private Grid<SamplePerson> grid;
//
//    private final Filters filters;
//    private final SamplePersonService samplePersonService;
//    private final BookingService bookingService;
//    private final CarrierService carrierService;
//    private final ShipmentService shipmentService;
//
//    public ShippingOrderView(SamplePersonService SamplePersonService, BookingService bookingService, CarrierService carrierService, ShipmentService shipmentService) {
//        this.samplePersonService = SamplePersonService;
//        this.bookingService = bookingService;
//        this.carrierService = carrierService;
//        this.shipmentService = shipmentService;
//
//        setSizeFull();
//        addClassNames("shipping-order-view");
//
//        filters = new Filters(this::refreshGrid);
//        VerticalLayout layout = new VerticalLayout(filters, createGrid());
//        layout.setSizeFull();
//        layout.setPadding(false);
//        layout.setSpacing(false);
//        add(layout);
//    }
//
//
//    public static class Filters extends Div implements Specification<Shipment> {
//
//        private final TextField bookingNo = new TextField("Booking No");
//        private final TextField blNo = new TextField("Bill Of Lading No");
//        private final ComboBox<ContainerSize> containerSize = new ComboBox<>("Container");
//        private final DatePicker createdDate = new DatePicker("Created Date");
//        private final ComboBox<Carrier> carrierComboBox = new ComboBox<>("Carrier");
//        private final ComboBox<Port> destinationPort = new ComboBox<>("Destination Port");
//
//        public Filters(Runnable onSearch, CarrierService carrierService, PortService portService) {
//
//            setWidthFull();
//            addClassName("filter-layout");
//            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
//                    LumoUtility.BoxSizing.BORDER);
//            bookingNo.setPlaceholder("Booking No");
//            blNo.setPlaceholder("B/L No");
//
//            carrierComboBox.setItems(carrierService.getAllCarriers());
//            destinationPort.setItems(portService.getAllPorts());
//            destinationPort.addClassName("double-width");
//
//            // Action buttons
//            Button resetBtn = new Button("Reset");
//            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
//            resetBtn.addClickListener(e -> {
//                bookingNo.clear();
//                blNo.clear();
//                containerSize.clear();
//                createdDate.clear();
//                //endDate.clear();
//                carrierComboBox.clear();
//                destinationPort.clear();
//                onSearch.run();
//            });
//            Button searchBtn = new Button("Search");
//            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//            searchBtn.addClickListener(e -> onSearch.run());
//
//            Div actions = new Div(resetBtn, searchBtn);
//            actions.addClassName(LumoUtility.Gap.SMALL);
//            actions.addClassName("actions");
//
//            add(bookingNo, containerSize, createDateRangeFilter(), carrierComboBox, destinationPort, actions);
//        }
//
//        private Component createDateRangeFilter() {
//            createdDate.setPlaceholder("From");
//
//            // For screen readers
//            createdDate.setAriaLabel("From date");
//
//            FlexLayout dateRangeComponent = new FlexLayout(createdDate);
//            dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
//            dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);
//
//            return dateRangeComponent;
//        }
//
//        @Override
//        public Predicate toPredicate(Root<Shipment> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//            List<Predicate> predicates = new ArrayList<>();
//
//            if (!bookingNo.isEmpty()) {
//                String bookingNoValue = bookingNo.getValue();
//                Predicate lastNameMatch = criteriaBuilder.like((root.get("bookingNo")), "%" + bookingNoValue + "%");
//                predicates.add(criteriaBuilder.or(firstNameMatch, lastNameMatch));
//            }
//            if (!containerSize.isEmpty()) {
//                String databaseColumn = "phone";
//                String ignore = "- ()";
//
//                String lowerCaseFilter = ignoreCharacters(ignore, containerSize.getValue().toLowerCase());
//                Predicate phoneMatch = criteriaBuilder.like(
//                        ignoreCharacters(ignore, criteriaBuilder, criteriaBuilder.lower(root.get(databaseColumn))),
//                        "%" + lowerCaseFilter + "%");
//                predicates.add(phoneMatch);
//
//            }
//            if (createdDate.getValue() != null) {
//                String databaseColumn = "dateOfBirth";
//                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(databaseColumn),
//                        criteriaBuilder.literal(createdDate.getValue())));
//            }
//            if (endDate.getValue() != null) {
//                String databaseColumn = "dateOfBirth";
//                predicates.add(criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.literal(endDate.getValue()),
//                        root.get(databaseColumn)));
//            }
//            if (!carrierComboBox.isEmpty()) {
//                String databaseColumn = "occupation";
//                List<Predicate> occupationPredicates = new ArrayList<>();
//                for (String occupation : carrierComboBox.getValue()) {
//                    occupationPredicates
//                            .add(criteriaBuilder.equal(criteriaBuilder.literal(occupation), root.get(databaseColumn)));
//                }
//                predicates.add(criteriaBuilder.or(occupationPredicates.toArray(Predicate[]::new)));
//            }
//            if (!destinationPort.isEmpty()) {
//                String databaseColumn = "role";
//                List<Predicate> rolePredicates = new ArrayList<>();
//                for (String role : destinationPort.getValue()) {
//                    rolePredicates.add(criteriaBuilder.equal(criteriaBuilder.literal(role), root.get(databaseColumn)));
//                }
//                predicates.add(criteriaBuilder.or(rolePredicates.toArray(Predicate[]::new)));
//            }
//            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
//        }
//
//        private String ignoreCharacters(String characters, String in) {
//            String result = in;
//            for (int i = 0; i < characters.length(); i++) {
//                result = result.replace("" + characters.charAt(i), "");
//            }
//            return result;
//        }
//
//        private Expression<String> ignoreCharacters(String characters, CriteriaBuilder criteriaBuilder,
//                Expression<String> inExpression) {
//            Expression<String> expression = inExpression;
//            for (int i = 0; i < characters.length(); i++) {
//                expression = criteriaBuilder.function("replace", String.class, expression,
//                        criteriaBuilder.literal(characters.charAt(i)), criteriaBuilder.literal(""));
//            }
//            return expression;
//        }
//
//    }
//
//    private Component createGrid() {
//        grid = new Grid<>(SamplePerson.class, false);
//        grid.addColumn("firstName").setAutoWidth(true);
//        grid.addColumn("lastName").setAutoWidth(true);
//        grid.addColumn("email").setAutoWidth(true);
//        grid.addColumn("phone").setAutoWidth(true);
//        grid.addColumn("dateOfBirth").setAutoWidth(true);
//        grid.addColumn("occupation").setAutoWidth(true);
//        grid.addColumn("role").setAutoWidth(true);
//
//        grid.setItems(query -> samplePersonService.list(
//                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
//                filters).stream());
//        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
//        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
//
//        return grid;
//    }
//
//    private void refreshGrid() {
//        grid.getDataProvider().refreshAll();
//    }

}
