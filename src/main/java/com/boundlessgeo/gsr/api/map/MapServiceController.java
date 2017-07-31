/* Copyright (c) 2017 Boundless - http://boundlessgeo.com All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package com.boundlessgeo.gsr.api.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.PublishedType;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.wms.WMSInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boundlessgeo.gsr.api.AbstractGSRController;
import com.boundlessgeo.gsr.core.map.LayerNameComparator;
import com.boundlessgeo.gsr.core.map.LayerOrTable;
import com.boundlessgeo.gsr.core.map.LayersAndTables;
import com.boundlessgeo.gsr.core.map.MapServiceRoot;

/**
 * Controller for the root Map Service endpoint
 */
@RestController
@RequestMapping(path = "/gsr/services/{workspaceName}/MapServer", produces = MediaType.APPLICATION_JSON_VALUE)
public class MapServiceController extends AbstractGSRController {

    @Autowired
    public MapServiceController(@Qualifier("geoServer") GeoServer geoServer) {
        super(geoServer);
    }

    @GetMapping
    public MapServiceRoot mapServiceGet(@PathVariable String workspaceName) throws IOException {
        WorkspaceInfo workspace = geoServer.getCatalog().getWorkspaceByName(workspaceName);
        if (workspace == null) {
            throw new NoSuchElementException("Workspace name " + workspaceName + " does not correspond to any workspace.");
        }
        WMSInfo service = geoServer.getService(workspace, WMSInfo.class);
        if (service == null) {
            service = geoServer.getService(WMSInfo.class);
        }
        List<LayerInfo> layersInWorkspace = new ArrayList<>();
        for (LayerInfo l : geoServer.getCatalog().getLayers()) {
            if (l.getType() == PublishedType.VECTOR && l.getResource().getStore().getWorkspace().equals(workspace)) {
                layersInWorkspace.add(l);
            }
        }
        layersInWorkspace.sort(LayerNameComparator.INSTANCE);

        return new MapServiceRoot(service, Collections.unmodifiableList(layersInWorkspace));
    }

    @GetMapping(path = {"/{layerId}"})
    public LayerOrTable getLayer(@PathVariable String workspaceName, @PathVariable Integer layerId) throws IOException {
        return LayersAndTables.find(catalog, workspaceName, layerId);
    }

    @GetMapping(path = "/identify")
    public IdentifyServiceResult identify() {

        IdentifyServiceResult result = new IdentifyServiceResult();

        return result;
    }
}
