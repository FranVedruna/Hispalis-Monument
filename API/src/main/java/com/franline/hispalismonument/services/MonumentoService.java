package com.franline.hispalismonument.services;

import com.franline.hispalismonument.dto.MonumentoDTO;
import com.franline.hispalismonument.persistance.model.Monumento;
import com.franline.hispalismonument.persistance.repository.MonumentRepositoryI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MonumentoService {

    private final MonumentRepositoryI monumentoRepository;
    private final ImageStorageService imageStorageService;

    @Autowired
    public MonumentoService(MonumentRepositoryI monumentoRepository, ImageStorageService imageStorageService) {
        this.monumentoRepository = monumentoRepository;
        this.imageStorageService = imageStorageService;
    }

    /**
     * Crea o actualiza un Monumento, almacenando la imagen si se envía.
     *
     * @param monumento Objeto de tipo Monumento a guardar o actualizar.
     * @param imageFile Archivo de imagen (opcional).
     * @return El Monumento guardado.
     */
    public Monumento createOrUpdateMonumento(Monumento monumento, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = imageStorageService.store(imageFile);
            monumento.setFotoUrl(imageUrl);
        }
        return monumentoRepository.save(monumento);
    }

    public Monumento searchMonumentByName(String monumentName){
        Optional<Monumento> optionalMonumento = monumentoRepository.findByNombre(monumentName);
        if (optionalMonumento.isPresent()){
            Monumento monumento = optionalMonumento.get();
            return monumento;
        }else {
            throw new RuntimeException("Monumento no encontrado");
        }
    }

    public MonumentoDTO searchMonumentById(int id){
        Optional<Monumento> optionalMonumento = monumentoRepository.findById(id);
        if (optionalMonumento.isPresent()){
            Monumento monumento = optionalMonumento.get();
            return new MonumentoDTO(monumento);
        }else {
            throw new RuntimeException("Monumento no encontrado");
        }
    }

    public Page<MonumentoDTO> searchAll(Pageable pageable){
        List<Monumento> allmonuments = monumentoRepository.findAll();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allmonuments.size());
        List<Monumento> pageList = allmonuments.subList(start, end);

        return new PageImpl<>(pageList, pageable, allmonuments.size()).map(MonumentoDTO::new);
    }

    // Otros métodos de negocio, por ejemplo, para eliminar, buscar, etc.
}
