package com.reactivespring.moviesinfoservice.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;

public class SinkTest {

    /*
     replay().all() will be emitting all the events to the all the subscriber thatz been subscribed to it before or after
     the data is emitted, here in the example all the subscribers will be getting all the event thats been published to it
     */
    @Test
    public void sink_many_test(){
        Sinks.Many<Integer> sinkMany = Sinks.many().replay().all();

        sinkMany.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        sinkMany.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        sinkMany.asFlux().subscribe((i) -> System.out.println("Subscriber 1 : "+i));
        sinkMany.asFlux().subscribe((i) -> System.out.println("Subscriber 2 : "+i));

        sinkMany.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
        sinkMany.asFlux().subscribe((i) -> System.out.println("Subscriber 3 : "+i));
    }

    /*
    In MultiCast subscribers will be getting the events that is published only after their subscription, in the example,
    the 3rd subscriber doesnt receive any events because there no events have been published after its subscription.
     */
    @Test
    public void sink_multiCast_test(){
        Sinks.Many<Integer> sinkMany = Sinks.many().multicast().onBackpressureBuffer();

        sinkMany.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        sinkMany.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        sinkMany.asFlux().subscribe((i) -> System.out.println("Subscriber 1 : "+i));
        sinkMany.asFlux().subscribe((i) -> System.out.println("Subscriber 2 : "+i));

        sinkMany.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
        sinkMany.asFlux().subscribe((i) -> System.out.println("Subscriber 3 : "+i));
    }

    /* In uniCast, just the single subscriber can only be subscribed to the publisher and it receives all the events emitted
     before and after their subscription
     */
    @Test
    public void sink_uniCast_test(){
        Sinks.Many<Integer> sinkMany = Sinks.many().unicast().onBackpressureBuffer();

        sinkMany.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        sinkMany.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        sinkMany.asFlux().subscribe((i) -> System.out.println("Subscriber 1 : "+i));

        sinkMany.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
        sinkMany.emitNext(4, Sinks.EmitFailureHandler.FAIL_FAST);


    }

}
