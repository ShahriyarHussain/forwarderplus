package com.lazoft.forwarderplus.views.config;

import com.lazoft.forwarderplus.components.common.*;
import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.services.*;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.*;

@Slf4j
@PageTitle("Data Configuration")
@Route(value = "data-configuration", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "IT"})
public class DataConfigurationView extends VerticalLayout {

    private final Grid<Client> clientGrid = new Grid<>(Client.class, false);
    private final Grid<BankDetails> bankDetailsGrid = new Grid<>(BankDetails.class, false);
    private final Grid<Port> portGrid = new Grid<>(Port.class, false);
    private final Grid<Carrier> carrierGrid = new Grid<>(Carrier.class, false);
    private final Grid<IdGeneration> idGrid = new Grid<>(IdGeneration.class, false);

    private final ListBox<GridDataType> dataItems = new ListBox<>();
    private final Button addNewButton = new Button("Add New", LineAwesomeIcon.PLUS_CIRCLE_SOLID.create());
    private final Button refreshButton = new Button("Refresh", LineAwesomeIcon.SYNC_ALT_SOLID.create());
    private final Button deleteButton = new Button("Delete", LineAwesomeIcon.TRASH_ALT_SOLID.create());
    private final H3 title = new H3("Edit Clients");

    private final ClientService clientService;
    private final PortService portService;
    private final BankDetailsService bankDetailsService;
    private final CarrierService carrierService;
    private final IdGenerationService idGenerationService;

    private final Map<DataType, Grid> gridMap = Map.of(DataType.CLIENT, clientGrid, DataType.BANK_DETAILS, bankDetailsGrid,
            DataType.PORT, portGrid, DataType.CARRIER, carrierGrid, DataType.ID_GENERATION, idGrid);

    enum DataType {
        CLIENT, BANK_DETAILS, PORT, CARRIER, ID_GENERATION
    }

    public DataConfigurationView(ClientService clientService, PortService portService,
                                 BankDetailsService bankDetailsService, CarrierService carrierService,
                                 IdGenerationService idGenerationService) {
        this.clientService = clientService;
        this.portService = portService;
        this.bankDetailsService = bankDetailsService;
        this.carrierService = carrierService;
        this.idGenerationService = idGenerationService;

        setAttributes();
        setListeners();
        setListBoxData();
        setClientGrid();
        setBankDetailsGrid();
        setCarrierGrid();
        setPortGrid();
        setIdGrid();
        refreshData(DataType.CLIENT);
        makeGridVisible(DataType.CLIENT);

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();

        VerticalLayout dataListLayout = new VerticalLayout(dataItems);
        dataListLayout.setHeight("100%");
        dataListLayout.setWidth("10%");

        VerticalLayout gridLayout = new VerticalLayout(clientGrid, bankDetailsGrid, portGrid, carrierGrid, idGrid);
        gridLayout.setHeight("100%");
        gridLayout.setWidth("80%");

        VerticalLayout editOptionLayout = new VerticalLayout(refreshButton, addNewButton, deleteButton);
        editOptionLayout.setHeight("100%");
        editOptionLayout.setWidth("10%");

        mainLayout.add(dataListLayout, gridLayout, editOptionLayout);
        add(title, mainLayout);

    }

    private void setAttributes() {
        gridMap.values().forEach(grid -> {
            grid.getStyle().set("hover", "cursor");
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
//            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
            grid.addClassNames(LumoUtility.Border.TOP);
            grid.setHeight("600px");
        });

        addNewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
    }

    record GridDataType(DataType type, String label, boolean enabled) {
    }

    private void setListBoxData() {
        List<GridDataType> gridDataTypes = new ArrayList<>();
        gridDataTypes.add(new GridDataType(DataType.CLIENT, "Client", true));
        gridDataTypes.add(new GridDataType(DataType.BANK_DETAILS, "Bank Details", false));
        gridDataTypes.add(new GridDataType(DataType.CARRIER, "Carrier", false));
        gridDataTypes.add(new GridDataType(DataType.PORT, "Port", false));
        gridDataTypes.add(new GridDataType(DataType.ID_GENERATION, "Id Generation", false));

        dataItems.setItems(gridDataTypes);
        dataItems.setItemLabelGenerator(GridDataType::label);
        dataItems.addValueChangeListener(event -> {
            makeGridVisible(event.getValue().type);
            refreshData(event.getValue().type);
        });
        dataItems.setValue(gridDataTypes.get(0));
    }

    private void makeGridVisible(DataType type) {
        gridMap.keySet().forEach(dataType -> gridMap.get(dataType).setVisible(dataType == type));
        title.setText("Edit " + type.name());
    }

    private void setClientGrid() {
        clientGrid.addColumn("name").setHeader("Name").setAutoWidth(true);
        clientGrid.addColumn("email").setHeader("Email").setAutoWidth(true);
        clientGrid.addColumn("city").setHeader("City").setAutoWidth(true);
        clientGrid.addColumn("country").setHeader("Country").setAutoWidth(true);
        clientGrid.addColumn("address").setHeader("Address").setAutoWidth(false);
        clientGrid.setItems(query -> clientService.getAllClients(PageRequest.of(query.getPage(), query.getPageSize(),
                VaadinSpringDataHelpers.toSpringDataSort(query))).stream());
    }

