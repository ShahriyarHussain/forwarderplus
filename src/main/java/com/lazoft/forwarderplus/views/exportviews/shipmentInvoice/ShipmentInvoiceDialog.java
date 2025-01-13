package com.lazoft.forwarderplus.views.exportviews.shipmentInvoice;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.InvoiceService;
import com.lazoft.forwarderplus.util.AmountFormatter;
import com.lazoft.forwarderplus.util.DateUtil;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.lazoft.forwarderplus.util.ReportUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
public class ShipmentInvoiceDialog extends Dialog {

    private final InvoiceService invoiceService;

    private final Button saveButton = new Button("Save");
    private final Button downloadButton = new Button("Download as PDF");
    private final Button closeButton = new Button("Close");

    private final TextField invoiceNo = new TextField("Invoice No");
    private final Button generateInvoiceNo = new Button(LineAwesomeIcon.ATOM_SOLID.create());

    private final TextField expNo = new TextField("Exp No");
    private final DatePicker expDate = new DatePicker("Exp Date");

    private final BigDecimalField grandTotal = new BigDecimalField("Grand Total", BigDecimal.ZERO, "");
    private final BigDecimalField conversionRate = new BigDecimalField("Conversion Rate", BigDecimal.ONE, "");

    private final TextField description = new TextField("Description");
    private final BigDecimalField price = new BigDecimalField("Price (Per Unit)");
    private final IntegerField quantity = new IntegerField("Quantity");
    private final TextField itemUnit = new TextField("Unit");
    private final Checkbox foreignCurrency = new Checkbox("Foreign Currency ?");

    private final ComboBox<AmountCurrency> foreignCurrComboBox = new ComboBox<>("Carrier Currency");
    private final ComboBox<AmountCurrency> localCurrencyComboBox = new ComboBox<>("Local Currency");

    private final Checkbox showBankDetails = new Checkbox("Show Bank Details?");
    private final Checkbox showEarlyPaymentMessage = new Checkbox("Show Payment Message?");
    private final Checkbox showRespondentEmail = new Checkbox("Show email?");
    private final Checkbox hideRespondentPhone = new Checkbox("Hide contact no?");
    private final Checkbox showDesignation = new Checkbox("Show designation?");
    private final DatePicker invoiceDate = new DatePicker("Invoice Date");
    private final ComboBox<BankDetails> bankDetails = new ComboBox<>("Bank Details");
    private final ComboBox<User> respondent = new ComboBox<>("Contact Details");

    private final Button addItem = new Button(LineAwesomeIcon.PLUS_CIRCLE_SOLID.create());
    private final TextField inWords = new TextField("In Words");
    private final Grid<InvoiceItem> invoiceItemGrid = new Grid<>(InvoiceItem.class, false);
    private Grid.Column<InvoiceItem> foreignCurrTotalColumn;
    private Grid.Column<InvoiceItem> localCurrTotalColumn;

    private final List<InvoiceItem> invoiceItems = new LinkedList<>();
    private boolean isSaved = false;

    private final AuthenticatedUser user;
    private final Invoice invoice;
    private final Shipment shipment;

    public ShipmentInvoiceDialog(InvoiceService invoiceService, AuthenticatedUser user, Shipment shipment) {
        this.invoiceService = invoiceService;
        this.user = user;
        this.invoice = invoiceService.getInvoiceFromShipment(shipment);
        this.shipment = shipment;

        this.setWidth("70%");
        this.setHeight("85%");
        this.setHeaderTitle("Create Invoice");
        this.setCloseOnOutsideClick(false);

        setUpFormLayout();
        setFieldAttributes();
        setUpInvoiceItemGrid();
        fillUpExistingValues();
        setListeners();

        getFooter().add(closeButton, downloadButton, saveButton);
    }

    private void setFieldAttributes() {
        addItem.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        description.setWidth("90%");
        inWords.setReadOnly(true);
        grandTotal.setReadOnly(true);
        invoiceItemGrid.getStyle().set("margin-top", "10px");

        localCurrencyComboBox.setItems(AmountCurrency.values());
        localCurrencyComboBox.setValue(AmountCurrency.BDT);
        localCurrencyComboBox.addValueChangeListener(event -> localCurrTotalColumn
                .setHeader(getTotalColumnLabel(localCurrencyComboBox, false)));

        foreignCurrComboBox.setItems(AmountCurrency.values());
        foreignCurrComboBox.addValueChangeListener(event -> foreignCurrTotalColumn
                .setHeader(getTotalColumnLabel(foreignCurrComboBox, true)));

        invoiceItemGrid.setAllRowsVisible(true);
    }

