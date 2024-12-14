package com.lazoft.forwarderplus.views.dataconfiguration;

import com.lazoft.forwarderplus.entity.SamplePerson;
import com.lazoft.forwarderplus.services.SamplePersonService;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Data Configuration")
@Route(value = "data-configuration", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class DataConfigurationView extends Composite<VerticalLayout> {

    public DataConfigurationView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        H3 h3 = new H3();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        MultiSelectListBox textItems = new MultiSelectListBox();
        VerticalLayout layoutColumn3 = new VerticalLayout();
        Grid multiSelectGrid = new Grid(SamplePerson.class);
        VerticalLayout layoutColumn4 = new VerticalLayout();
        TextField textField = new TextField();
        Button buttonPrimary = new Button();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        h3.setText("Selected Item Name");
        h3.setWidth("max-content");
        layoutRow2.addClassName(Gap.XSMALL);
        layoutRow2.addClassName(Padding.XSMALL);
        layoutRow2.setWidth("100%");
        layoutRow2.getStyle().set("flex-grow", "1");
        layoutColumn2.setSpacing(false);
        layoutColumn2.setPadding(false);
        layoutColumn2.setWidth("min-content");
        layoutColumn2.getStyle().set("flex-grow", "1");
        textItems.setWidth("min-content");
        textItems.getStyle().set("flex-grow", "1");
        setMultiSelectListBoxSampleData(textItems);
        layoutColumn3.addClassName(Gap.XSMALL);
        layoutColumn3.addClassName(Padding.XSMALL);
        layoutColumn3.getStyle().set("flex-grow", "1");
        layoutColumn3.getStyle().set("flex-grow", "1");
        multiSelectGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        multiSelectGrid.setWidth("100%");
        multiSelectGrid.setHeight("100%");
        setGridSampleData(multiSelectGrid);
        layoutColumn4.setWidth("300px");
        layoutColumn4.setHeight("100%");
        textField.setLabel("Text field");
        textField.setWidth("100%");
        buttonPrimary.setText("Button");
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getContent().add(layoutRow);
        layoutRow.add(h3);
        getContent().add(layoutRow2);
        layoutRow2.add(layoutColumn2);
        layoutColumn2.add(textItems);
        layoutRow2.add(layoutColumn3);
        layoutColumn3.add(multiSelectGrid);
        layoutRow2.add(layoutColumn4);
        layoutColumn4.add(textField);
        layoutColumn4.add(buttonPrimary);
    }

    record SampleItem(String value, String label, Boolean disabled) {
    }

    private void setMultiSelectListBoxSampleData(MultiSelectListBox multiSelectListBox) {
        List<SampleItem> sampleItems = new ArrayList<>();
        sampleItems.add(new SampleItem("first", "First", null));
        sampleItems.add(new SampleItem("second", "Second", null));
        sampleItems.add(new SampleItem("third", "Third", Boolean.TRUE));
        sampleItems.add(new SampleItem("fourth", "Fourth", null));
        multiSelectListBox.setItems(sampleItems);
        multiSelectListBox.setItemLabelGenerator(item -> ((SampleItem) item).label());
        multiSelectListBox.setItemEnabledProvider(item -> !Boolean.TRUE.equals(((SampleItem) item).disabled()));
    }

    private void setGridSampleData(Grid grid) {
        grid.setItems(query -> samplePersonService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
    }

    @Autowired()
    private SamplePersonService samplePersonService;
}
