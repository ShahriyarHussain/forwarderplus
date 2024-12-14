package com.lazoft.forwarderplus.views.admin;

import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.enums.Role;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.UserService;
import com.lazoft.forwarderplus.views.MainLayout;
import com.lazoft.forwarderplus.views.shippingorder.ShippingOrderDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.ArrayList;
import java.util.List;


@PageTitle("User Configuration")
@Route(value = "user-config", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class UserConfigurationView extends Div {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUser authenticatedUser;

    private Grid<User> grid;
    private final Filters filters;

    public UserConfigurationView(UserService userService, PasswordEncoder passwordEncoder, AuthenticatedUser authenticatedUser, AuthenticatedUser authenticatedUser1) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticatedUser = authenticatedUser1;

        setSizeFull();
        addClassNames("view-shipments-view");
        filters = new Filters(this::refreshGrid);
        VerticalLayout layout = new VerticalLayout(filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    public static class Filters extends Div implements Specification<User> {

        private final TextField userName = new TextField("Username");
        private final TextField email = new TextField("Email");
        private final Select<UserStatus> status = new Select<>();
        private final DatePicker createFromDate = new DatePicker("Created Date");
        private final DatePicker createdToDate = new DatePicker();

        public Filters(Runnable onSearch) {
            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            userName.setPlaceholder("Booking No");
            email.setPlaceholder("B/L No");

            status.setLabel("User Status");
            status.setItems(UserStatus.values());

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                userName.clear();
                email.clear();
                createFromDate.clear();
                createdToDate.clear();
                status.setValue(status.getEmptyValue());
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(userName, email, status, createDateFilter(), actions);
        }

        private Component createDateFilter() {
            createFromDate.setPlaceholder("From");
            createdToDate.setPlaceholder("To");

            // For screen readers
            createFromDate.setAriaLabel("Created From");
            createdToDate.setAriaLabel("Created From");

            FlexLayout portSelectionComponent = new FlexLayout(createFromDate, new Text(" – "), createdToDate);
            portSelectionComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
            portSelectionComponent.addClassName(LumoUtility.Gap.XSMALL);
            return portSelectionComponent;
        }
        @Override
        public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

            if (!StringUtils.isBlank(userName.getValue())) {
                String userNameValue = StringUtils.defaultIfBlank(userName.getValue(), "");
                Predicate usernameFilter = criteriaBuilder.equal(root.get("username"), userNameValue);
                predicates.add(usernameFilter);
            }
            if (!StringUtils.isBlank(email.getValue())) {
                String emailValue = StringUtils.defaultIfBlank(email.getValue(), "");
                Predicate emailFilter = criteriaBuilder.equal(root.get("email"), emailValue);
                predicates.add(emailFilter);
            }
            if (status.getValue() != null && status.getValue() == UserStatus.LOCKED) {
                Predicate statusFilter = criteriaBuilder.equal(root.get("isUserNotLocked"), false);
                predicates.add(statusFilter);
            }
            if (status.getValue() != null && status.getValue() == UserStatus.UNLOCKED) {
                Predicate statusFilter = criteriaBuilder.equal(root.get("isUserNotLocked"), true);
                predicates.add(statusFilter);
            }
            if (status.getValue() != null && status.getValue() == UserStatus.DELETED) {
                Predicate statusFilter = criteriaBuilder.equal(root.get("isNotTerminated"), false);
                predicates.add(statusFilter);
            }
            if (createFromDate.getValue() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdOn"),
                        criteriaBuilder.literal(createFromDate.getValue())));
            }
            if (createdToDate.getValue() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdOn"),
                        criteriaBuilder.literal(createdToDate.getValue())));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }
    }

    enum UserStatus {
        LOCKED,
        UNLOCKED,
        DELETED,
        ALL
    }

    private Component createGrid() {
        grid = new Grid<>(User.class, false);
        grid.addColumn("username").setHeader("Username").setAutoWidth(true);
        grid.addColumn("name").setHeader("House B/L No").setAutoWidth(true).setSortable(false);
        grid.addColumn(user -> user.getRoles().stream().map(Role::toString).reduce((role1, role2) -> role1 + "," + role2)
                        .orElse("NONE")).setHeader("Roles").setAutoWidth(true).setSortable(false);
        grid.addColumn("email").setHeader("Email").setAutoWidth(true);
        grid.addColumn("designation").setHeader("Designation").setAutoWidth(true);
        grid.addComponentColumn(user -> getIconByStatus(user.isUserNotLocked())).setHeader("Unlocked?").setAutoWidth(true);
        grid.addComponentColumn(user -> getIconByStatus(user.isNotTerminated())).setHeader("Enabled?").setAutoWidth(true);
        grid.addComponentColumn(this::getModifyUserButton).setHeader("Edit").setAutoWidth(true);

        grid.setItems(query -> userService.getUsersByFilters(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private Icon getIconByStatus(boolean isOk) {
        if (isOk) {
            Icon icon = new Icon(VaadinIcon.CHECK_CIRCLE);
            icon.setColor("darkGreen");
            return icon;
        }
        Icon icon = new Icon(VaadinIcon.CLOSE_CIRCLE);
        icon.setColor("darkRed");
        return icon;
    }

    private Button getModifyUserButton(User user) {
        Button create = new Button(LineAwesomeIcon.PEN_SOLID.create());
        create.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        create.addClickListener(event -> {
            if (authenticatedUser.get().isEmpty()) {
                Notification notification = new Notification();
                notification.setText("User not logged in!");
                notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                notification.setDuration(4000);
                notification.setPosition(Notification.Position.TOP_END);
                notification.open();
                event.getSource().getUI().ifPresent(ui -> ui.navigate("login"));
                return;
            }
            if (user.getUsername().equals(authenticatedUser.get().get().getUsername())) {
                Notification notification = new Notification();
                notification.setText("You cannot edit your own User Configuration");
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                notification.setDuration(4000);
                notification.setPosition(Notification.Position.TOP_END);
                notification.open();
                return;
            }
            new EditUserDialog(userService, passwordEncoder, user).open();
        });
        return create;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}