    public void fillUpExistingValues() {
        if (invoice == null) {
            return;
        }
    }

    public void setUpFormLayout() {
        FormLayout invoiceLayout = new FormLayout();
        HorizontalLayout invoiceComponent = new HorizontalLayout(invoiceNo, generateInvoiceNo);
        invoiceComponent.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        invoiceComponent.setAlignItems(FlexComponent.Alignment.END);
        Hr line = new Hr();
        invoiceLayout.add(invoiceComponent, line, invoiceDate,
                expNo, expDate, localCurrencyComboBox, foreignCurrComboBox, conversionRate);
        invoiceLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 5));
        invoiceLayout.setColspan(line, 3);

        Accordion editInvoicePanel = new Accordion();
        editInvoicePanel.add("Item Details", getAddInvoiceItemForm());
        add(invoiceLayout, editInvoicePanel);
    }

    private FormLayout getAddInvoiceItemForm() {
        FormLayout invoiceItemDetailLayout = new FormLayout();
        HorizontalLayout unitAndAddBtn = new HorizontalLayout(addItem, description);
        unitAndAddBtn.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        unitAndAddBtn.setAlignItems(FlexComponent.Alignment.END);
        Hr split2 = new Hr(), split1 = new Hr();

        invoiceItemDetailLayout.add(unitAndAddBtn, price, quantity, itemUnit, foreignCurrency,
                split1,
                invoiceItemGrid,
                inWords, split2, grandTotal);
        invoiceItemDetailLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 6));
        invoiceItemDetailLayout.setColspan(unitAndAddBtn, 2);
        invoiceItemDetailLayout.setColspan(invoiceItemGrid, 6);
        invoiceItemDetailLayout.setColspan(split1, 6);

        invoiceItemDetailLayout.setColspan(inWords, 2);
        invoiceItemDetailLayout.setColspan(split2, 2);
        invoiceItemDetailLayout.setColspan(grandTotal, 2);
        return invoiceItemDetailLayout;
    }

    public void setUpInvoiceItemGrid() {
        invoiceItemGrid.addColumn(InvoiceItem::getSl).setHeader("Sl").setAutoWidth(true);
        invoiceItemGrid.addColumn(InvoiceItem::getDescription).setHeader("Description");
        invoiceItemGrid.addColumn(item -> item.getPrice() + (foreignCurrency.getValue() ?
                foreignCurrComboBox.getValue().getSymbol() : localCurrencyComboBox.getValue().toString()))
                .setHeader("Price/Unit").setAutoWidth(true);
        invoiceItemGrid.addColumn(item -> StringUtils.defaultIfBlank(String.valueOf(item.getQuantity()), "") +
                        (StringUtils.isBlank(item.getItemUnit()) ? "" : ( "X " + item.getItemUnit()))).setHeader("Quantity").setAutoWidth(true);
        foreignCurrTotalColumn = invoiceItemGrid.addColumn(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .setHeader(getTotalColumnLabel(foreignCurrComboBox, true));
        localCurrTotalColumn = invoiceItemGrid.addColumn(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity()))
                        .multiply(conversionRate.getValue())).setHeader(getTotalColumnLabel(localCurrencyComboBox, false));
        invoiceItemGrid.addComponentColumn(invoiceItem -> {
            Button deleteButton = new Button(LineAwesomeIcon.MINUS_CIRCLE_SOLID.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(event -> {
                invoiceItems.remove(invoiceItem);
                invoiceItemGrid.setItems(invoiceItems);
                grandTotal.setValue(invoiceItems.stream()
                        .map(InvoiceItem::getSubTotalInLocalCurr)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
                inWords.setValue(AmountFormatter.getAmountInWords(grandTotal.getValue(), localCurrencyComboBox.getValue()));
            });
            return deleteButton;
        });
        invoiceItemGrid.setItems(invoiceItems);
    }

    private FormLayout getReportOptionsFormLayout() {
        FormLayout reportConfigLayout = new FormLayout();
        User user = this.user.get().get();
        showRespondentEmail.setEnabled(!StringUtils.isBlank(user.getEmail()));
        showDesignation.setEnabled(!StringUtils.isBlank(user.getDesignation()));
        reportConfigLayout.add(showBankDetails, showEarlyPaymentMessage, showDesignation, showRespondentEmail, hideRespondentPhone,
                invoiceDate, bankDetails, respondent);
        reportConfigLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 5));
        reportConfigLayout.setColspan(respondent, 2);
        return reportConfigLayout;
    }

    private void setListeners() {
        saveButton.addClickListener(event -> {
            if (isInvalidEntriesForSave()) {
                NotificationUtil.getNotification("Please properly provide marked fields", "", false,
                        NotificationVariant.LUMO_WARNING, 4000);
                return;
            }
            try {
                invoiceService.saveInvoice(invoice);
                isSaved = true;
            } catch (Exception e) {
                NotificationUtil.getNotification("Unexpected error while saving Invoice", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 6000);
            }
        });

        addItem.addClickListener(event -> {
            if (isInvalidDataToAddItem()) {
                return;
            }

            InvoiceItem item = getInvoiceItemFromEntries();
            invoiceItems.add(item);
            invoiceItemGrid.setItems(invoiceItems);
            grandTotal.setValue(invoiceItems.stream()
                    .map(InvoiceItem::getSubTotalInLocalCurr)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));// TODO: WHAT IF USER CHANGES CONV RATE AFTER ADDING A FEW ITEMS
            inWords.setValue(AmountFormatter.getAmountInWords(grandTotal.getValue(), localCurrencyComboBox.getValue()));
        });

        downloadButton.addClickListener(event -> {
            List<String> errors = findErrorsForReportData();
            Dialog dialog = new Dialog();
            dialog.getFooter().add(new Button("Close", e -> dialog.close()));
            if (!errors.isEmpty()) {
                dialog.setHeaderTitle("Errors in Data");
                ListBox<String> listBox = new ListBox<>();
                listBox.setItems(errors);
                dialog.add(new H4("Please fix the following before downloading Invoice"), listBox);
                dialog.open();
                return;
            }
            dialog.setHeaderTitle("Invoice is ready!");
            dialog.add(new Hr(), new H3("Report Options"), getReportOptionsFormLayout());

            Anchor downloadAdviceAnchor = getInvoiceDownloadAnchor();
            dialog.getFooter().add(downloadAdviceAnchor);
            dialog.open();
        });

        closeButton.addClickListener(event -> {
            if (isSaved) {
                this.close();
                return;
            }
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Close Shipment Invoice Window ?");
            confirmDialog.setText("Are you sure you want to close ? All unsaved changes will be lost.");
            confirmDialog.setCancelable(true);
            confirmDialog.setConfirmButton(new Button("Yes, I am Sure", confirmEvent -> this.close()));
            Button cancel = new Button("Close", confirmEvent -> confirmDialog.close());
            cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
            confirmDialog.setCancelButton(cancel);
            confirmDialog.open();
        });
    }

    private boolean isInvalidDataToAddItem() {
        if (foreignCurrency.getValue() && foreignCurrComboBox.getValue() == null) {
            foreignCurrComboBox.isInvalid();
            foreignCurrComboBox.setErrorMessage("Must provide currency for FC Items");
        }
        if (price.getValue() == null || price.getValue().equals(BigDecimal.ZERO)) {
            price.setInvalid(true);
            price.setErrorMessage("Price cannot be empty or Zero");
            return true;
        }
        if (!StringUtils.isBlank(itemUnit.getValue()) && quantity.getValue() == null) {
            quantity.setInvalid(true);
            quantity.setErrorMessage("Cannot have Unit without Quantity");
            return true;
        }
        if (StringUtils.isBlank(description.getValue())) {
            description.setInvalid(true);
            description.setErrorMessage("Description cannot be empty");
            return true;
        }
        return false;
    }

    private InvoiceItem getInvoiceItemFromEntries() {
        InvoiceItem item = new InvoiceItem();
        item.setSl(invoiceItems.size() + 1);
        item.setItemUnit(itemUnit.getValue());
        item.setDescription(description.getValue());
        item.setPrice(price.getValue());
        item.setQuantity(quantity.getValue());
        BigDecimal convRate = BigDecimal.ONE;
        if (foreignCurrency.getValue() != null && foreignCurrency.getValue()) {
            item.setSubTotalInForeignCurr(price.getValue().multiply(new BigDecimal(quantity.getValue())));
            convRate = conversionRate.getValue();
        }
        item.setSubTotalInLocalCurr(price.getValue().multiply(new BigDecimal(quantity.getValue())).multiply(convRate));
        item.setId(shipment.getShipmentId() + String.valueOf(item.getSl()));
        return item;
    }

    private boolean isInvalidEntriesForSave() {
        return true;
    }

    private List<String> findErrorsForReportData() {
        return null;
    }

    private Map<String, Object> prepareParamsForShipmentInvoice() {
        final Map<String, Object> parameters = new HashMap<>();

        parameters.put("LOGO_URL", ReportUtil.image_path);

        parameters.put("INVOICE_NO", invoice.getInvoiceNo());
        parameters.put("INVOICE_DATE", DateUtil.getDateAsString(invoiceDate.getValue()));

        parameters.put("SHIPPER_NAME", shipment.getShipper().getName());
        parameters.put("BL_NO", shipment.getMblNo());

        parameters.put("ADDRESS", shipment.getShipper().getAddress());
        parameters.put("VESSEL", shipment.getSchedule().getPortOfLoadingVesselName());

        parameters.put("SHIPPER_EMAIL", shipment.getShipper().getEmail());
        parameters.put("POL_ETD", DateUtil.getDateAsString(shipment.getSchedule().getPortOfLoadingETD()));

        parameters.put("COMMODITY", shipment.getCommodity());
        parameters.put("DEST_ETA", DateUtil.getDateAsString(shipment.getSchedule().getPortOfDestinationETA()));

        parameters.put("PORT_OF_LOADING", shipment.getSchedule().getPortOfLoading().getPortName() + ", "
                + shipment.getSchedule().getPortOfLoading().getPortCountry());
        parameters.put("DEST_PORT", shipment.getSchedule().getPortOfDestination().getPortName() + ", "
                + shipment.getSchedule().getPortOfDestination().getPortCountry());

        int numOfContainers = shipment.getBooking().getNumOfContainers();
        if (numOfContainers > 9) {
            parameters.put("CONTAINERS", numOfContainers + "x" +
                    shipment.getBooking().getContainerSize().getContainerSize());
        } else {
            parameters.put("CONTAINERS", shipment.getContainerDetails().stream().map(ContainerDetails::getContainerNo)
                    .reduce((c1, c2) -> c1 + ", " + c2).orElse(""));
        }
        parameters.put("SHIPPER_INV_NO", invoice.getInvoiceNo());

        parameters.put("EXP_NO", invoice.getExpNo());
        parameters.put("EXP_DATE", DateUtil.getDateAsString(invoice.getExpDate()));

        parameters.put("FOREIGN_CURRENCY", invoice.getForeignCurrency().toString());
        parameters.put("LOCAL_CURRENCY", invoice.getLocalCurrency().toString());
        parameters.put("CONVERSION_RATE", AmountFormatter.getFormattedAmount(invoice.getConversionRate().setScale(2, RoundingMode.UNNECESSARY),
                foreignCurrComboBox.getValue()));

        BigDecimal grandTotal = invoice.getInvoiceItems().stream().map(InvoiceItem::getSubTotalInLocalCurr).reduce(BigDecimal.ZERO, BigDecimal::add);
        parameters.put("TOTAL", AmountFormatter.getFormattedAmount(
                grandTotal.setScale(1, RoundingMode.UNNECESSARY), localCurrencyComboBox.getValue()));
        parameters.put("TOTAL_IN_WORD", AmountFormatter.getAmountInWords(grandTotal, localCurrencyComboBox.getValue()));

        BankDetails bankDetails = invoice.getBankDetails();
        parameters.put("BANK_NAME", bankDetails.getBankName());
        parameters.put("AC_NAME", bankDetails.getAccName());
        parameters.put("AC_NO", bankDetails.getAccNo());
        parameters.put("ROUTING_NO", bankDetails.getRoutingNo());
        parameters.put("BRANCH", bankDetails.getBranchName());

        User contactDetails = user.get().get();
        parameters.put("SIGNED_BY", contactDetails.getName());
        parameters.put("SIGNED_BY_EMAIL", contactDetails.getEmail());
        parameters.put("SIGNED_BY_CONTACT", contactDetails.getContactNo());

        return parameters;
    }

    private Anchor getInvoiceDownloadAnchor() {
        Anchor anchor = new Anchor(new StreamResource("Invoice-" + "shipment.getBlNo()" + ".pdf",
                (InputStreamFactory) () -> {
                    String report = "invoice.jasper";
                    Map<String, Object> parameters = prepareParamsForShipmentInvoice();

                    try (InputStream stream = getClass().getResourceAsStream("/Reports/" + report)) {
                        return new ByteArrayInputStream(JasperRunManager
                                .runReportToPdf(stream, parameters, new JREmptyDataSource(1)));
                    } catch (JRException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }), "");
        anchor.getElement().setAttribute("download", true);
        Button downloadButton = new Button("Download Invoice");
        downloadButton.setIcon(LineAwesomeIcon.PRINT_SOLID.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        anchor.add(downloadButton);
        return anchor;
    }

    private String getTotalColumnLabel(ComboBox<AmountCurrency> comboBox, boolean isFC) {
        String defaultValue = isFC ? "Foreign Currency" : "Local Currency";
        if (comboBox.getValue() == null || StringUtils.isBlank(comboBox.getValue().toString())) {
            return "Total in " + defaultValue;
        }
        return "Total in " + comboBox.getValue().toString();
    }
}
