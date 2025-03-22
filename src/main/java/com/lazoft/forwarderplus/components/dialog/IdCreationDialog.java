package com.lazoft.forwarderplus.components.dialog;

import com.lazoft.forwarderplus.entity.IdGeneration;
import com.lazoft.forwarderplus.enums.IdTypes;
import com.lazoft.forwarderplus.services.IdGenerationService;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

public class IdCreationDialog extends Dialog {

    private final TextField idName = new TextField("Name");
    private final TextField idPrefix = new TextField("Prefix");
    private final IntegerField idNumber = new IntegerField("Number");
    private final TextField idSuffix = new TextField("Suffix");
    private final IntegerField idIncrementBy = new IntegerField("Increase By");
    private final ComboBox<IdTypes> idUseFor = new ComboBox<>("Always Use For:");

    private final Button add = new Button("Add");
    private final Button close = new Button("Close");

    private final IdGenerationService idGenerationService;

    public IdCreationDialog(IdGenerationService idGenerationService) {
        this.idGenerationService = idGenerationService;
        H3 h3 = new H3("Add ID");
        this.getHeader().add(h3);

        setFieldAttributes();
        setListeners();

        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("fit-content");
        formLayout.add(idName, idPrefix, idNumber, idSuffix, idIncrementBy, idUseFor);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        this.add(formLayout);
        this.setWidth(700, Unit.PIXELS);
        this.getFooter().add(add, close);
    }

    public void setFieldAttributes() {
        idNumber.setRequired(true);
        idIncrementBy.setRequired(true);
        idUseFor.setHelperText("Optional. Choose a view");
        idUseFor.setItems(IdTypes.values());
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        close.addThemeVariants(ButtonVariant.LUMO_ERROR);
    }

    public void setListeners() {
        add.addClickListener(event -> {
            if (idNumber.getValue() == null) {
                idNumber.setInvalid(true);
                idNumber.setErrorMessage("Please enter a number");
                return;
            }
            if (idIncrementBy.getValue() == null) {
                idIncrementBy.setInvalid(true);
                idIncrementBy.setErrorMessage("Please enter a number");
                return;
            }

            IdGeneration idGeneration = new IdGeneration();
            idGeneration.setName(idName.getValue());
            idGeneration.setPrefix(idPrefix.getValue());
            idGeneration.setIncrementNum(Long.valueOf(idNumber.getValue()));
            idGeneration.setIncrementBy(idIncrementBy.getValue());
            idGeneration.setSuffix(idSuffix.getValue());
            idGeneration.setAlwaysUseFor(idUseFor.getValue());

            try {
                idGenerationService.saveId(idGeneration);
                NotificationUtil.getNotification("Data Saved Successfully!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 4000).open();
                this.close();
            } catch (Exception e) {
                NotificationUtil.getNotification("Unexpected Error while saving ID Generation Process!",
                        e.getMessage(), true, NotificationVariant.LUMO_ERROR, 5000).open();
            }
        });
    }
}
