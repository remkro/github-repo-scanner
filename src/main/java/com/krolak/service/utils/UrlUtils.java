package com.krolak.service.utils;

public class UrlUtils {
    public static String prepareUserReposUrl(String baseUrl, String username) {
        return String.format("%s/users/%s/repos", baseUrl, username);
    }

    public static String prepareRepoBranchesUrl(String baseUrl, String username, String repositoryName) {
        return String.format("%s/repos/%s/%s/branches", baseUrl, username, repositoryName);
    }
}
