package com.lazoft.forwarderplus.views.exportviews.shipmentInvoice;

import com.lazoft.forwarderplus.entity.BankDetails;
import com.lazoft.forwarderplus.entity.InvoiceItem;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.vaadin.flow.component.Unit;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ShipmentInvoiceDialog extends Dialog {

    private final Button saveButton = new Button("Save");
    private final Button downloadButton = new Button("Download as PDF");
    private final Button closeButton = new Button("Close");

    private final Checkbox showBankDetails = new Checkbox("Show Bank Details?");
    private final Checkbox showEarlyPaymentMessage = new Checkbox("Show Payment Message?");
    private final Checkbox showRespondentEmail = new Checkbox("Show email?");
    private final Checkbox hideRespondentPhone = new Checkbox("Hide contact no?");
    private final Checkbox showDesignation = new Checkbox("Show designation?");
    private final DatePicker invoiceDate = new DatePicker("Invoice Date");
    private final ComboBox<BankDetails> bankDetails = new ComboBox<>("Bank Details");
    private final ComboBox<User> respondent = new ComboBox<>("Contact Details");

    private boolean isSaved = false;

    private final AuthenticatedUser user;

    public ShipmentInvoiceDialog(AuthenticatedUser user) {
        this.user = user;

        this.setWidth("85%");
        this.setHeight("85%");
        this.setHeaderTitle("Create Invoice");

        getFooter().add(closeButton, downloadButton, saveButton);
    }

    private void setFieldAttributes() {}

    public void fillUpExistingValues() {}

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
                inWords.setText(addSuffixToWordAmountByCurrency(localCurrencyComboBox.getValue(),
                        Util.getAmountInWords(total.getValue())));
            });
            return deleteButton;
        });
        invoiceItemGrid.setMaxHeight(17, Unit.EM);
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

            Anchor downloadAdviceAnchor = getShipmentAdviceDownloadAnchor();
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

    private void setValuesToShipmentForSaving() {}

    private boolean isInvalidEntriesForSave() {return true;}

    private List<String> findErrorsForReportData() {
        return null;
    }

    private Map<String, Object> prepareParamsForShipmentInvoice() {return new HashMap<>();}

    private Anchor getShipmentAdviceDownloadAnchor() {
        Anchor anchor = new Anchor(new StreamResource("Invoice-" + "shipment.getBlNo()" + ".pdf",
                (InputStreamFactory) () -> {
            String report = "invoice.jasper.jasper";
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
