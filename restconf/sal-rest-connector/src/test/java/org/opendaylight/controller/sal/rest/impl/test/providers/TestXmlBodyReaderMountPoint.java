/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.sal.rest.impl.test.providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.util.Collection;
import javax.ws.rs.core.MediaType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.controller.md.sal.dom.api.DOMMountPoint;
import org.opendaylight.controller.md.sal.dom.api.DOMMountPointService;
import org.opendaylight.controller.md.sal.rest.common.TestRestconfUtils;
import org.opendaylight.netconf.sal.rest.impl.XmlNormalizedNodeBodyReader;
import org.opendaylight.netconf.sal.restconf.impl.ControllerContext;
import org.opendaylight.netconf.sal.restconf.impl.NormalizedNodeContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * sal-rest-connector org.opendaylight.controller.sal.rest.impl.test.providers
 *
 *
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 *         Created: Mar 9, 2015
 */
public class TestXmlBodyReaderMountPoint extends AbstractBodyReaderTest {
    private final XmlNormalizedNodeBodyReader xmlBodyReader;
    private static SchemaContext schemaContext;

    private static final QNameModule INSTANCE_IDENTIFIER_MODULE_QNAME = initializeInstanceIdentifierModule();

    private static QNameModule initializeInstanceIdentifierModule() {
        try {
            return QNameModule.create(URI.create("instance:identifier:module"),
                SimpleDateFormatUtil.getRevisionFormat().parse("2014-01-17"));
        } catch (final ParseException e) {
            throw new Error(e);
        }
    }


    public TestXmlBodyReaderMountPoint() throws NoSuchFieldException,
            SecurityException {
        super();
        this.xmlBodyReader = new XmlNormalizedNodeBodyReader();
    }

    @Override
    protected MediaType getMediaType() {
        return new MediaType(MediaType.APPLICATION_XML, null);
    }

    @BeforeClass
    public static void initialization() throws Exception {
        final Collection<File> testFiles = TestRestconfUtils.loadFiles("/instanceidentifier/yang");
        testFiles.addAll(TestRestconfUtils.loadFiles("/invoke-rpc"));
        schemaContext = YangParserTestUtils.parseYangSources(testFiles);

        final DOMMountPoint mountInstance = mock(DOMMountPoint.class);
        when(mountInstance.getSchemaContext()).thenReturn(schemaContext);
        final DOMMountPointService mockMountService = mock(DOMMountPointService.class);
        when(mockMountService.getMountPoint(any(YangInstanceIdentifier.class)))
                .thenReturn(Optional.of(mountInstance));

        ControllerContext.getInstance().setMountService(mockMountService);
        controllerContext.setSchemas(schemaContext);
    }

    @Test
    public void moduleDataTest() throws Exception {
        final DataSchemaNode dataSchemaNode = schemaContext
                .getDataChildByName(QName.create(INSTANCE_IDENTIFIER_MODULE_QNAME, "cont"));
        final String uri = "instance-identifier-module:cont/yang-ext:mount/instance-identifier-module:cont";
        mockBodyReader(uri, this.xmlBodyReader, false);
        final InputStream inputStream = TestXmlBodyReaderMountPoint.class
                .getResourceAsStream("/instanceidentifier/xml/xmldata.xml");
        final NormalizedNodeContext returnValue = this.xmlBodyReader.readFrom(null,
                null, null, this.mediaType, null, inputStream);
        checkMountPointNormalizedNodeContext(returnValue);
        checkExpectValueNormalizeNodeContext(dataSchemaNode, returnValue);
    }

    @Test
    public void moduleSubContainerDataPutTest() throws Exception {
        final DataSchemaNode dataSchemaNode = schemaContext
                .getDataChildByName(QName.create(INSTANCE_IDENTIFIER_MODULE_QNAME, "cont"));
        final String uri = "instance-identifier-module:cont/yang-ext:mount/instance-identifier-module:cont/cont1";
        mockBodyReader(uri, this.xmlBodyReader, false);
        final InputStream inputStream = TestXmlBodyReaderMountPoint.class
                .getResourceAsStream("/instanceidentifier/xml/xml_sub_container.xml");
        final NormalizedNodeContext returnValue = this.xmlBodyReader.readFrom(null,
                null, null, this.mediaType, null, inputStream);
        checkMountPointNormalizedNodeContext(returnValue);
        checkExpectValueNormalizeNodeContext(dataSchemaNode, returnValue,
                QName.create(dataSchemaNode.getQName(), "cont1"));
    }

    @Test
    public void moduleSubContainerDataPostTest() throws Exception {
        final DataSchemaNode dataSchemaNode = schemaContext
                .getDataChildByName(QName.create(INSTANCE_IDENTIFIER_MODULE_QNAME, "cont"));
        final String uri = "instance-identifier-module:cont/yang-ext:mount/instance-identifier-module:cont";
        mockBodyReader(uri, this.xmlBodyReader, true);
        final InputStream inputStream = TestXmlBodyReaderMountPoint.class
                .getResourceAsStream("/instanceidentifier/xml/xml_sub_container.xml");
        final NormalizedNodeContext returnValue = this.xmlBodyReader.readFrom(null,
                null, null, this.mediaType, null, inputStream);
        checkMountPointNormalizedNodeContext(returnValue);
        checkExpectValueNormalizeNodeContext(dataSchemaNode, returnValue);
    }

