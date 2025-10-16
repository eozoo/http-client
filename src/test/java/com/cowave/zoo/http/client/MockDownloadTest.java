package com.cowave.zoo.http.client;

import com.cowave.zoo.http.client.download.DownloadMockController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *
 * @author shanhuiming
 *
 */
@ContextConfiguration(classes = Application.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
public class MockDownloadTest {

    @Autowired
    private DownloadMockController downloadMockController;

    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(downloadMockController).build();
    }

    @Test
    public void download1() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/mock/download1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("12345678"));
    }
}
