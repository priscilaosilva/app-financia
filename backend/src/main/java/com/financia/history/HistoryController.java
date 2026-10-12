package com.financia.history;

import com.financia.auth.SecurityUtils;
import com.financia.history.dto.BalanceResponse;
import com.financia.history.dto.DayDeletionRequest;
import com.financia.history.dto.HistoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;
    private final HistoryDeletionService historyDeletionService;

    @GetMapping("/balance")
    public BalanceResponse balance() {
        return historyService.balance(SecurityUtils.currentUserId());
    }

    @GetMapping("/history")
    public HistoryResponse history(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return historyService.history(SecurityUtils.currentUserId(), from, to);
    }

    @PostMapping("/history/day-deletions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDayEntries(@Valid @RequestBody DayDeletionRequest request) {
        historyDeletionService.deleteDayEntries(SecurityUtils.currentUserId(), request);
    }
}
