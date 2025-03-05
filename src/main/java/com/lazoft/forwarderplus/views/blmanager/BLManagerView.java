package com.lazoft.forwarderplus.views.blmanager;

import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.entity.Client;
import com.lazoft.forwarderplus.entity.ContainerDetails;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.services.ShipmentService;
import com.lazoft.forwarderplus.util.AmountFormatter;
import com.lazoft.forwarderplus.util.DateUtil;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
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

@PageTitle("B/L Manager")
@Route(value = "bl-manager", layout = MainLayout.class)
@RolesAllowed("USER")
public class BLManagerView extends VerticalLayout {

    private final TextField shipmentSearchBox = new TextField("Search by B/L No/Booking No");
    private final Button searchButton = new Button(LineAwesomeIcon.SEARCH_SOLID.create());
    private final Grid<Shipment> shipmentGrid = new Grid<>(Shipment.class, false);

    private final FormLayout blFormLayout = new FormLayout();
    private final TextArea shipper = new TextArea("Shipper");
    private final TextArea consignee = new TextArea("Consignee");
    private final TextArea notifyParty = new TextArea("Notify Party");
    private final TextArea deliveryAgent = new TextArea("Delivery Agent");
    private final TextArea alsoNotifyParty = new TextArea("Also Notify Party");
    private final TextArea exportReference = new TextArea("Export Reference");
    private final TextField vesselVoyage = new TextField("Vessel & Voyage");
    private final TextField motherVessel = new TextField("Mother Vessel");
    private final TextArea portOfLoading = new TextArea("Port of Loading");
    private final TextArea portOfDischarge = new TextArea("Port of Discharge");
    private final TextArea placeOfDelivery = new TextArea("Place of Delivery");
    private final TextArea placeOfReceipt = new TextArea("Place of Receipt");
    private final TextArea shipperMarks = new TextArea("Shipper Marks");
    private final TextArea goodsDescription = new TextArea("Goods Description");
    private final TextField grossWeight = new TextField("Gross Weight");
    private final TextField measurement = new TextField("Measurement");
    private final TextField blNo = new TextField("B/L No");
    private final TextField mblNo = new TextField("MB/L No");
    private final TextField bookingNo = new TextField("Booking No");
    private final TextField container = new TextField("Container");
    private final TextField quantity = new TextField("Quantity");
    private final TextArea containerNumbers = new TextArea("Container No");
    private final TextArea containerSeals = new TextArea("Container Seals");
    private final TextField freightTerm = new TextField("Freight Term");
    private final TextArea remarks = new TextArea("Remarks");
    private final ComboBox<String> blType = new ComboBox<>("B/L Type");

    private final ShipmentService shipmentService;


    public BLManagerView(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
        setAttributes();
        setGridProperties();
        setSearchLayout();
        setListener();
        setUpBLLayout();
        add(shipmentGrid, blFormLayout);
    }

    public void setSearchLayout() {
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setAlignItems(FlexComponent.Alignment.END);
        searchLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        searchLayout.setWidth("100%");
        searchLayout.add(shipmentSearchBox, searchButton);
        add(searchLayout);
    }

    public void setGridProperties() {
        shipmentGrid.setVisible(true);
        shipmentGrid.addColumn("mblNo").setHeader("MB/L No").setAutoWidth(true);
        shipmentGrid.addColumn("hblNo").setHeader("HB/L No").setAutoWidth(true);
        shipmentGrid.addColumn("clientInvoiceNo").setHeader("Client Invoice").setAutoWidth(true);
        shipmentGrid.addColumn(shipment -> {
            Booking booking = shipment.getBooking();
            return booking.getNumOfContainers() + " X " + booking.getContainerSize().getContainerSize();
        }).setHeader("MB/L No").setAutoWidth(true);
        shipmentGrid.addColumn(shipment -> shipment.getShipper().getName()).setHeader("Shipper").setAutoWidth(true);
        shipmentGrid.addColumn("createdOn").setHeader("Created").setAutoWidth(true);
        shipmentGrid.addComponentColumn(shipment -> {
          Button button = new Button("Select");
          button.addClickListener(e -> selectShipmentForBLEdit(shipment));
          shipmentGrid.setItems(getShipmentsBySearchString());
//          shipmentGrid.setItems(query -> shipmentService.getShipmentsByFilter(
//                  PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
//                  getFilterSpecification()).stream());
          return button;
        });
    }

