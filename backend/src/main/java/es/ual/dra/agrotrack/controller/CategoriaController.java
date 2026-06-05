package es.ual.dra.agrotrack.controller;

import es.ual.dra.agrotrack.dto.response.CategoriaResponse;
import es.ual.dra.agrotrack.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaRepository categoriaRepo;

    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaRepo.findAll().stream()
            .map(CategoriaResponse::from)
            .toList();
    }
}
