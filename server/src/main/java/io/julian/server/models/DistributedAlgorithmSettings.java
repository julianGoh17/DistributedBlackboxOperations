package io.julian.server.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributedAlgorithmSettings {
    private final boolean isJarFilePathEnvInstantiated;
    private final boolean isPackageNameEnvInstantiated;
    private final String jarPath;
    private final String packageName;

    public DistributedAlgorithmSettings(final boolean isJarFilePathEnvInstantiated, final boolean isPackageNameEnvInstantiated, final String jarPath, final String packageName) {
        this.isJarFilePathEnvInstantiated = isJarFilePathEnvInstantiated;
        this.isPackageNameEnvInstantiated = isPackageNameEnvInstantiated;
        this.jarPath = jarPath;
        this.packageName = packageName;
    }
}
