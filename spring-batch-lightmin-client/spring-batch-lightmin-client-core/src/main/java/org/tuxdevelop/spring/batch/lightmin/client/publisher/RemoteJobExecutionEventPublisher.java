package org.tuxdevelop.spring.batch.lightmin.client.publisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.tuxdevelop.spring.batch.lightmin.api.resource.monitoring.JobExecutionEventInfo;
import org.tuxdevelop.spring.batch.lightmin.client.service.LightminServerLocatorService;
import org.tuxdevelop.spring.batch.lightmin.util.RequestUtil;

import java.util.List;

/**
 * @author Marcel Becker
 * @since 0.5
 */
@Slf4j
public class RemoteJobExecutionEventPublisher implements JobExecutionEventPublisher {

    private final RestTemplate restTemplate;
    private final LightminServerLocatorService lightminServerLocator;

    public RemoteJobExecutionEventPublisher(final RestTemplate restTemplate, final LightminServerLocatorService lightminServerLocator) {
        this.restTemplate = restTemplate;
        this.lightminServerLocator = lightminServerLocator;
    }

    @Override
    public void publishEvent(final JobExecutionEventInfo jobExecutionEventInfo) {
        final HttpEntity<JobExecutionEventInfo> entity = RequestUtil.createApplicationJsonEntity(jobExecutionEventInfo);
        final List<String> lightminUrls = this.getLightminServerUrls();
        log.debug("Sending JobExecutionInfos to Servers {}", lightminUrls);
        lightminUrls
                .forEach(
                        lightminUrl -> {
                            try {
                                final String url = lightminUrl + "/api/events/jobexecutions";
                                final ResponseEntity<Void> response =
                                        RemoteJobExecutionEventPublisher.this.restTemplate.postForEntity(
                                                url,
                                                entity,
                                                Void.class);

                                if (HttpStatus.CREATED.equals(response.getStatusCode())) {
                                    log.debug("Send JobExecutionEventInfo > {} to server > {}",
                                            jobExecutionEventInfo, lightminUrl);
                                } else {
                                    log.warn("Could send JobExecutionEventInfo to Server {}", response);
                                }
                            } catch (final Exception e) {
                                log.warn("Could not send JobExecutionEventInfo > {} to server {}. Error {} ",
                                        jobExecutionEventInfo, lightminUrl, e.getMessage());
                            }
                        }
                );
    }

    private List<String> getLightminServerUrls() {
        return this.lightminServerLocator.getRemoteUrls();
    }


}
