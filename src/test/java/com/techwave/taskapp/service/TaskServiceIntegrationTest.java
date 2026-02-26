package com.techwave.taskapp.service;

import com.techwave.taskapp.dto.CreateTaskRequest;
import com.techwave.taskapp.entity.Task;
import com.techwave.taskapp.enums.TaskStatus;
import com.techwave.taskapp.repository.interfaces.ITaskRepository;
import com.techwave.taskapp.service.mapper.TaskMapper;
import com.techwave.taskapp.service.validation.TaskValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration and Behavior Tests for TaskService.
 * 
 * This test suite focuses on:
 * - End-to-end behavior verification
 * - Component interaction patterns
 * - Data flow validation
 * - Business rule enforcement
 * - Side effect verification
 * - State consistency checks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Integration and Behavior Tests")
class TaskServiceIntegrationTest {

    @Mock
    private ITaskRepository taskRepository;
    
    @Mock
    private TaskValidator taskValidator;
    
    @Mock
    private TaskMapper taskMapper;
    
    @InjectMocks
    private TaskService taskService;

    @Nested
    @DisplayName("Data Flow Integration Tests")
    class DataFlowTests {

        @Test
        @DisplayName("Should pass data correctly through the processing pipeline")
        void shouldPassDataThroughProcessingPipeline() {
            // Given - Track data transformation through the pipeline
            CreateTaskRequest originalRequest = new CreateTaskRequest("  Original Title  ", "  Original Description  ");
            CreateTaskRequest sanitizedRequest = new CreateTaskRequest("Original Title", "Original Description");
            
            Task mappedTask = new Task();
            mappedTask.setTitle("Original Title");
            mappedTask.setDescription("Original Description");
            mappedTask.setStatus(TaskStatus.PENDING);
            mappedTask.setCreatedAt(Instant.now());
            
            Task persistedTask = new Task();
            persistedTask.setId("generated-uuid");
            persistedTask.setTitle("Original Title");
            persistedTask.setDescription("Original Description");
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(Instant.now());

            // Configure mocks to simulate data transformation
            doAnswer(invocation -> {
                CreateTaskRequest req = invocation.getArgument(0);
                // Simulate sanitization: trim whitespace
                req.setTitle(req.getTitle().trim());
                req.setDescription(req.getDescription().trim());
                return null;
            }).when(taskValidator).sanitizeCreateRequest(any(CreateTaskRequest.class));

            when(taskMapper.mapToEntity(any(CreateTaskRequest.class))).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(originalRequest);

            // Then - Verify data flow and transformations
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("generated-uuid");
            assertThat(result.getTitle()).isEqualTo("Original Title");
            assertThat(result.getDescription()).isEqualTo("Original Description");

            // Verify the request object was modified by sanitization
            assertThat(originalRequest.getTitle()).isEqualTo("Original Title"); // Trimmed
            assertThat(originalRequest.getDescription()).isEqualTo("Original Description"); // Trimmed

            // Verify method call order and parameters
            ArgumentCaptor<CreateTaskRequest> validationCaptor = ArgumentCaptor.forClass(CreateTaskRequest.class);
            ArgumentCaptor<CreateTaskRequest> sanitizationCaptor = ArgumentCaptor.forClass(CreateTaskRequest.class);
            ArgumentCaptor<CreateTaskRequest> mappingCaptor = ArgumentCaptor.forClass(CreateTaskRequest.class);

            verify(taskValidator).validateCreateRequest(validationCaptor.capture());
            verify(taskValidator).sanitizeCreateRequest(sanitizationCaptor.capture());
            verify(taskMapper).mapToEntity(mappingCaptor.capture());

            // All should receive the same request object instance
            assertThat(validationCaptor.getValue()).isSameAs(originalRequest);
            assertThat(sanitizationCaptor.getValue()).isSameAs(originalRequest);
            assertThat(mappingCaptor.getValue()).isSameAs(originalRequest);
        }

        @Test
        @DisplayName("Should handle null description transformation correctly")
        void shouldHandleNullDescriptionTransformation() {
            // Given
            CreateTaskRequest requestWithNullDescription = new CreateTaskRequest("Valid Title", null);
            
            Task taskWithNullDescription = new Task();
            taskWithNullDescription.setTitle("Valid Title");
            taskWithNullDescription.setDescription(null);
            taskWithNullDescription.setStatus(TaskStatus.PENDING);
            taskWithNullDescription.setCreatedAt(Instant.now());
            
            Task persistedTask = new Task();
            persistedTask.setId("task-null-desc");
            persistedTask.setTitle("Valid Title");
            persistedTask.setDescription(null);
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(requestWithNullDescription)).thenReturn(taskWithNullDescription);
            when(taskRepository.create(taskWithNullDescription)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(requestWithNullDescription);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Valid Title");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
            assertThat(result.getId()).isEqualTo("task-null-desc");
        }

