package com.pradale.kterm.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.pradale.kterm.domain.type.Terminal;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractService {

    @Value("${kterm.path.requests}")
    private String requestDirectory;

    @Autowired
    private XmlMapper xmlMapper;

    public void save(String path, Object shellCommand) throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
        xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(stringWriter);
        try {
            xmlStreamWriter.writeStartDocument();
            xmlMapper.writeValue(xmlStreamWriter, shellCommand);
            xmlStreamWriter.writeEndDocument();

            File file = new File(path);
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                writer.write(stringWriter.toString());
            }
        } finally {
            stringWriter.close();
            xmlStreamWriter.close();
        }
    }

    public List<File> getSavedFiles() {
        File requests = new File(requestDirectory);

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String extension = FilenameUtils.getExtension(pathname.getAbsolutePath());
                return extension.equalsIgnoreCase("xml");
            }
        };

        File[] paths = requests.listFiles(filter);
        return Arrays.stream(paths).collect(Collectors.toList());
    }

    public Terminal toTerminal(File file) throws Exception {
        return xmlMapper.readValue(file, Terminal.class);
    }
}
