package com.lazoft.forwarderplus.views;

import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.views.about.AboutView;
import com.lazoft.forwarderplus.views.admin.UserConfigurationView;
import com.lazoft.forwarderplus.views.blmanager.BLManagerView;
import com.lazoft.forwarderplus.views.chat.ChatView;
import com.lazoft.forwarderplus.views.dashboard.DashboardView;
import com.lazoft.forwarderplus.views.dataconfiguration.DataConfigurationView;
import com.lazoft.forwarderplus.views.finances.FinancesView;
import com.lazoft.forwarderplus.views.newbooking.NewBookingView;
import com.lazoft.forwarderplus.views.shippingorder.ShippingOrderView;
import com.lazoft.forwarderplus.views.viewshipments.ViewShipmentsView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private static final Logger log = LoggerFactory.getLogger(MainLayout.class);
    private H1 viewTitle;

    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("Forwarder-Plus");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        if (accessChecker.hasAccess(DashboardView.class)) {
            nav.addItem(new SideNavItem("Dashboard", DashboardView.class, LineAwesomeIcon.CHART_BAR.create()));
        }

        SideNavItem exportOverview = new SideNavItem("Export Overview");
        exportOverview.setPrefixComponent(LineAwesomeIcon.SHIP_SOLID.create());
        exportOverview.addItem(new SideNavItem("New Booking", NewBookingView.class, LineAwesomeIcon.FOLDER_PLUS_SOLID.create()));
        exportOverview.addItem(new SideNavItem("Shipping Order", ShippingOrderView.class,LineAwesomeIcon.BRIEFCASE_SOLID.create()));
        exportOverview.addItem(new SideNavItem("B/L Manager", BLManagerView.class, LineAwesomeIcon.PAGER_SOLID.create()));

        if (accessChecker.hasAccess(NewBookingView.class)) {
            nav.addItem(exportOverview);
        }
        if (accessChecker.hasAccess(ViewShipmentsView.class)) {
            nav.addItem(new SideNavItem("View Shipments", ViewShipmentsView.class, LineAwesomeIcon.FILTER_SOLID.create()));
        }
        if (accessChecker.hasAccess(FinancesView.class)) {
            nav.addItem(new SideNavItem("Finances", FinancesView.class, LineAwesomeIcon.MONEY_BILL_WAVE_SOLID.create()));
        }
        if (accessChecker.hasAccess(DataConfigurationView.class)) {
            nav.addItem(new SideNavItem("Data Configuration", DataConfigurationView.class, LineAwesomeIcon.WRENCH_SOLID.create()));
        }
        if (accessChecker.hasAccess(DataConfigurationView.class)) {
            nav.addItem(new SideNavItem("User Management", UserConfigurationView.class, LineAwesomeIcon.USER_EDIT_SOLID.create()));
        }
        if (accessChecker.hasAccess(ChatView.class)) {
            nav.addItem(new SideNavItem("Chat", ChatView.class, LineAwesomeIcon.SMS_SOLID.create()));
        }
        if (accessChecker.hasAccess(AboutView.class)) {
            nav.addItem(new SideNavItem("About", AboutView.class, LineAwesomeIcon.QUESTION_CIRCLE_SOLID.create()));
        }
        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName());
//            StreamResource resource = new StreamResource("profile-pic",
//                    () -> new ByteArrayInputStream(user.getProfilePicture()));
//            avatar.setImageResource(resource);
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getName());
            div.add(LineAwesomeIcon.ANGLE_UP_SOLID.create());
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);

            Button logoutButton = new Button("Sign Out");
            logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            logoutButton.setIcon(LineAwesomeIcon.SIGN_OUT_ALT_SOLID.create());
            logoutButton.addClickListener(event -> authenticatedUser.logout());
            userName.getSubMenu().addItem(logoutButton);

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
