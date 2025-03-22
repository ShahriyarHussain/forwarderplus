package com.lazoft.forwarderplus.views.export.advice;

import com.lazoft.forwarderplus.entity.ContainerDetails;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.enums.PackageUnit;
import com.lazoft.forwarderplus.services.ShipmentService;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class EditContainerDetailsDialog extends Dialog {
    private final ShipmentService shipmentService;

    private final List<ContainerDetails> containerList = new LinkedList<>();
    private final Shipment shipment;
    private final ShipmentAdviceDialog shipmentAdviceDialog;

    private final IntegerField numOfCont = new IntegerField("Container Quantity");
    private final TextField containerSize = new TextField("Container Size");
    private final TextField containerType = new TextField("Container Type");

    private final BigDecimalField grossWeight = new BigDecimalField("Gross Weight");
    private final IntegerField noOfPackages = new IntegerField("Quantity");
    private final ComboBox<PackageUnit> packageUnitComboBox = new ComboBox<>("Unit");
    private final TextArea containerNo = new TextArea("Container No.");
    private final TextArea sealNo = new TextArea("Seal No.");
    private final Button addContainerBtn = new Button(LineAwesomeIcon.PLUS_CIRCLE_SOLID.create());
    private final Button close = new Button("Close");

    Grid<ContainerDetails> grid = new Grid<>(ContainerDetails.class);

    private final Button saveButton = new Button("Save");

    private boolean disallowSave = true;

    public EditContainerDetailsDialog(Shipment shipment, ShipmentService shipmentService,
                                      ShipmentAdviceDialog shipmentAdviceDialog) {

        this.shipmentAdviceDialog = shipmentAdviceDialog;
        this.shipment = shipment;
        this.shipmentService = shipmentService;
        this.setHeaderTitle("Container Details");
        this.setWidth(900, Unit.PIXELS);
        this.getFooter().add(close, new Button("Clear All"), saveButton);
        this.setCloseOnOutsideClick(false);

        grid = getContainerDetailsGrid();
        FormLayout formLayout = getCargoDetailsForm();

        setAttributes();
        setListeners();
        fillUpExistingValues();

        add(formLayout, grid);
    }

    private void fillUpExistingValues() {
        if (shipment.getContainerDetails() != null && !shipment.getContainerDetails().isEmpty()) {
            containerList.addAll(shipment.getContainerDetails());
        }
        grid.setItems(containerList);
        numOfCont.setValue(shipment.getNumOfContainers());
        containerSize.setValue(shipment.getBooking().getContainerSize().getContainerSize());
        containerType.setValue(shipment.getBooking().getContainerType().getContainerType());
    }

    private void setAttributes() {
        containerNo.setHelperText("Separate values with comma or newline to add all at once");
        sealNo.setHelperText("Separate values with comma or newline to add all at once");
        grossWeight.setHelperText("Per container");
        numOfCont.setReadOnly(true);
        containerSize.setReadOnly(true);
        containerType.setReadOnly(true);
        addContainerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        close.addThemeVariants(ButtonVariant.LUMO_ERROR);
        packageUnitComboBox.setWidth("80%");
        packageUnitComboBox.setItems(PackageUnit.values());
    }

    private void setListeners() {
        saveButton.addClickListener(event -> {
            try {
                if (numOfCont.getValue() != containerList.size() && disallowSave) {
                    NotificationUtil.getNotification("Number of containers does not match with specified data in " +
                                    "Shipment Details. If you still want to save, click Save again.",
                            "", false, NotificationVariant.LUMO_WARNING, 6000).open();
                    disallowSave = false;
                    return;
                }
                shipmentService.addContainerDetailsToShipment(shipment, containerList);
                NotificationUtil.getNotification("Saved Successfully!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 3000).open();
            } catch (Exception e) {
                log.error("Error is saving container details", e);
                NotificationUtil.getNotification("Unexpected Error! Could not save data.", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 5000).open();
            }
        });

        addContainerBtn.addClickListener(event -> {
            addContainersToList();
        });

        close.addClickListener(event -> {
            shipmentAdviceDialog.fillUpExistingValues();
            close();
        });
    }

    private void addContainersToList() {
        if (isInvalidEntries()) {
            return;
        }

        List<String> containers = getBulkEntryItemsAsListFromString(containerNo.getValue());
        List<String> sealNumbers = getBulkEntryItemsAsListFromString(sealNo.getValue());
        StringBuilder duplicateContainers = new StringBuilder();
        List<ContainerDetails> containerDetailsList = new LinkedList<>();

        BigDecimal numOfContainers = new BigDecimal(containers.size());

        BigDecimal grossWeightPerContainer = grossWeight.getValue().divide(numOfContainers, 0, RoundingMode.FLOOR);
        int quantityPerContainer = noOfPackages.getValue() / containers.size();

        for (int i = 0; i < containers.size(); i++) {
            containerDetailsList.add(new ContainerDetails(containers.get(i), sealNumbers.get(i), grossWeightPerContainer,
                    quantityPerContainer, packageUnitComboBox.getValue(), shipment.getShipmentId()));
        }

        boolean itemExists = false;
        for (ContainerDetails containerDetail : containerDetailsList) {
            if (containerList.contains(containerDetail)) {
                itemExists = true;
                duplicateContainers.append("Container/Seal: ").append(containerDetail.getContainerNo()).append("/")
                        .append(containerDetail.getSealNo());
                continue;
            }
            containerList.add(containerDetail);
        }
        grid.setItems(containerList);

        if (itemExists) {
            NotificationUtil.getNotification("Valid Items Added. Some items were skipped because they already exist",
                    duplicateContainers.toString(), true, NotificationVariant.LUMO_WARNING, 6000).open();
        } else {
            NotificationUtil.getNotification("Added Successfully!", "", false,
                    NotificationVariant.LUMO_PRIMARY, 3000).open();
        }
    }

    private Grid<ContainerDetails> getContainerDetailsGrid() {
        Grid<ContainerDetails> grid = new Grid<>(ContainerDetails.class, false);
        grid.addColumn("containerNo").setHeader("Container No").setAutoWidth(true).setSortable(false);
        grid.addColumn("sealNo").setHeader("Seal No").setAutoWidth(true).setSortable(false);
        grid.addColumn("grossWeight").setHeader("Gross Weight").setAutoWidth(true).setSortable(false);
        grid.addColumn("noOfPackages").setHeader("Quantity").setAutoWidth(true).setSortable(false);
        grid.addColumn("packageUnit").setHeader("Unit").setAutoWidth(true).setSortable(false);
        grid.addComponentColumn(containerDetails -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(event -> {
                containerList.remove(containerDetails);
                grid.setItems(containerList);
            });
            return deleteButton;
        }).setHeader("Delete");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        return grid;
    }

    private FormLayout getCargoDetailsForm() {
        FormLayout formLayout = new FormLayout();
        HorizontalLayout unitAndAddBtn = new HorizontalLayout(packageUnitComboBox, addContainerBtn);
        unitAndAddBtn.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        unitAndAddBtn.setAlignItems(FlexComponent.Alignment.END);
        Hr line = new Hr();

        formLayout.add(numOfCont, containerSize, containerType, new Text(""),
                line,
                containerNo, sealNo,
                grossWeight, noOfPackages, unitAndAddBtn);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 4));
        formLayout.setColspan(line, 4);
        formLayout.setColspan(containerNo, 2);
        formLayout.setColspan(sealNo, 2);
        formLayout.setColspan(unitAndAddBtn, 2);
        return formLayout;
    }

    private boolean isInvalidEntries() {
        disallowSave = true;
        boolean isInvalid = false;
        if (grossWeight.getValue() == null || grossWeight.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            grossWeight.setInvalid(true);
            grossWeight.setErrorMessage("Gross weight cannot be null or ZERO");
            isInvalid = true;
        }
        if (noOfPackages.getValue() == null || noOfPackages.getValue() == 0) {
            noOfPackages.setInvalid(true);
            noOfPackages.setErrorMessage("Quantity cannot be ZERO");
            isInvalid = true;
        }
        if (packageUnitComboBox.getValue() == null) {
            packageUnitComboBox.setInvalid(true);
            packageUnitComboBox.setErrorMessage("Must select a unit");
            isInvalid = true;
        }
        if (StringUtils.isBlank(containerNo.getValue())) {
            containerNo.setInvalid(true);
            containerNo.setErrorMessage("Must provide container no.");
            isInvalid = true;
        }
        if (StringUtils.isBlank(sealNo.getValue())) {
            sealNo.setInvalid(true);
            sealNo.setErrorMessage("Must provide seal no.");
            isInvalid = true;
        }
        int numOfContainers = getBulkEntryItemsAsListFromString(containerNo.getValue()).size();
        if (numOfContainers !=
                getBulkEntryItemsAsListFromString(sealNo.getValue()).size()) {
            String errorMessage = "Number of Container and Seal No should be same!";
            containerNo.setInvalid(true);
            sealNo.setInvalid(true);
            containerNo.setErrorMessage(errorMessage);
            sealNo.setErrorMessage(errorMessage);
            NotificationUtil.getNotification(errorMessage,"", false,
                    NotificationVariant.LUMO_WARNING, 4000).open();
            isInvalid = true;
        }
        return isInvalid;
    }

    private List<String> getBulkEntryItemsAsListFromString(String items) {
        char LINE_BREAK = '\n', COMMA = ',';
        List<String> itemList = new LinkedList<>();

        StringBuilder item = new StringBuilder();
        for (char c : items.toCharArray()) {
            String itemTrimmed = item.toString().trim();
            if (c == LINE_BREAK || c == COMMA) {
                if (!itemTrimmed.isEmpty()) {
                    itemList.add(itemTrimmed);
                }
                item = new StringBuilder();
                continue;
            }
            item.append(c);
        }
        if (!item.toString().trim().isEmpty()) {
            itemList.add(item.toString().trim());
        }
        return itemList;
    }
}
