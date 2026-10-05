package com.financia.income;

import com.financia.auth.SecurityUtils;
import com.financia.income.dto.NamedItemRequest;
import com.financia.income.dto.NamedItemResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/income-sources")
@RequiredArgsConstructor
public class IncomeSourceController {

    private final IncomeSourceService service;

    @GetMapping
    public List<NamedItemResponse> list() {
        return service.listActive(SecurityUtils.currentUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NamedItemResponse create(@Valid @RequestBody NamedItemRequest request) {
        return service.create(SecurityUtils.currentUserId(), request.getName());
    }

    @PutMapping("/{id}")
    public NamedItemResponse rename(@PathVariable Long id, @Valid @RequestBody NamedItemRequest request) {
        return service.rename(SecurityUtils.currentUserId(), id, request.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.softDelete(SecurityUtils.currentUserId(), id);
    }
}
