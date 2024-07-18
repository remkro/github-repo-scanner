package com.krolak.controller;

import com.krolak.controller.dto.RepositoryInfoDto;
import com.krolak.service.RepoService;
import com.krolak.service.exception.GitHubApiException;
import com.krolak.service.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RepoController.class)
class RepoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RepoService repoService;

    private RepositoryInfoDto repositoryInfoDto;

    @BeforeEach
    void setUp() {
        repositoryInfoDto = RepositoryInfoDto.builder()
                .username("testUser")
                .repositories(List.of(
                        RepositoryInfoDto.RepositoryDto.builder()
                                .repositoryName("repo1")
                                .ownerLogin("testUser")
                                .branches(List.of(
                                        RepositoryInfoDto.BranchDto.builder()
                                                .name("main")
                                                .lastCommitSha("abc123")
                                                .build()
                                ))
                                .build()
                ))
                .elements(1)
                .pageNumber(0)
                .pageSize(5)
                .build();
    }

    @Test
    void testGetUserRepositories_shouldReturnRepositoryInfo() throws Exception {
        when(repoService.getUserRepositories("testUser", 0, 5)).thenReturn(repositoryInfoDto);

        performGetUserRepositories("testUser", 0, 5)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.repositories[0].repositoryName").value("repo1"))
                .andExpect(jsonPath("$.repositories[0].branches[0].name").value("main"))
                .andExpect(jsonPath("$.elements").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(5));
    }

    @Test
    void testGetUserRepositories_shouldReturnNotFound_forNonExistentUser() throws Exception {
        when(repoService.getUserRepositories("nonExistentUser", 0, 5))
                .thenThrow(new UserNotFoundException("User doesn't exist: nonExistentUser"));

        performGetUserRepositories("nonExistentUser", 0, 5)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User doesn't exist: nonExistentUser"));
    }

    @Test
    void testGetUserRepositories_shouldReturnInternalServerError_onGitHubApiException() throws Exception {
        when(repoService.getUserRepositories("testUser", 0, 5))
                .thenThrow(new GitHubApiException("GitHub API failed"));

        performGetUserRepositories("testUser", 0, 5)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("GitHub API failed"));
    }

    private ResultActions performGetUserRepositories(String username, int page, int size) throws Exception {
        return mockMvc.perform(get("/github/repos/" + username)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .contentType(MediaType.APPLICATION_JSON));
    }
}