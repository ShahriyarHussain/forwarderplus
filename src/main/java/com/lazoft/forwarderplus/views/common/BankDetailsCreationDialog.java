package com.lazoft.forwarderplus.views.common;

import com.lazoft.forwarderplus.entity.BankDetails;
import com.lazoft.forwarderplus.services.BankDetailsService;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

public class BankDetailsCreationDialog extends Dialog {

    public BankDetailsCreationDialog(BankDetailsService bankDetailsService) {
        H3 h3 = new H3("Add Bank Details");
        this.getHeader().add(h3);

        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("fit-content");

        TextField bankName = new TextField("Bank Name");
        bankName.setRequired(true);
        TextField accNo = new TextField("A/C No.");
        accNo.setRequired(true);
        TextField accName = new TextField("A/C Name");
        accName.setRequired(true);
        TextField routingNo = new TextField("Routing No.");
        routingNo.setRequired(true);
        TextField branchName = new TextField("Branch Name");
        branchName.setRequired(true);
        

        Button addButton = new Button("Add", event -> {
            if (isInvalidData(bankName, accNo, accName, routingNo)) {
                NotificationUtil.getNotification("Please fill up all required fields", "", false,
                        NotificationVariant.LUMO_WARNING, 4000).open();
                return;
            }
            BankDetails bankDetails = new BankDetails();
            bankDetails.setBankName(bankName.getValue());
            bankDetails.setAccNo(accNo.getValue());
            bankDetails.setAccName(accName.getValue());
            bankDetails.setRoutingNo(routingNo.getValue());
            bankDetails.setBranchName(branchName.getValue());

            try {
                bankDetailsService.saveBankDetails(bankDetails);
                NotificationUtil.getNotification("Bank Details Created Successfully!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 4000).open();
                this.close();
            } catch (Exception e) {
                NotificationUtil.getNotification("Unexpected Error while saving Bank Details!", e.getMessage(), true,
                        NotificationVariant.LUMO_ERROR, 5000).open();
            }
        });
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button closeButton = new Button("Close", event -> this.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        formLayout.add(bankName, branchName, routingNo, accName, accNo);

        this.add(formLayout);
        this.setWidth(700, Unit.PIXELS);
        this.getFooter().add(addButton, closeButton);
    }

    private boolean isInvalidData(TextField bankName, TextField accNo, TextField accName, TextField routingNo) {
        boolean isInvalid = false;
        if (StringUtils.isBlank(bankName.getValue())) {
            bankName.setInvalid(true);
            isInvalid = true;
        }
        if (StringUtils.isBlank(accNo.getValue())) {
            accNo.setInvalid(true);
            isInvalid = true;
        }
        if (StringUtils.isBlank(accName.getValue())) {
            accName.setInvalid(true);
            isInvalid = true;
        }
        if (StringUtils.isBlank(routingNo.getValue())) {
            routingNo.setInvalid(true);
            isInvalid = true;
        }
        return isInvalid;
    }
}