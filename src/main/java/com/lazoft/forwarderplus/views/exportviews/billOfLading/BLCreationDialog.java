package com.lazoft.forwarderplus.views.exportviews.billOfLading;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.services.BillOfLadingService;
import com.lazoft.forwarderplus.util.AmountFormatter;
import com.lazoft.forwarderplus.util.DateUtil;
import com.lazoft.forwarderplus.util.NotificationUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class BLCreationDialog extends Dialog {

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
    private final Button save = new Button("Save", LineAwesomeIcon.SAVE.create());
    private final Button clear = new Button("Clear Old Data", LineAwesomeIcon.TRASH_ALT_SOLID.create());

    private final Shipment shipment;
    private final BillOfLadingService billOfLadingService;
    private BillOfLading billOfLading;


    public BLCreationDialog(Shipment shipment, BillOfLadingService billOfLadingService) {
        this.shipment = shipment;
        this.billOfLadingService = billOfLadingService;
        setHeaderTitle("Create Bill Of Lading");
        setCloseOnOutsideClick(false);

        setAttributes();
        setUpBLLayout();
        setListeners();
        setBlValuesFromShipmentInfo();
        setBlValuesToFields();
        add(blFormLayout);
        Button closeButton = new Button("Close", e -> this.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        getFooter().add(closeButton, clear, save, getBlDownloadButtonByType());
    }

    private void setBlValuesFromShipmentInfo() {
        Optional<BillOfLading> blData = billOfLadingService.getBillOfLadingByShipmentId(shipment.getShipmentId());
        if (blData.isPresent()) {
            billOfLading = blData.get();
            fillUpNullDataIfUpdated();
            return;
        }
        billOfLading = new BillOfLading();
        billOfLading.setShipmentId(shipment.getShipmentId());
        billOfLading.setShipper(getClientDetailsForBl(shipment.getShipper()));
        billOfLading.setConsignee(getClientDetailsForBl(shipment.getConsignee()));
        billOfLading.setNotifyParty(getClientDetailsForBl(shipment.getNotifyParty()));
        billOfLading.setExportReference(DateUtil.getCurrentDateAsString());
        Schedule schedule = shipment.getSchedule();
        billOfLading.setVesselVoyage(schedule == null ? "" : StringUtils.defaultString(shipment.getSchedule().getPortOfLoadingVesselName()));
        billOfLading.setMotherVessel(schedule == null ? "" : StringUtils.defaultString(shipment.getSchedule().getMotherVesselName()));
        billOfLading.setPortOfLoading(shipment.getBooking().getLoadingPort().getPortCityAndCountry());
        billOfLading.setPortOfDischarge(shipment.getBooking().getDestinationPort().getPortCityAndCountry());
        billOfLading.setPlaceOfDelivery(shipment.getBooking().getDestinationPort().getPortCityAndCountry());
        billOfLading.setPlaceOfReceipt(shipment.getBooking().getLoadingPort().getPortCityAndCountry());
        billOfLading.setShipperMarks(StringUtils.defaultString(shipment.getShipperMarks()));
        billOfLading.setGoodsDescription(StringUtils.defaultString(shipment.getGoodsDescription()));
        billOfLading.setBlNo(StringUtils.defaultIfBlank(shipment.getHblNo(), StringUtils.defaultString(shipment.getMblNo())));
        billOfLading.setMblNo(StringUtils.defaultString(shipment.getMblNo()));
        billOfLading.setBookingNo(shipment.getBooking().getBookingNo());

        billOfLading.setContainer(shipment.getBooking().getNumOfContainers() + " X " + shipment.getBooking().getContainerSize().getContainerSize());
        billOfLading.setFreightTerm(shipment.getShippingTerm() == null ? "" : shipment.getShippingTerm().toString());

        if (shipment.getContainerDetails() == null || shipment.getContainerDetails().isEmpty()) {
            return;
        }

        billOfLading.setGrossWeight(AmountFormatter.getBDRegionFormattedAmount(shipment.getContainerDetails().stream().map(ContainerDetails::getGrossWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add)) + " KG");
        billOfLading.setQuantity((shipment.getContainerDetails().stream().map(ContainerDetails::getNoOfPackages)
                .reduce(Integer::sum)).orElse(0) + " " + shipment.getContainerDetails().get(0).getPackageUnit());
        billOfLading.setContainerNumbers(shipment.getContainerDetails().stream().map(ContainerDetails::getContainerNo)
                .reduce((cont1, cont2) -> cont1 + ", " + cont2).orElse(""));
        billOfLading.setContainerSeals(shipment.getContainerDetails().stream().map(ContainerDetails::getSealNo)
                .reduce((cont1, cont2) -> cont1 + ", " + cont2).orElse(""));
    }

    private void fillUpNullDataIfUpdated() {
        if (StringUtils.isBlank(billOfLading.getShipper())) {
            billOfLading.setShipper(getClientDetailsForBl(shipment.getShipper()));
        }
        if (StringUtils.isBlank(billOfLading.getConsignee())) {
            billOfLading.setConsignee(getClientDetailsForBl(shipment.getConsignee()));
        }
        if (StringUtils.isBlank(billOfLading.getNotifyParty())) {
            billOfLading.setNotifyParty(getClientDetailsForBl(shipment.getNotifyParty()));
        }
        if (StringUtils.isBlank(billOfLading.getExportReference())) {
            billOfLading.setExportReference(DateUtil.getCurrentDateAsString());
        }
        Schedule schedule = shipment.getSchedule();
        if (StringUtils.isBlank(billOfLading.getVesselVoyage())) {
            billOfLading.setVesselVoyage(schedule == null ? "" : StringUtils.defaultString(shipment.getSchedule().getPortOfLoadingVesselName()));
        }
        if (StringUtils.isBlank(billOfLading.getMotherVessel())) {
            billOfLading.setMotherVessel(schedule == null ? "" : StringUtils.defaultString(shipment.getSchedule().getMotherVesselName()));
        }
        if (StringUtils.isBlank(billOfLading.getPortOfLoading())) {
            billOfLading.setPortOfLoading(shipment.getBooking().getLoadingPort().getPortCityAndCountry());
        }
        if (StringUtils.isBlank(billOfLading.getPortOfDischarge())) {
            billOfLading.setPortOfDischarge(shipment.getBooking().getDestinationPort().getPortCityAndCountry());
        }
        if (StringUtils.isBlank(billOfLading.getPlaceOfDelivery())) {
            billOfLading.setPlaceOfDelivery(shipment.getBooking().getDestinationPort().getPortCityAndCountry());
        }
        if (StringUtils.isBlank(billOfLading.getPlaceOfReceipt())) {
            billOfLading.setPlaceOfReceipt(shipment.getBooking().getLoadingPort().getPortCityAndCountry());
        }
        if (StringUtils.isBlank(billOfLading.getShipperMarks())) {
            billOfLading.setShipperMarks(StringUtils.defaultString(shipment.getShipperMarks()));
        }
        if (StringUtils.isBlank(billOfLading.getGoodsDescription())) {
            billOfLading.setGoodsDescription(StringUtils.defaultString(shipment.getGoodsDescription()));
        }
        if (StringUtils.isBlank(billOfLading.getBlNo())) {
            billOfLading.setBlNo(StringUtils.defaultIfBlank(shipment.getHblNo(), StringUtils.defaultString(shipment.getMblNo())));
        }
        if (StringUtils.isBlank(billOfLading.getMblNo())) {
            billOfLading.setMblNo(StringUtils.defaultString(shipment.getMblNo()));
        }
        if (StringUtils.isBlank(billOfLading.getBookingNo())) {
            billOfLading.setBookingNo(shipment.getBooking().getBookingNo());
        }
        if (StringUtils.isBlank(billOfLading.getContainer())) {
            billOfLading.setContainer(shipment.getBooking().getNumOfContainers() + " X " + shipment.getBooking().getContainerSize().getContainerSize());
        }
        if (StringUtils.isBlank(billOfLading.getFreightTerm())) {
            billOfLading.setFreightTerm(shipment.getShippingTerm() == null ? "" : shipment.getShippingTerm().toString());
        }

        if (shipment.getContainerDetails() == null || shipment.getContainerDetails().isEmpty()) {
            return;
        }
        if (StringUtils.isBlank(billOfLading.getGrossWeight())) {
            billOfLading.setGrossWeight(AmountFormatter.getBDRegionFormattedAmount(shipment.getContainerDetails().stream().map(ContainerDetails::getGrossWeight).reduce(BigDecimal.ZERO, BigDecimal::add)) + " KG");
        }
        if (StringUtils.isBlank(billOfLading.getQuantity())) {
            billOfLading.setQuantity((shipment.getContainerDetails().stream().map(ContainerDetails::getNoOfPackages).reduce(Integer::sum)).orElse(0) + " " + shipment.getContainerDetails().get(0).getPackageUnit());
        }
        if (StringUtils.isBlank(billOfLading.getContainerNumbers())) {
            billOfLading.setContainerNumbers(shipment.getContainerDetails().stream().map(ContainerDetails::getContainerNo).reduce((cont1, cont2) -> cont1 + ", " + cont2).orElse(""));
        }
        if (StringUtils.isBlank(billOfLading.getContainerSeals())) {
            billOfLading.setContainerSeals(shipment.getContainerDetails().stream().map(ContainerDetails::getSealNo).reduce((cont1, cont2) -> cont1 + ", " + cont2).orElse(""));
        }
    }

    private void setBlValuesToFields() {
        shipper.setValue(billOfLading.getShipper());
        consignee.setValue(billOfLading.getConsignee());
        notifyParty.setValue(billOfLading.getNotifyParty());
        exportReference.setValue(billOfLading.getExportReference());
        vesselVoyage.setValue(billOfLading.getVesselVoyage());
        motherVessel.setValue(billOfLading.getMotherVessel());
        portOfLoading.setValue(billOfLading.getPortOfLoading());
        portOfDischarge.setValue(billOfLading.getPortOfDischarge());
        placeOfDelivery.setValue(billOfLading.getPlaceOfDelivery());
        placeOfReceipt.setValue(billOfLading.getPlaceOfReceipt());
        shipperMarks.setValue(billOfLading.getShipperMarks());
        goodsDescription.setValue(billOfLading.getGoodsDescription());
        blNo.setValue(billOfLading.getBlNo());
        mblNo.setValue(billOfLading.getMblNo());
        bookingNo.setValue(billOfLading.getBookingNo());
        container.setValue(billOfLading.getContainer());
        freightTerm.setValue(billOfLading.getFreightTerm());
        grossWeight.setValue(billOfLading.getGrossWeight());
        measurement.setValue(StringUtils.defaultString(billOfLading.getMeasurement()));
        quantity.setValue(billOfLading.getQuantity());
        containerNumbers.setValue(billOfLading.getContainerNumbers());
        containerSeals.setValue(billOfLading.getContainerSeals());
        remarks.setValue(StringUtils.defaultString(billOfLading.getRemarks()));
    }

    public String getClientDetailsForBl(Client client) {
        if (client == null) {
            return "";
        }
        return StringUtils.defaultString(client.getName()) + System.lineSeparator() +
                StringUtils.defaultString(client.getAddress()) + System.lineSeparator();
    }

    public void setAttributes() {
        blType.setItems(List.of("Draft", "Original/Non-Negotiable"));
        blType.setValue("Draft");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        clear.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
    }

    public void setUpBLLayout() {
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
                blType);
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

    private void setListeners() {
        save.addClickListener(event -> {
            try {
                updateBlDataFromFields();
                billOfLadingService.saveBillOfLading(billOfLading);
                NotificationUtil.getNotification("Data Saved!", "", false,
                        NotificationVariant.LUMO_PRIMARY, 2000).open();
            } catch (Exception e) {
                log.error("error while delete old data", e);
                NotificationUtil.getNotification("Failed to save data due to an error", e.getMessage(),
                        true, NotificationVariant.LUMO_ERROR, 5000).open();
            }
        });
        clear.addClickListener(event -> {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setConfirmButton("Yes", confirmEvent -> {
                try {
                    billOfLadingService.deleteBillOfLading(billOfLading);
                    setBlValuesFromShipmentInfo();
                    setBlValuesToFields();
                    NotificationUtil.getNotification("Data cleared!", "", false,
                            NotificationVariant.LUMO_PRIMARY, 2000).open();
                } catch (Exception e) {
                    log.error("error while delete old data", e);
                    NotificationUtil.getNotification("Failed to delete data due to an error", e.getMessage(),
                            true, NotificationVariant.LUMO_ERROR, 5000).open();
                }
            });
            confirmDialog.setCancelButton("No", cancelEvent -> {
                confirmDialog.close();
            });
            confirmDialog.setText("Are you sure you want to delete saved BL data ? (Original Shipment data will remain and will replace Current BL data)");
            confirmDialog.setHeader("Confirm Data Clearing");
            confirmDialog.open();
        });
    }

    private void updateBlDataFromFields() {
        billOfLading.setShipper(StringUtils.defaultString(shipper.getValue()));
        billOfLading.setConsignee(StringUtils.defaultString(consignee.getValue()));
        billOfLading.setNotifyParty(StringUtils.defaultString(notifyParty.getValue()));
        billOfLading.setExportReference(StringUtils.defaultString(exportReference.getValue()));
        billOfLading.setVesselVoyage(StringUtils.defaultString(vesselVoyage.getValue()));
        billOfLading.setMotherVessel(StringUtils.defaultString(motherVessel.getValue()));
        billOfLading.setPortOfLoading(StringUtils.defaultString(portOfLoading.getValue()));
        billOfLading.setPortOfDischarge(StringUtils.defaultString(portOfDischarge.getValue()));
        billOfLading.setPlaceOfDelivery(StringUtils.defaultString(placeOfDelivery.getValue()));
        billOfLading.setPlaceOfReceipt(StringUtils.defaultString(placeOfReceipt.getValue()));
        billOfLading.setShipperMarks(StringUtils.defaultString(shipperMarks.getValue()));
        billOfLading.setGoodsDescription(StringUtils.defaultString(goodsDescription.getValue()));
        billOfLading.setBlNo(StringUtils.defaultString(blNo.getValue()));
        billOfLading.setMblNo(StringUtils.defaultString(mblNo.getValue()));
        billOfLading.setBookingNo(StringUtils.defaultString(bookingNo.getValue()));
        billOfLading.setContainer(StringUtils.defaultString(container.getValue()));
        billOfLading.setFreightTerm(StringUtils.defaultString(freightTerm.getValue()));
        billOfLading.setGrossWeight(StringUtils.defaultString(grossWeight.getValue()));
        billOfLading.setQuantity(StringUtils.defaultString(quantity.getValue()));
        billOfLading.setContainerNumbers(StringUtils.defaultString(containerNumbers.getValue()));
        billOfLading.setContainerSeals(StringUtils.defaultString(containerSeals.getValue()));
        billOfLading.setMeasurement(StringUtils.defaultString(measurement.getValue()));
        billOfLading.setRemarks(StringUtils.defaultString(remarks.getValue()));
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