    @Test
    public void rpcModuleInputTest() throws Exception {
        final String uri = "instance-identifier-module:cont/yang-ext:mount/invoke-rpc-module:rpc-test";
        mockBodyReader(uri, this.xmlBodyReader, true);
        final InputStream inputStream = TestXmlBodyReaderMountPoint.class
                .getResourceAsStream("/invoke-rpc/xml/rpc-input.xml");
        final NormalizedNodeContext returnValue = this.xmlBodyReader.readFrom(null,
                null, null, this.mediaType, null, inputStream);
        checkNormalizedNodeContext(returnValue);
        final ContainerNode contNode = (ContainerNode) returnValue.getData();
        final YangInstanceIdentifier yangCont = YangInstanceIdentifier.of(QName.create(contNode.getNodeType(), "cont"));
        final Optional<DataContainerChild<? extends PathArgument, ?>> contDataNodePotential = contNode.getChild(yangCont
                .getLastPathArgument());
        assertTrue(contDataNodePotential.isPresent());
        final ContainerNode contDataNode = (ContainerNode) contDataNodePotential.get();
        final YangInstanceIdentifier yangLeaf = YangInstanceIdentifier.of(QName.create(contDataNode.getNodeType(), "lf"));
        final Optional<DataContainerChild<? extends PathArgument, ?>> leafDataNode = contDataNode.getChild(yangLeaf
                .getLastPathArgument());
        assertTrue(leafDataNode.isPresent());
        assertTrue("lf-test".equalsIgnoreCase(leafDataNode.get().getValue().toString()));
    }

    private void checkExpectValueNormalizeNodeContext(
            final DataSchemaNode dataSchemaNode,
            final NormalizedNodeContext nnContext) {
        checkExpectValueNormalizeNodeContext(dataSchemaNode, nnContext, null);
    }

    protected void checkExpectValueNormalizeNodeContext(
            final DataSchemaNode dataSchemaNode,
            final NormalizedNodeContext nnContext, final QName qName) {
        YangInstanceIdentifier dataNodeIdent = YangInstanceIdentifier
                .of(dataSchemaNode.getQName());
        final DOMMountPoint mountPoint = nnContext
                .getInstanceIdentifierContext().getMountPoint();
        final DataSchemaNode mountDataSchemaNode = mountPoint
                .getSchemaContext().getDataChildByName(
                        dataSchemaNode.getQName());
        assertNotNull(mountDataSchemaNode);
        if ((qName != null) && (dataSchemaNode instanceof DataNodeContainer)) {
            final DataSchemaNode child = ((DataNodeContainer) dataSchemaNode)
                    .getDataChildByName(qName);
            dataNodeIdent = YangInstanceIdentifier.builder(dataNodeIdent)
                    .node(child.getQName()).build();
            assertTrue(nnContext.getInstanceIdentifierContext().getSchemaNode()
                    .equals(child));
        } else {
            assertTrue(mountDataSchemaNode.equals(dataSchemaNode));
        }
        assertNotNull(NormalizedNodes.findNode(nnContext.getData(),
                dataNodeIdent));
    }

    /**
     * Test when container with the same name is placed in two modules (foo-module and bar-module). Namespace must be
     * used to distinguish between them to find correct one. Check if container was found not only according to its name
     * but also by correct namespace used in payload.
     */
    @Test
    public void findFooContainerUsingNamespaceTest() throws Exception {
        mockBodyReader("instance-identifier-module:cont/yang-ext:mount", this.xmlBodyReader, true);
        final InputStream inputStream = TestXmlBodyReader.class
                .getResourceAsStream("/instanceidentifier/xml/xmlDataFindFooContainer.xml");
        final NormalizedNodeContext returnValue = this.xmlBodyReader
                .readFrom(null, null, null, this.mediaType, null, inputStream);

        // check return value
        checkMountPointNormalizedNodeContext(returnValue);
        // check if container was found both according to its name and namespace
        assertEquals("Not correct container found, name was ignored",
                "foo-bar-container", returnValue.getData().getNodeType().getLocalName());
        assertEquals("Not correct container found, namespace was ignored",
                "foo:module", returnValue.getData().getNodeType().getNamespace().toString());
    }

    /**
     * Test when container with the same name is placed in two modules (foo-module and bar-module). Namespace must be
     * used to distinguish between them to find correct one. Check if container was found not only according to its name
     * but also by correct namespace used in payload.
     */
    @Test
    public void findBarContainerUsingNamespaceTest() throws Exception {
        mockBodyReader("instance-identifier-module:cont/yang-ext:mount", this.xmlBodyReader, true);
        final InputStream inputStream = TestXmlBodyReader.class
                .getResourceAsStream("/instanceidentifier/xml/xmlDataFindBarContainer.xml");
        final NormalizedNodeContext returnValue = this.xmlBodyReader
                .readFrom(null, null, null, this.mediaType, null, inputStream);

        // check return value
        checkMountPointNormalizedNodeContext(returnValue);
        // check if container was found both according to its name and namespace
        assertEquals("Not correct container found, name was ignored",
                "foo-bar-container", returnValue.getData().getNodeType().getLocalName());
        assertEquals("Not correct container found, namespace was ignored",
                "bar:module", returnValue.getData().getNodeType().getNamespace().toString());
    }
}
