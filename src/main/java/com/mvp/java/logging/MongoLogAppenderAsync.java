package com.mvp.java.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.mongodb.ConnectionString;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.bson.Document;

public class MongoLogAppenderAsync extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private MongoClient client;
    private ConnectionString connectionString;

    @Override
    public void start() {
        try {
            client = MongoClients.create(connectionString); //doesn't actually create any connections yet
            waitForInitialConnection();
            super.start();
        } catch (Exception ex) {
            addError("The following exception occured when attempting to start Appender", ex);
        }
        System.out.println("Connection to MongoDB confirmed.");
    }

    @Override
    public void stop() {
        if (client != null) {
            client.close();
        }
        super.stop();
    }

    public void setConnectionString(String uri) {
        if (uri == null) {
            addError("MongoDB URI cannot be null. Specify one in the logback config file.");
            return;
        }
        this.connectionString = new ConnectionString(uri);
    }

    @Override
    protected void append(ILoggingEvent evt) {
        if (Objects.isNull(evt)) {
            return;
        }
        Document log = convertEventIntoDocumentForLogging(evt);
        MongoDatabase database = client.getDatabase(connectionString.getDatabase());
        MongoCollection<Document> logCollection = database.getCollection(connectionString.getCollection());
        logCollection.insertOne(log, callbackWhenFinishedInsert());
    }

    /*
     * *************************************************************************
     ******************************* PRIVATE ***********************************
     * *************************************************************************
     */
    private Document convertEventIntoDocumentForLogging(ILoggingEvent evt) {    
        Document log = new Document();
        log.put("logger", evt.getLoggerName());
        log.put("timestamp", new Date(evt.getTimeStamp()));
        log.put("level", String.valueOf(evt.getLevel()));
        log.put("thread", evt.getThreadName());
        log.put("message", evt.getFormattedMessage());

        IThrowableProxy throwableProxy = evt.getThrowableProxy();
        if (Objects.nonNull(throwableProxy)) {
            addThrowabletoLog(throwableProxy, log);
        }
        return log;
    }

    private void addThrowabletoLog(final IThrowableProxy throwableProxy, final Document log) {
        String proxyAsString = ThrowableProxyUtil.asString(throwableProxy);
        List<String> stackTrace = Arrays.asList(proxyAsString.replace("\t", "").
                split(CoreConstants.LINE_SEPARATOR));
        final int stackTraceSize = stackTrace.size();
        if (stackTraceSize > 0) {
            log.put("exception", stackTrace.get(0));
        }
        if (stackTraceSize > 1) {
            log.put("stacktrace", stackTrace.subList(1, stackTraceSize));
        }
    }

    private void waitForInitialConnection() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> callbackThrowable = new AtomicReference<>();

        System.out.println("++++++++++++++++ MONGO +++++++++++++++++++++++");
        System.out.format("Connecting to Mongo with connection string [%s]\n", connectionString.getConnectionString());

        client.listDatabaseNames().forEach((dbName) -> {
        }, callbackWhenFinishedListingDbNames(latch, callbackThrowable));
        latch.await();

        if (Objects.nonNull(callbackThrowable.get())) {
            System.out.format("Unable to connect to MongoDB. \nError [%s]\n", callbackThrowable.get());
            System.exit(1);
        }
    }

    /*
     * *************************************************************************
     ***************************** CALL BACKS **********************************
     * *************************************************************************
     */
    private SingleResultCallback<Void> callbackWhenFinishedInsert() {
        return (final Void result, final Throwable thowable) -> {
            if (Objects.nonNull(thowable)) {
                addWarn("Unable to log message", thowable);
            }
        };
    }

    private SingleResultCallback<Void> callbackWhenFinishedListingDbNames(CountDownLatch latch, AtomicReference<Throwable> callbackThrowable) {
        return (final Void result, final Throwable throwable) -> {
            if (Objects.nonNull(throwable)) {
                callbackThrowable.set(throwable);
            }
            latch.countDown();
        };
    }
}
