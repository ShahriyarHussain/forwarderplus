package com.lazoft.forwarderplus.views.commonViews;

import com.lazoft.forwarderplus.dto.ReportOptionsDto;
import com.lazoft.forwarderplus.entity.BankDetails;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.enums.View;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ReportOptionsDialog extends Dialog {

    private final Checkbox useHbl = new Checkbox("Use HB/L instead of MB/L?");
    private final Checkbox useConsignee = new Checkbox("Use Consignee instead of Notify ?");
    private final DatePicker reportDate = new DatePicker("Report Date");
    private final Checkbox showRespondentEmail = new Checkbox("Show email?");
    private final Checkbox hideRespondentPhone = new Checkbox("Hide contact no?");
    private final Checkbox showDesignation = new Checkbox("Show designation?");
    private final ComboBox<User> respondent = new ComboBox<>("Contact Details");

    private final Checkbox showBankDetails = new Checkbox("Show Bank Details?");
    private final Checkbox showEarlyPaymentMessage = new Checkbox("Show Payment Message?");
    private final ComboBox<BankDetails> bankDetails = new ComboBox<>("Bank Details");

    private final ReportOptionsDto reportOptionsDto;

    public ReportOptionsDialog(ReportOptionsDto reportOptionsDto) {
        this.reportOptionsDto = reportOptionsDto;
        setHeaderTitle(reportOptionsDto.getView().getViewName() + " is ready!");
        setAttributes();
        setValuesToFields();

        getFooter().add(new Button("Close", e -> this.close()));
        FormLayout formLayout = getReportOptionsFormLayout();
        Anchor downloadAdviceAnchor = getReportDownloadAnchor();
        add(new Hr(), new H3("Report Options"), formLayout);
        getFooter().add(downloadAdviceAnchor);
        setCloseOnOutsideClick(false);
        setCloseOnEsc(true);
    }

    private FormLayout getReportOptionsFormLayout() {
        FormLayout layout = new FormLayout();
        layout.add(useHbl, useConsignee, showBankDetails, showEarlyPaymentMessage, showDesignation, showRespondentEmail,
                hideRespondentPhone, reportDate, bankDetails, respondent);
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 6));
        layout.setColspan(respondent, 2);
        layout.setColspan(bankDetails, 2);
        return layout;
    }

    private void setValuesToFields() {
        User user = reportOptionsDto.getUser();
        useConsignee.setEnabled(!StringUtils.isBlank(reportOptionsDto.getConsignee()));
        useHbl.setEnabled(!StringUtils.isBlank(reportOptionsDto.getHblNo()));
        showRespondentEmail.setEnabled(!StringUtils.isBlank(user.getEmail()));
        showDesignation.setEnabled(!StringUtils.isBlank(user.getDesignation()));
        respondent.setItems(reportOptionsDto.getUsers());
        respondent.setValue(user);
        bankDetails.setItems(reportOptionsDto.getBankDetailsList());
    }

    private void setAttributes() {
        showBankDetails.setVisible(reportOptionsDto.getView() == View.SHIPMENT_INVOICE);
        showEarlyPaymentMessage.setVisible(reportOptionsDto.getView() == View.SHIPMENT_INVOICE);
        bankDetails.setVisible(reportOptionsDto.getView() == View.SHIPMENT_INVOICE);

        useHbl.setVisible(reportOptionsDto.getView() == View.SHIPMENT_ADVICE);
        useConsignee.setVisible(reportOptionsDto.getView() == View.SHIPMENT_ADVICE);

        reportDate.setValue(reportOptionsDto.getReportDate());

        showDesignation.setEnabled(true);
        showBankDetails.setEnabled(true);
        showEarlyPaymentMessage.setEnabled(true);

        respondent.setItemLabelGenerator(User::getName);
        bankDetails.setItemLabelGenerator(details-> details.getBankName() + ", A/C: " + details.getAccName());
    }

    private Anchor getReportDownloadAnchor() {
        Anchor anchor = new Anchor(new StreamResource(reportOptionsDto.getFileName() + ".pdf",
                (InputStreamFactory) () -> {
                    try (InputStream stream = getClass().getResourceAsStream("/Reports/" + reportOptionsDto.getReportSourceFileName())) {
                        return new ByteArrayInputStream(JasperRunManager
                                .runReportToPdf(stream, reportOptionsDto.getParameters(), new JREmptyDataSource(1)));
                    } catch (JRException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }), "");
        anchor.getElement().setAttribute("download", true);
        Button downloadButton = new Button("Download " + reportOptionsDto.getView().getViewName());
        downloadButton.setIcon(LineAwesomeIcon.PRINT_SOLID.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        anchor.add(downloadButton);
        return anchor;
    }
}
