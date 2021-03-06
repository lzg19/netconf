/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netconf.cli.commands.local;

import org.opendaylight.netconf.cli.NetconfDeviceConnectionManager;
import org.opendaylight.netconf.cli.commands.AbstractCommand;
import org.opendaylight.netconf.cli.commands.Command;
import org.opendaylight.netconf.cli.commands.input.Input;
import org.opendaylight.netconf.cli.commands.input.InputDefinition;
import org.opendaylight.netconf.cli.commands.output.Output;
import org.opendaylight.netconf.cli.commands.output.OutputDefinition;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;

/**
 * Local disconnect command
 */
public class Disconnect extends AbstractCommand {

    private final NetconfDeviceConnectionManager connectionManager;

    public Disconnect(final QName qName, final InputDefinition inputDefinition,
            final OutputDefinition outputDefinition, final NetconfDeviceConnectionManager connectionManager,
            final String description) {
        super(qName, inputDefinition, outputDefinition, description);
        this.connectionManager = connectionManager;
    }

    @Override
    public Output invoke(final Input inputArgs) {
        connectionManager.disconnect();

        return new Output(
                ImmutableContainerNodeBuilder.create()
                        .withNodeIdentifier(new NodeIdentifier(getCommandId()))
                        .withChild(ImmutableLeafNodeBuilder.create()
                                .withNodeIdentifier(new NodeIdentifier(QName.create(getCommandId(), "status")))
                                .withValue("Connection disconnected").build()).build());
    }

    public static Command create(final RpcDefinition rpcDefinition,
            final NetconfDeviceConnectionManager commandDispatcher) {
        return new Disconnect(rpcDefinition.getQName(), getInputDefinition(rpcDefinition),
                getOutputDefinition(rpcDefinition), commandDispatcher, rpcDefinition.getDescription());
    }
}