        @Test
        @DisplayName("Should handle empty string to null conversion")
        void shouldHandleEmptyStringToNullConversion() {
            // Given
            CreateTaskRequest requestWithEmptyDescription = new CreateTaskRequest("Valid Title", "");
            
            // Simulate validator converting empty string to null
            doAnswer(invocation -> {
                CreateTaskRequest req = invocation.getArgument(0);
                if (req.getDescription() != null && req.getDescription().trim().isEmpty()) {
                    req.setDescription(null);
                }
                return null;
            }).when(taskValidator).sanitizeCreateRequest(any(CreateTaskRequest.class));
            
            Task taskWithNullDescription = new Task();
            taskWithNullDescription.setTitle("Valid Title");
            taskWithNullDescription.setDescription(null); // Converted from empty string
            taskWithNullDescription.setStatus(TaskStatus.PENDING);
            taskWithNullDescription.setCreatedAt(Instant.now());
            
            Task persistedTask = new Task();
            persistedTask.setId("task-empty-to-null");
            persistedTask.setTitle("Valid Title");
            persistedTask.setDescription(null);
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(any(CreateTaskRequest.class))).thenReturn(taskWithNullDescription);
            when(taskRepository.create(taskWithNullDescription)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(requestWithEmptyDescription);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isNull(); // Should be null, not empty string
            assertThat(requestWithEmptyDescription.getDescription()).isNull(); // Original request modified

            verify(taskValidator).sanitizeCreateRequest(requestWithEmptyDescription);
        }
    }

    @Nested
    @DisplayName("Business Rule Verification Tests")
    class BusinessRuleTests {

        @Test
        @DisplayName("Should enforce default task status as PENDING")
        void shouldEnforceDefaultTaskStatus() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest("New Task", "Task Description");
            
            // Capture what gets mapped and persisted
            ArgumentCaptor<Task> mapperResultCaptor = ArgumentCaptor.forClass(Task.class);
            ArgumentCaptor<Task> repositoryInputCaptor = ArgumentCaptor.forClass(Task.class);
            
            Task taskFromMapper = new Task();
            taskFromMapper.setTitle("New Task");
            taskFromMapper.setDescription("Task Description");
            taskFromMapper.setStatus(TaskStatus.PENDING); // Business rule: default status
            taskFromMapper.setCreatedAt(Instant.now());
            
            Task persistedTask = new Task();
            persistedTask.setId("task-pending");
            persistedTask.setTitle("New Task");
            persistedTask.setDescription("Task Description");
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(request)).thenReturn(taskFromMapper);
            when(taskRepository.create(taskFromMapper)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(request);

            // Then
            assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
            
            verify(taskRepository).create(repositoryInputCaptor.capture());
            assertThat(repositoryInputCaptor.getValue().getStatus()).isEqualTo(TaskStatus.PENDING);
        }

        @Test
        @DisplayName("Should ensure task has timestamp at creation")
        void shouldEnsureTaskHasTimestamp() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest("Timestamped Task", "Description");
            Instant beforeCreation = Instant.now();
            
            Task taskFromMapper = new Task();
            taskFromMapper.setTitle("Timestamped Task");
            taskFromMapper.setDescription("Description");
            taskFromMapper.setStatus(TaskStatus.PENDING);
            taskFromMapper.setCreatedAt(Instant.now()); // Should be recent
            
            Task persistedTask = new Task();
            persistedTask.setId("task-timestamped");
            persistedTask.setTitle("Timestamped Task");
            persistedTask.setDescription("Description");
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(taskFromMapper.getCreatedAt());

            when(taskMapper.mapToEntity(request)).thenReturn(taskFromMapper);
            when(taskRepository.create(taskFromMapper)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(request);
            Instant afterCreation = Instant.now();

            // Then
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getCreatedAt())
                    .isAfterOrEqualTo(beforeCreation)
                    .isBeforeOrEqualTo(afterCreation);
                    
            // Verify timestamp precision (should be within 1 second)
            assertThat(result.getCreatedAt())
                    .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("Should ensure task ID is set by repository")
        void shouldEnsureTaskIdIsSetByRepository() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest("ID Test Task", "Description");
            
