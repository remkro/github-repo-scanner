package com.krolak.controller;

import com.krolak.controller.dto.RepositoryInfoDto;
import com.krolak.service.RepoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/github")
public class RepoController {
    private final RepoService repoService;

    @GetMapping("/repos/{username}")
    public ResponseEntity<RepositoryInfoDto> getUserRepositories(@PathVariable String username,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "5") int size) {
        log.info("Received request to get github repositories for username: {}", username);
        var result = repoService.getUserRepositories(username, page, size);
        return ResponseEntity.ok(result);
    }
}
