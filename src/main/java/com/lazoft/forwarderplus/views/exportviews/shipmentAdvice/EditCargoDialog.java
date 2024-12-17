package com.lazoft.forwarderplus.views.exportviews.shipmentAdvice;

import com.lazoft.forwarderplus.entity.ContainerDetails;
import com.lazoft.forwarderplus.enums.PackageUnit;
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
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class EditCargoDialog extends Dialog {

    private final Set<ContainerDetails> containerList = new HashSet<>();


    private final IntegerField numOfCont = new IntegerField("Container Quantity");
    private final TextField containerSize = new TextField("Container Size");
    private final TextField containerType = new TextField("Container Type");

    private final BigDecimalField grossWeight = new BigDecimalField("Gross Weight");
    private final IntegerField noOfPackages = new IntegerField("Quantity");
    private final ComboBox<PackageUnit> packageUnitComboBox = new ComboBox<>("Unit");
    private final TextArea containerNo = new TextArea("Container No.");
    private final TextArea sealNo = new TextArea("Seal No.");
    private final Button addContainerBtn = new Button("Add");
    private final Button clearAllBtn = new Button("Clear All");

    Button saveButton = new Button("Save");

    public EditCargoDialog() {
        this.setHeaderTitle("Cargo Details");
        this.setWidth(900, Unit.PIXELS);
        this.getFooter().add(clearAllBtn, new Button("Close"), saveButton);

        setAttributes();

        Grid<ContainerDetails> grid = getContainerDetailsGrid();
        FormLayout formLayout = getCargoDetailsForm();

        add(formLayout, grid);
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
        clearAllBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
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
        grid.setItems(containerList);

        packageUnitComboBox.setItems(PackageUnit.values());

        addContainerBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        addContainerBtn.addClickListener(event -> {
            List<ContainerDetails> containerDetails = getContainerDetailsFromEntry();
            containerList.addAll(containerDetails);
            grid.setItems(containerList);
        });
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
        return formLayout;
    }

    private List<ContainerDetails> getContainerDetailsFromEntry() {
        if (isInvalidEntries()) {
            return new ArrayList<>();
        }

        List<String> containers = getBulkEntryItemsAsListFromString(containerNo.getValue());
        List<String> sealNumbers = getBulkEntryItemsAsListFromString(sealNo.getValue());

        if (containers.size() != sealNumbers.size()) {
            NotificationUtil.getNotification("Container No and Seal No quantity are not same!",
                    "", false, NotificationVariant.LUMO_WARNING, 4000).open();
        }

        List<ContainerDetails> containerDetailsList = new LinkedList<>();
        for (int i = 0; i < containers.size(); i++) {
            containerDetailsList.add(new ContainerDetails(containers.get(i), sealNumbers.get(i), grossWeight.getValue(),
                    noOfPackages.getValue(), packageUnitComboBox.getValue()));
        }
        return containerDetailsList;
    }

    private boolean isInvalidEntries() {
        if (grossWeight.getValue() == null || noOfPackages.getValue() == 0 || packageUnitComboBox.getValue() == null) {
            NotificationUtil.getNotification("Please provide correct weight, quantity and unit",
                    "", false, NotificationVariant.LUMO_WARNING, 4000).open();
            return true;
        }
        if (StringUtils.isBlank(containerNo.getValue()) || StringUtils.isBlank(sealNo.getValue())) {
            NotificationUtil.getNotification("Container No or Seal No is empty!",
                    "", false, NotificationVariant.LUMO_WARNING, 4000).open();
            return true;
        }
        return false;
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
