package com.pradale.kterm.domain.type;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@SuperBuilder
@Jacksonized
@JacksonXmlRootElement(localName = "textItem")
public class TextItem implements Item {
    private String id;
    private String name;
    private String displayPath;
    private String filePath;

    @Override
    public ItemType getType() {
        return ItemType.TEXT;
    }

    public static TextItem getTextItem(String id) {
        return TextItem.builder()
                .id(id)
                .name(id)
                .build();
    }
}
