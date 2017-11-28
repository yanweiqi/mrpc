package com.kongzhong.mrpc.trace.interceptor;

import com.kongzhong.basic.zipkin.TraceContext;
import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.basic.zipkin.agent.KafkaAgent;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.trace.TraceConstants;
import com.kongzhong.mrpc.trace.config.TraceServerAutoConfigure;
import com.kongzhong.mrpc.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;

/**
 * ServerTraceInterceptor
 */
@Slf4j
public class TraceServerInterceptor implements RpcServerInterceptor {

    private AbstractAgent agent;

    @Resource
    private TraceServerAutoConfigure traceServerAutoConfigure;

    @PostConstruct
    public void init() {
        if (null == traceServerAutoConfigure) {
            traceServerAutoConfigure = new TraceServerAutoConfigure();
        } else {
            this.agent = new KafkaAgent(traceServerAutoConfigure.getUrl());
        }
    }

    @Override
    public Object execute(ServerInvocation invocation) throws Exception {
        if (!traceServerAutoConfigure.getEnable()) {
            // not enable tracing
            return invocation.next();
        }

        RpcRequest request = invocation.getRequest();
        String     traceId = request.getContext().get(TraceConstants.TRACE_ID);
        if (null == traceId) {
            // don't need tracing
            return invocation.next();
        }

        // prepare trace context
        startTrace(request.getContext());

        try {
            Object result = invocation.next();
            request.getContext().put(TraceConstants.SS_TIME, String.valueOf(TimeUtils.currentMicros()));
            endTrace();
            return result;
        } catch (Exception e) {
            endTrace();
            throw e;
        }

    }

    private void startTrace(Map<String, String> attaches) {

        long traceId      = Long.parseLong(attaches.get(TraceConstants.TRACE_ID));
        long parentSpanId = Long.parseLong(attaches.get(TraceConstants.SPAN_ID));

        // start tracing
        TraceContext.start();
        TraceContext.setTraceId(traceId);
        TraceContext.setSpanId(parentSpanId);
    }

    private void endTrace() {
        agent.send(TraceContext.getSpans());
        TraceContext.clear();
    }


}