package com.lazoft.forwarderplus.components.common;

import com.lazoft.forwarderplus.dto.ReportOptionsDto;
import com.lazoft.forwarderplus.entity.BankDetails;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.enums.View;
import com.lazoft.forwarderplus.util.DateUtil;
import com.lazoft.forwarderplus.util.NotificationUtil;
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
import java.util.Map;

public class ReportOptionsDialog extends Dialog {

    private final Checkbox useHbl = new Checkbox("Use HB/L instead of MB/L?");
    private final Checkbox useConsignee = new Checkbox("Use Consignee instead of Notify ?");
    private final DatePicker reportDate = new DatePicker("Report Date");
    private final Checkbox showRespondentEmail = new Checkbox("Show email?");
    private final Checkbox showRespondentPhone = new Checkbox("Show contact no?");
    private final Checkbox showDesignation = new Checkbox("Show designation?");
    private final ComboBox<User> respondent = new ComboBox<>("Contact Details");

    private final Checkbox showBankDetails = new Checkbox("Show Bank Details?");
    private final Checkbox showEarlyPaymentMessage = new Checkbox("Show Payment Message?");
    private final ComboBox<BankDetails> bankDetails = new ComboBox<>("Bank Details");

    private final ReportOptionsDto reportOptionsDto;
    private final Map<String, Object> parameters;

    public ReportOptionsDialog(ReportOptionsDto reportOptionsDto) {
        this.reportOptionsDto = reportOptionsDto;
        parameters = reportOptionsDto.getParameters();
        setHeaderTitle(reportOptionsDto.getView().getViewName() + " is ready!");
        setAttributes();
        setValuesToFields();
        setListeners();

        getFooter().add(new Button("Close", e -> this.close()));
        FormLayout formLayout = getReportOptionsFormLayout();
        Anchor downloadAdviceAnchor = getReportDownloadAnchor();
        add(new Hr(), new H3("Report Options"), formLayout);
        getFooter().add(downloadAdviceAnchor);
        setCloseOnOutsideClick(false);
        setCloseOnEsc(true);
    }

