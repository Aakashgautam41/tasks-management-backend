package com.example.tasks_management_backend.controller;

import com.example.tasks_management_backend.dto.TaskRequest;
import com.example.tasks_management_backend.model.Task;
import com.example.tasks_management_backend.service.JwtUtil;
import com.example.tasks_management_backend.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import com.example.tasks_management_backend.service.MyUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private TaskService taskService;

        @MockBean
        private MyUserDetailsService myUserDetailsService;

        @Autowired
        private JwtUtil jwtUtil;

        @Autowired
        private ObjectMapper objectMapper;

        private String token;

        @BeforeEach
        void setUp() {
                UserDetails userDetails = User.withUsername("testuser")
                                .password("password")
                                .authorities("USER")
                                .build();

                when(myUserDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

                token = "Bearer " + jwtUtil.generateToken(userDetails);
        }

        @Test
        void createTask_Success() throws Exception {
                TaskRequest request = new TaskRequest("New Task", Task.Priority.HIGH, LocalDate.now().plusDays(1),
                                Task.Status.PENDING, new ArrayList<>());
                Task task = new Task();
                task.setId(1L);
                task.setTitle("New Task");

                when(taskService.saveTask(any(Task.class))).thenReturn(task);

                mockMvc.perform(post("/api/tasks")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.title").value("New Task"));
        }

        @Test
        void getTasks_Success() throws Exception {
                when(taskService.getTasks(any(), any(), any(), any(), any(), any(Integer.class), any(Integer.class)))
                                .thenReturn(org.springframework.data.domain.Page.empty());

                mockMvc.perform(get("/api/tasks")
                                .header("Authorization", token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void unauthorizedAccess_Forbidden() throws Exception {
                mockMvc.perform(get("/api/tasks"))
                                .andExpect(status().isForbidden());
        }
}
