package com.lazoft.forwarderplus.util;

import com.lazoft.forwarderplus.dto.xml.CustomItem;
import com.lazoft.forwarderplus.dto.xml.CustomItems;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class CustomItemUtil {

    public static JAXBContext getContext() {
        try {
            return JAXBContext.newInstance(CustomItems.class);
        } catch (JAXBException e) {
            log.error("Error while getting context", e);
        }
        return null;
    }

    public static File getFile(String fileName) {
        try {
            File commoditiesFile = new File("./" + fileName + ".xml");
            if (commoditiesFile.exists()) {
                return commoditiesFile;
            }
            boolean newFileCreated = commoditiesFile.createNewFile();
            if (newFileCreated) {
                return commoditiesFile;
            }
            return null;
        } catch (IOException e) {
            log.error("Error while getting file", e);
            return null;
        }
    }

    public static List<CustomItem> getItemsListFromFile(String customItemFilePath) {
        JAXBContext context = getContext();
        File customItemFile = getFile(customItemFilePath);
        if (context == null || customItemFile == null) {
            log.error("Context/File returned null in getItemsListFromFile");
            return new LinkedList<>();
        }
        try {
            CustomItems categories = (CustomItems) context.createUnmarshaller().unmarshal(customItemFile);
            return categories.getCategories();
        } catch (JAXBException e) {
            log.error(e.getMessage(), e);
        }
        return new LinkedList<>();
    }

    public static void saveCustomItems(CustomItems customItems, String customItemFilePath) {
        JAXBContext context = getContext();
        File customItemsFile = getFile(customItemFilePath);
        if (context == null || customItemsFile == null) {
            log.error("Context/File returned null in saveCustomItems");
            return;
        }
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(customItems, customItemsFile);
        } catch (JAXBException e) {
            log.error("Error while saving categories", e);
        }
    }

}
