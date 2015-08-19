/**
 * Copyright (c) 2007-2014 Kaazing Corporation. All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.kaazing.gateway.management.monitoring.configuration.impl;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.kaazing.gateway.management.monitoring.configuration.MonitoringDataManager;
import org.kaazing.gateway.management.monitoring.service.MonitoredService;
import org.kaazing.gateway.management.monitoring.writer.GatewayWriter;
import org.kaazing.gateway.management.monitoring.writer.ServiceWriter;
import org.kaazing.gateway.management.monitoring.writer.impl.MMFGatewayWriter;
import org.kaazing.gateway.management.monitoring.writer.impl.MMFSeviceWriter;
import org.kaazing.gateway.service.MonitoringEntityFactory;
import org.kaazing.gateway.util.InternalSystemProperty;

import uk.co.real_logic.agrona.IoUtil;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

/**
 * Agrona implementation for the monitoring entity factory builder.
 */
public class MMFMonitoringDataManager implements MonitoringDataManager {

    private static final String LINUX = "Linux";
    private static final String OS_NAME_SYSTEM_PROPERTY = "os.name";
    private static final String LINUX_DEV_SHM_DIRECTORY = "/dev/shm";
    private static final String MONITOR_DIR_NAME = "/kaazing";
    private MonitorFileDescriptor monitorDescriptor;

    private Properties configuration;
    private UnsafeBuffer metaDataBuffer;

    private MappedByteBuffer mappedMonitorFile;
    private File monitoringDir;
    /**
     * TODO: To have a services abstraction passed to this class
     */
    private Collection<MonitoredService> services;
    private ConcurrentHashMap<MonitoredService, MonitoringEntityFactory> monitoringEntityFactories = new ConcurrentHashMap<>();

    public MMFMonitoringDataManager(Collection<MonitoredService> services, Properties configuration) {
        super();
        this.configuration = configuration;
        this.services = services;
        String gatewayId = InternalSystemProperty.GATEWAY_IDENTIFIER.getProperty(configuration);
        monitorDescriptor = new MonitorFileDescriptor(gatewayId, services);
    }

    @Override
    public ConcurrentHashMap<MonitoredService, MonitoringEntityFactory> initialize() {
        // create MMF
        createMonitoringFile();

        // create gateway writer
        GatewayWriter gatewayWriter = new MMFGatewayWriter(monitorDescriptor, mappedMonitorFile, metaDataBuffer, monitoringDir);
        MonitoringEntityFactory gwCountersFactory = gatewayWriter.writeCountersFactory();
        // TODO: Consider passing the gateway counters factory
        //monitoringEntityFactories.put(null, gwCountersFactory);

        // create service writer
        int i = 0;
        for (MonitoredService service : services) {
            ServiceWriter serviceWriter = new MMFSeviceWriter(monitorDescriptor, mappedMonitorFile, metaDataBuffer,
                    monitoringDir, i++);
            MonitoringEntityFactory serviceCountersFactory = serviceWriter.writeCountersFactory();
            monitoringEntityFactories.put(service, serviceCountersFactory);
        }

        return monitoringEntityFactories;
    }


    @Override
    public ConcurrentHashMap<MonitoredService, MonitoringEntityFactory> getMonitoringEntityFactories() {
        return monitoringEntityFactories;
    }

    /**
     * Method creating the monitoring MMF
     */
    private void createMonitoringFile() {
        String monitoringDirName = getMonitoringDirName();
        monitoringDir = new File(monitoringDirName);

        String fileName = InternalSystemProperty.GATEWAY_IDENTIFIER.getProperty(configuration);
        File monitoringFile = new File(monitoringDir, fileName);
        IoUtil.deleteIfExists(monitoringFile);

        int fileSize = monitorDescriptor.computeMonitorTotalFileLength();
        mappedMonitorFile = IoUtil.mapNewFile(monitoringFile, fileSize);

        metaDataBuffer = addMetadataToAgronaFile(mappedMonitorFile);
    }

    /**
     * Method adding metadata to the Agrona file
     * @param mappedMonitorFile
     * @return
     */
    private UnsafeBuffer addMetadataToAgronaFile(MappedByteBuffer mappedMonitorFile) {
        UnsafeBuffer metaDataBuffer = monitorDescriptor.createMetaDataBuffer(mappedMonitorFile);
        monitorDescriptor.fillMetaData(metaDataBuffer);
        return metaDataBuffer;
    }

    /**
     * This method is used to compute the monitoring directory name which will be used by Agrona in order
     * to create a file in which to write the data in shared memory.
     *
     * The monitoring directory will be dependent of the operating system.
     *
     * For Linux we will use the OS implementation of the shared memory. So the directory will be created
     * in /dev/shm. For the other operating systems we will create a monitoring folder under the
     * gateway folder.
     *
     * @return the monitoring directory name
     */
    private String getMonitoringDirName() {
        String monitoringDirName = IoUtil.tmpDirName() + MONITOR_DIR_NAME;

        if (LINUX.equalsIgnoreCase(System.getProperty(OS_NAME_SYSTEM_PROPERTY))) {
            final File devShmDir = new File(LINUX_DEV_SHM_DIRECTORY);

            if (devShmDir.exists()) {
                monitoringDirName = LINUX_DEV_SHM_DIRECTORY + monitoringDirName;
            }
        }

        return monitoringDirName;
    }

}
