/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb;

import org.mongodb.codecs.DocumentCodec;
import org.mongodb.connection.BufferProvider;
import org.mongodb.operation.CommandOperation;
import org.mongodb.operation.GetDatabaseNamesOperation;

import java.util.List;

/**
 * Contains the commands that can be run on MongoDB that do not require a database to be selected first.  These commands can be accessed via
 * MongoClient.
 */
class ClientAdministrationImpl implements ClientAdministration {
    private static final String ADMIN_DATABASE = "admin";
    private static final Document PING_COMMAND = new Document("ping", 1);

    private final Codec<Document> commandCodec = new DocumentCodec();
    private final MongoClientImpl client;

    ClientAdministrationImpl(final MongoClientImpl client) {
        this.client = client;
    }

    //TODO: it's not clear from the documentation what the return type should be
    //http://docs.mongodb.org/manual/reference/command/ping/
    @Override
    public double ping() {
        CommandResult pingResult = new CommandOperation(ADMIN_DATABASE, PING_COMMAND, null, commandCodec, commandCodec,
                                                        client.getCluster().getDescription(), getBufferPool(), client.getSession(),
                                                        false)
                                       .execute();

        return (Double) pingResult.getResponse().get("ok");
    }

    @Override
    public List<String> getDatabaseNames() {
        return new GetDatabaseNamesOperation(client.getBufferProvider(), client.getSession(), false).execute();
    }

    public BufferProvider getBufferPool() {
        return client.getBufferProvider();
    }
}