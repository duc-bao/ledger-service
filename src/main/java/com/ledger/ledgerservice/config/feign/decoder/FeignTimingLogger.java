package com.ledger.ledgerservice.config.feign.decoder;

import feign.Logger;
import feign.Response;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FeignTimingLogger extends Logger {

    private static final long SLOW_CALL_THRESHOLD_MS = 2000;

    @Override
    protected void log(String configKey, String format, Object... args) {
        /*
         * Intentionally left blank.
         * We override Feign Logger#log to disable default Feign logging.
         * Logging is handled centrally by FeignTimingLogger / AccessLog
         * to avoid duplicate logs and reduce noise in production.
         */
    }

    @Override
    protected Response logAndRebufferResponse(
            String configKey,
            Level logLevel,
            Response response,
            long elapsedTime
    ) {

        if (!log.isInfoEnabled()) {
            return response;
        }

        String method  = extractMethod(configKey);
        String httpMethod = response.request().httpMethod().name();
        String url = response.request().url();
        int status = response.status();


        if (elapsedTime >= SLOW_CALL_THRESHOLD_MS) {
            log.warn(
                    "[FEIGN-SLOW] {} {} {} TOOK={}ms STATUS={}",
                    method, httpMethod, url, elapsedTime, status
            );
        } else if (status >= 400) {
            log.warn(
                    "[FEIGN-ERROR] {} {} {} TOOK={}ms STATUS={}",
                    method, httpMethod, url, elapsedTime, status
            );
        } else {
            log.info(
                    "[FEIGN] {} {} {} TOOK={}ms STATUS={}",
                    method, httpMethod, url, elapsedTime, status
            );
        }

        return response;
    }


    private String extractMethod(String configKey) {
        // DeviceClient#registerDevice(JwePayloadDto)
        int idx = configKey.indexOf('(');
        return idx > 0 ? configKey.substring(0, idx) : configKey;
    }

}