    private void selectShipmentForBLEdit(Shipment shipment) {
        shipmentGrid.setVisible(false);
        blFormLayout.setVisible(true);
        shipper.setValue(getClientDetailsForBl(shipment.getShipper()));
        consignee.setValue(getClientDetailsForBl(shipment.getConsignee()));
        notifyParty.setValue(getClientDetailsForBl(shipment.getNotifyParty()));
        exportReference.setValue(DateUtil.getCurrentDateAsString());
        vesselVoyage.setValue(shipment.getSchedule() == null ? "" :
                StringUtils.defaultString(shipment.getSchedule().getPortOfLoadingVesselName()));
        motherVessel.setValue(shipment.getSchedule() == null ? "" :
                StringUtils.defaultString(shipment.getSchedule().getMotherVesselName()));
        portOfLoading.setValue(shipment.getBooking().getLoadingPort().getPortCityAndCountry());
        portOfDischarge.setValue(shipment.getBooking().getDestinationPort().getPortCityAndCountry());
        placeOfDelivery.setValue(shipment.getBooking().getDestinationPort().getPortCityAndCountry());
        placeOfReceipt.setValue(shipment.getBooking().getLoadingPort().getPortCityAndCountry());
        shipperMarks.setValue(shipment.getShipperMarks());
        goodsDescription.setValue(shipment.getGoodsDescription());
        blNo.setValue(StringUtils.defaultIfBlank(shipment.getHblNo(), shipment.getMblNo()));
        mblNo.setValue(StringUtils.defaultString(shipment.getMblNo()));
        bookingNo.setValue(shipment.getBooking().getBookingNo());

        container.setValue(shipment.getBooking().getNumOfContainers() + " X "
                + shipment.getBooking().getContainerSize().getContainerSize());
        freightTerm.setValue(shipment.getShippingTerm().toString());

        if (shipment.getContainerDetails() == null) {
            return;
        }

        grossWeight.setValue(AmountFormatter.getBDRegionFormattedAmount(shipment.getContainerDetails().stream().map(ContainerDetails::getGrossWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add)) + " KG");
        quantity.setValue((shipment.getContainerDetails().stream().map(ContainerDetails::getNoOfPackages)
                .reduce(Integer::sum)).orElse(0) + " " + shipment.getContainerDetails().get(0).getPackageUnit());
        containerNumbers.setValue(shipment.getContainerDetails().stream().map(ContainerDetails::getContainerNo)
                .reduce((cont1, cont2) -> cont1 + ", " + cont2).orElse(""));
        containerSeals.setValue(shipment.getContainerDetails().stream().map(ContainerDetails::getSealNo)
                .reduce((cont1, cont2) -> cont1 + ", " + cont2).orElse(""));
    }

    public String getClientDetailsForBl(Client client) {
        if (client == null) {
            return "";
        }
        return StringUtils.defaultString(client.getName()) + System.lineSeparator() +
                StringUtils.defaultString(client.getAddress()) + System.lineSeparator();
    }

    public void setAttributes() {
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        shipmentSearchBox.setWidth("50%");
        blType.setItems(List.of("Draft", "Original/Non-Negotiable"));
        blType.setValue("Draft");
    }

    public void setListener() {
        searchButton.addClickListener(event -> {
            if (StringUtils.isBlank(shipmentSearchBox.getValue())) {
                return;
            }
            shipmentGrid.setVisible(true);
            blFormLayout.setVisible(false);
            shipmentGrid.setItems(getShipmentsBySearchString());
        });
    }

    private List<Shipment> getShipmentsBySearchString() {
        String searchString = "%" + StringUtils.defaultString(shipmentSearchBox.getValue()).trim().toLowerCase() + "%";
        return shipmentService.getShipmentsByFilter(searchString);
    }


