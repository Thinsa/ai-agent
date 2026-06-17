package com.yun.yunaiagent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 启动时把已存在的本地环境变量整合进 local-api-keys.txt。
 */
@Component
public class LocalEnvironmentFileInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LocalEnvironmentFileInitializer.class);

    private final LocalEnvironmentFileService service = new LocalEnvironmentFileService();

    @Override
    public void run(ApplicationArguments args) {
        try {
            LocalEnvironmentFileService.MergeResult result = service.merge(
                    Path.of("local-api-keys.txt"),
                    readWindowsUserEnvironment(),
                    System.getenv()
            );
            log.info("Local environment file synchronized without printing secret values. Keys: {}", result.writtenKeys());
        } catch (Exception e) {
            log.warn("Failed to synchronize local environment file: {}", e.getMessage());
        }
    }

    private Map<String, String> readWindowsUserEnvironment() {
        Map<String, String> userEnvironment = new LinkedHashMap<>();
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return userEnvironment;
        }
        try {
            for (String key : LocalEnvironmentFileService.MANAGED_KEYS) {
                String value = readWindowsUserEnvironmentValue(key);
                if (value != null && !value.isBlank()) {
                    userEnvironment.put(key, value);
                }
            }
        } catch (Exception e) {
            log.debug("Windows user environment registry read skipped: {}", e.getMessage());
        }
        return userEnvironment;
    }

    private String readWindowsUserEnvironmentValue(String key) {
        try {
            Process process = new ProcessBuilder("reg", "query", "HKCU\\Environment", "/v", key)
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(2, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return null;
            }
            if (process.exitValue() != 0) {
                return null;
            }
            String output = new String(process.getInputStream().readAllBytes());
            for (String line : output.split("\\R")) {
                String trimmed = line.trim();
                if (!trimmed.startsWith(key + " ")) {
                    continue;
                }
                String[] parts = trimmed.split("\\s+", 3);
                if (parts.length == 3) {
                    return parts[2];
                }
            }
        } catch (Exception e) {
            log.debug("Windows user environment value read skipped for {}: {}", key, e.getMessage());
        }
        return null;
    }
}
