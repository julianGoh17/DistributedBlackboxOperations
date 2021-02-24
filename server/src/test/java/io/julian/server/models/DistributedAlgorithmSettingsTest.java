package io.julian.server.models;

import io.julian.server.components.Configuration;
import org.junit.Assert;
import org.junit.Test;

public class DistributedAlgorithmSettingsTest {

    @Test
    public void TestDefaultConfigurationCanCreateSettings() {
        DistributedAlgorithmSettings settings = new Configuration().getDistributedAlgorithmSettings();
        Assert.assertFalse(settings.isJarFilePathEnvInstantiated());
        Assert.assertFalse(settings.isPackageNameEnvInstantiated());

        Assert.assertEquals("", settings.getJarPath());
        Assert.assertEquals("", settings.getPackageName());
    }
}
