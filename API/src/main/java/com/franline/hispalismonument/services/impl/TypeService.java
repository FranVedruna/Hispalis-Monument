package com.franline.hispalismonument.services.impl;

import com.franline.hispalismonument.dto.TypeDTO;
import com.franline.hispalismonument.persistance.repository.TypeRepository;
import com.franline.hispalismonument.services.ITypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TypeService implements ITypeService {
    private final TypeRepository typeRepository;

    @Autowired
    public TypeService(TypeRepository typeRepository){
        this.typeRepository = typeRepository;
    }

    public List<TypeDTO> getAllTypes() {
        return typeRepository.findAll().stream()
                .map(TypeDTO::new)
                .collect(Collectors.toList());
    }
}
