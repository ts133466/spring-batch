package com.ifast.batch.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class GenericXmlDaoImpl {


    /**
     * Xml File Type
     */
    private static final String XML_FILE_TYPE = ".xml";

    /**
     * Logger for this class
     */
    private final Logger logger = LoggerFactory.getLogger(GenericXmlDaoImpl.class);

    /**
     * Xml class to define the xml file's location.
     * 
     * @return Xml Class that the xml file belong to
     */
    protected abstract Class<?> getSqlXmlClass();

    /**
     * Sql Map which will holds the sql which parsed from the xml file
     */
    protected Map<String, String> sqlMap;

    /**
     * Constructor
     */
    protected GenericXmlDaoImpl() {
        parseSqlXmlFile();
    }

    /**
     * Parse Xml into Map. <br/>
     * 
     * 1. Read Input Stream From Class. <br/>
     * 2. If sql xml class is not null, then read the xml file from classpath resources and parse it
     * into map.
     * 
     */
    private void parseSqlXmlFile() {
        sqlMap = new HashMap<>();

        InputStream inputStream = null;
        if (getSqlXmlClass() != null) {
            try {
                inputStream = getXmlInputStreamFromClass(getSqlXmlClass());
            } catch (IOException e) {
                logger.error("Error in reading file from resources folder", new DaoException(e));
            }
        }
        if (inputStream != null) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(inputStream);
                doc.getDocumentElement().normalize();
                Node rootNode = doc.getFirstChild();
                NodeList sqlNodes = rootNode.getChildNodes();
                for (int i = 0; i < sqlNodes.getLength(); i++) {
                    Node sqlNode = sqlNodes.item(i);
                    sqlMap.put(sqlNode.getNodeName(), sqlNode.getTextContent());
                }
            } catch (Exception e) {
                logger.error("Error in parsing dao xml file", new DaoException(e));
            }
        }
    }

    /**
     * Read xml file from input stream
     * 
     * @param clazz class to read
     * @return input stream for xml file.
     * @throws IOException
     */
    private InputStream getXmlInputStreamFromClass(Class<?> clazz) throws IOException {
        Resource resource = new ClassPathResource(clazz.getSimpleName() + XML_FILE_TYPE, clazz);
        return resource.getInputStream();
    }

}