    public void setUpBLLayout() {
        blFormLayout.setVisible(false);
        blFormLayout.setWidth("100%");
        blFormLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 4));
        Hr topDivider1 = new Hr(), topDivider2 = new Hr(), topDivider3 = new Hr(), topDivider4 = new Hr();
        Hr sectionDivider1 = new Hr(), sectionDivider2 = new Hr(), sectionDivider3 = new Hr(), sectionDivider4 = new Hr(),
                sectionDivider5 = new Hr(), sectionDivider6 = new Hr(), sectionDivider7 = new Hr(), sectionDivider8 = new Hr();
        Hr blDivider = new Hr();
        blDivider.getStyle().set("background-color", "#f2f1e8");

        blFormLayout.add(blNo, mblNo, bookingNo, blDivider,
                sectionDivider1,
                shipper, topDivider1, exportReference,
                sectionDivider2,
                consignee, topDivider2, deliveryAgent,
                sectionDivider3,
                notifyParty, topDivider3, alsoNotifyParty,
                sectionDivider4,
                vesselVoyage, topDivider4, motherVessel,
                sectionDivider5,
                portOfLoading, portOfDischarge, placeOfDelivery, placeOfReceipt,
                sectionDivider6,
                shipperMarks, goodsDescription, grossWeight, measurement,
                sectionDivider7,
                containerNumbers, containerSeals,
                sectionDivider8,
                container, quantity, freightTerm, remarks,
                blType, getBlDownloadButtonByType());
        blFormLayout.setColspan(topDivider1, 2);
        blFormLayout.setColspan(topDivider2, 2);
        blFormLayout.setColspan(topDivider3, 2);
        blFormLayout.setColspan(topDivider4, 2);
        blFormLayout.setColspan(sectionDivider1, 4);
        blFormLayout.setColspan(sectionDivider2, 4);
        blFormLayout.setColspan(sectionDivider3, 4);
        blFormLayout.setColspan(sectionDivider4, 4);
        blFormLayout.setColspan(sectionDivider5, 4);
        blFormLayout.setColspan(sectionDivider6, 4);
        blFormLayout.setColspan(sectionDivider7, 4);
        blFormLayout.setColspan(sectionDivider8, 4);
        blFormLayout.setColspan(containerNumbers, 2);
        blFormLayout.setColspan(containerSeals, 2);
    }

    private Anchor getBlDownloadButtonByType() {
        Anchor anchor = new Anchor(new StreamResource("BL_Draft_" + blNo.getValue() +
                ".pdf", (InputStreamFactory) () -> {
            Map<String, Object> parameters = prepareReportParameters();

            String report = "";
            if (blType.getValue().equals("Draft")) {
                report = "bl_draft.jasper";
            } else {
                report = "bl_orig.jasper";
            }

            try (InputStream stream = getClass().getResourceAsStream("/Reports/" + report)) {
                return new ByteArrayInputStream(JasperRunManager
                        .runReportToPdf(stream, parameters, new JREmptyDataSource(1)));
            } catch (JRException | IOException e) {
                throw new RuntimeException(e);
            }
        }), "");
        anchor.getElement().setAttribute("download", true);
        Button downloadButton = new Button("Download B/L");
        downloadButton.setIcon(LineAwesomeIcon.PRINT_SOLID.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        anchor.add(downloadButton);
        return anchor;
    }

    private Map<String, Object> prepareReportParameters() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("REMARKS", remarks.getValue());
        paramMap.put("FREIGHT", freightTerm.getValue());
        paramMap.put("CONTAINER_SEAL", containerSeals.getValue());
        paramMap.put("CONTAINER_NO", containerNumbers.getValue());
        paramMap.put("QUANTITY", quantity.getValue());
        paramMap.put("CONTAINER", container.getValue());
        paramMap.put("MEASUREMENT", measurement.getValue());
        paramMap.put("GROSS_WEIGHT", grossWeight.getValue());
        paramMap.put("DESCRIPTION", goodsDescription.getValue());
        paramMap.put("SHIPPER_MARKS", shipperMarks.getValue());
        paramMap.put("BL_NO", blNo.getValue());
        paramMap.put("MBL_NO", blNo.getValue());
        paramMap.put("BOOKING_NO", blNo.getValue());
        paramMap.put("EXPORT_REFERENCE", exportReference.getValue());
        paramMap.put("ALSO_NOTIFY_PARTY", alsoNotifyParty.getValue());
        paramMap.put("DELIVERY_AGENT", deliveryAgent.getValue());
        paramMap.put("PLACE_OF_RECEIPT", placeOfReceipt.getValue());
        paramMap.put("PLACE_OF_DELIVERY", placeOfDelivery.getValue());
        paramMap.put("PORT_OF_DISCHARGE", portOfDischarge.getValue());
        paramMap.put("PORT_OF_LOADING", portOfLoading.getValue());
        paramMap.put("VESSEL_VOYAGE", vesselVoyage.getValue());
        paramMap.put("MOTHER_VESSEL", motherVessel.getValue());
        paramMap.put("NOTIFY_PARTY", notifyParty.getValue());
        paramMap.put("CONSIGNEE", consignee.getValue());
        paramMap.put("SHIPPER", shipper.getValue());
        return paramMap;
    }
}
