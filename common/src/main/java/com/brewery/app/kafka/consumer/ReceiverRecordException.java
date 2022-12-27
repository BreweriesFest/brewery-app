package com.brewery.app.kafka.consumer;

import reactor.kafka.receiver.ReceiverRecord;

class ReceiverRecordException extends RuntimeException {
    private final ReceiverRecord record;

    ReceiverRecordException(ReceiverRecord record, Throwable t) {
        super(t);
        this.record = record;
    }

    public ReceiverRecord getRecord() {
        return this.record;
    }
}