            Task taskBeforePersistence = new Task();
            taskBeforePersistence.setId(null); // Should be null before persistence
            taskBeforePersistence.setTitle("ID Test Task");
            taskBeforePersistence.setDescription("Description");
            taskBeforePersistence.setStatus(TaskStatus.PENDING);
            taskBeforePersistence.setCreatedAt(Instant.now());
            
            Task taskAfterPersistence = new Task();
            taskAfterPersistence.setId("generated-uuid-123"); // Repository generates ID
            taskAfterPersistence.setTitle("ID Test Task");
            taskAfterPersistence.setDescription("Description");
            taskAfterPersistence.setStatus(TaskStatus.PENDING);
            taskAfterPersistence.setCreatedAt(taskBeforePersistence.getCreatedAt());

            when(taskMapper.mapToEntity(request)).thenReturn(taskBeforePersistence);
            when(taskRepository.create(taskBeforePersistence)).thenReturn(taskAfterPersistence);

            // When
            Task result = taskService.createTask(request);

            // Then
            assertThat(result.getId()).isNotNull();
            assertThat(result.getId()).isEqualTo("generated-uuid-123");
            
            // Verify that mapper creates task with null ID
            ArgumentCaptor<Task> repositoryInputCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).create(repositoryInputCaptor.capture());
            assertThat(repositoryInputCaptor.getValue().getId()).isNull();
        }
    }

    @Nested
    @DisplayName("Side Effect and State Consistency Tests")
    class SideEffectTests {

        @Test
        @DisplayName("Should not modify original request object beyond sanitization")
        void shouldNotModifyOriginalRequestBeyondSanitization() {
            // Given
            String originalTitle = "  Test Title  ";
            String originalDescription = "  Test Description  ";
            CreateTaskRequest request = new CreateTaskRequest(originalTitle, originalDescription);
            
            // Store original values for comparison
            String titleBeforeCall = request.getTitle();
            String descriptionBeforeCall = request.getDescription();
            
            // Configure minimal sanitization
            doAnswer(invocation -> {
                CreateTaskRequest req = invocation.getArgument(0);
                req.setTitle(req.getTitle().trim());
                if (req.getDescription() != null) {
                    req.setDescription(req.getDescription().trim());
                }
                return null;
            }).when(taskValidator).sanitizeCreateRequest(any(CreateTaskRequest.class));
            
            Task mappedTask = new Task();
            mappedTask.setTitle("Test Title");
            mappedTask.setDescription("Test Description");
            mappedTask.setStatus(TaskStatus.PENDING);
            mappedTask.setCreatedAt(Instant.now());
            
            Task persistedTask = new Task();
            persistedTask.setId("task-side-effect");
            persistedTask.setTitle("Test Title");
            persistedTask.setDescription("Test Description");
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(request)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            taskService.createTask(request);

            // Then - Only sanitization changes should be present
            assertThat(request.getTitle()).isEqualTo("Test Title"); // Trimmed
            assertThat(request.getDescription()).isEqualTo("Test Description"); // Trimmed
            
            // Should not have any additional unexpected modifications
            assertThat(request.getTitle()).isNotEqualTo(titleBeforeCall); // Changed due to sanitization
            assertThat(request.getDescription()).isNotEqualTo(descriptionBeforeCall); // Changed due to sanitization
        }

        @Test
        @DisplayName("Should maintain request object reference throughout processing")
        void shouldMaintainRequestObjectReference() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest("Reference Test", "Description");
            
            Task mappedTask = new Task();
            when(taskMapper.mapToEntity(request)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(mappedTask);

            // When
            taskService.createTask(request);

            // Then - Verify all components receive the same object instance
            ArgumentCaptor<CreateTaskRequest> allRequestCaptures = ArgumentCaptor.forClass(CreateTaskRequest.class);
            
            verify(taskValidator).validateCreateRequest(allRequestCaptures.capture());
            verify(taskValidator).sanitizeCreateRequest(allRequestCaptures.capture());
            verify(taskMapper).mapToEntity(allRequestCaptures.capture());
            
            // All captured instances should be the same object
            for (CreateTaskRequest capturedRequest : allRequestCaptures.getAllValues()) {
                assertThat(capturedRequest).isSameAs(request);
            }
        }

        @Test
        @DisplayName("Should not have side effects on component state")
        void shouldNotHaveSideEffectsOnComponentState() {
            // Given
            CreateTaskRequest request1 = new CreateTaskRequest("Task 1", "Description 1");
            CreateTaskRequest request2 = new CreateTaskRequest("Task 2", "Description 2");
            
            Task task1 = new Task();
            task1.setId("1");
            task1.setTitle("Task 1");
            task1.setDescription("Description 1");
            task1.setStatus(TaskStatus.PENDING);
            task1.setCreatedAt(Instant.now());
            
            Task task2 = new Task();
            task2.setId("2");
            task2.setTitle("Task 2");
            task2.setDescription("Description 2");
            task2.setStatus(TaskStatus.PENDING);
            task2.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(request1)).thenReturn(task1);
            when(taskMapper.mapToEntity(request2)).thenReturn(task2);
            when(taskRepository.create(task1)).thenReturn(task1);
            when(taskRepository.create(task2)).thenReturn(task2);

            // When - Process multiple requests
            Task result1 = taskService.createTask(request1);
            Task result2 = taskService.createTask(request2);

            // Then - Each request should be processed independently
            assertThat(result1.getId()).isEqualTo("1");
            assertThat(result1.getTitle()).isEqualTo("Task 1");
            
            assertThat(result2.getId()).isEqualTo("2");
            assertThat(result2.getTitle()).isEqualTo("Task 2");
            
            // Verify no cross-contamination between requests
            assertThat(result1).isNotEqualTo(result2);
            assertThat(result1.getTitle()).isNotEqualTo(result2.getTitle());
            assertThat(result1.getDescription()).isNotEqualTo(result2.getDescription());
            
            // Verify each component was called for both requests
            verify(taskValidator, times(2)).validateCreateRequest(any(CreateTaskRequest.class));
            verify(taskValidator, times(2)).sanitizeCreateRequest(any(CreateTaskRequest.class));
            verify(taskMapper, times(2)).mapToEntity(any(CreateTaskRequest.class));
            verify(taskRepository, times(2)).create(any(Task.class));
        }
    }

    @Nested
    @DisplayName("Component Collaboration Verification")
    class ComponentCollaborationTests {

        @Test
        @DisplayName("Should coordinate components in correct sequence")
        void shouldCoordinateComponentsInCorrectSequence() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest("Coordination Test", "Description");
            Task mappedTask = new Task();
            Task persistedTask = new Task();
            persistedTask.setId("coordination-test");

            when(taskMapper.mapToEntity(request)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            taskService.createTask(request);

            // Then - Verify coordination sequence using InOrder
            var inOrder = inOrder(taskValidator, taskMapper, taskRepository);
            
            // Step 1: Validation must happen first
            inOrder.verify(taskValidator).validateCreateRequest(request);
            
            // Step 2: Sanitization must happen after validation
            inOrder.verify(taskValidator).sanitizeCreateRequest(request);
            
            // Step 3: Mapping must happen after sanitization
            inOrder.verify(taskMapper).mapToEntity(request);
            
            // Step 4: Persistence must happen last
            inOrder.verify(taskRepository).create(mappedTask);
            
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("Should handle component failure isolation")
        void shouldHandleComponentFailureIsolation() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest("Isolation Test", "Description");
            
            // Configure mapper to fail
            when(taskMapper.mapToEntity(request))
                    .thenThrow(new RuntimeException("Mapper failure"));

            // When & Then
            try {
                taskService.createTask(request);
            } catch (RuntimeException e) {
                // Expected
            }

            // Verify validation and sanitization still occurred
            verify(taskValidator).validateCreateRequest(request);
            verify(taskValidator).sanitizeCreateRequest(request);
            verify(taskMapper).mapToEntity(request); // Failed here
            
            // Verify repository was not called due to mapper failure
            verifyNoInteractions(taskRepository);
        }

        @Test
        @DisplayName("Should delegate responsibilities correctly to each component")
        void shouldDelegateResponsibilitiesCorrectly() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest("Delegation Test", "Description");
            Task mappedTask = new Task();
            Task persistedTask = new Task();

            when(taskMapper.mapToEntity(request)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(request);

            // Then - Verify each component handles its specific responsibility
            
            // TaskValidator: Validation and sanitization
            verify(taskValidator, times(1)).validateCreateRequest(request);
            verify(taskValidator, times(1)).sanitizeCreateRequest(request);
            verifyNoMoreInteractions(taskValidator);
            
            // TaskMapper: DTO to Entity mapping  
            verify(taskMapper, times(1)).mapToEntity(request);
            verifyNoMoreInteractions(taskMapper);
            
            // TaskRepository: Persistence
            verify(taskRepository, times(1)).create(mappedTask);
            verifyNoMoreInteractions(taskRepository);
            
            // TaskService: Returns repository result
            assertThat(result).isSameAs(persistedTask);
        }
    }
}