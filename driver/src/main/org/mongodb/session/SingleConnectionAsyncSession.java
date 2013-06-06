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

package org.mongodb.session;

import org.mongodb.MongoException;
import org.mongodb.connection.AsyncServerConnection;
import org.mongodb.connection.ServerSelector;
import org.mongodb.connection.SingleResultCallback;
import org.mongodb.operation.MongoFuture;
import org.mongodb.operation.SingleResultFuture;

import static org.mongodb.assertions.Assertions.isTrue;

class SingleConnectionAsyncSession implements AsyncSession {
    private final ServerSelector serverSelector;
    private AsyncServerConnection connection;
    private AsyncServerSelectingSession session;
    private volatile boolean isClosed;

    public SingleConnectionAsyncSession(final ServerSelector serverSelector, final AsyncServerSelectingSession session) {
        this.session = session;
        this.serverSelector = serverSelector;
    }

    public void close() {
        if (!isClosed()) {
            connection.close();
            isClosed = true;
        }
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public MongoFuture<AsyncServerConnection> getConnection() {
        isTrue("open", !isClosed());

        if (connection != null) {
            return new SingleResultFuture<AsyncServerConnection>(new DelayedCloseAsyncConnection(connection), null);
        }
        else {
            final SingleResultFuture<AsyncServerConnection> retVal = new SingleResultFuture<AsyncServerConnection>();
            session.getConnection(serverSelector).register(new SingleResultCallback<AsyncServerConnection>() {
                @Override
                public void onResult(final AsyncServerConnection newConnection, final MongoException e) {
                    if (e != null) {
                        retVal.init(null, e);
                    }
                    else {
                        connection = newConnection;
                        retVal.init(new DelayedCloseAsyncConnection(newConnection), null);
                    }
                }
            });
            return retVal;
        }
    }
}