package com.lazoft.forwarderplus.views.exportviews.shipmentInvoice;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.InvoiceService;
import com.lazoft.forwarderplus.util.AmountFormatter;
import com.lazoft.forwarderplus.util.DateUtil;
import com.lazoft.forwarderplus.util.ReportUtil;
import com.vaadin.flow.component.Text;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.textfield.BigDecimalField;
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

    private final BigDecimalField total = new BigDecimalField("Total", BigDecimal.ZERO, "");
    private final Text inWords = new Text("Zero");

    private final Set<InvoiceItem> invoiceItems = new HashSet<>();
    private boolean isSaved = false;

    private final AuthenticatedUser user;
    private final Invoice invoice;
    private final Shipment shipment;

    public ShipmentInvoiceDialog(InvoiceService invoiceService, AuthenticatedUser user, Shipment shipment) {
        this.invoiceService = invoiceService;
        this.user = user;
        this.invoice = invoiceService.getInvoiceFromShipment(shipment);
        this.shipment = shipment;

        this.setWidth("85%");
        this.setHeight("85%");
        this.setHeaderTitle("Create Invoice");

        getFooter().add(closeButton, downloadButton, saveButton);
    }

    private void setFieldAttributes() {
    }

    public void fillUpExistingValues() {
    }

    public void setUpFormLayout() {
    }

    public void setUpInvoiceItemGrid() {
        Grid<InvoiceItem> invoiceItemGrid = new Grid<>();

//        invoiceItemGrid.setItems(invoiceItems);
        invoiceItemGrid.addColumn(InvoiceItem::getDescription).setHeader("Sl");
        invoiceItemGrid.addColumn(InvoiceItem::getDescription).setHeader("Description");
        invoiceItemGrid.addColumn(InvoiceItem::getRate).setHeader("Rate");
        invoiceItemGrid.addColumn(InvoiceItem::getQuantity).setHeader("Quantity");
        invoiceItemGrid.addColumn(InvoiceItem::getItemUnit).setHeader("Unit");
        Grid.Column<InvoiceItem> foreignCurrTotal = invoiceItemGrid.addColumn(InvoiceItem::getTotalInForeignCurr)
                .setHeader("Total In " + foreignCurrComboBox.getValue());
        Grid.Column<InvoiceItem> localCurrTotal = invoiceItemGrid.addColumn(InvoiceItem::getTotalInLocalCurr)
                .setHeader("Total In " + localCurrencyComboBox.getValue());
        invoiceItemGrid.addComponentColumn(invoiceItem -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(event -> {
                invoiceItems.remove(invoiceItem);
                invoiceItemGrid.setVisible(!invoiceItems.isEmpty());
                invoiceItemGrid.setItems(invoiceItems);
                total.setValue(invoiceItems.stream()
                        .map(InvoiceItem::getTotalInLocalCurr)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
                inWords.setText(AmountFormatter.getAmountInWords(total.getValue(), localCurrencyComboBox.getValue()));
            });
            return deleteButton;
        });
        //invoiceItemGrid.setMaxHeight(17, Unit.EM);
        invoiceItemGrid.setVisible(!invoiceItems.isEmpty());
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

        downloadButton.addClickListener(event -> {
            List<String> errors = findErrorsForReportData();
            Dialog dialog = new Dialog();
            dialog.getFooter().add(new Button("Close", e -> dialog.close()));
            if (!errors.isEmpty()) {
                dialog.setHeaderTitle("Errors in Data");
                ListBox<String> listBox = new ListBox<>();
                listBox.setItems(errors);
                dialog.add(new H4("Please fix the following before downloading Advice"), listBox);
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
            confirmDialog.setHeader("Close Shipment Edit Window ?");
            confirmDialog.setText("Are you sure you want to close ? All unsaved changes will be lost.");
            confirmDialog.setCancelable(true);
            confirmDialog.setConfirmButton(new Button("Yes, I am Sure", confirmEvent -> this.close()));
            Button cancel = new Button("Close", confirmEvent -> confirmDialog.close());
            cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
            confirmDialog.setCancelButton(cancel);
            confirmDialog.open();
        });
    }

    private void setValuesToShipmentForSaving() {
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

        BigDecimal grandTotal = invoice.getInvoiceItems().stream().map(InvoiceItem::getTotalInLocalCurr).reduce(BigDecimal.ZERO, BigDecimal::add);
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
}
