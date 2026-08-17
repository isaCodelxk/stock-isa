package com.isateca.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AttributeDefinitionService {

    private final AttributeDefinitionRepository attributeDefinitionRepository;

    AttributeDefinitionService(AttributeDefinitionRepository attributeDefinitionRepository) {
        this.attributeDefinitionRepository = attributeDefinitionRepository;
    }

    @Transactional(readOnly = true)
    public List<AttributeDefinition> list() {
        return attributeDefinitionRepository.findAll();
    }

    @Transactional
    public AttributeDefinition save(AttributeDefinition attributeDefinition) {
        return attributeDefinitionRepository.save(attributeDefinition);
    }

    @Transactional
    public void delete(AttributeDefinition attributeDefinition) {
        attributeDefinitionRepository.delete(attributeDefinition);
    }
}
