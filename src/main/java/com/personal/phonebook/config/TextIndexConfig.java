package com.personal.phonebook.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.core.index.TextIndexDefinition.TextIndexDefinitionBuilder;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class TextIndexConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndices () {
        TextIndexDefinition textIndex = new TextIndexDefinitionBuilder().onField("firstName")
                                                                        .onField("lastName")
                                                                        .onField("phone")
                                                                        .onField("address")
                                                                        .build();

        mongoTemplate.indexOps("contacts").ensureIndex(textIndex);
        log.info("Successfully created text index for contacts collection on fields: firstName, lastName, phone, address");
    }
}