    private void setListeners() {
        showRespondentEmail.addValueChangeListener(event -> parameters.put("SHOW_EMAIL", event.getValue()));
        useHbl.addValueChangeListener(event -> parameters.put("SHOW_HBL", event.getValue()));
        useConsignee.addValueChangeListener(event -> parameters.put("SHOW_CONSIGNEE", event.getValue()));
        showBankDetails.addValueChangeListener(event -> parameters.put("SHOW_BANK", event.getValue()));
        showEarlyPaymentMessage.addValueChangeListener(event -> parameters.put("SHOW_PAYMENT", event.getValue()));
        showDesignation.addValueChangeListener(event -> parameters.put("SHOW_DESIGNATION", event.getValue()));
        showRespondentPhone.addValueChangeListener(event -> parameters.put("SHOW_PHONE", event.getValue()));
        reportDate.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                parameters.put("REPORT_DATE", DateUtil.getDateAsString(event.getValue()));
            }
        });

        bankDetails.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                return;
            }
            BankDetails bankDetails = event.getValue();
            parameters.put("BANK_NAME", bankDetails.getBankName());
            parameters.put("AC_NAME", bankDetails.getAccName());
            parameters.put("AC_NO", bankDetails.getAccNo());
            parameters.put("ROUTING_NO", bankDetails.getRoutingNo());
            parameters.put("BRANCH", bankDetails.getBranchName());
        });

        respondent.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                return;
            }
            User user = event.getValue();
            parameters.put("SIGNED_BY", user.getName());
            parameters.put("SIGNED_BY_EMAIL", user.getEmail());
            parameters.put("SIGNED_BY_CONTACT", user.getContactNo());
            parameters.put("SIGNED_BY_DESIGNATION", user.getDesignation());
        });
    }

    private FormLayout getReportOptionsFormLayout() {
        FormLayout layout = new FormLayout();
        layout.add(useHbl, useConsignee, showBankDetails, showEarlyPaymentMessage, showDesignation, showRespondentEmail,
                showRespondentPhone, reportDate, bankDetails, respondent);
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 6));
        layout.setColspan(respondent, 2);
        layout.setColspan(bankDetails, 2);
        return layout;
    }

    private void setValuesToFields() {
        User user = reportOptionsDto.getUser();

        useConsignee.setEnabled(!StringUtils.isBlank(reportOptionsDto.getConsignee()));
        useConsignee.setValue(false);
        parameters.put("SHOW_CONSIGNEE", useConsignee.getValue());

        useHbl.setEnabled(!StringUtils.isBlank(reportOptionsDto.getHblNo()));
        useHbl.setValue(false);
        parameters.put("SHOW_HBL", useHbl.getValue());

        showRespondentEmail.setEnabled(!StringUtils.isBlank(user.getEmail()));
        showRespondentEmail.setValue(!StringUtils.isBlank(user.getEmail()));
        parameters.put("SHOW_EMAIL", showRespondentEmail.getValue());

        showDesignation.setEnabled(!StringUtils.isBlank(user.getDesignation()));
        showDesignation.setValue(!StringUtils.isBlank(user.getDesignation()));
        parameters.put("SHOW_DESIGNATION", showDesignation.getValue());

        showRespondentPhone.setValue(true);
        parameters.put("SHOW_PHONE", showRespondentPhone.getValue());

        respondent.setItems(reportOptionsDto.getUsers());
        respondent.setValue(user);
        setRespondentValues(user);

        reportDate.setValue(reportOptionsDto.getReportDate());
        parameters.put("REPORT_DATE", DateUtil.getDateAsString(reportDate.getValue()));

        if (reportOptionsDto.getView() == View.SHIPMENT_INVOICE) {
            bankDetails.setItems(reportOptionsDto.getBankDetailsList());
            bankDetails.setValue(reportOptionsDto.getBankDetailsList().get(0));
            showBankDetails.setValue(true);
            parameters.put("SHOW_BANK", true);

            showEarlyPaymentMessage.setValue(true);
            parameters.put("SHOW_PAYMENT", true);
        }
    }

    private void setRespondentValues(User user) {
        parameters.put("SIGNED_BY", user.getName());
        parameters.put("SIGNED_BY_EMAIL", user.getEmail());
        parameters.put("SIGNED_BY_CONTACT", user.getContactNo());
        parameters.put("SIGNED_BY_DESIGNATION", user.getDesignation());
    }

    private void setAttributes() {
        showBankDetails.setVisible(reportOptionsDto.getView() == View.SHIPMENT_INVOICE);
        showEarlyPaymentMessage.setVisible(reportOptionsDto.getView() == View.SHIPMENT_INVOICE);
        bankDetails.setVisible(reportOptionsDto.getView() == View.SHIPMENT_INVOICE);

        useHbl.setVisible(reportOptionsDto.getView() == View.SHIPMENT_ADVICE);
        useConsignee.setVisible(reportOptionsDto.getView() == View.SHIPMENT_ADVICE);

        reportDate.setValue(reportOptionsDto.getReportDate());

        respondent.setItemLabelGenerator(User::getName);
        bankDetails.setItemLabelGenerator(details-> details.getBankName() + ", A/C: " + details.getAccName());
    }

    private Anchor getReportDownloadAnchor() {
        Anchor anchor = new Anchor(new StreamResource(reportOptionsDto.getFileName() + ".pdf",
                (InputStreamFactory) () -> {
                    try (InputStream stream = getClass().getResourceAsStream("/Reports/" + reportOptionsDto.getReportSourceFileName())) {
                        return new ByteArrayInputStream(JasperRunManager
                                .runReportToPdf(stream, parameters, new JREmptyDataSource(1)));
                    } catch (JRException | IOException e) {
//                        NotificationUtil.getNotification("Error while generating report", e.getMessage(),
//                                true, NotificationVariant.LUMO_ERROR, 5000).open();
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
