package com.franline.hispalismonument.services;

import com.franline.hispalismonument.dto.MonumentoDTO;
import com.franline.hispalismonument.persistance.model.Monumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IMonumentService {
    public Monumento createOrUpdateMonumento(Monumento monumento, MultipartFile imageFile);
    public void deleteMonumentByName(String nombre);
    public boolean getMonumentIsVisited(String username, String monumentName);
    public MonumentoDTO searchMonumentByName(String monumentName);
    public MonumentoDTO searchMonumentById(int id);
    public void setMonumentIsVisited(String username, String monumentName);
    public Page<MonumentoDTO> searchAll(Pageable pageable);
    public Page<MonumentoDTO> searchMonumentsByPartialName(String nombreParcial, Pageable pageable);
}
