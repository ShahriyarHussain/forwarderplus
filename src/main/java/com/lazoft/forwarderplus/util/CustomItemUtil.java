package com.lazoft.forwarderplus.util;

import com.lazoft.forwarderplus.model.xml.CustomItem;
import com.lazoft.forwarderplus.model.xml.CustomItems;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class CustomItemUtil {

    private static final String NEW_FILE_CONTENT = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Items>
            </Items>""";

    private CustomItemUtil() {}

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
            File customItemFile = new File("./" + fileName + ".xml");
            if (customItemFile.exists()) {
                return customItemFile;
            }
            boolean newFileCreated = customItemFile.createNewFile();
            if (newFileCreated) {
                FileWriter fileWriter = new FileWriter(customItemFile);
                fileWriter.write(NEW_FILE_CONTENT);
                fileWriter.close();
                return customItemFile;
            }
            throw new IOException("Cannot create file " + customItemFile.getAbsolutePath());
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
            return categories == null || categories.getCategories() == null ? new LinkedList<>() : categories.getCategories();
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
