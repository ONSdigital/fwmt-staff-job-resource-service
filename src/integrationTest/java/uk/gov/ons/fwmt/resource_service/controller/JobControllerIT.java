package uk.gov.ons.fwmt.resource_service.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.fwmt.resource_service.ApplicationConfig;
import uk.gov.ons.fwmt.resource_service.entity.TMJobEntity;
import uk.gov.ons.fwmt.resource_service.repo.TMJobRepo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = ApplicationConfig.class)
@AutoConfigureMockMvc
@Transactional
public class JobControllerIT {

  private static final String JOB_JSON = "{ \"tmJobId\": \"1234-5678\", \"lastAuthNo\": \"1276\" , \"lastUpdated\": \"2018-08-01T01:06:01\" }";
  @Autowired private MockMvc mockMvc;
  @Autowired private TMJobRepo jobRepo;

  @Test
  public void getJobByJobIdIT() throws Exception {
    TMJobEntity jobEntity = new TMJobEntity();
    jobEntity.setTmJobId("1234-5678");
    jobEntity.setLastAuthNo("1234");
    jobEntity.setLastUpdated(LocalDateTime.parse("2018-08-01T01:06:01",DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    jobRepo.save(jobEntity);
    mockMvc.perform(get("/jobs/1234-5678").with(httpBasic("user", "password"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.tmJobId", is("1234-5678")))
            .andExpect(jsonPath("$.lastAuthNo", is("1234")))
            .andExpect(jsonPath("$.lastUpdated", is("2018-08-01T01:06:01")));

  }

  @Test
  public void updateJobIT() throws Exception {
    TMJobEntity jobEntity = new TMJobEntity();
    jobEntity.setTmJobId("1234-5678");
    jobEntity.setLastAuthNo("1234");
    jobEntity.setLastUpdated(LocalDateTime.parse("2018-08-01T01:06:01",DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    jobRepo.save(jobEntity);
    mockMvc.perform(
        (put("/jobs")).contentType(MediaType.APPLICATION_JSON).content(JOB_JSON).with(httpBasic("user", "password")));
    final TMJobEntity updatedJob = jobRepo.findByTmJobId("1234-5678");
    assertThat(updatedJob.getLastAuthNo()).isEqualTo("1276");
  }

  @Test
  public void deleteJobIT() throws Exception {
    TMJobEntity jobEntity = new TMJobEntity();
    jobEntity.setTmJobId("1234-5678");
    jobEntity.setLastAuthNo("1276");
    jobEntity.setLastUpdated(LocalDateTime.parse("2018-08-01T01:06:01",DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    jobRepo.save(jobEntity);
    mockMvc.perform(
        delete("/jobs").contentType(MediaType.APPLICATION_JSON).content(JOB_JSON).with(httpBasic("user", "password")));
    final TMJobEntity deletedJob = jobRepo.findByTmJobId("1234-5678");
    assertThat(deletedJob).isNull();
  }

  @Test
  public void createJobIT() throws Exception {
    mockMvc.perform(
        post("/jobs").contentType(MediaType.APPLICATION_JSON).content(JOB_JSON).with(httpBasic("user", "password")));
    final TMJobEntity createdJob = jobRepo.findByTmJobId("1234-5678");
    assertThat(createdJob.getLastAuthNo()).isEqualTo("1276");
  }

  @Test
  public void getJobsIT() throws Exception {

    TMJobEntity jobEntity1 = new TMJobEntity();
    jobEntity1.setTmJobId("1234-5678");
    jobEntity1.setLastAuthNo("1234");
    jobEntity1.setLastUpdated(LocalDateTime.parse("2018-08-01T01:06:01",DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    jobRepo.save(jobEntity1);
    TMJobEntity jobEntity2 = new TMJobEntity();
    jobEntity2.setTmJobId("1243-8765");
    jobEntity2.setLastAuthNo("1243");
    jobEntity2.setLastUpdated(LocalDateTime.parse("2018-08-01T01:06:01",DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    jobRepo.save(jobEntity2);
    mockMvc.perform(get("/jobs").with(httpBasic("user", "password"))).andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  public void updateJobNotExistIT() throws Exception {
    mockMvc.perform(
        (put("/jobs")).contentType(MediaType.APPLICATION_JSON).content(JOB_JSON).with(httpBasic("user", "password")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error", is("FWMT_RESOURCE_SERVICE_0002")))
        .andExpect(jsonPath("$.exception", is("uk.gov.ons.fwmt.resource_service.exception.FWMTException")))
        .andExpect(jsonPath("$.message", is("FWMT_RESOURCE_SERVICE_0002 - Job 1234-5678 not found")))
        .andExpect(jsonPath("$.path", is("/jobs")))
        .andExpect(jsonPath("$.status", is(404)))
        .andExpect(jsonPath("$.timestamp", isA(String.class)));
  }
}