    private void setBankDetailsGrid() {
        bankDetailsGrid.addColumn("bankName").setHeader("Name").setAutoWidth(true);
        bankDetailsGrid.addColumn("accNo").setHeader("A/C No").setAutoWidth(true);
        bankDetailsGrid.addColumn("accName").setHeader("A/C Name").setAutoWidth(true);
        bankDetailsGrid.addColumn("routingNo").setHeader("Routing No").setAutoWidth(true);
        bankDetailsGrid.addColumn("branchName").setHeader("Branch").setAutoWidth(true);
        bankDetailsGrid.setItems(query -> bankDetailsService.getAllBankDetails(PageRequest.of(query.getPage(), query.getPageSize(),
                VaadinSpringDataHelpers.toSpringDataSort(query))).stream());
    }

    private void setCarrierGrid() {
        carrierGrid.addColumn("name").setHeader("Name");
        carrierGrid.addColumn("country").setHeader("Country");
        carrierGrid.setItems(query -> carrierService.getAllCarriers(PageRequest.of(query.getPage(), query.getPageSize(),
                VaadinSpringDataHelpers.toSpringDataSort(query))).stream());
    }

    private void setPortGrid() {
        portGrid.addColumn("portName").setHeader("Name").setAutoWidth(true);
        portGrid.addColumn("portCountry").setHeader("Email").setAutoWidth(true);
        portGrid.addColumn("portCity").setHeader("Post Code").setAutoWidth(true);
        portGrid.addColumn("portShortCode").setHeader("Short Code").setAutoWidth(true);
        portGrid.setItems(query -> portService.getAllPorts(PageRequest.of(query.getPage(), query.getPageSize(),
                VaadinSpringDataHelpers.toSpringDataSort(query))).stream());
    }

    private void setIdGrid() {
        idGrid.addColumn("name").setHeader("Name").setAutoWidth(true);
        idGrid.addColumn("prefix").setHeader("Prefix").setAutoWidth(true);
        idGrid.addColumn("incrementNum").setHeader("Id Number").setAutoWidth(true);
        idGrid.addColumn("suffix").setHeader("Suffix").setAutoWidth(true);
        idGrid.addColumn("incrementBy").setHeader("Increase By").setAutoWidth(true);
        idGrid.addColumn("alwaysUseFor").setHeader("Use For").setAutoWidth(true);
        idGrid.setItems(query -> idGenerationService.getAllIds(PageRequest.of(query.getPage(),
                query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))).stream());
    }

    private void refreshData(DataType type) {
        gridMap.get(type).getDataProvider().refreshAll();
    }

    private void setListeners() {
        deleteButton.addClickListener(event -> {
            DataType type = dataItems.getValue().type;
            if (type == DataType.CLIENT) {
                deleteSelectedClients((Grid<Client>) gridMap.get(DataType.CLIENT));
            } else if (type == DataType.BANK_DETAILS) {
                deleteSelectedBankDetails((Grid<BankDetails>) gridMap.get(type));
            } else if (type == DataType.CARRIER) {
                deleteSelectedCarriers((Grid<Carrier>) gridMap.get(type));
            } else if (type == DataType.PORT) {
                deleteSelectedPorts((Grid<Port>) gridMap.get(type));
            } else if (type == DataType.ID_GENERATION) {
                deleteSelectedIds((Grid<IdGeneration>) gridMap.get(type));
            }

            refreshData(type);
            NotificationUtil.getNotification("Deleted Successfully!", "", false,
                    NotificationVariant.LUMO_PRIMARY, 2000).open();
        });

        addNewButton.addClickListener(event -> {
            DataType type = dataItems.getValue().type;
            if (type == DataType.CLIENT) {
                new ClientCreationDialog(clientService, new LinkedList<>()).open();
            } else if (type == DataType.BANK_DETAILS) {
                new BankDetailsCreationDialog(bankDetailsService).open();
            } else if (type == DataType.CARRIER) {
                new CarrierCreationDialog(carrierService).open();
            } else if (type == DataType.PORT) {
                new PortCreationDialog(portService).open();
            } else if (type == DataType.ID_GENERATION) {
                new IdCreationDialog(idGenerationService).open();
            }
            refreshData(type);
        });

        refreshButton.addClickListener(event -> {
            DataType type = dataItems.getValue().type;
            refreshData(type);
            NotificationUtil.getNotification("Refreshed Data!", "", false,
                    NotificationVariant.LUMO_PRIMARY, 2000).open();
        });
    }

    private void deleteSelectedIds(Grid<IdGeneration> idGenerationGrid) {
        Set<IdGeneration> selectedItems = idGenerationGrid.getSelectedItems();
        idGenerationService.deleteIds(selectedItems);
    }

    private void deleteSelectedPorts(Grid<Port> portGrid) {
        Set<Port> selectedItems = portGrid.getSelectedItems();
        portService.deletePorts(selectedItems);
    }

    private void deleteSelectedCarriers(Grid<Carrier> carrierGrid) {
        Set<Carrier> selectedItems = carrierGrid.getSelectedItems();
        carrierService.deleteCarriers(selectedItems);
    }

    private void deleteSelectedBankDetails(Grid<BankDetails> bankDetailsGrid) {
        Set<BankDetails> selectedItems = bankDetailsGrid.getSelectedItems();
        bankDetailsService.deleteBankDetails(selectedItems);
    }

    private void deleteSelectedClients(Grid<Client> clientGrid) {
        Set<Client> selectedItems = clientGrid.getSelectedItems();
        clientService.deleteClients(selectedItems);
    }


}
