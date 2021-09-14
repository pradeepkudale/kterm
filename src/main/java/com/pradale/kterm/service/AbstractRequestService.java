package com.pradale.kterm.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

public abstract class AbstractRequestService {

    @Autowired
    private XmlMapper xmlMapper;

    public void save(String path, Object shellCommand) throws Exception {
        StringWriter xmlwriter = new StringWriter();
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
        xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(xmlwriter);

        try {
            streamWriter.writeStartDocument();
            xmlMapper.writeValue(streamWriter, shellCommand);
            streamWriter.writeEndDocument();

            File file = new File(path + ".xml");
            try (FileWriter writer = new FileWriter(file, false)) {
                writer.write(xmlwriter.toString());
            }
        } finally {
            streamWriter.close();
            xmlwriter.close();
        }
    }
}
